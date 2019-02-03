package com.nullpointers.toutmate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.nullpointers.toutmate.Model.NetworkConnectivity;
import com.nullpointers.toutmate.fragment.CurrentWeatherFragment;
import com.nullpointers.toutmate.fragment.ForecastWeatherFragment;

import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static String units = "metric";
    boolean isMetric = true;

    public static double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager  = findViewById(R.id.viewPager);


        setupFragmentWithViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        setTitle("Weather Report");

        if (!NetworkConnectivity.isNetworkAvailable(this)){
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                Log.i("onPlaceSelected: ", "Lat: " + latitude +", Lon: " + longitude);
                refresh();
                /*viewPager = findViewById(R.id.viewPagerForTab);
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);*/
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(WeatherActivity.this, "Place search error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem metricItem = menu.findItem(R.id.item_metric);
        MenuItem imperialItem = menu.findItem(R.id.item_imperial);
        if (isMetric) {
            metricItem.setVisible(false);
            imperialItem.setVisible(true);
        } else {
            metricItem.setVisible(true);
            imperialItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_metric:
                units = "metric";
                isMetric = true;
                refresh();
                break;
            case R.id.item_imperial:
                units = "imperial";
                isMetric = false;
                refresh();
                break;
            case R.id.action_home:
                startActivity(new Intent(WeatherActivity.this,MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh(){
        /*Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);*/
        viewPager = findViewById(R.id.viewPager);
        setupFragmentWithViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }
    public void setupFragmentWithViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CurrentWeatherFragment(),"Current Weather");
        adapter.addFragment(new ForecastWeatherFragment(),"7 Day Forecast");
        viewPager.setAdapter(adapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragments = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        public void addFragment(Fragment fragment, String title){
            this.fragments.add(fragment);
            this.titles.add(title);
        }
    }



    public static boolean isLocationOn(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnable = false;
        boolean networkEnable = false;
        try {
            gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (!gpsEnable && !networkEnable){
            return false;
        }else {
            return true;
        }
    }

}
