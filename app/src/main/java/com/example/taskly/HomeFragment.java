package com.example.taskly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;  // <-- Add this import
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements TaskBottomSheetFragment.TaskListener, TaskAdapter.OnTaskInteractionListener {

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseReference tasksRef;
    private Spinner sortSpinner;

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
                taskList.clear(); // Clear the list before adding tasks
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null && "In Progress".equals(task.getStatus())) {
                        taskList.add(task); // Add only "In Progress" tasks
                    }
                }
                sortTasks(); // Sort tasks if needed
                taskAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tasks.", Toast.LENGTH_SHORT).show();
            }
        });


        // Set up the Spinner for sorting options
        sortSpinner = view.findViewById(R.id.sortSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        // Set a listener for the Spinner to handle sort option selection
        // Set a listener for the Spinner to handle sort option selection
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedSortOption = sortSpinner.getSelectedItem().toString();
                Log.d("HomeFragment", "Selected sort option: " + selectedSortOption);
                sortTasks(); // Sort tasks when the selected option changes
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action needed
            }
        });


        return view;
    }

    // Method to sort tasks based on the selected sorting option
    private void sortTasks() {
        String selectedSortOption = sortSpinner.getSelectedItem().toString();
        if ("Priority".equals(selectedSortOption)) {
            sortTasksByPriority();
        } else if ("Date".equals(selectedSortOption)) {
            sortTasksByDate();
        } else if ("Title".equals(selectedSortOption)) {  // Handle "Sort by Title"
            sortTasksByTitle();
        }
        taskAdapter.notifyDataSetChanged();  // Update RecyclerView after sorting
    }

    // Method to sort tasks by priority
    private void sortTasksByPriority() {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                // Convert priority levels to an integer value for sorting
                int priority1 = getPriorityValue(t1.getPriority());
                int priority2 = getPriorityValue(t2.getPriority());
                return Integer.compare(priority1, priority2);
            }
        });
    }

    // Helper method to convert priority to integer
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High Priority":
                return 3;
            case "Medium Priority":
                return 2;
            case "Low Priority":
                return 1;
            default:
                return 0;
        }
    }

    // Method to sort tasks by date
    private void sortTasksByDate() {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getDate().compareTo(t2.getDate());
            }
        });
    }

    private void sortTasksByTitle() {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getTitle().compareTo(t2.getTitle());  // Sort titles alphabetically
            }
        });
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
        Task newTask = new Task();
        newTask.setId("someUniqueId"); // Set a unique ID (use actual ID from Firebase)
        newTask.setTitle(taskTitle);
        newTask.setDescription(taskText);
        newTask.setPriority(selectedPriority);
        newTask.setDate(selectedDate);

        taskList.add(newTask);
        sortTasks(); // Sort after adding a new task
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskUpdated(String taskId, String taskTitle, String taskText, String selectedPriority, String selectedDate) {
        for (Task task : taskList) {
            if (task.getId().equals(taskId)) {
                task.setTitle(taskTitle);
                task.setDescription(taskText);
                task.setPriority(selectedPriority);
                task.setDate(selectedDate);
                break;
            }
        }
        sortTasks(); // Sort after updating a task
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditTask(Task task) {
        TaskBottomSheetFragment editTaskFragment = TaskBottomSheetFragment.newInstance(task);
        editTaskFragment.show(getChildFragmentManager(), "EditTaskFragment");
    }

    @Override
    public void onDeleteTask(Task task) {
        tasksRef.child(task.getId()).removeValue().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                taskList.remove(task);
                sortTasks(); // Sort after deleting a task
                taskAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUpdateTaskStatus(Task task) {
        // Retrieve the logged-in username from arguments or shared preferences
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username"); // Or use shared preferences if needed

        // Ensure logged-in username is available
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Toast.makeText(getContext(), "Username not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the user's tasks in Firebase
        DatabaseReference userTasksRef = FirebaseDatabase.getInstance().getReference("users")
                .child(loggedInUsername).child("tasks");

        // Prepare the task data to be updated (just the status field)
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("status", "Completed"); // Update the status to "Completed"

        // Update the task status under the specific task node using task ID
        userTasksRef.child(task.getId()).updateChildren(taskMap)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Toast.makeText(getContext(), "Task marked as completed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                });
    }

}
