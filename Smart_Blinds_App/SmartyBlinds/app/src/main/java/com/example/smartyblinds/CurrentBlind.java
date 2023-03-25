package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CurrentBlind extends AppCompatActivity {

    TextView textview_blind_name, textview_blind_model, current_state, current_light, current_temp;
    Button ON_button, OFF_button, add_user, back, turn_ai_on, turn_ai_off, schedule_button, set_length_button;
    EditText set_length;

    private DatabaseReference reference;

//    String ai = "ON";

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

        //Buttons to turn AI on/off
        turn_ai_on = findViewById(R.id.turn_on_ai);
        turn_ai_off = findViewById(R.id.turn_off_ai);

        turn_ai_off.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("AI").setValue("OFF");
            get_current(serial);
            Toast.makeText(this, "AI Mode Disabled!", Toast.LENGTH_SHORT).show();
        });

        turn_ai_on.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("AI").setValue("ON");
            get_current(serial);
            Toast.makeText(this, "AI Mode Enabled!", Toast.LENGTH_SHORT).show();
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

        //Set Blind Closing Length (Inch)
        set_length = findViewById(R.id.set_length);
        set_length_button = findViewById(R.id.set_length_button);
        set_length_button.setOnClickListener(view -> {

            get_current(serial);
            String inch = set_length.getText().toString().trim();

            //Input Validation
            if (inch.isEmpty()) {
                set_length.setError("Length is required!");
                set_length.requestFocus();
                return;
            }

            if (Integer.parseInt(inch) > 60){
                set_length.setError("Length Exceeds Maximum!");
                set_length.requestFocus();
                return;
            }
            if (Integer.parseInt(inch) <= 0){
                set_length.setError("Positive non-zero numbers required!");
                set_length.requestFocus();
                return;
            }

            //1 inch is ~400 milliseconds
            int length_dur = Integer.parseInt(inch) * 400;

            //Want to make sure blinds are rolled up before length is set
            if (current_state.getText().toString().equals("Rolled Up")) {
                FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/length").setValue(length_dur);
                Toast.makeText(this, "Blind Length Set!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Make sure blinds are rolled up first!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Get current blind data
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

                //Buttons to turn AI on/off
                String ai = dataSnapshot.child("AI").getValue(String.class);
                if (ai.equals("ON")){
                    turn_ai_off.setVisibility(View.VISIBLE);
                    turn_ai_on.setVisibility(View.GONE);
                } else{
                    turn_ai_off.setVisibility(View.GONE);
                    turn_ai_on.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CurrentBlind.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}