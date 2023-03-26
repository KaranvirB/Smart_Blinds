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

public class blind_info extends AppCompatActivity {

    Button back8, set_length_button, add_user;
    EditText set_length;
    TextView current_state, current_light, current_temp, textview_blind_name;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind_info);

        //Get blind name and serial number from previous activity
        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String serial = i.getStringExtra("serial");

        textview_blind_name = findViewById(R.id.textview_blind_name);
        textview_blind_name.setText(title);

        get_current(serial);

        //Add a user to blinds
        add_user = findViewById(R.id.add_user);
        add_user.setOnClickListener(view -> {
            Intent ii = new Intent(blind_info.this, add_user.class);
            ii.putExtra("serial", serial);
            ii.putExtra("title", title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        //Go back to previous activity
        back8 = findViewById(R.id.back8);
        back8.setOnClickListener(view -> {
            Intent ii = new Intent(blind_info.this, CurrentBlind.class);
            ii.putExtra("serial",serial);
            ii.putExtra("title",title);
            startActivity(ii);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

            if (Integer.parseInt(inch) > 60) {
                set_length.setError("Length Exceeds Maximum!");
                set_length.requestFocus();
                return;
            }
            if (Integer.parseInt(inch) <= 0) {
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
    private void get_current(String x) {
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(blind_info.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}