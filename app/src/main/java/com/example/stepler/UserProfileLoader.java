// UserProfileLoader.java
package com.example.stepler;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UserProfileLoader {
    public interface ProfileDataListener {
        void onProfileLoaded(String name, String email);
        void onError(String message);
    }

    public static void loadUserProfile(FirebaseUser user,
                                       DatabaseReference databaseRef,
                                       ProfileDataListener listener) {

        if (user == null) {
            listener.onError("User not authenticated");
            return;
        }

        String email = user.getEmail();

        // Загружаем имя из базы данных
        databaseRef.child(user.getUid()).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        listener.onProfileLoaded(
                                name != null ? name : "",
                                email != null ? email : ""
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UserProfileLoader", "DB error: ", error.toException());
                        listener.onError(error.getMessage());
                    }
                });
    }
}