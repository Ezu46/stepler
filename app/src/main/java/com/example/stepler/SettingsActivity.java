package com.example.stepler;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements NavigationDrawerHelper.NavigationListener {

    private EditText etUserName;
    private Button btnSave;
    private FirebaseUser currentUser;
    private NavigationDrawerHelper drawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация полей
        etUserName = findViewById(R.id.et_user_name);
        btnSave    = findViewById(R.id.btn_save);

        // Настройка Navigation Drawer
        drawerHelper = new NavigationDrawerHelper(
                this,
                R.id.drawer_layout,
                R.id.toolbar,
                R.id.nav_view
        );
        drawerHelper.setNavigationListener(this);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Настройки");

        // Получение header view
        NavigationView navView = findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        TextView tvHeaderName  = headerView.findViewById(R.id.tvHeaderName);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        // Текущий пользователь
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        // Отображаем email сразу
        tvHeaderEmail.setText(currentUser.getEmail());

        // Загружаем имя из базы и заполняем поле и header
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getValue(String.class);
                    if (name != null) {
                        etUserName.setText(name);
                        tvHeaderName.setText(name);
                    }
                });

        // Сохранение нового имени
        btnSave.setOnClickListener(v -> {
            String newName = etUserName.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                etUserName.setError("Имя не может быть пустым");
                return;
            }
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("name")
                    .setValue(newName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Имя сохранено", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_service_centers) {
            startActivity(new Intent(this, ServiceCentersActivity.class));
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
