package com.example.smartyblinds;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button ON_button, OFF_button, Set;
    EditText SetLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ON_button = findViewById(R.id.ON_button);
        OFF_button = findViewById(R.id.OFF_button);
        Set = findViewById(R.id.Set);
        SetLight = findViewById(R.id.SetLight);

        //Roll Blinds Up
        ON_button.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds").setValue(1);
        });

        //Roll Blinds Down
        OFF_button.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference("Blinds").setValue(0);
        });

        Set.setOnClickListener(view -> {
            int num = Integer.parseInt(SetLight.getText().toString());
            FirebaseDatabase.getInstance().getReference("SetLight").setValue(num);
        });


    }
}