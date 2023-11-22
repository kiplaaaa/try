package com.example.atry;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextRegistrationNumber;
    private EditText editTextName;
    private EditText editTextEmail;
    private ListView listViewCourses;
    private Button buttonSubmit;

    private ArrayAdapter<String> courseAdapter;
    private List<String> selectedCourses;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextRegistrationNumber = findViewById(R.id.editTextRegistrationNumber);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        listViewCourses = findViewById(R.id.listViewCourses);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // Initialize the list of courses
        List<String> coursesList = new ArrayList<>();
        coursesList.add("Mobile Application Programming");
        coursesList.add("Compiler Construction");
        coursesList.add("Theory Of Computation");
        coursesList.add("Multimedia Systems");
        coursesList.add("Simulation And Modelling");
        coursesList.add("Design Analysis of Algorithms");
        coursesList.add("Research Methodology");
        coursesList.add("Artificial Intelligence");
        coursesList.add("Software Engineering");
        coursesList.add("Internet Application");

        // Initialize the selected courses list
        selectedCourses = new ArrayList<>();

        // Set up the adapter for the ListView
        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, coursesList);
        listViewCourses.setAdapter(courseAdapter);
        listViewCourses.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Set item click listener for the ListView
        listViewCourses.setOnItemClickListener((parent, view, position, id) -> {
            // Handle item click to update the selected courses list
            if (listViewCourses.isItemChecked(position)) {
                // Check if the maximum limit of 5 courses is reached
                if (selectedCourses.size() < 5) {
                    selectedCourses.add(coursesList.get(position));
                } else {
                    // If the limit is reached, uncheck the item and show a toast
                    listViewCourses.setItemChecked(position, false);
                    Toast.makeText(MainActivity.this, "You can select up to 5 courses", Toast.LENGTH_SHORT).show();
                }
            } else {
                selectedCourses.remove(coursesList.get(position));
            }
        });

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("registrations");

        buttonSubmit.setOnClickListener(v -> submitRegistration());
    }


    private void submitRegistration() {
        // Get registration details
        String registrationNumber = editTextRegistrationNumber.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        // Validate registration details
        if (!registrationNumber.isEmpty() && !name.isEmpty() && !email.isEmpty() && !selectedCourses.isEmpty()) {
            // Generate a unique key for each registration
            String registrationKey = databaseReference.push().getKey();

            // Create a Registration object
            User registration = new User(registrationNumber, name, email, selectedCourses);

            // Save the registration data to the database using the generated key
            databaseReference.child(registrationKey).setValue(registration)
                    .addOnCompleteListener(task -> {
                        // Reset the form after successful submission
                        editTextRegistrationNumber.getText().clear();
                        editTextName.getText().clear();
                        editTextEmail.getText().clear();
                        listViewCourses.clearChoices();
                        selectedCourses.clear();

                        // Display a success message
                        Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Display an error message if the submission fails
                        Toast.makeText(MainActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "Registration failed", e);
                    });
        } else {
            // Display an error message if any field is empty or no course is selected
            Toast.makeText(this, "Invalid input. Please check your entries.", Toast.LENGTH_SHORT).show();
        }
    }
}
