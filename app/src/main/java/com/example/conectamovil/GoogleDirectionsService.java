package com.example.conectamovil;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleDirectionsService {
    @GET("json")
    Call<DirectionsResponse> getDirections(
            @Query("mode") String mode,
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String apiKey
    );
}
