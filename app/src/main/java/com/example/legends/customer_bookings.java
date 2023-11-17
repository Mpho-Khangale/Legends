package com.example.legends;

import android.app.TimePickerDialog;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TimePicker;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.legends.databinding.ActivityCustomerBookingsBinding;

import java.util.Calendar;

public class customer_bookings extends AppCompatActivity {
    private CalendarView calendarView;
    private Button selectTimeButton;
    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_bookings);

        calendarView = findViewById(R.id.calendar);
        selectTimeButton = findViewById(R.id.selectTimeButton);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        });

        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

    }

    private void showTimePickerDialog() {
        int hour = selectedCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = selectedCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedCalendar.set(Calendar.MINUTE, minute);

                        // Now, selectedCalendar contains the selected date and time.
                        // You can use it as needed.
                    }
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }
}
