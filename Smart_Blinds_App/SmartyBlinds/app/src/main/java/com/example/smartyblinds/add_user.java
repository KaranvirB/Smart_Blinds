package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class add_user extends AppCompatActivity {

    EditText new_user_email;
    Button add_user_button, back3;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        //Go back to previous activity
        back3 = findViewById(R.id.back3);
        back3.setOnClickListener(view -> {
            startActivity(new Intent(add_user.this, HomePage.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        //Get blind name and serial number from previous activity
        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String serial = i.getStringExtra("serial");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        new_user_email = findViewById(R.id.new_user_email);
        add_user_button = findViewById(R.id.add_user_button);

        add_user_button.setOnClickListener(view -> {
            String email = new_user_email.getText().toString().trim();

            //Input Validation
            if (email.isEmpty()) {
                new_user_email.setError("Email is required!");
                new_user_email.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                new_user_email.setError("Provide valid Email!");
                new_user_email.requestFocus();
                return;
            }

            // Query the database to find the user ID associated with the entered email
            FirebaseDatabase.getInstance().getReference("Users").orderByChild("email").equalTo(email.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User with the entered email exists in the database
                        String userID = snapshot.getChildren().iterator().next().getKey();

                        // Use the retrieved user ID to add the user to the database
                        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("total");
                        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int total = dataSnapshot.getValue(Integer.class);
                                total += 1;

                                FirebaseDatabase.getInstance().getReference("Users").child(userID).child("Reg_Blinds").child(Integer.toString(total)).child("title").setValue(title);
                                FirebaseDatabase.getInstance().getReference("Users").child(userID).child("Reg_Blinds").child(Integer.toString(total)).child("serial").setValue(serial);
                                FirebaseDatabase.getInstance().getReference("Users").child(userID).child("total").setValue(total);

                                Toast.makeText(add_user.this, "Successfully Added User!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError2) {
                                Toast.makeText(add_user.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // User with the entered email does not exist in the database
                        Toast.makeText(add_user.this, "The email is not registered", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(add_user.this, "Database Error", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }
}