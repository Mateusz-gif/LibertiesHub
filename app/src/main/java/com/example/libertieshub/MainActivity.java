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
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

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
    Button nearMeButton;
    ArrayList<Double> allLatitudes;
    ArrayList<Double> allLongitudes;
    Location userLocation;
    LocationManager locationManager;
    LocationListener locationListener;

    boolean nearMeOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventsListView = findViewById(R.id.eventsListView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        categorySpinner = findViewById(R.id.categorySpinner);
        nearMeButton = findViewById(R.id.nearMeButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        allTitles = new ArrayList<>();
        allKeys = new ArrayList<>();
        allCategories = new ArrayList<>();
        allLatitudes = new ArrayList<>();
        allLongitudes = new ArrayList<>();

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
                allLatitudes.clear();
                allLongitudes.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String title = eventSnapshot.child("title")
                            .getValue(String.class);

                    String category = eventSnapshot.child("category")
                            .getValue(String.class);

                    Double latitude = eventSnapshot
                            .child("location")
                            .child("coordinates")
                            .child("lat")
                            .getValue(Double.class);

                    Double longitude = eventSnapshot
                            .child("location")
                            .child("coordinates")
                            .child("lng")
                            .getValue(Double.class);

                    if (title != null) {
                        allTitles.add(title);
                        allKeys.add(eventSnapshot.getKey());
                        allCategories.add(category);

                        allLatitudes.add(latitude);
                        allLongitudes.add(longitude);
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

        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(
                        MainActivity.this, MapsActivity.class);
                startActivity(intent);
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

            boolean matchesDistance = true;

            if (nearMeOnly) {

                matchesDistance = false;

                Double latitude = allLatitudes.get(i);
                Double longitude = allLongitudes.get(i);

                if (userLocation != null
                        && latitude != null
                        && longitude != null) {

                    Location eventLocation = new Location("event");

                    eventLocation.setLatitude(latitude);
                    eventLocation.setLongitude(longitude);

                    float distance = userLocation.distanceTo(eventLocation);

                    if (distance <= 5000) {
                        matchesDistance = true;
                    }
                }
            }

            if (matchesSearch && matchesCategory && matchesDistance) {
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
    private void getUserLocation() {

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
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
        } else if (
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                        &&
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;

        } else {

            Toast.makeText(this,
                    "Please turn on location services",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Location lastLocation = locationManager.getLastKnownLocation(provider);
        if (lastLocation != null
                && System.currentTimeMillis() - lastLocation.getTime() < 300000) {
            showNearbyEvents(lastLocation);
            return;
        }

        Toast.makeText(this,
                "Finding your location...",
                Toast.LENGTH_SHORT).show();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                locationManager.removeUpdates(this);
                locationListener = null;
                showNearbyEvents(location);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(
                provider, 1000, 0, locationListener);
    }
    private void showNearbyEvents(Location location) {
        userLocation = location;
        nearMeOnly = true;
        nearMeButton.setText("Show All Locations");
        searchEvents();
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                getUserLocation();

            } else {

                Toast.makeText(this,
                        "Location permission is needed for Near Me",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onStop() {

        super.onStop();

        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }
   }