package com.app.traveljournalapp.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
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
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SwitchMaterial locationSwitch;
    private String address;
    private boolean isWaitingForLocation = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 101;

    public JourneyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewJourneys);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        journeyAdapter = new JourneyAdapter();
        recyclerView.setAdapter(journeyAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        sharedPreferencesHelper = new SharedPreferencesHelper(getContext());

        locationSwitch = view.findViewById(R.id.locationSwitch);

        // Update switch state based on current GPS status
        updateLocationSwitchState();

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                isWaitingForLocation = true;
                checkLocationPermissionAndFetch();
            } else {
                address = null;
                Toast.makeText(getContext(), "Location turned off", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.addJourneyButton).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddJourneyActivity.class);
            if (address != null && !address.isEmpty()) {
                intent.putExtra("address", address);
            }
            startActivity(intent);
        });

        fetchJourneys();

        return view;
    }

    // ------------------- LOCATION LOGIC -------------------

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // Update switch checked state according to GPS status
    private void updateLocationSwitchState() {
        locationSwitch.setChecked(isLocationEnabled());
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationSettings();
        }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> fetchLocation())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(getContext(), "Unable to turn on location", Toast.LENGTH_SHORT).show();
                            locationSwitch.setChecked(false);
                        }
                    } else {
                        Toast.makeText(getContext(), "Location services are off", Toast.LENGTH_SHORT).show();
                        locationSwitch.setChecked(false);
                    }
                });
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) handleLocation(location);
                    else requestNewLocation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    locationSwitch.setChecked(false);
                });
    }

    private void requestNewLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500)
                .setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    handleLocation(locationResult.getLastLocation());
                    fusedLocationProviderClient.removeLocationUpdates(this);
                } else {
                    Toast.makeText(getContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
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
        }
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "Unknown Location";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                locationSwitch.setChecked(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLocationSwitchState();

        // Auto-fetch if location is enabled and we have permission
        if (isLocationEnabled() &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            updateLocationSwitchState(); // refresh switch state
            if (resultCode == android.app.Activity.RESULT_OK) fetchLocation();
            else Toast.makeText(getContext(), "Location is required to proceed", Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------- FETCH JOURNEYS -------------------

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
                            List<Journey> journeys = response.body().getJourneys();
                            if (journeys != null && !journeys.isEmpty()) journeyAdapter.setJourneys(journeys);
                            else Toast.makeText(getContext(), "No journeys found", Toast.LENGTH_SHORT).show();
                        } else Toast.makeText(getContext(), "Failed to fetch journeys", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<JourneyResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
