package com.app.traveljournalapp.network;

import com.app.traveljournalapp.data.db.entity.User;
import com.app.traveljournalapp.data.db.model.ApiResponse;
import com.app.traveljournalapp.data.db.model.JourneyResponse;
import com.app.traveljournalapp.data.db.model.LoginResponse;
import com.app.traveljournalapp.data.db.model.MemoryResponse;


import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("app_api.php")
    Call<LoginResponse> login(@Body RequestBody request);
    @POST("app_api.php")
    Call<ApiResponse> register(@Body RequestBody request);
    @POST("app_api.php")
    Call<JourneyResponse> getJourneys(@Body RequestBody requestBody);
    @POST("app_api.php")
    Call<ApiResponse> saveJourney(@Body RequestBody requestBody);
    @POST("app_api.php")
    Call<Void> saveMemory(@Body RequestBody requestBody);
    @POST("app_api.php")
    Call<MemoryResponse> getMemories(@Body RequestBody requestBody);
}
