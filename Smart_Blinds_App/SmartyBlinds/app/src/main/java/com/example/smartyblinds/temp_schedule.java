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

public class temp_schedule extends AppCompatActivity {

    EditText set_temp;
    Button create_schedule3, back7;
    int operation = 3;
    RadioGroup set_operation3;

    int temp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_schedule);

        //Get blind name and serial number from previous activity
        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String serial = i.getStringExtra("serial");

        //Set Temp
        set_temp = findViewById(R.id.set_temp);

        //Set the operation
        set_operation3 = findViewById(R.id.set_operation3);
        set_operation3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
        create_schedule3 = findViewById(R.id.create_schedule3);
        create_schedule3.setOnClickListener(view -> {

            //Input Validation
            if(set_temp.getText().toString().isEmpty()){
                set_temp.setError("Please select a temperature value!");
                set_temp.requestFocus();
                return;
            }

            temp = Integer.parseInt(set_temp.getText().toString().trim());

            if(temp <= 0){
                set_temp.setError("Please select a positive temperature value!");
                set_temp.requestFocus();
                return;
            }if(operation == 3){
                Toast.makeText(temp_schedule.this, "Please select an operation!", Toast.LENGTH_SHORT).show();
                return;
            }

            //Update database with schedule values
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/type").setValue("temp");
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/light").setValue(temp);
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/schedule/operation").setValue(operation);

            Toast.makeText(this, "Successfully created temperature schedule!", Toast.LENGTH_SHORT).show();
        });

        //Go back to previous activity
        back7 = findViewById(R.id.back7);
        back7.setOnClickListener(view -> {
            Intent ii = new Intent(temp_schedule.this, schedule.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }


}