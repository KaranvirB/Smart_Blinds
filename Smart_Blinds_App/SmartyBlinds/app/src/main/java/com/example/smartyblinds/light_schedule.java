package com.example.smartyblinds;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

public class light_schedule extends AppCompatActivity {

    EditText set_light;
    Button create_schedule2, back6, sun, moon;
    int operation = 3;
    RadioGroup set_operation2;

    int light = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_schedule);

        //Get blind name and serial number from previous activity
        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String serial = i.getStringExtra("serial");

        //Set Light
        set_light = findViewById(R.id.set_light);

        //Sun button
        sun = findViewById(R.id.sun);
        sun.setOnClickListener(view -> {
            set_light.setText(Integer.toString(630));
        });

        //Moon button
        moon = findViewById(R.id.moon);
        moon.setOnClickListener(view -> {
            set_light.setText(Integer.toString(500));
        });

        //Set the operation
        set_operation2 = findViewById(R.id.set_operation2);
        set_operation2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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

        //Set schedule
        create_schedule2 = findViewById(R.id.create_schedule2);
        create_schedule2.setOnClickListener(view -> {

            //Input Validation
            if(set_light.getText().toString().isEmpty()){
                set_light.setError("Please select a light value!");
                set_light.requestFocus();
                return;
            }

            light = Integer.parseInt(set_light.getText().toString().trim());

            if(light <= 0){
                set_light.setError("Please select a positive light value!");
                set_light.requestFocus();
                return;
            }if(operation == 3){
                Toast.makeText(light_schedule.this, "Please select an operation!", Toast.LENGTH_SHORT).show();
                return;
            }

            //Update database with schedule values
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/type").setValue("light");
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/light").setValue(light);
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/operation").setValue(operation);

            Toast.makeText(this, "Successfully created light schedule!", Toast.LENGTH_SHORT).show();
        });

        //Go back to previous activity
        back6 = findViewById(R.id.back6);
            back6.setOnClickListener(view -> {
            Intent ii = new Intent(light_schedule.this, schedule.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }
}