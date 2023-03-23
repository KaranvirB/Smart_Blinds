package com.example.smartyblinds;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private TextView register;
    private EditText email_log, password_log;
    private Button Login, debug;

    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize views
        register = findViewById(R.id.registerLink);
        email_log = findViewById(R.id.email);
        password_log = findViewById(R.id.password);
        Login = findViewById(R.id.signIn);

        mAuth = FirebaseAuth.getInstance();

        //Send user to register page
        register.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, RegisterUser.class));
        });


        //Log user in
        Login.setOnClickListener(view -> {
            userLogin();
        });


        //Debug
        debug = findViewById(R.id.debug);
        debug.setOnClickListener(view -> {
            //Retrieve user from FireBase
            mAuth.signInWithEmailAndPassword("bojczuk.nathan@gmail.com","Abcdefg123!").addOnCompleteListener(task -> {

                if(task.isSuccessful()){
                    Intent i = new Intent(MainActivity.this,HomePage.class);
                    //Transfer data between activities
                    i.putExtra("code","yo");
                    i.putExtra("code2","yo2");
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }else{
                    Toast.makeText(MainActivity.this, "Login Failed! Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void userLogin() {
        String email = email_log.getText().toString().trim();
        String password = password_log.getText().toString().trim();

        //Input Validation
        if (email.isEmpty()){
            email_log.setError("Email is required!");
            email_log.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_log.setError("Enter a valid Email!");
            email_log.requestFocus();
            return;
        }
        if (password.isEmpty()){
            password_log.setError("Password is required!");
            password_log.requestFocus();
            return;
        }
        if (password.length() < 6){
            password_log.setError("Password must be at least 6 characters!");
            password_log.requestFocus();
            return;
        }

        //Retrieve user from FireBase
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                startActivity(new Intent(MainActivity.this,HomePage.class));
            }else{
                Toast.makeText(MainActivity.this, "Login Failed! Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}