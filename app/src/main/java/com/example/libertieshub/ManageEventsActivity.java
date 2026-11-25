package com.example.libertieshub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageEventsActivity extends AppCompatActivity {

    ListView manageEventsListView;

    ArrayList<String> eventTitles;
    ArrayList<String> eventKeys;

    ArrayAdapter<String> adapter;

    DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        manageEventsListView = findViewById(R.id.manageEventsListView);

        eventTitles = new ArrayList<>();
        eventKeys = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                eventTitles);

        manageEventsListView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        eventsRef = database.getReference("community_events");

        eventsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                eventTitles.clear();
                eventKeys.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                    String key = eventSnapshot.getKey();

                    String title = eventSnapshot.child("title")
                            .getValue(String.class);

                    // Keep the lecturer's original numbered records untouched
                    if (key != null && title != null
                            && !key.matches("\\d+")) {

                        eventTitles.add(title);
                        eventKeys.add(key);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(ManageEventsActivity.this,
                        databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Select an event
        manageEventsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view,
                                            int position,
                                            long id) {

                        Intent intent = new Intent(
                                ManageEventsActivity.this,
                                AddEventActivity.class);

                        intent.putExtra("eventKey", eventKeys.get(position));

                        startActivity(intent);
                    }
                });
    }
}