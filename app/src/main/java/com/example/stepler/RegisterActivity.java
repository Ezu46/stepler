package com.example.stepler;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация элементов
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (isValidRegistration(name, email, password, confirmPassword)) {
                // TODO: Отправить данные на сервер
            }
        });
    }

    private boolean isValidRegistration(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            etName.setError("Введите имя");
            return false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Некорректный email");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            return false;
        }

        return true;
    }
}