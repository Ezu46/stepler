package com.example.stepler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button btnLedOn, btnLedOff;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final String BASE_URL = "http://172.16.17.236/led/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Инициализация элементов
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnLedOn = findViewById(R.id.btnLedOn);
        btnLedOff = findViewById(R.id.btnLedOff);
        setupLedControls();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Проверка авторизации
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Загрузка данных пользователя
        loadUserData();

        // Обработчик выхода
        btnLogout.setOnClickListener(v -> logoutUser());
    }
    private void setupLedControls() {
        btnLedOn.setOnClickListener(v -> sendLedCommand("on"));
        btnLedOff.setOnClickListener(v -> sendLedCommand("off"));
    }

    private void sendLedCommand(String action) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + action);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);

                int responseCode = connection.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        Toast.makeText(this, "LED " + action.toUpperCase(), Toast.LENGTH_SHORT).show();
                    } else {
                        showError("Ошибка: код " + responseCode);
                    }
                });

                connection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> showError(e.getMessage()));
            }
        });
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Email всегда доступен через FirebaseUser
            tvUserEmail.setText(user.getEmail());

            // Загрузка имени из базы данных
            mDatabase.child(user.getUid()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                tvUserName.setText(name);
                            } else {
                                tvUserName.setText("Имя не указано");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvUserName.setText("Ошибка загрузки имени");
                        }
                    });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Проверка авторизации при возвращении на экран
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }



}