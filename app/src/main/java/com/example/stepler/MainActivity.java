package com.example.stepler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // Инициализация элементов
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            // Если да, переходим на главный экран
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        // Обработка клика по кнопке входа
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(isValidInput(email, password)) {
                showProgressDialog(); // Показываем индикатор загрузки

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            dismissProgressDialog(); // Скрываем индикатор

                            if(task.isSuccessful()) {
                                // Вход успешен
                                FirebaseUser user = mAuth.getCurrentUser();
                                startActivity(new Intent(this, HomeActivity.class));
                                finish();
                            } else {
                                // Обработка ошибок
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthInvalidUserException e) {
                                    etEmail.setError("Аккаунт не найден");
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    etPassword.setError("Неверный пароль");
                                } catch(Exception e) {
                                    Toast.makeText(this, "Ошибка: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Обработка перехода к регистрации
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    private ProgressDialog progressDialog;
    private void dismissProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Вход...");
        progressDialog.setCancelable(false);
        progressDialog.show();
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