package com.app.traveljournalapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.traveljournalapp.R;
import com.app.traveljournalapp.data.db.entity.Journey;
import com.app.traveljournalapp.data.db.model.JourneyResponse;
import com.app.traveljournalapp.network.ApiService;
import com.app.traveljournalapp.network.RetrofitClient;
import com.app.traveljournalapp.utils.SharedPreferencesHelper;

import java.io.ByteArrayOutputStream;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JourneyDetailActivity extends AppCompatActivity {

    private TextView titleTextView, dateTextView, addressTextView, descriptionTextView, addPhotosButton;
    private ImageButton backButton;
    private int position;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private ImageView capturedImageView;
    private Bitmap photoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_detail);

        // Initialize views
        titleTextView = findViewById(R.id.journeyTitleDetails);
        dateTextView = findViewById(R.id.journeyDate);
        addressTextView = findViewById(R.id.journeyAddress);
        descriptionTextView = findViewById(R.id.journeyDescription);
        addPhotosButton = findViewById(R.id.addPhotosButton);
        capturedImageView = findViewById(R.id.capturedImageView);
        backButton = findViewById(R.id.backButton);
        
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(JourneyDetailActivity.this, JourneyManagementActivity.class);
            startActivity(intent);
            finish();
        });

        // Initialize SharedPreferencesHelper
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        // Check if the app has permission to access the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        // Get the journey ID passed from the previous activity
        int  journeyId = getIntent().getIntExtra("journeyId", -1);
        position = getIntent().getIntExtra("position", -1);

        if (journeyId != -1) {
            // Fetch journey details from the API
            fetchJourneyDetails(journeyId);
        }

        // On Click: Add Photos
        addPhotosButton.setOnClickListener(v -> openCamera());
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can access the camera now
                openCamera();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Camera permission is required to capture photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Method to open the camera for capturing a photo
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the result of the camera intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the photo as a Bitmap
            Bundle extras = data.getExtras();
            if (extras != null) {
                photoBitmap = (Bitmap) extras.get("data");
                capturedImageView.setImageBitmap(photoBitmap);  // Show the captured photo

                // Convert the Bitmap to Base64
                String base64Photo = convertBitmapToBase64(photoBitmap);

                // Upload the image to the server
                uploadMemoryPhoto(base64Photo);
            }
        }
    }

    // Convert the Bitmap photo to a Base64 string
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Upload the photo to the server
    private void uploadMemoryPhoto(String base64Photo) {
        int journeyId = getIntent().getIntExtra("journeyId", -1);
        String userId = String.valueOf(sharedPreferencesHelper.getUserId());  // Replace with actual user ID

        // Prepare API request body
        RequestBody requestBody = new FormBody.Builder()
                .add("action", "save_memory")
                .add("user_id", userId)
                .add("journey_id", String.valueOf(journeyId))
                .add("photo_base64", base64Photo)
                .build();

        // Call API to save the memory photo
        RetrofitClient.getClient().create(ApiService.class)
                .saveMemory(requestBody)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(JourneyDetailActivity.this, "Photo added successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(JourneyDetailActivity.this, "Failed to add photo", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(JourneyDetailActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // Method to fetch journey details using API
    private void fetchJourneyDetails(long journeyId) {
        // Create the API request body
        RequestBody requestBody = new FormBody.Builder()
                .add("action", "get_journeys")
                .add("user_id", String.valueOf(sharedPreferencesHelper.getUserId())) // Replace with actual user ID dynamically
                .add("journey_id", String.valueOf(journeyId))
                .build();

        // Call the API to fetch journey details
        RetrofitClient.getClient().create(ApiService.class)
                .getJourneys(requestBody)
                .enqueue(new Callback<JourneyResponse>() {
                    @Override
                    public void onResponse(Call<JourneyResponse> call, Response<JourneyResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JourneyResponse apiResponse = response.body();

                            // Get the first journey from the response (since it's a single journey, not a list)
                            if (apiResponse != null && apiResponse.getJourneys() != null && !apiResponse.getJourneys().isEmpty()) {
                                // Assuming the first journey is the one we are looking for (adjust logic if needed)
                                Journey journey = apiResponse.getJourneys().get(position);

                                // Set the journey details to the views
                                titleTextView.setText(journey.getTitle());
                                dateTextView.setText(journey.getDate());
                                addressTextView.setText(journey.getAddress());
                                descriptionTextView.setText(journey.getDescription());
                            }
                        } else {
                            Toast.makeText(JourneyDetailActivity.this, "Failed to fetch journey details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JourneyResponse> call, Throwable t) {
                        Toast.makeText(JourneyDetailActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
