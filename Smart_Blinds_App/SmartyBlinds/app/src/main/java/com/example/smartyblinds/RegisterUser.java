package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterUser extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;

    private DatabaseReference databaseReference;

    private EditText serial_reg, name_reg, email_reg, password_reg, rePassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        //Initialize views
        Button but_button_reg = findViewById(R.id.registerUser);
        serial_reg = findViewById(R.id.SerialNum);
        name_reg = findViewById(R.id.fullName);
        email_reg = findViewById(R.id.emailRegister);
        password_reg = findViewById(R.id.passwordRegister);
        rePassword = findViewById(R.id.rePassword);

        //Register the user
        but_button_reg.setOnClickListener(view -> registerUser());
    }


    private void registerUser() {
        String serial = serial_reg.getText().toString().trim();
        String name = name_reg.getText().toString().trim();
        String email = email_reg.getText().toString().trim();
        String password = password_reg.getText().toString().trim();
        String Repassword = rePassword.getText().toString().trim();

        //Input Validation
        if (serial.isEmpty()) {
            serial_reg.setError("Serial Number is required!");
            serial_reg.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            name_reg.setError("Name is required!");
            name_reg.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            email_reg.setError("Email is required!");
            email_reg.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            password_reg.setError("Password is required!");
            password_reg.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_reg.setError("Provide valid Email!");
            email_reg.requestFocus();
            return;
        }
        if (password.length() < 6) {
            password_reg.setError("Password must be at least 6 characters!");
            password_reg.requestFocus();
            return;
        }
        if (!Repassword.equals(password)) {
            rePassword.setError("Password must match!");
            rePassword.requestFocus();
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
                        //Create user in FireBase
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(RegisterUser.this, task -> {
                                    if (task.isSuccessful()) {
                                        //Add user attributes to realtime database
                                        User user = new User(name, email, serial);
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(user);
                                        //Set the blinds to be registered under this user.
                                        databaseReference.setValue(email);
                                        Toast.makeText(RegisterUser.this, "Registration Success!", Toast.LENGTH_SHORT).show();
                                        //Send user back to login page
                                        startActivity(new Intent(RegisterUser.this, MainActivity.class));
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(RegisterUser.this, "Error in Registration!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // The blinds are already registered with another user, show an error message
                        serial_reg.setError("Blinds are already registered with another user!");
                        serial_reg.requestFocus();
                    }
                } else {
                    // The reference does not exist in the database
                    serial_reg.setError("Serial Number does not exist!");
                    serial_reg.requestFocus();
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error checking if the reference exists: " + databaseError.getMessage());
            }
        });

        }







}