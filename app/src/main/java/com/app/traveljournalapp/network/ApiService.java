package com.app.traveljournalapp.network;


import com.app.traveljournalapp.data.db.model.ApiResponse;
import com.app.traveljournalapp.data.db.model.LoginRequest;
import com.app.traveljournalapp.data.db.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("app_api.php")
    Call<ApiResponse> login(@Body LoginRequest request);

    @POST("app_api.php")
    Call<ApiResponse> register(@Body RegisterRequest request);
}
