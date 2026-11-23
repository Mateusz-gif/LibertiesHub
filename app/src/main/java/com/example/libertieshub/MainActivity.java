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
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    ListView eventsListView;
    ArrayList<String> eventTitles;
    ArrayList<String> eventKeys;
    ArrayAdapter<String> adapter;
    DatabaseReference eventsRef;

    EditText searchEditText;
    Button searchButton;

    ArrayList<String> allTitles;
    ArrayList<String> allKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventsListView = findViewById(R.id.eventsListView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        allTitles = new ArrayList<>();
        allKeys = new ArrayList<>();

        eventTitles = new ArrayList<>();
        eventKeys = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, eventTitles);

        eventsListView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        eventsRef = database.getReference("community_events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allTitles.clear();
                allKeys.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String title = eventSnapshot.child("title")
                            .getValue(String.class);

                    if (title != null) {
                        allTitles.add(title);
                        allKeys.add(eventSnapshot.getKey());
                    }
                }
                searchEvents();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(MainActivity.this,
                        databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchEvents();
            }
        });

        eventsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position, long id) {
                        Intent intent = new Intent(
                                MainActivity.this, DetailsActivity.class);
                        intent.putExtra("eventKey", eventKeys.get(position));
                        startActivity(intent);
                    }
                });
       }
       private void searchEvents() {

        String searchText = searchEditText.getText()
                .toString().trim().toLowerCase();

        eventTitles.clear();
        eventKeys.clear();

        for (int i = 0; i < allTitles.size(); i++) {
            String title = allTitles.get(i);

            if (title.toLowerCase().contains(searchText)) {
                eventTitles.add(title);
                eventKeys.add(allKeys.get(i));
            }
        }

        adapter.notifyDataSetChanged();
    }
   }