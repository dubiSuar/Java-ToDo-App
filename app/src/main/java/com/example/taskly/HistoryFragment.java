package com.example.taskly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        allTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(allTasks, new TaskAdapter.OnTaskInteractionListener() {
            @Override
            public void onEditTask(Task task) {
                // Handle Edit Task
            }

            @Override
            public void onDeleteTask(Task task) {
                // Handle Delete Task
            }

            @Override
            public void onUpdateTaskStatus(Task task) {
                // Handle Update Task Status
            }
        });

        recyclerView.setAdapter(taskAdapter);

        loadCompletedTasks();

        return view;
    }

    private void loadCompletedTasks() {
        // Get the logged-in username (assuming it's passed via Intent or shared preferences)
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username");

        if (loggedInUsername != null) {
            // Reference to the user's "tasks" node in Firebase
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                    .child(loggedInUsername) // Accessing specific user
                    .child("tasks"); // Accessing the tasks node for this user

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    allTasks.clear();
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        Task task = taskSnapshot.getValue(Task.class);
                        if (task != null && "Completed".equalsIgnoreCase(task.getStatus())) {
                            allTasks.add(task);
                        }
                    }
                    taskAdapter.updateTaskList(allTasks); // Update the adapter with completed tasks only
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                }
            });
        } else {
            // Handle case where username is null (error or not logged in)
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
