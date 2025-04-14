package com.example.stepler;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity
        implements NavigationDrawerHelper.NavigationListener,
        UserProfileLoader.ProfileDataListener {

    private NavigationDrawerHelper drawerHelper;
    private TextView tvGreeting;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Инициализация компонентов
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        tvGreeting = findViewById(R.id.tvGreeting);

        // Настройка Navigation Drawer
        drawerHelper = new NavigationDrawerHelper(
                this,
                R.id.drawer_layout,
                R.id.toolbar,
                R.id.nav_view
        );
        drawerHelper.setNavigationListener(this);

        // Проверка авторизации
        if (mAuth.getCurrentUser() == null) {
            redirectToMain();
            return;
        }

        // Загрузка данных пользователя
        UserProfileLoader.loadUserProfile(
                mAuth.getCurrentUser(),
                mDatabase,
                this
        );

        updateGreeting();
    }

    @Override
    public void onProfileLoaded(String name, String email) {
        runOnUiThread(() -> {
            drawerHelper.updateHeader(name, email);
            updateGreeting(name);
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, "Ошибка загрузки: " + message, Toast.LENGTH_SHORT).show()
        );
    }

    private void updateGreeting() {
        updateGreeting("");
    }

    private void updateGreeting(String name) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetingResId = R.string.greeting_day;

        if (hour >= 5 && hour < 12) {
            greetingResId = R.string.greeting_morning;
        } else if (hour >= 18 && hour < 23) {
            greetingResId = R.string.greeting_evening;
        } else if (hour >= 23 || hour < 5) {
            greetingResId = R.string.greeting_night;
        }

        String greeting = getString(greetingResId);
        if (!name.isEmpty()) {
            greeting += ", " + name;
        }
        tvGreeting.setText(greeting);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            logoutUser();
        } else if (id == R.id.nav_logs) {
            startActivity(new Intent(this, LogsActivity.class));
        } else if (id == R.id.nav_profile) {
            // Уже в профиле
        } else if (id == R.id.nav_car_control) {
            startActivity(new Intent(this, CarControlActivity.class));
        }

        drawerHelper.handleBackPressed();
        return true;
    }

    private void logoutUser() {
        mAuth.signOut();
        redirectToMain();
    }

    private void redirectToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        drawerHelper.handleBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            redirectToMain();
        }
    }
}