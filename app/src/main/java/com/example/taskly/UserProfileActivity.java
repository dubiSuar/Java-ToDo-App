package com.example.taskly;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class UserProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile); // Set the layout for this activity

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Get the username from the intent
        String username = getIntent().getStringExtra("username");

        // Set default fragment (optional, e.g., HomeFragment)
        if (savedInstanceState == null) {
            // Pass username to UserFragment and load it
            UserFragment userFragment = new UserFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username", username); // Pass the username
            userFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, userFragment)
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
                    // Pass the username again when navigating to UserFragment
                    UserFragment userFragment = new UserFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("username", username); // Pass the username
                    userFragment.setArguments(bundle);
                    selectedFragment = userFragment;
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

        // Initialize the Floating Action Button
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the TaskBottomSheetFragment
                TaskBottomSheetFragment taskBottomSheet = new TaskBottomSheetFragment();
                taskBottomSheet.show(getSupportFragmentManager(), taskBottomSheet.getTag());
            }
        });
    }
}