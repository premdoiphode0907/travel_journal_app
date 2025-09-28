package com.app.traveljournalapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.traveljournalapp.R;
import com.app.traveljournalapp.activity.AddJourneyActivity;
import com.app.traveljournalapp.adapter.JourneyAdapter;
import com.app.traveljournalapp.data.db.entity.Journey;
import com.app.traveljournalapp.data.db.model.JourneyResponse;
import com.app.traveljournalapp.network.ApiService;
import com.app.traveljournalapp.network.RetrofitClient;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JourneyFragment extends Fragment {

    private RecyclerView recyclerView;
    private JourneyAdapter journeyAdapter;
    private ProgressBar progressBar;  // ProgressBar for loading indication

    public JourneyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey, container, false);

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewJourneys);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        journeyAdapter = new JourneyAdapter();
        recyclerView.setAdapter(journeyAdapter);

        // Initialize ProgressBar for loading indication
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize Toolbar and Button
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Journey Management");

        // Set up the button to open AddJourneyActivity
        view.findViewById(R.id.addJourneyButton).setOnClickListener(v -> {
            // Open Add Journey Activity when button is clicked
            startActivity(new Intent(getContext(), AddJourneyActivity.class));
        });

        // Fetch journeys from the API
        fetchJourneys();

        return view;
    }

    private void fetchJourneys() {
        // Show progress bar while data is loading
        progressBar.setVisibility(View.VISIBLE);

        // Create the request body for user ID (change the user_id as required)
        RequestBody requestBody = new FormBody.Builder()
                .add("action", "get_journeys")  // action to fetch journeys
                .add("id", "75") // Replace with actual user ID dynamically
                .build();

        // Call the API to get the journeys
        RetrofitClient.getClient().create(ApiService.class)
                .getJourneys(requestBody)
                .enqueue(new Callback<JourneyResponse>() {
                    @Override
                    public void onResponse(Call<JourneyResponse> call, Response<JourneyResponse> response) {
                        // Hide progress bar after data is fetched
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            JourneyResponse apiResponse = response.body();

                            // Get the list of journeys from the 'journeys' field
                            List<Journey> journeys = apiResponse.getJourneys();

                            // Check if the journeys list is empty or null
                            if (journeys != null && !journeys.isEmpty()) {
                                // Update the RecyclerView with the journey data
                                journeyAdapter.setJourneys(journeys);
                            } else {
                                Toast.makeText(getContext(), "No journeys found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // API response failed (e.g., bad response or empty body)
                            Toast.makeText(getContext(), "Failed to fetch journeys", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JourneyResponse> call, Throwable t) {
                        // Hide progress bar in case of failure
                        progressBar.setVisibility(View.GONE);
                        // Handle network failure or exception
                        Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
