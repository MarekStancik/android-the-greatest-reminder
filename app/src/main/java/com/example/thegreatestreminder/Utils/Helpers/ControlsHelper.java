package com.example.thegreatestreminder.Utils.Helpers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

import com.example.thegreatestreminder.Utils.Converters.DateTimeConverter;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class ControlsHelper {

    static public void setupEditTimeBehaviour(Context ctx,EditText et) {
        et.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);

        et.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(ctx,
                    (view, hour1, minute1) -> {
                        Time time = new Time(hour1, minute1,0);
                        et.setText(DateTimeConverter.timeToString(time));
                    }, hour, minute,true);

            timePicker.show();
        });
    }

    static public void setupEditDateBehaviour(Context ctx,EditText et) {
        et.setInputType(InputType.TYPE_CLASS_DATETIME);

        et.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            // date picker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(ctx,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        Date date = new Date(year1 - 1900,monthOfYear,dayOfMonth);
                        et.setText(DateTimeConverter.dateToString(date));
                    }, year, month, day);

            datePickerDialog.show();
        });
    }
}
