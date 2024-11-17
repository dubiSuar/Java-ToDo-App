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
import java.util.List;
import com.example.taskly.TaskAdapter.OnTaskInteractionListener;


public class CalendarFragment extends Fragment {

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Task> filteredTaskList;  // List to store tasks for the selected date
    private DatabaseReference tasksRef;

    // CalendarView component
    private android.widget.CalendarView calendarView;

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

        // Implement the OnTaskInteractionListener
        OnTaskInteractionListener taskInteractionListener = new OnTaskInteractionListener() {
            @Override
            public void onEditTask(Task task) {
                // Handle edit task logic here (e.g., show edit task dialog or navigate to another fragment/activity)
                Toast.makeText(getContext(), "Edit Task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteTask(Task task) {
                // Handle delete task logic here (e.g., delete task from Firebase)
                Toast.makeText(getContext(), "Delete Task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                deleteTaskFromFirebase(task);
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
                updateFilteredTasks();  // Update the filtered list based on selected date
                taskAdapter.notifyDataSetChanged();
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
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;  // Format date to "yyyy-MM-dd"
            filterTasksByDate(selectedDate);  // Filter tasks by selected date
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
        taskAdapter.notifyDataSetChanged();  // Update the RecyclerView with filtered tasks
    }

    // Helper method to update filtered tasks (based on selected date)
    private void updateFilteredTasks() {
        String selectedDate = getSelectedDateFromCalendar();  // Get the selected date from Calendar
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
        // Assuming the task object has an ID, use it to delete from Firebase
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
}
