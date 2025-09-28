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
    private void registerUser(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.name = name;
        request.email = email;
        request.password = password;

        // Call the register API
        RetrofitClient.getClient().create(ApiService.class).register(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    // Log response body to check what the API is returning
                    Log.d("RegisterActivity", "Response: " + response.body());

                    // Check if the response status is 'success'
                    if (response.body() != null  && response.body().success) {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class)); // Navigate to Login
                        finish();
                    } else {
                        // Handle case when the API returns failure status
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + response.body().message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Log the error message if the response was not successful
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
