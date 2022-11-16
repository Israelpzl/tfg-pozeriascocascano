package com.example.leeconmonclick;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeProfesionalActivity extends AppCompatActivity {

    private TextView nameProfesional;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_profesional);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        nameProfesional = findViewById(R.id.nameProfesionalId);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        String userCollection = user.getEmail();
        String[] parts = userCollection.split("@");
        userCollection = parts[0];
        userCollection = userCollection.toLowerCase();

        databaseReference.child("Users").child(userCollection).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                nameProfesional.setText(snapshot.child("nombre").getValue().toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    public void goHelp(View v){
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }


    public void goListPatient(View v){
        Intent helpIntent = new Intent(this, ListPatientActivity.class);
        startActivity(helpIntent);
    }

    public void goListTask(View v){
        Intent helpIntent = new Intent(this, TaskListActivity.class);
        startActivity(helpIntent);
    }

    public void goContent(View v){
        Intent helpIntent = new Intent(this, ContentListActivity.class);
        startActivity(helpIntent);
    }




    /*

      public void goListNotes(View v){
        Intent helpIntent = new Intent(this, ListNotes.class);
        startActivity(helpIntent);
    }

    public void goSettings(View v){
        Intent helpIntent = new Intent(this, Setting.class);
        startActivity(helpIntent);
    }
    */






}