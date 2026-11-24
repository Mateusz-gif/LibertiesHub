package com.example.libertieshub;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Centre the map on Dublin
        LatLng dublin = new LatLng(53.3498, -6.2603);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(dublin, 10));

        // Connect to Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        DatabaseReference eventsRef =
                database.getReference("community_events");

        // Read events from Firebase
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot eventSnapshot :
                        dataSnapshot.getChildren()) {

                    String title = eventSnapshot.child("title")
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

                    if (title != null &&
                            latitude != null &&
                            longitude != null) {

                        LatLng eventLocation =
                                new LatLng(latitude, longitude);

                        mMap.addMarker(new MarkerOptions()
                                .position(eventLocation)
                                .title(title));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(MapsActivity.this,
                        databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}