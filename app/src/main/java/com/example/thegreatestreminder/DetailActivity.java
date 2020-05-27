package com.example.thegreatestreminder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.thegreatestreminder.BusinessEntities.Notification;
import com.example.thegreatestreminder.BusinessEntities.Reminder;
import com.example.thegreatestreminder.BusinessLogic.ReminderService;
import com.example.thegreatestreminder.Utils.Adapters.NotificationArrayAdapter;
import com.example.thegreatestreminder.Utils.Converters.DateTimeConverter;
import com.example.thegreatestreminder.Utils.Helpers.ControlsHelper;

import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    final static int CAMERA_REQUEST = 1;

    EditText etName;
    EditText etDetail;
    EditText etDate;
    EditText etTime;

    ImageView img;

    Button btnSave;
    Button btnAddNotification;
    Button btnAddImage;

    ListView lvNotif;

    ReminderService reminderService;
    List<Notification> notificationList;

    private boolean isEditing(){
        Bundle bundle = getIntent().getExtras();
        return bundle != null && bundle.containsKey("reminderId");
    }

    private long getEditedId(){
        return getIntent().getExtras().getLong("reminderId");
    }

    private void setupControlsReferences(){
        etName = findViewById(R.id.etReminderName);
        etDetail = findViewById(R.id.etReminderDetail);
        etDate = findViewById(R.id.etReminderDate);
        etTime = findViewById(R.id.etReminderTime);

        img = findViewById(R.id.imgDetail);

        btnSave = findViewById(R.id.btnSaveDetail);
        btnAddNotification = findViewById(R.id.btnAddNotification);
        btnAddImage = findViewById(R.id.btnAddImage);

        lvNotif = findViewById(R.id.lvDetailNotifications);
    }

    private Reminder getReminder() throws ParseException {
        String name = this.etName.getText().toString();
        String detail = this.etDetail.getText().toString();
        Date triggerDate = DateTimeConverter.dateTimeFromString(this.etDate.getText().toString(),this.etTime.getText().toString());
        Reminder reminder = new Reminder(name,detail,triggerDate);

        for (Notification n :
                notificationList) {
            reminder.addNotification(n);
        }

        if(img.getVisibility() == View.VISIBLE){
            reminder.setPhoto(((BitmapDrawable)img.getDrawable()).getBitmap());
        }

        if(isEditing())
            reminder.setId(getEditedId());

        return reminder;
    }

    private void onSaveClick(View v){
        try {
            if(isEditing()) {
                this.reminderService.editReminder(getReminder());
            }else{
                this.reminderService.addReminder(getReminder());
            }

            this.setResult(1);
            this.finish();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast errorToast = Toast.makeText(this,R.string.invalid_date_error,Toast.LENGTH_SHORT);
            errorToast.show();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            Toast errorToast = Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT);
            errorToast.show();
        }
    }

    private void checkPermisions(Notification notification){
        if(notification.getType() == Notification.Type.SMS) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                    && checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {

                String[] permissions = {Manifest.permission.SEND_SMS};

                requestPermissions(permissions, 1);
            }
        }
        else{

        }
    }

    private void refreshNotifList(){
        NotificationArrayAdapter adapter = new NotificationArrayAdapter(this,notificationList);
        adapter.setOnItemDelete((Notification notif) -> {
            this.notificationList.remove(notif);
            refreshNotifList();
        });
        lvNotif.setAdapter(adapter);
    }

    private void onAddNotifClick(View v){
        AddNotificationDialog dialog = new AddNotificationDialog(this);
        dialog.show();
        dialog.setOnDismissListener((DialogInterface dialogInterface) -> {
            Notification notif = dialog.getNotification();
            if(notif != null){
                checkPermisions(notif);
                notificationList.add(notif);
                refreshNotifList();
            }
        });
    }

    private void onAddImageClick(View v){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }

    private void setupValues(){
        if(isEditing()){
            long id = getEditedId();

            Reminder reminder = reminderService.getReminder(id);

            etName.setText(reminder.getName());
            etDetail.setText(reminder.getDetail());
            etDate.setText(DateTimeConverter.dateToString(reminder.getTriggerDateTime()));
            etTime.setText(DateTimeConverter.timeToString(new Time(reminder.getTriggerDateTime().getTime())));

            Bitmap image = reminder.getPhoto();
            if(image != null){
                this.img.setVisibility(View.VISIBLE);
                this.img.setImageBitmap(image);
            }

            for (Notification notif :
                    reminder.getNotifications()) {
                notificationList.add(notif);
            }

            refreshNotifList();
        }
        else{
            Date currentDate = new Date();
            etDate.setText(DateTimeConverter.dateToString(currentDate));
            etTime.setText(DateTimeConverter.timeToString(new Time(currentDate.getTime())));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        this.reminderService = DependencyFactory.getInstance(this).getReminderService();
        this.notificationList = new ArrayList<>();

        setupControlsReferences();

        btnSave.setOnClickListener(this::onSaveClick);
        btnAddNotification.setOnClickListener(this::onAddNotifClick);
        btnAddImage.setOnClickListener(this::onAddImageClick);

        refreshNotifList();
        ControlsHelper.setupEditDateBehaviour(this,etDate);
        ControlsHelper.setupEditTimeBehaviour(this,etTime);

        setupValues();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img.setImageBitmap(imageBitmap);
            this.img.setVisibility(View.VISIBLE);
        }
    }
}
