package com.example.thegreatestreminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.thegreatestreminder.BusinessEntities.Reminder;
import com.example.thegreatestreminder.BusinessEntities.ReminderFilter;
import com.example.thegreatestreminder.BusinessLogic.ReminderService;
import com.example.thegreatestreminder.DAO.Impl.MailjetEmailClient;
import com.example.thegreatestreminder.Utils.Adapters.ReminderArrayAdapter;
import com.example.thegreatestreminder.Utils.Converters.DurationConverter;

import java.time.Duration;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listViewMain;
    private ArrayList<Reminder> reminders;

    private Button btnAdd;

    private ReminderService reminderService;

    private boolean showAll = true;
    private boolean orderName = true;
    private boolean orderAsc = true;

    //Constants for child activities
    final static int REMIND_DETAIL = 123;

    /**
     * Finds and assigns view by id for all the references used in this activity
     */
    private void setUpReferences(){
        listViewMain = findViewById(R.id.lvMain);

        btnAdd = findViewById(R.id.btnAddRecord);
    }

    private void setUpButtonAdd(){
        btnAdd.setOnClickListener((View v) -> {
            Intent x = new Intent(this, DetailActivity.class);
            startActivityForResult(x,REMIND_DETAIL);
        });
    }

    /* ---------------Methods for handling listview-------------- */

    /**
     * Sets up listview
     * - onItemClickListener
      */
    private void setUpListView(){
        listViewMain.setOnItemClickListener((parent,view,position,id) -> {
            Reminder entity = reminders.get(position);
            Duration durationUntilFired = entity.getDurationUntilFired();
            String durationAsString = DurationConverter.durationToString(durationUntilFired);
            String text = String.format("It takes \'%s\' until this will be reminded",durationAsString);
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Refreshes the listview
     * should be called when there are a new data available
     */
    private void refreshListView(){
        this.loadReminders();
        ReminderArrayAdapter adapter = new ReminderArrayAdapter(this,reminders);
        adapter.setOnItemDelete((reminder) -> {
            deleteReminder(reminder);
        });

        adapter.setOnItemEdit((reminder) ->{
            editReminder(reminder);
        });

        listViewMain.setAdapter(adapter);
    }

    /* -----------Methods for handling data access ---------------- */

    /**
     * Loads all the reminders from the data acces object into reminders arraylist
     */
    private void loadReminders(){
        this.reminders = reminderService.getAllReminders(getFilter());
    }

    private ReminderFilter getFilter(){
        return new ReminderFilter(showAll,orderName,orderAsc);
    }

    private void deleteReminder(Reminder reminder){
        this.reminderService.deleteReminder(reminder.getId());
        this.refreshListView();
    }

    private void editReminder(Reminder reminder){
        Intent x = new Intent(this, DetailActivity.class);
        x.putExtra("reminderId",reminder.getId());
        this.startActivityForResult(x,REMIND_DETAIL);
    }

    /* -----------Activity Overrides and event handlers---------------- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("The Greatest Reminder");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        this.reminderService = DependencyFactory.getInstance(this).getReminderService();
        this.setUpReferences();
        this.setUpListView();
        this.setUpButtonAdd();

        this.refreshListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.itShowMode:
                showAll = !showAll;
                refreshListView();
                return true;
            case R.id.itOrderAscDesc:
                orderAsc = !orderAsc;
                refreshListView();
                return true;
            case R.id.itOrderName:
                orderName = false;
                refreshListView();
                return true;
            case R.id.itOrderExpiration:
                orderName = true;
                refreshListView();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REMIND_DETAIL){
            this.refreshListView();
        }
    }
}
