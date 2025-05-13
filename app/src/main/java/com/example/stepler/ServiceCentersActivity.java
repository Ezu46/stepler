package com.example.stepler;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.example.stepler.UserProfileLoader;
import com.example.stepler.CarControlActivity;
import com.example.stepler.LogsActivity;
import com.example.stepler.HomeActivity;
import com.example.stepler.MainActivity;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.Animation;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.map.PlacemarkMapObject;

public class ServiceCentersActivity extends AppCompatActivity
        implements NavigationDrawerHelper.NavigationListener {

    private Spinner spinnerBrands;
    private NavigationDrawerHelper drawerHelper;
    private MapView mapView;
    private MapObjectCollection mapObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("2ffcc49f-6fdd-4a73-9230-6c21e8b00463");
        MapKitFactory.initialize(this);

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

        // Load current user profile into navigation header
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            UserProfileLoader.loadUserProfile(currentUser, usersRef, new UserProfileLoader.ProfileDataListener() {
                @Override
                public void onProfileLoaded(String name, String email) {
                    drawerHelper.updateHeader(name, email);
                }
                @Override
                public void onError(String message) {
                    // Optionally log or toast the error
                }
            });
        }

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

        // MapView initialization
        mapView = findViewById(R.id.mapview);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        // Move camera to Moscow
        mapView.getMap().move(
            new CameraPosition(new Point(55.7558, 37.6173), 10.0f, 0.0f, 0.0f),
            new Animation(Animation.Type.SMOOTH, 0),
            null
        );

        // Initial markers
        updateMarkersForBrand((String) spinnerBrands.getSelectedItem(), mapObjects, mapView);

        // При смене марки можно обновлять метки на карте:
        spinnerBrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String brand = parent.getItemAtPosition(position).toString();
                updateMarkersForBrand(brand, mapObjects, mapView);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }

    private void updateMarkersForBrand(String brand, MapObjectCollection mapObjects, MapView mapView) {
        mapObjects.clear();
        if ("Toyota".equals(brand)) {
            PlacemarkMapObject placemark = mapObjects.addPlacemark(
                new Point(55.76, 37.64),
                ImageProvider.fromResource(this, R.drawable.ic_service_center)
            );
            placemark.addTapListener((mapObject, point) -> {
                Toast.makeText(this,
                    "Toyota Центр Москва\nТел: +7 495 000 00 00",
                    Toast.LENGTH_LONG).show();
                return true;
            });
        }
        // Camera reposition on brand change
        mapView.getMap().move(
            new CameraPosition(new Point(55.7558, 37.6173), 10.0f, 0.0f, 0.0f),
            new Animation(Animation.Type.SMOOTH, 0),
            null
        );
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

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}