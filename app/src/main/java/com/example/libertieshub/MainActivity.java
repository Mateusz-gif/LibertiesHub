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
import android.widget.Spinner;

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
    Spinner categorySpinner;
    ArrayList<String> allCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventsListView = findViewById(R.id.eventsListView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        categorySpinner = findViewById(R.id.categorySpinner);

        allTitles = new ArrayList<>();
        allKeys = new ArrayList<>();
        allCategories = new ArrayList<>();

        eventTitles = new ArrayList<>();
        eventKeys = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, eventTitles);

        eventsListView.setAdapter(adapter);

        String[] categories = {
                "All Categories",
                "Education",
                "Health & Fitness",
                "Other",
                "Music",
                "Sports",
                "Arts & Culture",
                "Food & Markets"
        };

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        searchEvents();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        eventsRef = database.getReference("community_events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allTitles.clear();
                allKeys.clear();
                allCategories.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String title = eventSnapshot.child("title")
                            .getValue(String.class);

                    String category = eventSnapshot.child("category")
                            .getValue(String.class);

                    if (title != null) {
                        allTitles.add(title);
                        allKeys.add(eventSnapshot.getKey());
                        allCategories.add(category);
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

        String selectedCategory = categorySpinner.getSelectedItem()
                .toString();

        eventTitles.clear();
        eventKeys.clear();

        for (int i = 0; i < allTitles.size(); i++) {

            String title = allTitles.get(i);
            String category = getAppCategory(allCategories.get(i));

            boolean matchesSearch =
                    title.toLowerCase().contains(searchText);

            boolean matchesCategory =
                    selectedCategory.equals("All Categories")
                            || selectedCategory.equals(category);

            if (matchesSearch && matchesCategory) {
                eventTitles.add(title);
                eventKeys.add(allKeys.get(i));
            }
        }

        adapter.notifyDataSetChanged();
    }
    private String getAppCategory(String category) {

        if ("Education & Skills".equals(category)) {
            return "Education";
        }

        if ("Health & Wellbeing".equals(category)) {
            return "Health & Fitness";
        }

        if ("Arts & Culture".equals(category)) {
            return "Arts & Culture";
        }

        if ("Education".equals(category) ||
                "Health & Fitness".equals(category) ||
                "Music".equals(category) ||
                "Sports".equals(category) ||
                "Food & Markets".equals(category)) {

            return category;
        }

        return "Other";
    }
   }