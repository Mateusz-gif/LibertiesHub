package com.example.libertieshub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView eventsListView;
    EditText searchEditText;
    Spinner categorySpinner;
    Button nearMeButton;
    ArrayAdapter<String> adapter;

    // Event lists
    ArrayList<DataSnapshot> allEvents = new ArrayList<>();
    ArrayList<DataSnapshot> filteredEvents = new ArrayList<>();
    ArrayList<String> eventTitles = new ArrayList<>();

    // Location
    Location userLocation;
    LocationManager locationManager;
    LocationListener locationListener;
    boolean nearMeOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect XML
        eventsListView = findViewById(R.id.eventsListView);
        searchEditText = findViewById(R.id.searchEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        nearMeButton = findViewById(R.id.nearMeButton);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Event ListView
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, eventTitles);
        eventsListView.setAdapter(adapter);

        // Category filter
        String[] categories = {
                "All Categories", "Education", "Health & Fitness",
                "Other", "Music", "Sports", "Arts & Culture", "Food & Markets"
        };

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Firebase connection
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        // Load events from Firebase
        database.getReference("community_events")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        allEvents.clear();

                        for (DataSnapshot event : snapshot.getChildren()) {
                            String title = event.child("title").getValue(String.class);

                            if (title != null) {
                                allEvents.add(event);
                            }
                        }
                        searchEvents();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(MainActivity.this,
                                error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        // Search button
        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEvents();
            }
        });

        // Near Me button
        nearMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nearMeOnly) {
                    nearMeOnly = false;
                    nearMeButton.setText("Near Me (5 km)");
                    searchEvents();
                } else {
                    getUserLocation();
                }
            }
        });

        // Calendar button
        findViewById(R.id.calendarButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CalendarActivity.class));
            }
        });

        // Map button
        findViewById(R.id.mapButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });

        // Open event details
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("eventKey", filteredEvents.get(position).getKey());
                startActivity(intent);
            }
        });

        // Administrator button
        Button adminButton = findViewById(R.id.adminButton);

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
            }
        });

        // Check administrator permission
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            database.getReference("admins").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Boolean isAdmin = snapshot.getValue(Boolean.class);

                            if (Boolean.TRUE.equals(isAdmin)) {
                                adminButton.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(MainActivity.this,
                                    error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // Search by name, category and distance
    private void searchEvents() {
        String search = searchEditText.getText().toString().trim().toLowerCase();
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        filteredEvents.clear();
        eventTitles.clear();

        for (DataSnapshot event : allEvents) {
            String title = event.child("title").getValue(String.class);
            String category = event.child("category").getValue(String.class);

            boolean matchesSearch = title.toLowerCase().contains(search);
            boolean matchesCategory = selectedCategory.equals("All Categories")
                    || selectedCategory.equals(getAppCategory(category));

            boolean matchesDistance = true;

            // Near Me distance filter
            if (nearMeOnly) {
                matchesDistance = false;

                DataSnapshot coordinates = event.child("location").child("coordinates");
                Double lat = coordinates.child("lat").getValue(Double.class);
                Double lng = coordinates.child("lng").getValue(Double.class);

                if (userLocation != null && lat != null && lng != null) {
                    Location eventLocation = new Location("event");
                    eventLocation.setLatitude(lat);
                    eventLocation.setLongitude(lng);

                    if (userLocation.distanceTo(eventLocation) <= 5000) {
                        matchesDistance = true;
                    }
                }
            }
            if (matchesSearch && matchesCategory && matchesDistance) {
                filteredEvents.add(event);
                eventTitles.add(title);
            }
        }
        adapter.notifyDataSetChanged();
    }

    //categories
    private String getAppCategory(String category) {
        if ("Education & Skills".equals(category)) return "Education";
        if ("Health & Wellbeing".equals(category)) return "Health & Fitness";

        if ("Education".equals(category) || "Health & Fitness".equals(category)
                || "Music".equals(category) || "Sports".equals(category)
                || "Arts & Culture".equals(category)
                || "Food & Markets".equals(category)) {
            return category;
        }
        return "Other";
    }

    // Get user location for Near Me
    private void getUserLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
            return;
        }

        String provider;

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            Toast.makeText(this, "Please turn on location services",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Use recent location if available
        Location lastLocation = locationManager.getLastKnownLocation(provider);

        if (lastLocation != null &&
                System.currentTimeMillis() - lastLocation.getTime() < 300000) {
            showNearbyEvents(lastLocation);
            return;
        }

        Toast.makeText(this, "Finding your location...",
                Toast.LENGTH_SHORT).show();

        // Wait for location update
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(this);
                locationListener = null;
                showNearbyEvents(location);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
    }

    // Display nearby events
    private void showNearbyEvents(Location location) {
        userLocation = location;
        nearMeOnly = true;
        nearMeButton.setText("Show All Locations");
        searchEvents();
    }

    // Handle location permission
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission is needed for Near Me",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    // Stop location updates
    @Override
    protected void onStop() {
        super.onStop();

        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }
}