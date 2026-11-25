package com.example.libertieshub;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.HashMap;

public class SurveyActivity extends AppCompatActivity {

    TextView questionTextView;
    Spinner ratingSpinner;
    EditText commentEditText;
    Button submitFeedbackButton;

    DatabaseReference surveysRef, responsesRef;

    String eventKey;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        questionTextView = findViewById(R.id.questionTextView);
        ratingSpinner = findViewById(R.id.ratingSpinner);
        commentEditText = findViewById(R.id.commentEditText);
        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);

        // Rating options
        String[] ratings = {
                "Select a rating",
                "1 - Very Poor",
                "2 - Poor",
                "3 - Average",
                "4 - Good",
                "5 - Excellent"
        };

        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                ratings);

        ratingAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        ratingSpinner.setAdapter(ratingAdapter);

        eventKey = getIntent().getStringExtra("eventKey");
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (eventKey == null || user == null) {
            finish();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://libertieshub-default-rtdb.europe-west1.firebasedatabase.app/");

        surveysRef = database.getReference("surveys");
        responsesRef = database.getReference("survey_responses");

        // Load the administrator's question
        surveysRef.child(eventKey)
                .child("question")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String question = dataSnapshot.getValue(String.class);

                        if (question != null) {

                            questionTextView.setText(question);
                            submitFeedbackButton.setEnabled(true);

                        } else {

                            Toast.makeText(SurveyActivity.this,
                                    "No survey available",
                                    Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(SurveyActivity.this,
                                databaseError.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

        // Submit feedback
        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int selectedRating = ratingSpinner.getSelectedItemPosition();

                if (selectedRating == 0) {

                    Toast.makeText(SurveyActivity.this,
                            "Please select a rating",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String comment = commentEditText.getText()
                        .toString().trim();

                HashMap<String, Object> feedback = new HashMap<>();

                feedback.put("rating", (long) selectedRating);
                feedback.put("comment", comment);

                submitFeedbackButton.setEnabled(false);

                responsesRef.child(eventKey)
                        .child(user.getUid())
                        .setValue(feedback)
                        .addOnCompleteListener(
                                SurveyActivity.this,
                                new OnCompleteListener<Void>() {

                                    @Override
                                    public void onComplete(Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            Toast.makeText(
                                                    SurveyActivity.this,
                                                    "Feedback submitted",
                                                    Toast.LENGTH_SHORT).show();

                                            finish();

                                        } else {

                                            submitFeedbackButton.setEnabled(true);

                                            Toast.makeText(
                                                    SurveyActivity.this,
                                                    "Failed to submit feedback",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
            }
        });
    }
}