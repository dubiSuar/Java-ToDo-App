package com.example.taskly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText regUsername, regFirstName, regLastName, regPassword;
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
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String username = regUsername.getText().toString();
                String firstname = regFirstName.getText().toString();
                String lastname = regLastName.getText().toString();
                String password = regPassword.getText().toString();

                // Creating a new user record in Firebase
                HelperClass helperClass = new HelperClass(firstname, lastname, username, password);
                reference.child(username).setValue(helperClass);

                Toast.makeText(RegisterActivity.this, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
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
