package com.example.libertieshub;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

public class DetailsActivity extends AppCompatActivity {
    TextView titleTextView, detailsTextView;
    DatabaseReference eventRef;
    Button directionsButton;
    Double latitude, longitude;
    Button subscribeButton;
    DatabaseReference subscriptionRef;
    boolean subscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100);
            }
        }

        titleTextView = findViewById(R.id.titleTextView);
        detailsTextView = findViewById(R.id.detailsTextView);
        directionsButton = findViewById(R.id.directionsButton);
        subscribeButton = findViewById(R.id.subscribeButton);
        subscribeButton.setEnabled(false);
        directionsButton.setEnabled(false);

        directionsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = "https://www.google.com/maps/dir/?api=1&destination="
                        + latitude + "%2C" + longitude;

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));

                startActivity(intent);
            }
        });

        String eventKey = getIntent().getStringExtra("eventKey");
        if (eventKey == null) {
            finish();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this,
                    "Please log in first",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        subscriptionRef = database.getReference("subscriptions")
                .child(user.getUid())
                .child(eventKey);

        subscriptionRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                subscribed = dataSnapshot.exists();
                if (subscribed) {
                    subscribeButton.setText("Unsubscribe");
                } else {
                    subscribeButton.setText("Subscribe");
                }
                subscribeButton.setEnabled(true);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DetailsActivity.this,
                        databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
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
        eventRef = database.getReference("community_events")
                .child(eventKey);

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot eventSnapshot) {

                latitude = eventSnapshot
                        .child("location")
                        .child("coordinates")
                        .child("lat")
                        .getValue(Double.class);

                longitude = eventSnapshot
                        .child("location")
                        .child("coordinates")
                        .child("lng")
                        .getValue(Double.class);

                if (latitude != null && longitude != null) {
                    directionsButton.setEnabled(true);
                }

                if (!eventSnapshot.exists()) {
                    Toast.makeText(DetailsActivity.this,
                            "Event not found",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String title = eventSnapshot.child("title")
                        .getValue(String.class);

                String description = eventSnapshot.child("description")
                        .getValue(String.class);

                String category = eventSnapshot.child("category")
                        .getValue(String.class);

                String start = eventSnapshot.child("datetime")
                        .child("start").getValue(String.class);

                String end = eventSnapshot.child("datetime")
                        .child("end").getValue(String.class);

                String recurrence = eventSnapshot.child("datetime")
                        .child("recurrence").getValue(String.class);

                String venue = eventSnapshot.child("location")
                        .child("venue_name").getValue(String.class);

                String address = eventSnapshot.child("location")
                        .child("address").getValue(String.class);

                String eircode = eventSnapshot.child("location")
                        .child("eircode").getValue(String.class);

                String organisation = eventSnapshot
                        .child("partner_organization")
                        .child("name").getValue(String.class);

                Long capacity = eventSnapshot.child("capacity")
                        .getValue(Long.class);

                Boolean isFree = eventSnapshot.child("is_free")
                        .getValue(Boolean.class);

                String accessibility = "";

                for (DataSnapshot item :
                        eventSnapshot.child("accessibility").getChildren()) {

                    String feature = item.getValue(String.class);

                    if (feature != null) {

                        if (!accessibility.isEmpty()) {
                            accessibility += ", ";
                        }

                        accessibility += feature;
                    }
                }

                if (accessibility.isEmpty()) {
                    accessibility = "Not specified";
                }
                if (start != null) {
                    start = start.replace("T", " ").replace("Z", " UTC");
                }
                if (end != null) {
                    end = end.replace("T", " ").replace("Z", " UTC");
                }

                String listingType;

                if ("none".equals(recurrence)) {
                    listingType = "Event";
                } else {
                    listingType = "Service";
                }
                String price;
                if (Boolean.TRUE.equals(isFree)) {
                    price = "Free";
                } else {
                    price = "Not marked as free";
                }

                titleTextView.setText(title);
                String details =
                        "Type: " + listingType + "\n\n" +
                                "Category: " + category + "\n\n" +
                                "Description:\n" + description + "\n\n" +
                                "Start: " + start + "\n" +
                                "End: " + end + "\n" +
                                "Recurrence: " + recurrence + "\n\n" +
                                "Venue: " + venue + "\n" +
                                "Address: " + address + "\n" +
                                "Eircode: " + eircode + "\n\n" +
                                "Organisation: " + organisation + "\n\n" +
                                "Capacity: " + capacity + "\n" +
                                "Price: " + price + "\n\n" +
                                "Accessibility: " + accessibility;
                detailsTextView.setText(details);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DetailsActivity.this,
                        databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    private void showSubscriptionNotification() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                "subscriptions",
                "Event Subscriptions",
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(
                this, "subscriptions")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Successfully Subscribed!")
                .setContentText("You subscribed to: "
                        + titleTextView.getText().toString())
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }
}