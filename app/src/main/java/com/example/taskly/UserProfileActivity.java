package com.example.taskly;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile); // Set the layout for this activity

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment (optional, e.g., HomeFragment)
        if (savedInstanceState == null) {
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
                } else if (id == R.id.nav_calendar) {
                    selectedFragment = new CalendarFragment();
                } else if (id == R.id.nav_browse) {
                    selectedFragment = new BrowseFragment();
                } else if (id == R.id.nav_search) {
                    selectedFragment = new SearchFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new UserFragment();
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
    }
}
