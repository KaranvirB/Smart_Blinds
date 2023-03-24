package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CurrentBlind extends AppCompatActivity {

    TextView textview_blind_name, textview_blind_model, current_state, current_light, current_temp;
    Button ON_button, OFF_button, add_user, back, turn_auto_on, turn_auto_off, schedule_button;

    private DatabaseReference reference;

    String auto = "ON";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_blind);

        //Get blind name and serial number from previous activity
        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String serial = i.getStringExtra("serial");

        textview_blind_name = findViewById(R.id.textview_blind_name);
        textview_blind_name.setText(title);

        get_current(serial);

        //Buttons to turn auto on/off
        turn_auto_on = findViewById(R.id.turn_on_auto);
        turn_auto_off = findViewById(R.id.turn_off_auto);

        if (auto == "ON"){
            turn_auto_off.setVisibility(View.VISIBLE);
            turn_auto_on.setVisibility(View.GONE);
        } else{
            turn_auto_off.setVisibility(View.GONE);
            turn_auto_on.setVisibility(View.VISIBLE);
        }

        turn_auto_off.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("auto").setValue("OFF");
            get_current(serial);
            turn_auto_off.setVisibility(View.GONE);
            turn_auto_on.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Auto Mode Disabled!", Toast.LENGTH_SHORT).show();
        });

        turn_auto_on.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("auto").setValue("ON");
            get_current(serial);
            turn_auto_off.setVisibility(View.VISIBLE);
            turn_auto_on.setVisibility(View.GONE);
            Toast.makeText(this, "Auto Mode Enabled!", Toast.LENGTH_SHORT).show();
        });

        //Roll Blinds Up
        ON_button = findViewById(R.id.ON_button);
        ON_button.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Operation").setValue("Manual");
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Blind_State").setValue(1);
            get_current(serial);
            Toast.makeText(this, "Blinds Rolled Up!", Toast.LENGTH_SHORT).show();
        });

        //Roll Blinds Down
        OFF_button = findViewById(R.id.OFF_button);
        OFF_button.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Operation").setValue("Manual");
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Blind_State").setValue(0);
            get_current(serial);
            Toast.makeText(this, "Blinds Rolled Down!", Toast.LENGTH_SHORT).show();
        });

        //Add a user to blinds
        add_user= findViewById(R.id.add_user);
        add_user.setOnClickListener(view -> {
            Intent ii = new Intent(CurrentBlind.this, add_user.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        //Go back to previous activity
        back = findViewById(R.id.back);
        back.setOnClickListener(view -> {
            startActivity(new Intent(CurrentBlind.this, HomePage.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        //Go to schedule page
        schedule_button = findViewById(R.id.schedule_button);
        schedule_button.setOnClickListener(view -> {
            Intent iii = new Intent(CurrentBlind.this, schedule.class);
            iii.putExtra("serial",serial);
            iii.putExtra("title",title);
            startActivity(iii);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

    }

    private void get_current(String x){
        // Get a reference to the blinds
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Blinds").child(x);
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Get current blind data
                Integer stateObj = dataSnapshot.child("Blind_State").getValue(Integer.class);
                int state = (stateObj == null) ? 0 : stateObj;
                Integer stateObj2 = dataSnapshot.child("Current").child("Light").getValue(Integer.class);
                int light = (stateObj2 == null) ? 0 : stateObj2;
                Double stateObj3 = dataSnapshot.child("Current").child("Temp").getValue(Double.class);
                double temp = (stateObj3 == null) ? 0 : stateObj3;

                current_state = findViewById(R.id.current_state);
                current_light = findViewById(R.id.current_light);
                current_temp = findViewById(R.id.current_temp);

                if (state == 1) {
                    current_state.setText("Rolled Up");
                } else {
                    current_state.setText("Rolled Down");
                }

                current_light.setText("Light: " + Integer.toString(light));
                current_temp.setText("Temperature: " + Double.toString(temp));

                //Buttons to turn auto on/off
                auto = dataSnapshot.child("auto").getValue(String.class);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CurrentBlind.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}