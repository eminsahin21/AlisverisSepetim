package com.example.alisverissepetim.service;

import com.example.alisverissepetim.model.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;

public interface ApiServiceProduct {
    @GET("products")
    Call<List<Product>> getProducts();
}