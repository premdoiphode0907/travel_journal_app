package com.app.traveljournalapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.traveljournalapp.R;
import com.app.traveljournalapp.activity.FullImageActivity;
import com.app.traveljournalapp.activity.JourneyDetailActivity;
import com.app.traveljournalapp.adapter.MemoriesAdapter;
import com.app.traveljournalapp.data.db.model.MemoryResponse;
import com.app.traveljournalapp.network.ApiService;
import com.app.traveljournalapp.network.RetrofitClient;
import com.app.traveljournalapp.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MemoriesAdapter memoriesAdapter;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memories, container, false);

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewMemories);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));  // 2 items per row in grid

        memoriesAdapter = new MemoriesAdapter(item -> {
            // Handle click → open full screen image
            Intent intent = new Intent(getContext(), FullImageActivity.class);
            intent.putExtra("imageUrl", item.imageUrl);
            startActivity(intent);
        });

        recyclerView.setAdapter(memoriesAdapter);

        // Initialize SharedPreferencesHelper
        sharedPreferencesHelper = new SharedPreferencesHelper(getContext());

        // Fetch memories data
        fetchMemoriesData();

        return view;
    }

    private void fetchMemoriesData() {
        String userId = String.valueOf(sharedPreferencesHelper.getUserId());

        RequestBody requestBody = new FormBody.Builder()
                .add("action", "get_memories")
                .add("user_id", userId)
                .build();

        RetrofitClient.getClient().create(ApiService.class)
                .getMemories(requestBody)
                .enqueue(new Callback<MemoryResponse>() {
                    @Override
                    public void onResponse(Call<MemoryResponse> call, Response<MemoryResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            MemoryResponse apiResponse = response.body();
                            List<MemoryResponse.MemoryItem> memoryItems = apiResponse.getMemories();

                            // Convert MemoryResponse.MemoryItem to MemoriesAdapter.MemoryItem
                            List<MemoriesAdapter.MemoryItem> adapterItems = new ArrayList<>();
                            for (MemoryResponse.MemoryItem memoryItem : memoryItems) {
                                adapterItems.add(new MemoriesAdapter.MemoryItem(memoryItem.getPhotoUrl(), memoryItem.getJourneyTitle()));
                            }

                            // Submit data to the adapter
                            memoriesAdapter.submit(adapterItems);
                        } else {
                            Toast.makeText(getContext(), "Failed to fetch memories", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MemoryResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
