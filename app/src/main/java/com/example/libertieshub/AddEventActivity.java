package com.example.libertieshub;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddEventActivity extends AppCompatActivity {

    EditText titleEditText, descriptionEditText, startEditText, endEditText;
    EditText venueEditText, addressEditText, latitudeEditText, longitudeEditText;
    Spinner typeSpinner, categorySpinner, recurrenceSpinner;
    Button saveButton;
    DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Connect the form fields
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        startEditText = findViewById(R.id.startEditText);
        endEditText = findViewById(R.id.endEditText);

        venueEditText = findViewById(R.id.venueEditText);
        addressEditText = findViewById(R.id.addressEditText);
        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);

        typeSpinner = findViewById(R.id.typeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        recurrenceSpinner = findViewById(R.id.recurrenceSpinner);

        saveButton = findViewById(R.id.saveButton);

        // Event / Service, category and recurrence options
        setSpinner(typeSpinner, new String[]{"Event", "Service"});

        setSpinner(categorySpinner, new String[]{
                "Education",
                "Health & Fitness",
                "Other",
                "Music",
                "Sports",
                "Arts & Culture",
                "Food & Markets"
        });

        setSpinner(recurrenceSpinner,
                new String[]{"Daily", "Weekly", "Monthly"});

        // Firebase connection
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        eventsRef = database.getReference("community_events");

        // Save button
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });
    }

    // Set up the Spinners
    private void setSpinner(Spinner spinner, String[] options) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, options);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }

    private void saveEvent() {

        // Read the form
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String start = startEditText.getText().toString().trim();
        String end = endEditText.getText().toString().trim();
        String venue = venueEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        String latText = latitudeEditText.getText().toString().trim();
        String lngText = longitudeEditText.getText().toString().trim();

        // Check required fields
        if (title.isEmpty() || description.isEmpty() ||
                start.isEmpty() || end.isEmpty() ||
                venue.isEmpty() || address.isEmpty() ||
                latText.isEmpty() || lngText.isEmpty()) {

            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check date format
        String datePattern =
                "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";

        if (!start.matches(datePattern) ||
                !end.matches(datePattern) ||
                start.compareTo(end) >= 0) {
            Toast.makeText(this, "Check the start and end dates",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Convert map coordinates
        double latitude, longitude;

        try {
            latitude = Double.parseDouble(latText);
            longitude = Double.parseDouble(lngText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid map coordinates",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude < -90 || latitude > 90 ||
                longitude < -180 || longitude > 180) {

            Toast.makeText(this, "Invalid map coordinates",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Event or recurring Service
        String recurrence = "none";

        if (typeSpinner.getSelectedItemPosition() == 1) {
            recurrence = recurrenceSpinner.getSelectedItem()
                    .toString().toLowerCase();
        }

        // Date and time data
        HashMap<String, Object> datetime = new HashMap<>();
        datetime.put("start", start);
        datetime.put("end", end);
        datetime.put("recurrence", recurrence);

        // Map coordinates
        HashMap<String, Object> coordinates = new HashMap<>();
        coordinates.put("lat", latitude);
        coordinates.put("lng", longitude);

        // Location data
        HashMap<String, Object> location = new HashMap<>();
        location.put("venue_name", venue);
        location.put("address", address);
        location.put("coordinates", coordinates);

        // Event data
        HashMap<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description", description);
        event.put("category", categorySpinner.getSelectedItem().toString());
        event.put("datetime", datetime);
        event.put("location", location);

        // Save a new event to Firebase
        saveButton.setEnabled(false);

        eventsRef.push().setValue(event)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddEventActivity.this,
                                    "Event / Service added",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            saveButton.setEnabled(true);

                            Toast.makeText(AddEventActivity.this,
                                    "Failed to save",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}