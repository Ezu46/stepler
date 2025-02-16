package com.example.stepler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Обработка клика по кнопке входа
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (isValidInput(email, password)) {
                // TODO: Реализовать логику входа
            }
        });

        // Обработка перехода к регистрации
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean isValidInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Введите email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Некорректный email");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return false;
        }

        return true;
    }
}