package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity {

    Button ON_button, OFF_button, Set;

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String fullName = userProfile.Name;
                    String serial = userProfile.serial;


                    ON_button = findViewById(R.id.ON_button);
                    OFF_button = findViewById(R.id.OFF_button);

                    //Roll Blinds Up
                    ON_button.setOnClickListener(view -> {
                        FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Operation").setValue("Manual");
                        FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Blind_State").setValue(1);
                    });

                    //Roll Blinds Down
                    OFF_button.setOnClickListener(view -> {
                        FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Operation").setValue("Manual");
                        FirebaseDatabase.getInstance().getReference("Blinds/" + serial + "/Blind_State").setValue(0);
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });


    }
}