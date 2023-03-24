package com.example.smartyblinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomePage extends AppCompatActivity {

    Button logout, addButton;

    TextView greeting, greeting2;

    private ArrayList<item> itemsList;
    private RecyclerView recyclerView;

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        recyclerView = findViewById(R.id.recyclerView);
        itemsList = new ArrayList<>();

        //logout
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomePage.this, MainActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String fullName = userProfile.Name;
                    String email = userProfile.email;

                    //Friendly greeting
                    greeting = findViewById(R.id.greeting);
                    greeting2 = findViewById(R.id.greeting2);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH");
                    String currentTimeString = sdf.format(new Date());
                    int currentTime = Integer.parseInt(currentTimeString);

                    if (currentTime < 12){
                        greeting.setText("Good Morning,");
                    } else {
                        greeting.setText("Good Afternoon,");
                    }
                    greeting2.setText(fullName);

                    //add blinds
                    addButton = findViewById(R.id.addButton);
                    addButton.setOnClickListener(view -> {
                        //Transfer email between activities
                        Intent i = new Intent(HomePage.this, BlindCreator.class);
                        i.putExtra("email",email);
                        startActivity(i);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    });


                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });

        // Get a reference to the "data" node
        DatabaseReference dataRef = reference.child(userID).child("Reg_Blinds");

        //Count the number of children
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numChildren = (int) dataSnapshot.getChildrenCount();

                if (numChildren != 0) {
                    for (int i = 1; i <= numChildren; i++) {
                        // Get the title and text values from the current child node
                        String title = dataSnapshot.child(String.valueOf(i)).child("title").getValue(String.class);
                        String serial = dataSnapshot.child(String.valueOf(i)).child("serial").getValue(String.class);

                        // Add the title and serial values to the list
                        itemsList.add(new item(title, serial));
                    }
                }
                setAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePage.this, "An error has occurred!", Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void setAdapter() {
        recyclerAdapter adapter = new recyclerAdapter(itemsList, HomePage.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }
}