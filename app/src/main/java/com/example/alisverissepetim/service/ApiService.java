package com.example.alisverissepetim.service;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/user/create")
    Call<Void> createUser(@Body Map<String, String> body);

}
