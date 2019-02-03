package com.nullpointers.toutmate.Nearby;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface NearbyPlaceService {
    @GET
    Call<NearbyPlacesResponse> getNearbyPlaces(@Url String url);
}
