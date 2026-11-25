package com.example.libertieshub;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsActivity extends AppCompatActivity {

    TextView titleTextView, detailsTextView;
    Button directionsButton, subscribeButton, feedbackButton;

    DatabaseReference subscriptionRef;
    Double latitude, longitude;
    boolean subscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Connect XML components
        titleTextView = findViewById(R.id.titleTextView);
        detailsTextView = findViewById(R.id.detailsTextView);
        directionsButton = findViewById(R.id.directionsButton);
        subscribeButton = findViewById(R.id.subscribeButton);
        feedbackButton = findViewById(R.id.feedbackButton);

        directionsButton.setEnabled(false);
        subscribeButton.setEnabled(false);
        feedbackButton.setVisibility(View.GONE);

        // Request notification permission
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        // Get selected event
        String eventKey = getIntent().getStringExtra("eventKey");
        if (eventKey == null) {
            finish();
            return;
        }

        // Check logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        // Open directions in Google Maps
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "https://www.google.com/maps/dir/?api=1&destination="
                        + latitude + "%2C" + longitude;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        // Open feedback survey
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(
                        DetailsActivity.this, SurveyActivity.class);

                intent.putExtra("eventKey", eventKey);
                startActivity(intent);
            }
        });

        // Check whether a survey exists
        database.getReference("surveys").child(eventKey).child("question")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        String question = snapshot.getValue(String.class);

                        if (question != null && !question.isEmpty()) {
                            feedbackButton.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(DetailsActivity.this,
                                error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        // Check subscription status
        subscriptionRef = database.getReference("subscriptions")
                .child(user.getUid()).child(eventKey);

        subscriptionRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                subscribed = snapshot.exists();

                if (subscribed) {
                    subscribeButton.setText("Unsubscribe");
                } else {
                    subscribeButton.setText("Subscribe");
                }
                subscribeButton.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DetailsActivity.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Subscribe or unsubscribe
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                subscribeButton.setEnabled(false);
                boolean subscribing = !subscribed;

                Task<Void> task;

                if (subscribing) {
                    task = subscriptionRef.setValue(true);
                } else {
                    task = subscriptionRef.removeValue();
                }

                task.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {

                        if (task.isSuccessful()) {
                            if (subscribing) {
                                showSubscriptionNotification();
                            }
                        } else {
                            subscribeButton.setEnabled(true);

                            Toast.makeText(DetailsActivity.this,
                                    "Could not update subscription",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        // Load event information from Firebase
        DatabaseReference eventRef = database.getReference("community_events")
                .child(eventKey);

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetailsActivity.this,
                            "Event not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Basic event information
                String title = snapshot.child("title").getValue(String.class);
                String description = snapshot.child("description")
                        .getValue(String.class);
                String category = snapshot.child("category")
                        .getValue(String.class);

                // Date and recurrence
                String start = snapshot.child("datetime").child("start")
                        .getValue(String.class);
                String end = snapshot.child("datetime").child("end")
                        .getValue(String.class);
                String recurrence = snapshot.child("datetime")
                        .child("recurrence").getValue(String.class);

                // Location
                DataSnapshot location = snapshot.child("location");

                String venue = location.child("venue_name")
                        .getValue(String.class);
                String address = location.child("address")
                        .getValue(String.class);

                latitude = location.child("coordinates").child("lat")
                        .getValue(Double.class);
                longitude = location.child("coordinates").child("lng")
                        .getValue(Double.class);

                if (latitude != null && longitude != null) {
                    directionsButton.setEnabled(true);
                }
                // Format dates for display
                if (start != null) {
                    start = start.replace("T", " ").replace("Z", " UTC");
                }
                if (end != null) {
                    end = end.replace("T", " ").replace("Z", " UTC");
                }

                // Identify Event or Service
                String listingType = "Service";
                if ("none".equals(recurrence)) {
                    listingType = "Event";
                }

                // Display main information
                titleTextView.setText(title);

                String details =
                        "Type: " + listingType + "\n\n" +
                                "Category: " + category + "\n\n" +
                                "Description:\n" + description + "\n\n" +
                                "Start: " + start + "\n" +
                                "End: " + end + "\n" +
                                "Recurrence: " + recurrence + "\n\n" +
                                "Venue: " + venue + "\n" +
                                "Address: " + address;

                // information about event
                String organisation = snapshot.child("partner_organization")
                        .child("name").getValue(String.class);

                Boolean isFree = snapshot.child("is_free")
                        .getValue(Boolean.class);

                if (organisation != null && !organisation.isEmpty()) {
                    details += "\n\nOrganisation: " + organisation;
                }
                if (isFree != null) {
                    if (isFree) {
                        details += "\nPrice: Free";
                    } else {
                        details += "\nPrice: Not marked as free";
                    }
                }
                detailsTextView.setText(details);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DetailsActivity.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    // Show notification after subscribing
    private void showSubscriptionNotification() {

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                "subscriptions",
                "Event Subscriptions",
                NotificationManager.IMPORTANCE_DEFAULT);

        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "subscriptions")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Successfully Subscribed!")
                .setContentText("You subscribed to: " + titleTextView.getText())
                .setAutoCancel(true)
                .build();
        manager.notify(1, notification);
    }
}