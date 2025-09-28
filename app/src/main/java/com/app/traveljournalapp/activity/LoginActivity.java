package com.app.traveljournalapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.app.traveljournalapp.MainActivity;
import com.app.traveljournalapp.R;
import com.app.traveljournalapp.data.db.AppDatabase;
import com.app.traveljournalapp.data.db.entity.User;
import com.app.traveljournalapp.data.db.model.ApiResponse;
import com.app.traveljournalapp.data.db.model.LoginRequest;
import com.app.traveljournalapp.network.ApiService;
import com.app.traveljournalapp.network.RetrofitClient;
import com.app.traveljournalapp.utils.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, register;
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
        register = findViewById(R.id.btnRegisterLink);

        // Check if already logged in
        if (sharedPreferencesHelper.isLoggedIn()) {
            navigateToJourneyActivity();
        }

        // Login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            loginUser(email, password);
        });
        register.setOnClickListener(v -> {
            navigateToRegisterActivity();
        });
    }

    private void loginUser(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.email = email;
        request.password = password;

        // Call the login API
        RetrofitClient.getClient().create(ApiService.class).login(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    // Save user to Room Database and SharedPreferences on successful login
                    saveUserLocally(email);
                    sharedPreferencesHelper.setLoggedIn(true);
                    navigateToJourneyActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserLocally(String email) {
        User user = new User();
        user.setEmail(email);
        // Save to Room Database
        new Thread(() -> appDatabase.userDao().insert(user)).start();
    }

    private void navigateToJourneyActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void navigateToRegisterActivity() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }
}
