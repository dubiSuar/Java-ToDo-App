package com.example.taskly;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText regUsername, regFirstName, regLastName, regPassword, regConfirmPassword;
    private TextView tvLogin;
    private Button btnRegister;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        regUsername = findViewById(R.id.reg_username);
        regFirstName = findViewById(R.id.reg_firstname);
        regLastName = findViewById(R.id.reg_lastname);
        regPassword = findViewById(R.id.reg_pword);
        regConfirmPassword = findViewById(R.id.reg_confirm_pword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = regUsername.getText().toString().trim();
                String firstname = regFirstName.getText().toString().trim();
                String lastname = regLastName.getText().toString().trim();
                String password = regPassword.getText().toString().trim();
                String confirmPassword = regConfirmPassword.getText().toString().trim();

                // Check for empty fields
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(firstname) || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check for minimum password length
                if (password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if password and confirm password match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                // Check if the username already exists
                reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            // Create a new user record
                            HelperClass helperClass = new HelperClass(firstname, lastname, username, password);
                            reference.child(username).setValue(helperClass);

                            Toast.makeText(RegisterActivity.this, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Redirect to login
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
