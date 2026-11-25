package com.example.libertieshub;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class AddEventActivity extends AppCompatActivity {

    EditText titleEditText, descriptionEditText;
    EditText startEditText, endEditText;
    EditText venueEditText, addressEditText, eircodeEditText;
    EditText latitudeEditText, longitudeEditText;
    EditText organisationEditText, capacityEditText;
    EditText accessibilityEditText;

    Spinner typeSpinner, categorySpinner, recurrenceSpinner;
    CheckBox freeCheckBox;
    Button saveButton;

    DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        startEditText = findViewById(R.id.startEditText);
        endEditText = findViewById(R.id.endEditText);

        venueEditText = findViewById(R.id.venueEditText);
        addressEditText = findViewById(R.id.addressEditText);
        eircodeEditText = findViewById(R.id.eircodeEditText);

        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);

        organisationEditText = findViewById(R.id.organisationEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        accessibilityEditText = findViewById(R.id.accessibilityEditText);

        typeSpinner = findViewById(R.id.typeSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        recurrenceSpinner = findViewById(R.id.recurrenceSpinner);

        freeCheckBox = findViewById(R.id.freeCheckBox);
        saveButton = findViewById(R.id.saveButton);

        // Event or Service
        String[] types = {"Event", "Service"};

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, types);

        typeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        typeSpinner.setAdapter(typeAdapter);

        // Categories
        String[] categories = {
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

        // Recurring Services
        String[] recurrenceOptions = {
                "Daily", "Weekly", "Monthly"
        };

        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                recurrenceOptions);

        recurrenceAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        recurrenceSpinner.setAdapter(recurrenceAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        eventsRef = database.getReference("community_events");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });
    }

    private void saveEvent() {

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        String start = startEditText.getText().toString().trim();
        String end = endEditText.getText().toString().trim();

        String venue = venueEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String eircode = eircodeEditText.getText().toString().trim();

        String organisation = organisationEditText.getText()
                .toString().trim();

        String latitudeText = latitudeEditText.getText().toString().trim();
        String longitudeText = longitudeEditText.getText().toString().trim();
        String capacityText = capacityEditText.getText().toString().trim();

        String accessibilityText = accessibilityEditText.getText()
                .toString().trim();

        String category = categorySpinner.getSelectedItem().toString();
        String type = typeSpinner.getSelectedItem().toString();

        // Check required fields
        if (title.isEmpty() || description.isEmpty()
                || start.isEmpty() || end.isEmpty()
                || venue.isEmpty() || address.isEmpty()
                || organisation.isEmpty()
                || latitudeText.isEmpty() || longitudeText.isEmpty()
                || capacityText.isEmpty()) {

            Toast.makeText(this, "Please fill in all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check the date format
        String datePattern =
                "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";

        if (!start.matches(datePattern) || !end.matches(datePattern)) {
            Toast.makeText(this, "Use the date format shown",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (start.compareTo(end) >= 0) {
            Toast.makeText(this, "End must be after start",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude;
        double longitude;
        long capacity;

        try {
            latitude = Double.parseDouble(latitudeText);
            longitude = Double.parseDouble(longitudeText);
            capacity = Long.parseLong(capacityText);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid coordinates or capacity",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180
                || capacity < 0) {

            Toast.makeText(this, "Invalid coordinates or capacity",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Events happen once; Services can repeat
        String recurrence = "none";

        if (type.equals("Service")) {
            recurrence = recurrenceSpinner.getSelectedItem()
                    .toString().toLowerCase();
        }

        // Date and time
        HashMap<String, Object> datetime = new HashMap<>();
        datetime.put("start", start);
        datetime.put("end", end);
        datetime.put("recurrence", recurrence);

        // Map coordinates
        HashMap<String, Object> coordinates = new HashMap<>();
        coordinates.put("lat", latitude);
        coordinates.put("lng", longitude);

        // Location
        HashMap<String, Object> location = new HashMap<>();
        location.put("venue_name", venue);
        location.put("address", address);
        location.put("eircode", eircode);
        location.put("coordinates", coordinates);

        // Organisation
        HashMap<String, Object> partnerOrganization = new HashMap<>();
        partnerOrganization.put("name", organisation);

        // Accessibility features
        ArrayList<String> accessibility = new ArrayList<>();

        if (!accessibilityText.isEmpty()) {

            String[] features = accessibilityText.split(",");

            for (String feature : features) {
                if (!feature.trim().isEmpty()) {
                    accessibility.add(feature.trim());
                }
            }
        }

        // New event data
        HashMap<String, Object> event = new HashMap<>();

        event.put("title", title);
        event.put("description", description);
        event.put("category", category);
        event.put("datetime", datetime);
        event.put("location", location);
        event.put("partner_organization", partnerOrganization);
        event.put("capacity", capacity);
        event.put("is_free", freeCheckBox.isChecked());
        event.put("accessibility", accessibility);

        // Create a new Firebase record
        saveButton.setEnabled(false);

        eventsRef.push().setValue(event)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(AddEventActivity.this,
                                    "Event / Service added successfully",
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