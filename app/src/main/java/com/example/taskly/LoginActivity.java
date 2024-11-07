package com.example.taskly;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView tvRegister, textViewCountdown;
    private Dialog loadingModal;

    // Counter for failed login attempts
    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3; // Maximum attempts
    private static final long LOCK_TIME = 15000; // Lock time in milliseconds (15 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Check and request notification permission (for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        editTextUsername = findViewById(R.id.editText_username);
        editTextPassword = findViewById(R.id.editText_password);
        buttonLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        textViewCountdown = findViewById(R.id.textViewCountdown); // Initialize countdown TextView

        // Initialize the loading modal
        loadingModal = new Dialog(this);
        loadingModal.setContentView(R.layout.loading_modal);
        loadingModal.setCancelable(false);

        // Login Button Click Listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateUsername() && validatePassword()) {
                    loadingModal.show(); // Show loading modal
                    checkUser();
                }
            }
        });

        // Register Text Click Listener
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Username Validation
    public boolean validateUsername() {
        String val = editTextUsername.getText().toString();
        if (val.isEmpty()) {
            editTextUsername.setError("Username cannot be empty");
            return false;
        } else {
            editTextUsername.setError(null);
            return true;
        }
    }

    // Password Validation
    public boolean validatePassword() {
        String val = editTextPassword.getText().toString();
        if (val.isEmpty()) {
            editTextPassword.setError("Password cannot be empty");
            return false;
        } else {
            editTextPassword.setError(null);
            return true;
        }
    }

    // Firebase User Check
    public void checkUser() {
        String userUsername = editTextUsername.getText().toString().trim();
        String userPassword = editTextPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingModal.dismiss(); // Hide the loading modal
                if (snapshot.exists()) {
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);
                    if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                        // Successful login
                        // Show notification
                        showLoginNotification(userUsername);

                        // Start the UserProfileActivity
                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                        intent.putExtra("username", userUsername); // Pass the username
                        startActivity(intent);
                        finish();
                    } else {
                        // Invalid password
                        handleFailedLogin();
                    }
                } else {
                    // User does not exist
                    editTextUsername.setError("User does not exist");
                    editTextUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingModal.dismiss();
                Toast.makeText(LoginActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFailedLogin() {
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - loginAttempts; // Calculate remaining attempts

        // Show how many attempts are left
        if (remainingAttempts > 0) {
            Toast.makeText(this, "You have " + remainingAttempts + " attempt(s) left", Toast.LENGTH_SHORT).show();
        } else {
            // Lock the login button
            buttonLogin.setEnabled(false);
            textViewCountdown.setVisibility(View.VISIBLE); // Show countdown text view

            // Start the countdown timer
            new CountDownTimer(LOCK_TIME, 1000) {
                public void onTick(long millisUntilFinished) {
                    textViewCountdown.setText("Please wait " + millisUntilFinished / 1000 + " seconds to try again");
                }

                public void onFinish() {
                    buttonLogin.setEnabled(true);
                    loginAttempts = 0; // Reset attempts after unlock
                    textViewCountdown.setVisibility(View.GONE); // Hide countdown text view
                    Toast.makeText(LoginActivity.this, "You can try logging in again.", Toast.LENGTH_SHORT).show();
                }
            }.start();
        }

        // If the user has not reached max attempts, increment the loginAttempts counter
        if (remainingAttempts > 0) {
            loginAttempts++;
        } else {
            editTextPassword.setError("Invalid Password");
            editTextPassword.requestFocus();
        }
    }

    // Method to show login notification
    private void showLoginNotification(String username) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if we need to create a notification channel (for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "login_channel"; // Channel ID
            CharSequence channelName = "Login Notifications"; // Channel Name
            int importance = NotificationManager.IMPORTANCE_HIGH; // Set importance to HIGH for heads-up notification
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "login_channel")
                .setSmallIcon(R.drawable.baseline_arrow_circle_up_24) // Set the notification icon (use an appropriate icon)
                .setContentTitle("Welcome Back!")
                .setContentText("Welcome Back, " + username)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up notification
                .setAutoCancel(true) // Dismiss the notification when clicked
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Ensure it's visible on the lock screen
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Use default settings (sound, vibration, etc.)

        // Show the notification
        notificationManager.notify(1, builder.build());
    }
}
