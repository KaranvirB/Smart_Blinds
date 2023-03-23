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

    private EditText name_reg, email_reg, password_reg, rePassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        //Initialize views
        Button but_button_reg = findViewById(R.id.registerUser);
        name_reg = findViewById(R.id.fullName);
        email_reg = findViewById(R.id.emailRegister);
        password_reg = findViewById(R.id.passwordRegister);
        rePassword = findViewById(R.id.rePassword);

        //Register the user
        but_button_reg.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String name = name_reg.getText().toString().trim();
        String email = email_reg.getText().toString().trim();
        String password = password_reg.getText().toString().trim();
        String Repassword = rePassword.getText().toString().trim();

        //Input Validation
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

        //Create user in FireBase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterUser.this, task -> {
                    if (task.isSuccessful()) {
                        //Add user attributes to realtime database
                        User user = new User(name, email.toLowerCase(), 0);
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user);

                        Toast.makeText(RegisterUser.this, "Registration Success!", Toast.LENGTH_SHORT).show();
                        //Send user back to login page
                        startActivity(new Intent(RegisterUser.this, MainActivity.class));
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(RegisterUser.this, "Error in Registration!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}