package com.example.stepler;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LogsActivity extends AppCompatActivity
        implements NavigationDrawerHelper.NavigationListener,
        UserProfileLoader.ProfileDataListener {

    private NavigationDrawerHelper drawerHelper;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        // Инициализация компонентов
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Настройка Navigation Drawer
        drawerHelper = new NavigationDrawerHelper(
                this,
                R.id.drawer_layout,
                R.id.toolbar,
                R.id.nav_view
        );
        drawerHelper.setNavigationListener(this);

        // Загрузка данных пользователя
        UserProfileLoader.loadUserProfile(
                mAuth.getCurrentUser(),
                mDatabase,
                this
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onProfileLoaded(String name, String email) {
        runOnUiThread(() -> drawerHelper.updateHeader(name, email));
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, "Ошибка загрузки: " + message, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, HomeActivity.class));
        }
        else if (id == R.id.nav_car_control) {
            startActivity(new Intent(this, CarControlActivity.class));
        }

        drawerHelper.handleBackPressed();
        return true;
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}