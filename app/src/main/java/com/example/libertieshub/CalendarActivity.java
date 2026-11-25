package com.example.libertieshub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;
    ListView calendarEventsListView;
    TextView selectedDateTextView;
    ArrayList<String> allTitles;
    ArrayList<String> allKeys;
    ArrayList<String> allDates;
    ArrayList<String> eventTitles;
    ArrayList<String> eventKeys;
    ArrayAdapter<String> adapter;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        calendarEventsListView = findViewById(R.id.calendarEventsListView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);

        TextView noEventsTextView = findViewById(R.id.noEventsTextView);

        allTitles = new ArrayList<>();
        allKeys = new ArrayList<>();
        allDates = new ArrayList<>();

        eventTitles = new ArrayList<>();
        eventKeys = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, eventTitles);

        calendarEventsListView.setAdapter(adapter);
        calendarEventsListView.setEmptyView(noEventsTextView);

        // Start with today's date
        Calendar today = Calendar.getInstance();
        selectedDate = String.format(Locale.US,
                "%04d-%02d-%02d",
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH));

        selectedDateTextView.setText("Events on " + selectedDate);

        // Read events from Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        database.getReference("community_events")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        allTitles.clear();
                        allKeys.clear();
                        allDates.clear();

                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            String title = eventSnapshot.child("title")
                                    .getValue(String.class);

                            String start = eventSnapshot.child("datetime")
                                    .child("start")
                                    .getValue(String.class);

                            if (title != null && start != null) {
                                allTitles.add(title);
                                allKeys.add(eventSnapshot.getKey());
                                allDates.add(start);
                            }
                        }

                        showEvents();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(CalendarActivity.this,
                                databaseError.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

        // When the user selects a date
        calendarView.setOnDateChangeListener(
                new CalendarView.OnDateChangeListener() {

                    @Override
                    public void onSelectedDayChange(CalendarView view,
                                                    int year,
                                                    int month,
                                                    int dayOfMonth) {
                        selectedDate = String.format(Locale.US,
                                "%04d-%02d-%02d",
                                year, month + 1, dayOfMonth);

                        selectedDateTextView.setText(
                                "Events on " + selectedDate);

                        showEvents();
                    }
                });

        // Open event details
        calendarEventsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view,
                                            int position,
                                            long id) {
                        Intent intent = new Intent(
                                CalendarActivity.this,
                                DetailsActivity.class);

                        intent.putExtra("eventKey",
                                eventKeys.get(position));

                        startActivity(intent);
                    }
                });
    }
    private void showEvents() {

        eventTitles.clear();
        eventKeys.clear();

        for (int i = 0; i < allTitles.size(); i++) {
            String start = allDates.get(i);

            if (start.startsWith(selectedDate)) {
                eventTitles.add(allTitles.get(i));
                eventKeys.add(allKeys.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }
}