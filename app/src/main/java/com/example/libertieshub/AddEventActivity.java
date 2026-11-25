package com.example.libertieshub;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AddEventActivity extends AppCompatActivity {

    EditText titleEditText, descriptionEditText;
    EditText startEditText, endEditText;
    EditText venueEditText, addressEditText, eircodeEditText;
    EditText latitudeEditText, longitudeEditText;
    EditText organisationEditText, capacityEditText;
    EditText accessibilityEditText;

    Spinner typeSpinner, categorySpinner, recurrenceSpinner;
    CheckBox freeCheckBox;
    Button saveButton, deleteButton;

    DatabaseReference eventsRef;
    String eventKey;

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
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.GONE);

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
        eventKey = getIntent().getStringExtra("eventKey");

        if (eventKey != null) {

            // Protect the lecturer's original numbered records
            if (eventKey.matches("\\d+")) {

                Toast.makeText(this,
                        "Original events cannot be edited",
                        Toast.LENGTH_SHORT).show();

                finish();
                return;
            }

            saveButton.setText("Save Changes");
            deleteButton.setVisibility(View.VISIBLE);

            loadEvent();
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    private void loadEvent() {

        eventsRef.child(eventKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot eventSnapshot) {

                        if (!eventSnapshot.exists()) {
                            Toast.makeText(AddEventActivity.this,
                                    "Event not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        titleEditText.setText(eventSnapshot.child("title")
                                .getValue(String.class));
                        descriptionEditText.setText(eventSnapshot.child("description")
                                .getValue(String.class));

                        String category = eventSnapshot.child("category")
                                .getValue(String.class);

                        for (int i = 0; i < categorySpinner.getCount(); i++) {
                            if (categorySpinner.getItemAtPosition(i)
                                    .toString().equals(category)) {
                                categorySpinner.setSelection(i);
                                break;
                            }
                        }

                        startEditText.setText(eventSnapshot.child("datetime")
                                .child("start").getValue(String.class));
                        endEditText.setText(eventSnapshot.child("datetime")
                                .child("end").getValue(String.class));

                        String recurrence = eventSnapshot.child("datetime")
                                .child("recurrence").getValue(String.class);

                        if (recurrence != null && !recurrence.equals("none")) {
                            typeSpinner.setSelection(1);

                            for (int i = 0; i < recurrenceSpinner.getCount(); i++) {
                                if (recurrenceSpinner.getItemAtPosition(i)
                                        .toString().equalsIgnoreCase(recurrence)) {
                                    recurrenceSpinner.setSelection(i);
                                    break;
                                }
                            }
                        } else {
                            typeSpinner.setSelection(0);
                        }

                        venueEditText.setText(eventSnapshot.child("location")
                                .child("venue_name").getValue(String.class));
                        addressEditText.setText(eventSnapshot.child("location")
                                .child("address").getValue(String.class));
                        eircodeEditText.setText(eventSnapshot.child("location")
                                .child("eircode").getValue(String.class));

                        Double latitude = eventSnapshot.child("location")
                                .child("coordinates").child("lat")
                                .getValue(Double.class);
                        Double longitude = eventSnapshot.child("location")
                                .child("coordinates").child("lng")
                                .getValue(Double.class);

                        if (latitude != null) {
                            latitudeEditText.setText(String.valueOf(latitude));
                        }
                        if (longitude != null) {
                            longitudeEditText.setText(String.valueOf(longitude));
                        }

                        organisationEditText.setText(eventSnapshot
                                .child("partner_organization").child("name")
                                .getValue(String.class));

                        Long capacity = eventSnapshot.child("capacity")
                                .getValue(Long.class);
                        if (capacity != null) {
                            capacityEditText.setText(String.valueOf(capacity));
                        }

                        Boolean isFree = eventSnapshot.child("is_free")
                                .getValue(Boolean.class);
                        freeCheckBox.setChecked(Boolean.TRUE.equals(isFree));

                        String accessibility = "";
                        for (DataSnapshot item : eventSnapshot
                                .child("accessibility").getChildren()) {
                            String feature = item.getValue(String.class);
                            if (feature != null) {
                                if (!accessibility.isEmpty()) {
                                    accessibility += ", ";
                                }
                                accessibility += feature;
                            }
                        }
                        accessibilityEditText.setText(accessibility);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AddEventActivity.this,
                                databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void confirmDelete() {

        // Only app-created events can be deleted from this form
        if (eventKey == null || eventKey.matches("\\d+")) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Event / Service")
                .setMessage("Are you sure you want to delete this? This cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteButton.setEnabled(false);

                        eventsRef.child(eventKey).removeValue()
                                .addOnCompleteListener(AddEventActivity.this,
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(AddEventActivity.this,
                                                            "Event / Service deleted",
                                                            Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    deleteButton.setEnabled(true);
                                                    Toast.makeText(AddEventActivity.this,
                                                            "Failed to delete",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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

        // Add a new record or update the selected one
        saveButton.setEnabled(false);

        DatabaseReference saveRef;

        if (eventKey == null) {
            saveRef = eventsRef.push();
        } else {
            saveRef = eventsRef.child(eventKey);
        }

        saveRef.setValue(event)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(AddEventActivity.this,
                                    "Event / Service saved successfully",
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