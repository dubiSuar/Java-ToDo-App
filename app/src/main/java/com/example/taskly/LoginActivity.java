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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView tvRegister, textViewCountdown;
    private Dialog loadingModal;

    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCK_TIME = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        editTextUsername = findViewById(R.id.editText_username);
        editTextPassword = findViewById(R.id.editText_password);
        buttonLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        textViewCountdown = findViewById(R.id.textViewCountdown);

        loadingModal = new Dialog(this);
        loadingModal.setContentView(R.layout.loading_modal);
        loadingModal.setCancelable(false);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateUsername() && validatePassword()) {
                    loadingModal.show();
                    checkUser();
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

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

    public void checkUser() {
        String userUsername = editTextUsername.getText().toString().trim();
        String userPassword = editTextPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingModal.dismiss();
                if (snapshot.exists()) {
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);
                    if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                        showLoginNotification(userUsername);
                        checkTasksDueToday(userUsername);

                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                        intent.putExtra("username", userUsername);
                        startActivity(intent);
                        finish();
                    } else {
                        handleFailedLogin();
                    }
                } else {
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
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - loginAttempts;

        if (remainingAttempts > 0) {
            Toast.makeText(this, "You have " + remainingAttempts + " attempt(s) left", Toast.LENGTH_SHORT).show();
        } else {
            buttonLogin.setEnabled(false);
            textViewCountdown.setVisibility(View.VISIBLE);

            new CountDownTimer(LOCK_TIME, 1000) {
                public void onTick(long millisUntilFinished) {
                    textViewCountdown.setText("Please wait " + millisUntilFinished / 1000 + " seconds to try again");
                }

                public void onFinish() {
                    buttonLogin.setEnabled(true);
                    loginAttempts = 0;
                    textViewCountdown.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "You can try logging in again.", Toast.LENGTH_SHORT).show();
                }
            }.start();
        }

        if (remainingAttempts > 0) {
            loginAttempts++;
        } else {
            editTextPassword.setError("Invalid Password");
            editTextPassword.requestFocus();
        }
    }

    private void showLoginNotification(String username) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "login_channel";
            CharSequence channelName = "Login Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "login_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Welcome Back!")
                .setContentText("Welcome Back, " + username)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify(1, builder.build());
    }

    private void checkTasksDueToday(String username) {
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("tasks");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        tasksRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    String taskDate = taskSnapshot.child("date").getValue(String.class);
                    String taskTitle = taskSnapshot.child("title").getValue(String.class);

                    if (taskDate != null && taskDate.equals(today) && taskTitle != null) {
                        showTaskDueNotification(taskTitle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTaskDueNotification(String taskTitle) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "task_due_channel";
            CharSequence channelName = "Task Due Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "task_due_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Task Due Today")
                .setContentText("Task: " + taskTitle + " is due today!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify(2, builder.build());
    }
}
