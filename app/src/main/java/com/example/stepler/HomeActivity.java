package com.example.stepler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private TextView tvGreeting;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Инициализация элементов
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        tvGreeting = findViewById(R.id.tvGreeting);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Проверка авторизации
        if (mAuth.getCurrentUser() == null) {
            redirectToMain();
            return;
        }

        // Загрузка данных и обновление интерфейса
        loadUserData();
        updateGreeting();
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetingResId = R.string.greeting_day;

        if (hour >= 5 && hour < 12) {
            greetingResId = R.string.greeting_morning;
        } else if (hour >= 18 && hour < 23) {
            greetingResId = R.string.greeting_evening;
        } else if (hour >= 23 || hour < 5) {
            greetingResId = R.string.greeting_night;
        }

        tvGreeting.setText(getString(greetingResId));
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Установка email в заголовок
            NavigationView navigationView = findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
            tvHeaderEmail.setText(user.getEmail());

            // Загрузка имени из базы данных
            mDatabase.child(user.getUid()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                updateUserName(name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("HomeActivity", "Error loading name", error.toException());
                        }
                    });
        }
    }

    private void updateUserName(String name) {
        // Обновление заголовка меню
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        tvHeaderName.setText(name);

        // Обновление приветствия
        String currentGreeting = tvGreeting.getText().toString();
        tvGreeting.setText(currentGreeting + ", " + name);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            logoutUser();
        } else if (id == R.id.nav_logs) {
            startActivity(new Intent(this, LogsActivity.class));
        }

        drawer.closeDrawer(GravityCompat.START);
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            redirectToMain();
        }
    }
}