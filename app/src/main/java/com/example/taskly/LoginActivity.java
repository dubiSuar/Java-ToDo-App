package com.example.taskly;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView tvRegister;
    private Dialog loadingModal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        editTextUsername = findViewById(R.id.editText_username);
        editTextPassword = findViewById(R.id.editText_password);
        buttonLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

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
                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                        intent.putExtra("username", userUsername); // Pass the username
                        startActivity(intent);
                        finish();
                    } else {
                        editTextPassword.setError("Invalid Credentials");
                        editTextPassword.requestFocus();
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
}
