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

public class HomeFragment extends Fragment implements TaskBottomSheetFragment.TaskListener, TaskAdapter.OnTaskInteractionListener {

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseReference tasksRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Retrieve the logged-in user's username passed from LoginActivity
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username");

        // Initialize RecyclerView and set up the adapter
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);
        recyclerViewTasks.setAdapter(taskAdapter);

        // Initialize Firebase Database reference to specific user's tasks
        tasksRef = FirebaseDatabase.getInstance().getReference("users").child(loggedInUsername).child("tasks");

        // Fetch tasks for the logged-in user
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tasks.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void openTaskBottomSheet(Task task) {
        TaskBottomSheetFragment taskBottomSheetFragment;
        if (task == null) {
            taskBottomSheetFragment = TaskBottomSheetFragment.newInstance(); // For adding a new task
        } else {
            taskBottomSheetFragment = TaskBottomSheetFragment.newInstance(task); // For editing an existing task
        }

        taskBottomSheetFragment.setTaskListener(this); // Set the listener
        taskBottomSheetFragment.show(getChildFragmentManager(), taskBottomSheetFragment.getTag());
    }

    @Override
    public void onTaskAdded(String taskTitle, String taskText, String selectedPriority, String selectedDate) {
        // Add the new task to your local task list and update UI
        Task newTask = new Task(); // Create a new Task object
        newTask.setId("someUniqueId"); // Set a unique ID (use actual ID from Firebase)
        newTask.setTitle(taskTitle);
        newTask.setDescription(taskText);
        newTask.setPriority(selectedPriority);
        newTask.setDate(selectedDate);

        // Add the new task to the list and update the RecyclerView
        taskList.add(newTask);
        taskAdapter.notifyDataSetChanged(); // Notify adapter about the new item
    }

    @Override
    public void onTaskUpdated(String taskId, String taskTitle, String taskText, String selectedPriority, String selectedDate) {
        // Find the existing task and update it
        for (Task task : taskList) {
            if (task.getId().equals(taskId)) {
                task.setTitle(taskTitle);
                task.setDescription(taskText);
                task.setPriority(selectedPriority);
                task.setDate(selectedDate);
                break;
            }
        }
        taskAdapter.notifyDataSetChanged(); // Notify adapter about the update
    }


    @Override
    public void onEditTask(Task task) {
        // Create a new instance of TaskBottomSheetFragment for editing
        TaskBottomSheetFragment editTaskFragment = TaskBottomSheetFragment.newInstance(task);

        // Show the fragment
        editTaskFragment.show(getChildFragmentManager(), "EditTaskFragment");
    }


    @Override
    public void onDeleteTask(Task task) {
        tasksRef.child(task.getId()).removeValue().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                taskList.remove(task);
                taskAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
