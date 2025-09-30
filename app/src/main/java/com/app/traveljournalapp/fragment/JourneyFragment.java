package com.app.traveljournalapp.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
import com.app.traveljournalapp.utils.SharedPreferencesHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JourneyFragment extends Fragment {

    private RecyclerView recyclerView;
    private JourneyAdapter journeyAdapter;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String address;
    private SwitchMaterial locationSwitch;
    private boolean isWaitingForLocation = false;

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        sharedPreferencesHelper = new SharedPreferencesHelper(getContext());

        locationSwitch = view.findViewById(R.id.locationSwitch);
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // When toggled ON → fetch location and open AddJourneyActivity
                isWaitingForLocation = true;
                fetchLocation();
            } else {
                // When toggled OFF → disable location
                address = null;
                Toast.makeText(getContext(), "Location turned off", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the button to open AddJourneyActivity
        view.findViewById(R.id.addJourneyButton).setOnClickListener(v -> {
            // Open Add Journey Activity when button is clicked
            Intent intent = new Intent(getContext(), AddJourneyActivity.class);
            if (address != null && !address.isEmpty()) {
                intent.putExtra("address", address);
            }
            startActivity(intent);
        });

        // Fetch journeys from the API
        fetchJourneys();

        return view;
    }

    private void fetchLocation() {
        // Check if permission is already granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        // Try to get last known location first
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        handleLocation(location);
                    } else {
                        // No cached location, request fresh location
                        Toast.makeText(getContext(), "Fetching current location...", Toast.LENGTH_SHORT).show();
                        requestNewLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    locationSwitch.setChecked(false);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted → retry fetching location
                fetchLocation();
            } else {
                // Permission denied → inform user and turn off switch
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                locationSwitch.setChecked(false);
            }
        }
    }

    private void requestNewLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000)
                .setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    handleLocation(location);
                    // Stop updates once we got a location
                    fusedLocationProviderClient.removeLocationUpdates(this);
                } else {
                    Toast.makeText(getContext(), "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
                    locationSwitch.setChecked(false);
                }
            }
        }, Looper.getMainLooper());
    }

    private void handleLocation(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            address = getAddressFromLatLng(lat, lng);

            Toast.makeText(getContext(), "Location: " + address, Toast.LENGTH_SHORT).show();

            // If user toggled switch, open AddJourneyActivity automatically
            if (isWaitingForLocation) {
                isWaitingForLocation = false;
                Intent intent = new Intent(getContext(), AddJourneyActivity.class);
                intent.putExtra("address", address);
                startActivity(intent);
            }
        }
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0);  // Full address
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "Unknown Location";
    }

    private void fetchJourneys() {
        RequestBody requestBody = new FormBody.Builder()
                .add("action", "get_journeys")
                .add("user_id", String.valueOf(sharedPreferencesHelper.getUserId()))
                .build();

        RetrofitClient.getClient().create(ApiService.class)
                .getJourneys(requestBody)
                .enqueue(new Callback<JourneyResponse>() {
                    @Override
                    public void onResponse(Call<JourneyResponse> call, Response<JourneyResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JourneyResponse apiResponse = response.body();
                            List<Journey> journeys = apiResponse.getJourneys();

                            if (journeys != null && !journeys.isEmpty()) {
                                journeyAdapter.setJourneys(journeys);
                            } else {
                                Toast.makeText(getContext(), "No journeys found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to fetch journeys", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JourneyResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}