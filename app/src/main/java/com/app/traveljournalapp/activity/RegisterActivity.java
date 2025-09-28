package com.app.traveljournalapp.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.app.traveljournalapp.R;
import com.app.traveljournalapp.data.db.model.ApiResponse;
import com.app.traveljournalapp.data.db.model.RegisterRequest;
import com.app.traveljournalapp.network.ApiService;
import com.app.traveljournalapp.network.RetrofitClient;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        nameEditText = findViewById(R.id.etName);
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        registerButton = findViewById(R.id.btnRegister);

        // Register button click listener
        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            registerUser(name, email, password);
        });
    }

    // Register user function using Retrofit
    // Register user function using Retrofit
    private void registerUser(String name, String email, String password) {
        // Build the form body to match the server's expected format
        RequestBody formBody = new FormBody.Builder()
                .add("action", "register")   // action parameter to specify registration
                .add("name", name)           // user's name
                .add("email", email)         // user's email
                .add("password", password)   // user's password
                .build();

        // Call the register API using Retrofit
        RetrofitClient.getClient().create(ApiService.class)
                .register(formBody).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            // Log the raw response to debug the result
                            Log.d("RegisterActivity", "Response: " + response.body());

                            // Check if the API response has the correct status
                            if (response.body() != null && response.body().getStatus() != null) {
                                if (response.body().getStatus().equals("success")) {
                                    // Registration was successful
                                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class)); // Navigate to Login
                                    finish();
                                } else {
                                    // API returned a failure status
                                    Log.d("RegisterActivity", "Failure message: " + response.body().getMessage());
                                    Toast.makeText(RegisterActivity.this, "Registration failed: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // If the response body is null or does not contain a valid status
                                Log.d("RegisterActivity", "Invalid response from server: " + response.message());
                                Toast.makeText(RegisterActivity.this, "Registration failed: Invalid response from server.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If the response code is not successful (not 200)
                            Log.e("RegisterActivity", "API Error: " + response.message());
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        // Log the error if the network call fails
                        Log.e("RegisterActivity", "Network Error: " + t.getMessage());
                        Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
