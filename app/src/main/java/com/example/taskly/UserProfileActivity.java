package com.example.taskly;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class UserProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton btnAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile); // Set the layout for this activity

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize the Floating Action Button (FAB) for Add Task
        btnAddTask = findViewById(R.id.btnAddTask);

        // Get the username from the intent
        String username = getIntent().getStringExtra("username");

        // Set default fragment (optional, e.g., HomeFragment)
        if (savedInstanceState == null) {
            // Load HomeFragment by default
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Set listener for BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    setFABVisibility(true); // Show FAB in HomeFragment
                } else if (id == R.id.nav_calendar) {
                    selectedFragment = new CalendarFragment();
                    setFABVisibility(false); // Hide FAB in CalendarFragment
                } else if (id == R.id.nav_search) {
                    selectedFragment = new SearchFragment();
                    setFABVisibility(false); // Hide FAB in SearchFragment
                } else if (id == R.id.nav_profile) {
                    // Pass the username again when navigating to UserFragment
                    UserFragment userFragment = new UserFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("username", username); // Pass the username
                    userFragment.setArguments(bundle);
                    selectedFragment = userFragment;
                    setFABVisibility(false); // Hide FAB in Profile Fragment
                }

                // Replace fragment if one is selected
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                return true;
            }
        });

        // Set FAB click listener
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the TaskBottomSheetFragment
                TaskBottomSheetFragment taskBottomSheet = new TaskBottomSheetFragment();
                taskBottomSheet.show(getSupportFragmentManager(), taskBottomSheet.getTag());
            }
        });
    }

    // Helper method to control FAB visibility
    private void setFABVisibility(boolean isVisible) {
        if (isVisible) {
            btnAddTask.setVisibility(View.VISIBLE);
        } else {
            btnAddTask.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("You're about to leave Taskly")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Redirect to the login page
                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Optional: finish current activity
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
