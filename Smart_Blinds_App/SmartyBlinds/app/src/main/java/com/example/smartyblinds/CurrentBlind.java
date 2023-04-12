package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CurrentBlind extends AppCompatActivity {

    TextView textview_blind_name;
    Button ON_button, OFF_button, back, turn_ml_on, turn_ml_off, schedule_button;
    ImageButton info;

    private DatabaseReference reference;

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

        //Buttons to turn ML on/off
        turn_ml_on = findViewById(R.id.turn_on_ml);
        turn_ml_off = findViewById(R.id.turn_off_ml);

        turn_ml_off.setOnClickListener(view -> {
            //Set ML to OFF in database
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("ML").setValue("OFF");
            get_current(serial);
            Toast.makeText(this, "ML Mode Disabled!", Toast.LENGTH_SHORT).show();
        });

        turn_ml_on.setOnClickListener(view -> {
            //Set ML to ON in database
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("ML").setValue("ON");

            //By turing on ML, we disable schedule mode automatically
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("schedule").child("ON").setValue("FALSE");
            get_current(serial);
            Toast.makeText(this, "ML Mode Enabled!", Toast.LENGTH_SHORT).show();
        });

        //Roll Blinds Up
        ON_button = findViewById(R.id.ON_button);
        ON_button.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Operation").setValue("Manual");
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Blind_State").setValue((int)1);

            //Disable ML and Schedule in database
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("ML").setValue("OFF");
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("schedule").child("ON").setValue("FALSE");

            get_current(serial);

            Toast.makeText(this, "Blinds Rolled Up!\n Auto Disabled!", Toast.LENGTH_SHORT).show();
        });

        //Roll Blinds Down
        OFF_button = findViewById(R.id.OFF_button);
        OFF_button.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Operation").setValue("Manual");
            FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Blind_State").setValue((int)0);

            //Disable ML and Schedule in database
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("ML").setValue("OFF");
            FirebaseDatabase.getInstance().getReference("Blinds").child(serial).child("schedule").child("ON").setValue("FALSE");

            get_current(serial);

            Toast.makeText(this, "Blinds Rolled Down!\n Auto Disabled!", Toast.LENGTH_SHORT).show();
        });

        //More information page
        info = findViewById(R.id.info);
        info.setOnClickListener(view -> {
            Intent ii = new Intent(CurrentBlind.this, blind_info.class);
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

    //Get current blind data
    private void get_current(String x){
        // Get a reference to the blinds
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Blinds").child(x);
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Buttons to turn ML on/off
                String ml = dataSnapshot.child("ML").getValue(String.class);
                if (ml.equals("ON")){
                    turn_ml_off.setVisibility(View.VISIBLE);
                    turn_ml_on.setVisibility(View.GONE);
                } else{
                    turn_ml_off.setVisibility(View.GONE);
                    turn_ml_on.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CurrentBlind.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}