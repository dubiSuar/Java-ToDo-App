package com.example.taskly;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserFragment extends Fragment {

    private Button btnLogout;
    private TextView tvFullName, tvUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize UI components
        btnLogout = view.findViewById(R.id.btnLogout);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);

        // Load user data from Firebase
        loadUserData();

        // Set a click listener to handle logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user from Firebase
                FirebaseAuth.getInstance().signOut();

                // Redirect the user to the LoginActivity
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish(); // Close the current activity
            }
        });

        return view;
    }

    private void loadUserData() {
        // Get the current user's username from shared preferences or passed intent
        String currentUsername = getArguments().getString("username"); // Retrieve this from shared preferences or intent

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.orderByChild("username").equalTo(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next(); // Get the first child
                    String fullName = userSnapshot.child("firstName").getValue(String.class) + " " +
                            userSnapshot.child("lastName").getValue(String.class);
                    String username = userSnapshot.child("username").getValue(String.class);

                    tvFullName.setText(fullName);
                    tvUsername.setText(username);
                } else {
                    Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
