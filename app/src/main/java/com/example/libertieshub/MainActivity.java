package com.example.libertieshub;

import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    ListView eventsListView;
    ArrayList<String> eventTitles;
    ArrayAdapter<String> adapter;
    DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventsListView = findViewById(R.id.eventsListView);
        eventTitles = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, eventTitles);
        eventsListView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        eventsRef = database.getReference("community_events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                eventTitles.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String title = eventSnapshot.child("title")
                            .getValue(String.class);

                    if (title != null) {
                        eventTitles.add(title);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(MainActivity.this,
                        databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}