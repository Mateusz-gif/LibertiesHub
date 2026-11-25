package com.example.libertieshub;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminSurveyActivity extends AppCompatActivity {

    ListView surveyEventsListView;
    TextView selectedEventTextView;
    EditText questionEditText;
    Button saveSurveyButton;

    ArrayList<String> eventTitles;
    ArrayList<String> eventKeys;
    ArrayAdapter<String> adapter;

    DatabaseReference eventsRef, surveysRef;

    String selectedEventKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_survey);

        surveyEventsListView = findViewById(R.id.surveyEventsListView);
        selectedEventTextView = findViewById(R.id.selectedEventTextView);
        questionEditText = findViewById(R.id.questionEditText);
        saveSurveyButton = findViewById(R.id.saveSurveyButton);

        saveSurveyButton.setEnabled(false);

        eventTitles = new ArrayList<>();
        eventKeys = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                eventTitles);

        surveyEventsListView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        eventsRef = database.getReference("community_events");
        surveysRef = database.getReference("surveys");

        // Load Events and Services
        eventsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                eventTitles.clear();
                eventKeys.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                    String title = eventSnapshot.child("title")
                            .getValue(String.class);

                    if (title != null) {
                        eventTitles.add(title);
                        eventKeys.add(eventSnapshot.getKey());
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(AdminSurveyActivity.this,
                        databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Select an event
        surveyEventsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view,
                                            int position,
                                            long id) {

                        selectedEventKey = eventKeys.get(position);

                        selectedEventTextView.setText(
                                eventTitles.get(position));

                        questionEditText.setText("");
                        saveSurveyButton.setEnabled(true);

                        // Load existing question, if there is one
                        surveysRef.child(selectedEventKey)
                                .child("question")
                                .addListenerForSingleValueEvent(
                                        new ValueEventListener() {

                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {

                                                String question = snapshot
                                                        .getValue(String.class);

                                                if (question != null) {
                                                    questionEditText.setText(question);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error) {

                                                Toast.makeText(
                                                        AdminSurveyActivity.this,
                                                        error.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                    }
                });

        // Save survey question
        saveSurveyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (selectedEventKey == null) {
                    return;
                }

                String question = questionEditText.getText()
                        .toString().trim();

                if (question.isEmpty()) {

                    Toast.makeText(AdminSurveyActivity.this,
                            "Please enter a question",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                surveysRef.child(selectedEventKey)
                        .child("question")
                        .setValue(question)
                        .addOnCompleteListener(
                                AdminSurveyActivity.this,
                                new OnCompleteListener<Void>() {

                                    @Override
                                    public void onComplete(Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            Toast.makeText(
                                                    AdminSurveyActivity.this,
                                                    "Survey saved",
                                                    Toast.LENGTH_SHORT).show();

                                        } else {

                                            Toast.makeText(
                                                    AdminSurveyActivity.this,
                                                    "Failed to save survey",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
            }
        });
    }
}