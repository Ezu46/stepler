package com.example.stepler;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.stepler.LogsActivity;
import com.example.stepler.HomeActivity;
import com.example.stepler.UserProfileLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class CarControlActivity extends AppCompatActivity
        implements NavigationDrawerHelper.NavigationListener {

    private DatabaseReference arduinoRef;
    private DatabaseReference stateRef;

    private ImageView   imgCar;
    private ImageButton btnEngine;
    private ImageButton btnWindows;
    private ImageButton btnLights;
    private ImageButton btnLock;
    private TextView    tvEngineStatus;

    private NavigationDrawerHelper drawerHelper;
    private Vibrator                vibrator;
    private TextView tvLocation;

    private boolean isWindowsOpen = false;
    private boolean isDoorsLocked = true;

    public class LogEntry {
        public String message;
        public long timestamp;
        public String userId;
        public String engine;
        public String windows;
        public String locks;

        public LogEntry() { }

        public LogEntry(String message, long timestamp, String userId,
                        String engine, String windows, String locks) {
            this.message  = message;
            this.timestamp= timestamp;
            this.userId   = userId;
            this.engine   = engine;
            this.windows  = windows;
            this.locks    = locks;
        }
        // геттер для форматирования времени…
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_control);

        // === Navigation Drawer & Toolbar ===
        drawerHelper = new NavigationDrawerHelper(
                this,
                R.id.drawer_layout,
                R.id.toolbar,
                R.id.nav_view
        );
        drawerHelper.setNavigationListener(this);

        // === View binding ===
        imgCar         = findViewById(R.id.img_car);
        btnEngine      = findViewById(R.id.btn_engine);
        btnWindows     = findViewById(R.id.btn_windows);
        btnLights      = findViewById(R.id.btn_lights);
        btnLock        = findViewById(R.id.btn_lock);
        tvEngineStatus = findViewById(R.id.engine_status);
        tvLocation     = findViewById(R.id.tv_location);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // === Firebase setup ===
        arduinoRef = FirebaseDatabase.getInstance()
                .getReference("Arduino");

        checkAuthentication();
        setupDatabaseListener();

        // Listen for saved state changes
        stateRef = FirebaseDatabase.getInstance().getReference("state");
        stateRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                // Engine state
                String engine = snapshot.child("engine").getValue(String.class);
                if (engine != null) {
                    tvEngineStatus.setText("Двигатель: " + engine);
                }
                // Windows state
                String win = snapshot.child("windows").getValue(String.class);
                isWindowsOpen = "OPEN".equals(win);
                updateWindowButton();
                // Locks state
                String lock = snapshot.child("locks").getValue(String.class);
                isDoorsLocked = "LOCKED".equals(lock);
                updateLockButton();
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e("CarControlActivity", "State listener error: " + error.getMessage());
            }
        });

        setupButtonClickListeners();
        fetchPublicIpAndShow();
        // === Load user profile ===
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance()
                    .getReference("users");          // << lowercase!
            UserProfileLoader.loadUserProfile(
                    currentUser,
                    usersRef,
                    new UserProfileLoader.ProfileDataListener() {
                        @Override
                        public void onProfileLoaded(String name, String email) {
                            // Nav Drawer header
                            drawerHelper.updateHeader(name, email);
                            // Toolbar title + subtitlе
                        }
                        @Override
                        public void onError(String message) {
                            Log.e("CarControlActivity", "Profile load error: " + message);
                        }
                    }
            );
        }
    }

    private void fetchPublicIpAndShow() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.ipify.org");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                final String ip = in.readLine();
                in.close();
                runOnUiThread(() ->
                        tvLocation.setText("Геопозиция машины: " + ip)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void checkAuthentication() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void setupDatabaseListener() {
        arduinoRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String action = snapshot.child("action").getValue(String.class);
                    if (action != null) updateUI(action);
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    private void setupButtonClickListeners() {
        // Окна
        btnWindows.setOnClickListener(v -> {
            vibrate(50);
            toggleWindows();
        });

        // Старт/Стоп двигателя
        btnEngine.setOnClickListener(v -> {
            vibrate(50);
            boolean engineOff = tvEngineStatus.getText().toString().endsWith("OFF");
            String cmd = engineOff ? "engine_start" : "engine_stop";
            sendCommand(cmd);
            logAction(cmd);
        });

        // Фары (мигалка)
        btnLights.setOnClickListener(v -> {
            vibrate(20);
            logAction("lights_flash");
        });

        // Блокировка/Разблокировка дверей
        btnLock.setOnClickListener(v -> {
            vibrate(50);
            isDoorsLocked = !isDoorsLocked;
            logAction(isDoorsLocked ? "lock_doors" : "unlock_doors");
            updateLockButton();
        });
    }

    private void toggleWindows() {
        isWindowsOpen = !isWindowsOpen;
        logAction(isWindowsOpen ? "windows_open" : "windows_close");
        updateWindowButton();
    }

    private void sendCommand(String command) {
        arduinoRef.child("action")
                .setValue(command)
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Send error: " + e.getMessage())
                );
    }

    private void updateUI(String action) {
        runOnUiThread(() -> {
            switch (action) {
                case "windows_open":
                    isWindowsOpen = true;
                    updateWindowButton();
                    break;
                case "windows_close":
                    isWindowsOpen = false;
                    updateWindowButton();
                    break;
                case "engine_start":
                    tvEngineStatus.setText("Двигатель: ON");
                    break;
                case "engine_stop":
                    tvEngineStatus.setText("Двигатель: OFF");
                    break;
                case "lock_doors":
                    isDoorsLocked = true;
                    updateLockButton();
                    break;
                case "unlock_doors":
                    isDoorsLocked = false;
                    updateLockButton();
                    break;
            }
        });
    }

    private void updateWindowButton() {
        btnWindows.setImageResource(
                isWindowsOpen
                        ? R.drawable.open_window
                        : R.drawable.close_window
        );
    }

    private void updateLockButton() {
        btnLock.setImageResource(
                isDoorsLocked
                        ? R.drawable.car_lock
                        : R.drawable.car_open
        );
    }

    private void vibrate(long ms) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //noinspection deprecation
                vibrator.vibrate(ms);
            }
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_logs) {
            startActivity(new Intent(this, LogsActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, HomeActivity.class));
        } else if (id == R.id.nav_car_control) {
            // Уже здесь))
        }
        else if (id == R.id.nav_service_centers) {
            startActivity(new Intent(this, ServiceCentersActivity.class));
        }
        drawerHelper.handleBackPressed();
        return true;
    }

    private void logAction(String action) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Читаем текущее состояние UI
        String engine;
        if ("engine_start".equals(action)) {
            engine = "ON";
        } else if ("engine_stop".equals(action)) {
            engine = "OFF";
        } else {
            engine = tvEngineStatus.getText().toString().endsWith("ON") ? "ON" : "OFF";
        }
        String windows = isWindowsOpen ? "OPEN" : "CLOSED";
        String locks   = isDoorsLocked ? "LOCKED" : "UNLOCKED";

        long timestamp = System.currentTimeMillis();
        String uid     = user.getUid();

        // Подготавливаем объект лога
        LogEntry entry = new LogEntry(action, timestamp, uid, engine, windows, locks);

        // Пишем в /logs/<uid>/
        DatabaseReference logsRef = FirebaseDatabase
                .getInstance()
                .getReference("logs")
                .child(uid);
        logsRef.push().setValue(entry);

        // Обновляем актуальное состояние в /state/
        DatabaseReference stateRef = FirebaseDatabase
                .getInstance()
                .getReference("state");
        stateRef.child("engine").setValue(engine);
        stateRef.child("windows").setValue(windows);
        stateRef.child("locks").setValue(locks);
    }

    @Override
    public void onBackPressed() {
        if (drawerHelper.isDrawerOpen()) {
            drawerHelper.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}