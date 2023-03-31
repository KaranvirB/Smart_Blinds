package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class schedule extends AppCompatActivity {

    Button back4, turn_on, turn_off;
    TextView time, light, temp;

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

        //Go to time scheduler
        time = findViewById(R.id.time);
        time.setOnClickListener(view -> {
            Intent ii = new Intent(schedule.this, time_schedule.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        //Go to light scheduler
        light = findViewById(R.id.light);
        light.setOnClickListener(view -> {
            Intent ii = new Intent(schedule.this, light_schedule.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        //Go to temperature scheduler
        temp = findViewById(R.id.temp);
        temp.setOnClickListener(view -> {
            Intent ii = new Intent(schedule.this, temp_schedule.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        get_current(serial);

        //Buttons to turn schedule on/off
        turn_on = findViewById(R.id.turn_on);
        turn_off = findViewById(R.id.turn_off);

        turn_off.setOnClickListener(view -> {
            //Set schedule to FALSE in database
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("schedule").child("ON").setValue("FALSE");
            get_current(serial);
            Toast.makeText(this, "Schedule Disabled!", Toast.LENGTH_SHORT).show();
        });

        turn_on.setOnClickListener(view -> {
            //Set schedule to TRUE in database
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("schedule").child("ON").setValue("TRUE");

            //By turing on the schedule, we disable ML mode automatically
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("ML").setValue("OFF");
            get_current(serial);
            Toast.makeText(this, "Schedule Enabled!", Toast.LENGTH_SHORT).show();
        });


    }

    //Get current blind data
    private void get_current(String x) {
        // Get a reference to the blinds
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Blinds").child(x);
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Buttons to turn schedule on/off
                String schedule = dataSnapshot.child("schedule").child("ON").getValue(String.class);

                if (schedule.equals("TRUE")){
                    turn_off.setVisibility(View.VISIBLE);
                    turn_on.setVisibility(View.GONE);
                } else{
                    turn_off.setVisibility(View.GONE);
                    turn_on.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(schedule.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}