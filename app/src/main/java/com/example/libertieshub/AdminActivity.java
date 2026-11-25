package com.example.libertieshub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    LinearLayout adminLayout;
    Button addEventButton, manageSurveysButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Connect buttons to XML
        adminLayout = findViewById(R.id.adminLayout);
        addEventButton = findViewById(R.id.addEventButton);
        manageSurveysButton = findViewById(R.id.manageSurveysButton);

        // Hide admin options until access is verified
        adminLayout.setVisibility(View.GONE);

        // Open Add Event screen
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        AdminActivity.this, AddEventActivity.class);
                startActivity(intent);
            }
        });

        // Open Manage Surveys screen
        manageSurveysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        AdminActivity.this, AdminSurveyActivity.class);
                startActivity(intent);
            }
        });

        // Get logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        // Check administrator permissions in Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        database.getReference("admins")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Boolean isAdmin = dataSnapshot.getValue(Boolean.class);

                        if (Boolean.TRUE.equals(isAdmin)) {
                            adminLayout.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(AdminActivity.this,
                                    "Administrator access required",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(AdminActivity.this,
                                databaseError.getMessage(),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }
}