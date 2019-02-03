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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nullpointers.toutmate.Model.CurrentWeatherResponse;
import com.nullpointers.toutmate.Model.DateConverter;
import com.nullpointers.toutmate.R;
import com.nullpointers.toutmate.WeatherAPI;
import com.nullpointers.toutmate.WeatherActivity;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentWeatherFragment extends Fragment {
    private String units;// = WeatherActivity.units;
    private WeatherAPI weatherAPI;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CurrentWeatherResponse currentWeatherResponse;
    final static String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    public  static double latitude, longitude;

    private DateConverter converter;

    private String imageIdStr;

    private TextView failedTextView;
    private TextView tempTextView;
    private TextView dateTextView;
    private TextView dayTextView;
    private TextView locationTextView;
    private TextView weatherDescTextView;
    private TextView maxTempTextView;
    private TextView maxTV;
    private TextView minTempTextView;
    private TextView minTV;
    private TextView sunriseTextView;
    private TextView sunriseTV;
    private TextView sunsetTextView;
    private TextView sunsetTV;
    private TextView humidityTextView;
    private TextView humidityTV;
    private TextView pressureTextView;
    private TextView pressureTV;

    private ImageView weatherIconImageView;
    private ImageView tempIconIV;
    private ImageView sunsetIconIV;
    private ImageView pressureIconIV;

    public CurrentWeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_currnet_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        failedTextView = view.findViewById(R.id.failedTextView);
        tempTextView = view.findViewById(R.id.temperatureTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        dayTextView = view.findViewById(R.id.dayTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        weatherDescTextView = view.findViewById(R.id.weatherDescTextView);
        maxTempTextView = view.findViewById(R.id.maxTempTextView);
        maxTV = view.findViewById(R.id.maxTV);
        minTempTextView = view.findViewById(R.id.minTempTextView);
        minTV = view.findViewById(R.id.minTV);
        sunriseTextView = view.findViewById(R.id.sunriseTextView);
        sunriseTV = view.findViewById(R.id.sunriseTV);
        sunsetTextView = view.findViewById(R.id.sunsetTextView);
        sunsetTV = view.findViewById(R.id.sunsetTV);
        humidityTextView = view.findViewById(R.id.humidityTextView);
        humidityTV = view.findViewById(R.id.humidityTV);
        pressureTextView = view.findViewById(R.id.pressureTextView);
        pressureTV = view.findViewById(R.id.pressureTV);

        converter = new DateConverter();

        weatherIconImageView = view.findViewById(R.id.weatherIconImageView);
        tempIconIV = view.findViewById(R.id.tempIconIV);
        sunsetIconIV = view.findViewById(R.id.sunsetIconIV);
        pressureIconIV = view.findViewById(R.id.rainIconIV);

    }

    private void getCurrentWeatherData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherAPI = retrofit.create(WeatherAPI.class);

        String urlString = String.format("weather?lat=%f&lon=%f&units=%s&appid=%s",latitude,longitude,units,getString(R.string.weather_api_key));

        Call<CurrentWeatherResponse> responseCall = weatherAPI.getCurrentWeatherData(urlString);

        // Set up progress before call
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMax(100);
        progressDialog.setMessage("Its loading....");
        progressDialog.setTitle("Fetching Data");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // show it
        progressDialog.show();

        responseCall.enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                if (response.code()==200){
                    progressDialog.dismiss();
                    currentWeatherResponse = response.body();
                    setCurrentWeatherData();
                }
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                progressDialog.dismiss();
                failedTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setCurrentWeatherData() {
        CurrentWeatherResponse.Main main = currentWeatherResponse.getMain();
        CurrentWeatherResponse.Sys sys = currentWeatherResponse.getSys();

        double temp = currentWeatherResponse.getMain().getTemp();
        if (units.equals("metric")) {
            tempTextView.setText(temp + " \u00B0C");
            maxTempTextView.setText(main.getTempMax() + " \u00B0C");

            tempIconIV.setImageResource(R.drawable.temperature);
            minTempTextView.setText(main.getTempMin() + " \u00B0C");
        } else if (units.equals("imperial")) {
            tempTextView.setText(temp + " \u00B0F");
            maxTempTextView.setText(main.getTempMax() + " \u00B0F");
            minTempTextView.setText(main.getTempMin() + " \u00B0F");
        }
        weatherDescTextView.setText(currentWeatherResponse.getWeather().get(0).getDescription());
        dateTextView.setText(converter.getDateInString(currentWeatherResponse.getDt()));
        dayTextView.setText(converter.unixToDay(currentWeatherResponse.getDt()));
        imageIdStr = currentWeatherResponse.getWeather().get(0).getIcon();
        long sunrise = sys.getSunrise();
        long sunset = sys.getSunset();

        maxTV.setText("Max");
        minTV.setText("Min");
        sunriseTextView.setText(converter.getTimeFromUnix(sunrise));
        sunriseTV.setText("Sun Rise");
        sunsetTextView.setText(converter.getTimeFromUnix(sunset));
        sunsetTV.setText("Sun Set");
        sunsetIconIV.setImageResource(R.drawable.sunset);
        humidityTextView.setText(main.getHumidity() + "%");
        humidityTV.setText("Humidity");
        pressureTextView.setText(main.getPressure() + "hPa");
        pressureTV.setText("Pressure");
        pressureIconIV.setImageResource(R.drawable.rain);

        locationTextView.setText(currentWeatherResponse.getName());

        Picasso.get().load("https://openweathermap.org/img/w/" + imageIdStr + ".png").into(weatherIconImageView);
    }



    @Override
    public void onResume() {
        super.onResume();
        units = WeatherActivity.units;
        if (WeatherActivity.isLocationOn(getContext())){
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

            if (WeatherActivity.latitude!=0.0 && WeatherActivity.longitude!=0.0){
                latitude = WeatherActivity.latitude;
                longitude = WeatherActivity.longitude;
                Log.e("City LatLng", "onResume: "+ latitude+" , "+longitude );
                getCurrentWeatherData();
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
                            getCurrentWeatherData();
                        }

                    }
                });
            }
        }else {
            Toast.makeText(getContext(), "Location Not On. Please On Your Device Location then Try Again", Toast.LENGTH_SHORT).show();
        }
    }


}
