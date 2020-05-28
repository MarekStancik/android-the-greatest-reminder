package com.example.thegreatestreminder.BusinessLogic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.thegreatestreminder.BusinessEntities.Reminder;

import java.util.Date;

class ReminderAlarmService {

    Context ctx;
    AlarmManager alarmManager;

    public ReminderAlarmService(Context ctx){
        this.ctx = ctx;
        this.alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent createIntent(long reminderId,int flags){
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("reminderId",reminderId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(ctx,(int)reminderId,intent,flags);
        return alarmIntent;
    }

    public void addReminder(Reminder reminder){
        PendingIntent alarmIntent = createIntent(reminder.getId(),0);

        Date triggerDate = reminder.getTriggerDateTime();
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerDate.getTime(),alarmIntent);
    }

    public void deleteReminder(long reminderId){
        PendingIntent alarmIntent = createIntent(reminderId,0);

        alarmManager.cancel(alarmIntent);
    }

    public void editReminder(Reminder reminder){
        this.deleteReminder(reminder.getId());
        this.addReminder(reminder);
    }
}
