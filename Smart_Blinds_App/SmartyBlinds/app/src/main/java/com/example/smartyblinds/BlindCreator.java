package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BlindCreator extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    Button pairBlind;
    EditText BlindName, SerialNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind_creator);

        //Get email from previous activity
        Intent i = getIntent();
        String email = i.getStringExtra("email");

        //Pair blind
        pairBlind = findViewById(R.id.pairBlind);
        pairBlind.setOnClickListener(view -> {

            BlindName = findViewById(R.id.BlindName);
            SerialNum = findViewById(R.id.SerialNum);

            String name = BlindName.getText().toString().trim();
            String serial = SerialNum.getText().toString().trim();

            //Input Validation
            if (serial.isEmpty()) {
                SerialNum.setError("Serial Number is required!");
                SerialNum.requestFocus();
                return;
            }
            if (name.isEmpty()) {
                BlindName.setError("Name is required!");
                BlindName.requestFocus();
                return;
            }

            //See if blinds exist
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Blinds/" + serial + "/User");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //Check if blinds are already registered with another user
                        String registeredEmail = dataSnapshot.getValue(String.class);
                        if (registeredEmail.equals("NULL")) {

                            //Set the blinds to be registered under this user.
                            databaseReference.setValue(email);
                            Toast.makeText(BlindCreator.this, "Registration Success!", Toast.LENGTH_SHORT).show();

                            //Set the user to be registered under the blinds
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            reference = FirebaseDatabase.getInstance().getReference("Users");
                            userID = user.getUid();

                            DatabaseReference dataRef = reference.child(userID).child("total");
                            dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                    int total = dataSnapshot2.getValue(Integer.class);
                                    total += 1;


                                    reference.child(userID).child("Reg_Blinds").child(Integer.toString(total)).child("title").setValue(name);
                                    reference.child(userID).child("Reg_Blinds").child(Integer.toString(total)).child("serial").setValue(serial);
                                    reference.child(userID).child("total").setValue(total);

                                    //Send user back to home page
                                    startActivity(new Intent(BlindCreator.this, HomePage.class));

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError2) {
                                    Toast.makeText(BlindCreator.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // The blinds are already registered with another user, show an error message
                            SerialNum.setError("Blinds are already registered with another user!");
                            SerialNum.requestFocus();
                        }
                    } else {
                        // The reference does not exist in the database
                        SerialNum.setError("Serial Number does not exist!");
                        SerialNum.requestFocus();
                        return;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Error checking if the reference exists: " + databaseError.getMessage());
                }
            });


        });
    }
}