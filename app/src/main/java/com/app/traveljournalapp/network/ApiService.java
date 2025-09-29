package com.app.traveljournalapp.network;


import com.app.traveljournalapp.data.db.entity.Journey;
import com.app.traveljournalapp.data.db.model.ApiResponse;
import com.app.traveljournalapp.data.db.model.JourneyResponse;
import com.app.traveljournalapp.data.db.model.LoginRequest;
import com.app.traveljournalapp.data.db.model.RegisterRequest;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("app_api.php")
    Call<ApiResponse> login(@Body RequestBody request);

    @POST("app_api.php")
    Call<ApiResponse> register(@Body RequestBody request);

    @POST("app_api.php")
    Call<JourneyResponse> getJourneys(@Body RequestBody requestBody);

    // Save a new journey using FormBody
    @POST("app_api.php")
    Call<ApiResponse> saveJourney(@Body RequestBody requestBody);
}
