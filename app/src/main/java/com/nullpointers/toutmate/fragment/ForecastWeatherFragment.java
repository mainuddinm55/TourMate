package com.nullpointers.toutmate.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nullpointers.toutmate.Model.ForecastWeather;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.WeatherActivity;
import com.nullpointers.toutmate.WeatherForecastApi;
import com.nullpointers.toutmate.adapter.ForecastAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastWeatherFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static  double latitude, longitude;
    private int cnt = 7;
    private String units;

    TextView noDataTV;

    private RecyclerView forecastRecyclerView;
    private RecyclerView.Adapter forecastAdapter;

    List<ForecastWeather.List> weatherList = new ArrayList<>();

    private Retrofit retrofit;
    private WeatherForecastApi weatherForecastApi;
    private String urlString;

    private static final String FORECAST_BASE_URL = "https://api.openweathermap.org/data/2.5/forecast/";


    public ForecastWeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        forecastRecyclerView = view.findViewById(R.id.weatherForecastRecyclerView);
        forecastRecyclerView.setHasFixedSize(true);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        noDataTV = view.findViewById(R.id.noDataTextView);


    }


    @Override
    public void onResume() {
        super.onResume();
        units = WeatherActivity.units;
        if (WeatherActivity.isLocationOn(getContext())) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            if (WeatherActivity.latitude!=0.0 && WeatherActivity.longitude!=0.0){
                latitude = WeatherActivity.latitude;
                longitude = WeatherActivity.longitude;
                getForecastData();
            }else {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10);
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location!=null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            getForecastData();
                        }

                    }
                });
            }

        }
        else {
            Toast.makeText(getContext(), "Location Not On. Please On Your Device Location then Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private void getForecastData() {
        retrofit = new Retrofit.Builder()
                .baseUrl(FORECAST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherForecastApi = retrofit.create(WeatherForecastApi.class);

        urlString = String.format("daily?lat=%f&lon=%f&units=%s&cnt=%d&appid=%s",
                latitude,
                longitude,
                units,
                cnt,
                getString(R.string.weather_api_key));

        Call<ForecastWeather> responseCall = weatherForecastApi.getWeatherData(urlString);

        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMax(100);
        progressDialog.setMessage("Its loading....");
        progressDialog.setTitle("Fetching Data");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // show it
        progressDialog.show();
        responseCall.enqueue(new Callback<ForecastWeather>() {
            @Override
            public void onResponse(Call<ForecastWeather> call, Response<ForecastWeather> response) {
                if (response.code()==200) {
                    progressDialog.dismiss();
                    ForecastWeather forecastWeather = response.body();

                    weatherList = forecastWeather.getList();
                    forecastAdapter = new ForecastAdapter(weatherList, getContext());

                    forecastRecyclerView.setAdapter(forecastAdapter);
                }
            }

            @Override
            public void onFailure(Call<ForecastWeather> call, Throwable t) {
                noDataTV.setVisibility(View.VISIBLE);
            }
        });
    }

}
