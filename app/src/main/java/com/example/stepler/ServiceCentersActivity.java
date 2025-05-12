package com.example.stepler;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.example.stepler.CarControlActivity;
import com.example.stepler.LogsActivity;
import com.example.stepler.HomeActivity;
import com.example.stepler.MainActivity;

public class ServiceCentersActivity extends AppCompatActivity
        implements NavigationDrawerHelper.NavigationListener, OnMapReadyCallback {

    private Spinner spinnerBrands;
    private GoogleMap mMap;
    private NavigationDrawerHelper drawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_centers);

        // Drawer + toolbar
        drawerHelper = new NavigationDrawerHelper(
                this,
                R.id.drawer_layout,
                R.id.toolbar,
                R.id.nav_view
        );
        drawerHelper.setNavigationListener(this);

        // Spinner выбора марки
        spinnerBrands = findViewById(R.id.spinner_brands);
        String[] brands = {"Toyota", "BMW", "Mercedes", "Audi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                brands
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrands.setAdapter(adapter);

        // MapFragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // При смене марки можно обновлять метки на карте:

        spinnerBrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String brand = parent.getItemAtPosition(position).toString();
                updateMarkersForBrand(brand);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Начальная позиция (например Москва)
        LatLng defaultCity = new LatLng(55.7558, 37.6173);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCity, 10));
        // Поставим пару маркеров по умолчанию
        updateMarkersForBrand((String) spinnerBrands.getSelectedItem());
    }

    private void updateMarkersForBrand(String brand) {
        if (mMap == null) return;
        mMap.clear();
        // TODO: замените на реальные координаты сервис-центров
        if (brand.equals("Toyota")) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(55.76, 37.64))
                    .title("Toyota Центр Москва")
                    .snippet("Тел: +7 495 000 00 00"));
        }
        // добавить для других брендов...
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_service_centers) {
            // already here
        } else if (id == R.id.nav_car_control) {
            startActivity(new Intent(this, CarControlActivity.class));
        } else if (id == R.id.nav_logs) {
            startActivity(new Intent(this, LogsActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, HomeActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        drawerHelper.handleBackPressed();
        return true;
    }
}