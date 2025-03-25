package com.example.stepler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация элементов
        mAuth = FirebaseAuth.getInstance();
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Регистрация...");
        progressDialog.setCancelable(false);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (isValidRegistration(name, email, password, confirmPassword)) {
                progressDialog.show();
                registerUser(name, email, password);
            }
        });
    }

    private void registerUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(user.getUid(), name, email);
                        }
                    } else {
                        progressDialog.dismiss();
                        handleRegistrationError(task.getException(), email);
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String email) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);

        dbRef.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleRegistrationError(Exception exception, String email) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            etEmail.setError("Этот email уже зарегистрирован");
            etEmail.requestFocus();
        } else {
            String errorMessage = exception != null ?
                    exception.getMessage() : "Неизвестная ошибка";
            Toast.makeText(this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidRegistration(String name, String email,
                                        String password, String confirmPassword) {
        boolean isValid = true;

        if (name.isEmpty()) {
            etName.setError("Введите имя");
            isValid = false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Некорректный email");
            isValid = false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            isValid = false;
        }

        return isValid;
    }
}
