package com.app.traveljournalapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.app.traveljournalapp.MainActivity;
import com.app.traveljournalapp.R;
import com.app.traveljournalapp.data.db.AppDatabase;
import com.app.traveljournalapp.data.db.entity.User;
import com.app.traveljournalapp.data.db.model.ApiResponse;
import com.app.traveljournalapp.network.ApiService;
import com.app.traveljournalapp.network.RetrofitClient;
import com.app.traveljournalapp.utils.SharedPreferencesHelper;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView register;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences and Room Database
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "travel_journal_db").build();

        // Initialize views
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        register = findViewById(R.id.tvCreateAccount);

        // Check if already logged in
        if (sharedPreferencesHelper.isLoggedIn()) {
            navigateToJourneyActivity();
        }

        // Login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Check for empty fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        // Register button click listener
        register.setOnClickListener(v -> {
            navigateToRegisterActivity();
        });
    }

    // Updated login function using FormBody to match the API
    private void loginUser(String email, String password) {
        // Create a FormBody with the required parameters (action, email, password)
        RequestBody formBody = new FormBody.Builder()
                .add("action", "login")   // action parameter to specify login
                .add("email", email)      // user's email
                .add("password", password) // user's password
                .build();

        // Call the login API using Retrofit
        RetrofitClient.getClient().create(ApiService.class)
                .login(formBody).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        // Check if the response is successful and contains a valid body
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse apiResponse = response.body();

                            // Log the response for debugging
                            Log.d("LoginActivity", "Response: " + apiResponse);

                            // Check if the response status is "success"
                            if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                                // Save user to Room Database and SharedPreferences on successful login
                                saveUserLocally(email);
                                sharedPreferencesHelper.setLoggedIn(true);
                                navigateToJourneyActivity();
                                // Show success Toast
//                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            } else {
                                // Show failure Toast with the message from API response
                                String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Login failed: Unknown error";
//                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If the response body is null, show a fallback message
                            String errorMessage = response.message() != null ? response.message() : "Login failed: Unknown error";
                            Log.e("LoginActivity", "API Error: " + response.message());
//                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        // Handle failure (network error or other issues)
                        Log.e("LoginActivity", "Network Error: " + t.getMessage());
                        Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Save logged-in user to Room database
    private void saveUserLocally(String email) {
        User user = new User();
        user.setEmail(email);
        // Save to Room Database in a background thread
        new Thread(() -> appDatabase.userDao().insert(user)).start();
    }

    // Navigate to Journey Activity after successful login
    private void navigateToJourneyActivity() {
        startActivity(new Intent(LoginActivity.this, JourneyManagementActivity.class));
        finish();
    }

    // Navigate to Register Activity for user registration
    private void navigateToRegisterActivity() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }
}
