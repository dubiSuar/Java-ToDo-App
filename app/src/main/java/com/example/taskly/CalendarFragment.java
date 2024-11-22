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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Task> filteredTaskList; // List to store tasks for the selected date
    private DatabaseReference tasksRef;

    // CalendarView component
    private android.widget.CalendarView calendarView;

    // "No tasks" message TextView
    private View noTasksMessage;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Get the logged-in user's username (assumed to be passed from another activity or fragment)
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username");

        // Initialize RecyclerView and Task Adapter
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();
        filteredTaskList = new ArrayList<>();

        // Initialize "No tasks" message TextView
        noTasksMessage = view.findViewById(R.id.textViewNoTasks);

        // Implement the OnTaskInteractionListener
        TaskAdapter.OnTaskInteractionListener taskInteractionListener = new TaskAdapter.OnTaskInteractionListener() {
            @Override
            public void onEditTask(Task task) {
                Toast.makeText(getContext(), "Edit Task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteTask(Task task) {
                deleteTaskFromFirebase(task);
                Toast.makeText(getContext(), "Delete Task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpdateTaskStatus(Task task) {
                updateTaskStatus(task, loggedInUsername);
            }
        };

        // Pass the listener to the TaskAdapter
        taskAdapter = new TaskAdapter(filteredTaskList, taskInteractionListener);
        recyclerViewTasks.setAdapter(taskAdapter);

        // Firebase Database reference for tasks of the logged-in user
        tasksRef = FirebaseDatabase.getInstance().getReference("users").child(loggedInUsername).child("tasks");

        // Fetch tasks from Firebase
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
                updateFilteredTasks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tasks.", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize CalendarView
        calendarView = view.findViewById(R.id.calendarView);

        // Set the date change listener to filter tasks by selected date
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth; // Format date to "yyyy-MM-dd"
            filterTasksByDate(selectedDate);
        });

        return view;
    }

    // Method to filter tasks by selected date
    private void filterTasksByDate(String selectedDate) {
        filteredTaskList.clear();
        for (Task task : taskList) {
            if (task.getDate().equals(selectedDate)) {
                filteredTaskList.add(task);
            }
        }

        // Show or hide "No tasks for this date" message based on filtered tasks
        if (filteredTaskList.isEmpty()) {
            noTasksMessage.setVisibility(View.VISIBLE);
        } else {
            noTasksMessage.setVisibility(View.GONE);
        }

        taskAdapter.notifyDataSetChanged(); // Update the RecyclerView with filtered tasks
    }

    // Helper method to update filtered tasks (based on selected date)
    private void updateFilteredTasks() {
        String selectedDate = getSelectedDateFromCalendar(); // Get the selected date from Calendar
        filterTasksByDate(selectedDate);
    }

    // Helper method to get the selected date in "yyyy-MM-dd" format from the CalendarView
    private String getSelectedDateFromCalendar() {
        long selectedDateInMillis = calendarView.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(selectedDateInMillis);
    }

    // Method to delete a task from Firebase
    private void deleteTaskFromFirebase(Task task) {
        String taskId = task.getId();
        if (taskId != null) {
            tasksRef.child(taskId).removeValue()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to update task status in Firebase
    private void updateTaskStatus(Task task, String loggedInUsername) {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Toast.makeText(getContext(), "Username not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userTasksRef = FirebaseDatabase.getInstance().getReference("users")
                .child(loggedInUsername).child("tasks");

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("status", "Completed");

        userTasksRef.child(task.getId()).updateChildren(taskMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Task marked as completed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update task", Toast.LENGTH_SHORT).show());
    }
}
