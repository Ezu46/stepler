package com.example.stepler;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogsActivity extends AppCompatActivity
        implements NavigationDrawerHelper.NavigationListener,
        UserProfileLoader.ProfileDataListener {

    private NavigationDrawerHelper drawerHelper;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RecyclerView logsRecyclerView;
    private LogAdapter logAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        // Инициализация компонентов
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Настройка RecyclerView
        logsRecyclerView = findViewById(R.id.logs_recycler_view);
        logsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter(new ArrayList<>());
        logsRecyclerView.setAdapter(logAdapter);

        // Загрузка логов
        loadLogs();

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

    private void loadLogs() {
        // Directly reference the Arduino logs at root (action branch)
        DatabaseReference logsRef = FirebaseDatabase
            .getInstance()
            .getReference("Arduino/action");
        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LogEntry> logEntries = new ArrayList<>();
                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    LogEntry logEntry = logSnapshot.getValue(LogEntry.class);
                    if (logEntry != null) {
                        logEntries.add(logEntry);
                    }
                }
                Collections.reverse(logEntries); // Новые логи сверху
                logAdapter.updateLogs(logEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LogsActivity.this,
                        "Ошибка загрузки логов: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class LogEntry {
        public String message;
        public long timestamp;
        public String userId;

        public LogEntry() {}

        public LogEntry(String message, long timestamp, String userId) {
            this.message = message;
            this.timestamp = timestamp;
            this.userId = userId;
        }

        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    private static class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
        private List<LogEntry> logEntries;

        public LogAdapter(List<LogEntry> logEntries) {
            this.logEntries = logEntries;
        }

        public void updateLogs(List<LogEntry> newLogs) {
            this.logEntries = newLogs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_log, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            LogEntry logEntry = logEntries.get(position);
            holder.timeTextView.setText(logEntry.getFormattedTime());
            holder.messageTextView.setText(logEntry.message);
        }

        @Override
        public int getItemCount() {
            return logEntries.size();
        }

        static class LogViewHolder extends RecyclerView.ViewHolder {
            TextView timeTextView;
            TextView messageTextView;

            public LogViewHolder(@NonNull View itemView) {
                super(itemView);
                timeTextView = itemView.findViewById(R.id.time_text_view);
                messageTextView = itemView.findViewById(R.id.message_text_view);
            }
        }
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
        else if (id == R.id.nav_service_centers) {
            startActivity(new Intent(this, ServiceCentersActivity.class));
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