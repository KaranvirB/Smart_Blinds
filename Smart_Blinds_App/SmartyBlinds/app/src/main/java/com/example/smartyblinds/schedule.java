package com.example.smartyblinds;

import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Locale;

public class schedule extends AppCompatActivity {

    Button back4, create_schedule;
    int hour, minute;
    String days, start_time, end_time = "";

    TextView time_text, time_text2;
    EditText set_light;
    CheckBox checkBox1,checkBox2,checkBox3,checkBox4,checkBox5,checkBox6,checkBox7;
    RadioGroup set_operation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        //Get blind name and serial number from previous activity
        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String serial = i.getStringExtra("serial");

        //Go back to previous activity
        back4 = findViewById(R.id.back4);
        back4.setOnClickListener(view -> {
            Intent ii = new Intent(schedule.this, CurrentBlind.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        //Set Dates
        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);
        checkBox5 = findViewById(R.id.checkBox5);
        checkBox6 = findViewById(R.id.checkBox6);
        checkBox7 = findViewById(R.id.checkBox7);

        // Check if each checkbox is checked
        if (checkBox1.isChecked()) {
            days += "1";
        }
        if (checkBox2.isChecked()) {
            days += "2";
        }
        if (checkBox3.isChecked()) {
            days += "3";
        }
        if (checkBox4.isChecked()) {
            days += "4";
        }
        if (checkBox5.isChecked()) {
            days += "5";
        }
        if (checkBox6.isChecked()) {
            days += "6";
        }
        if (checkBox7.isChecked()) {
            days += "7";
        }

        //Set starting time
        time_text = findViewById(R.id.time_text);
        time_text.setOnClickListener(view -> {
            popTimePicker(1);
        });

        //Set ending time
        time_text2 = findViewById(R.id.time_text2);
        time_text2.setOnClickListener(view -> {
            popTimePicker(2);
        });

        //Set Light
        set_light = findViewById(R.id.set_light);
        String light = set_light.getText().toString().trim();

        //Set the operation
        set_operation = findViewById(R.id.set_operation);
        set_operation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                Toast.makeText(schedule.this, "Selected Radio Button is : " + radioButton.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        //Set Schedule
        create_schedule = findViewById(R.id.create_schedule);
        create_schedule.setOnClickListener(view -> {

        });

    }

    public void popTimePicker(int x)
    {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                hour = selectedHour;
                minute = selectedMinute;

                if (x == 1) {
                    time_text.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                    start_time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                } else {
                    time_text2.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                    end_time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                }
            }
        };
        int style = AlertDialog.THEME_HOLO_DARK;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, style, onTimeSetListener, hour, minute, false);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }
}