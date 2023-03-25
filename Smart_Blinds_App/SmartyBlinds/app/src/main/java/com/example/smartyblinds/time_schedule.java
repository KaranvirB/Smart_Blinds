package com.example.smartyblinds;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class time_schedule extends AppCompatActivity {

    TextView time_text, time_text2;
    int hour, minute;
    String days = "";
    String start_time, end_time;
    int total_start_minutes, total_end_minutes = 0;
    CheckBox checkBox1,checkBox2,checkBox3,checkBox4,checkBox5,checkBox6,checkBox7;
    RadioGroup set_operation;

    Button create_schedule1, back5;
    int operation = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_schedule);

        //Get blind name and serial number from previous activity
        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String serial = i.getStringExtra("serial");

        //Set Dates
        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);
        checkBox5 = findViewById(R.id.checkBox5);
        checkBox6 = findViewById(R.id.checkBox6);
        checkBox7 = findViewById(R.id.checkBox7);

        // Set up the onCheckedChanged listener for the checkboxes
        CheckBox.OnCheckedChangeListener onCheckedChangeListener = new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days = "";
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
            }
        };
        checkBox1.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox2.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox3.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox4.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox5.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox6.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox7.setOnCheckedChangeListener(onCheckedChangeListener);

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

        //Set the operation
        set_operation = findViewById(R.id.set_operation);
        set_operation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                String option = radioButton.getText().toString();

                if(option.equals("Rolled Up")){
                    operation = 1;
                }else {
                    operation = 0;
                }
            }
        });

        //Set Schedule
        create_schedule1 = findViewById(R.id.create_schedule1);
        create_schedule1.setOnClickListener(view -> {

            //Input Validation
            if(total_start_minutes == total_end_minutes){
                time_text.setError("Start time cannot equal end time!");
                time_text.requestFocus();
                return;
            }if(days.equals("")){
                checkBox1.setError("Please select at least 1 day!");
                checkBox1.requestFocus();
                return;
            }if(operation == 3){
                Toast.makeText(time_schedule.this, "Please select an operation!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(total_start_minutes > total_end_minutes) {
                Toast.makeText(time_schedule.this, "Time set to next day!", Toast.LENGTH_SHORT).show();
            }

            //Update database with schedule values
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/type").setValue("time");
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/days").setValue(days);
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/start_minutes").setValue(total_start_minutes);
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/end_minutes").setValue(total_end_minutes);
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/operation").setValue(operation);

            Toast.makeText(this, "Successfully created time schedule!", Toast.LENGTH_SHORT).show();
        });

        //Go back to previous activity
        back5 = findViewById(R.id.back5);
        back5.setOnClickListener(view -> {
            Intent ii = new Intent(time_schedule.this, schedule.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
                    total_start_minutes = (hour * 60) + minute;
                } else {
                    time_text2.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                    end_time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    total_end_minutes = (hour * 60) + minute;
                }
            }
        };
        int style = AlertDialog.THEME_HOLO_DARK;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, style, onTimeSetListener, hour, minute, false);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }
}