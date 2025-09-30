package com.app.traveljournalapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.app.traveljournalapp.R;
import com.app.traveljournalapp.data.db.AppDatabase;
import com.app.traveljournalapp.data.db.entity.Journey;
import com.app.traveljournalapp.data.db.model.ApiResponse;
import com.app.traveljournalapp.data.db.model.SaveJourneyRequest;
import com.app.traveljournalapp.network.ApiService;
import com.app.traveljournalapp.network.RetrofitClient;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddJourneyActivity extends AppCompatActivity {

    private EditText titleEditText, dateEditText, addressEditText, descriptionEditText;
    private Button saveButton,backButton;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journey);

        // Initialize Room Database
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "travel_journal_db").build();

        // Initialize views
        titleEditText = findViewById(R.id.etTitle);
        dateEditText = findViewById(R.id.etDate);
        addressEditText = findViewById(R.id.etAddress);
        descriptionEditText = findViewById(R.id.etDescription);
        saveButton = findViewById(R.id.btnSaveJourney);
//        backButton = findViewById(R.id.backButton);

        // Button click listener to save journey
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String date = dateEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            // Validate inputs before saving
            if (title.isEmpty() || date.isEmpty() || address.isEmpty() || description.isEmpty()) {
                Toast.makeText(AddJourneyActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                // Save journey to API and Room database
                saveJourneyToApiAndDatabase(title, date, address, description);
            }
        });

//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AddJourneyActivity.this, JourneyManagementActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private void saveJourneyToApiAndDatabase(String title, String date, String address, String description) {
        // Prepare API request body
        String user_id = "75";
        SaveJourneyRequest request = new SaveJourneyRequest(user_id, title, date, address, description);

        // Create FormBody for Retrofit request
        RequestBody requestBody = new FormBody.Builder()
                .add("action", request.getAction())
                .add("user_id", request.getUser_id())
                .add("title", request.getTitle())
                .add("date", request.getDate())
                .add("address", request.getAddress())
                .add("description", request.getDescription())
                .build();

        // Call API to save journey
        RetrofitClient.getClient().create(ApiService.class)
                .saveJourney(requestBody)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse apiResponse = response.body();

                            if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                                // Save to Room database
                                saveJourneyLocally(title, date, address, description);
                                Toast.makeText(AddJourneyActivity.this, "Journey saved successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddJourneyActivity.this, JourneyManagementActivity.class);
                                startActivity(intent);
                                finish();  // Close the activity after saving
                            } else {
                                Toast.makeText(AddJourneyActivity.this, "Error saving journey", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Toast.makeText(AddJourneyActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveJourneyLocally(String title, String date, String address, String description) {
        // Save the journey to Room database
        Journey journey = new Journey();
        journey.setTitle(title);
        journey.setDate(date);
        journey.setAddress(address);
        journey.setDescription(description);

        // Insert into Room database in background thread
        new Thread(() -> appDatabase.journeyDao().insert(journey)).start();
    }
}
