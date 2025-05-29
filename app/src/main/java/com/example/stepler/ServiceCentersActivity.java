package com.example.stepler;

import static com.example.stepler.BuildConfig.YANDEX_API_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.ui_view.ViewProvider;

import com.example.stepler.UserProfileLoader;

import java.util.Objects;

public class ServiceCentersActivity extends AppCompatActivity
        implements NavigationDrawerHelper.NavigationListener {

    private static final Point KURSK_CENTER = new Point(51.7373, 36.1854); // Центр Курска

    private Spinner spinnerBrands;
    private MapView mapView;
    private NavigationDrawerHelper drawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(YANDEX_API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_service_centers);

        // Инициализация элементов интерфейса
        drawerHelper = new NavigationDrawerHelper(
                this,
                R.id.drawer_layout,
                R.id.toolbar,
                R.id.nav_view
        );
        drawerHelper.setNavigationListener(this);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Сервисные центры");

        // Загрузка профиля пользователя
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
                    // Обработка ошибки
                }
            });
        }

        // Настройка карты
        mapView = findViewById(R.id.mapview);

        // Настройка спиннера
        spinnerBrands = findViewById(R.id.spinner_brands);
        String[] brands = {"Toyota", "BMW", "Mercedes", "Audi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                brands
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrands.setAdapter(adapter);
        spinnerBrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMarkersForBrand(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void updateMarkersForBrand(String brand) {
        if (mapView == null) return;

        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        mapObjects.clear();

        switch (brand) {
            case "Toyota":
                addDealerMarker(new Point(51.78868802164937,36.17123276315079),
                        "Тойота Центр Курск\nпр-т Победы, 9\nТел: +7 4712 111-111");
                break;
            case "BMW":
                addDealerMarker(new Point(51.68666859359413,36.15096226082531),
                        "BMW Курск\nул. Энгельса, 173Д\nТел: +7 4712 222-222");
                break;
            case "Mercedes":
                addDealerMarker(new Point(51.7351, 36.2034),
                        "Mercedes-Benz Курск\nул. Энгельса, 8\nТел: +7 4712 333-333");
                break;
            case "Audi":
                addDealerMarker(new Point(51.7402, 36.1758),
                        "Audi Центр Курск\nул. Дзержинского, 45\nТел: +7 4712 444-444");
                break;
        }

        // Центрирование карты на Курске
        mapView.getMap().move(
                new CameraPosition(KURSK_CENTER, 13.0f, 0.0f, 0.0f)
        );
    }

    private void addDealerMarker(Point location, String info) {
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();

        // Добавление маркера
        mapObjects.addPlacemark(location);

        // Создание текстовой метки
        TextView tv = new TextView(this);
        tv.setText(info);
        tv.setBackgroundColor(Color.WHITE);
        tv.setTextColor(Color.BLACK);
        tv.setPadding(16, 8, 16, 8);

        // Позиция текста
        Point textPoint = new Point(
                location.getLatitude() + 0.0015,
                location.getLongitude()
        );

        mapObjects.addPlacemark(textPoint, new ViewProvider(tv));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_service_centers) {
            // текущая активность
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
        else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
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