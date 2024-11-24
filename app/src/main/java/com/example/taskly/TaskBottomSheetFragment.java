package com.example.taskly;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskBottomSheetFragment extends BottomSheetDialogFragment {

    // Interface for task listener
    public interface TaskListener {
        void onTaskAdded(String taskTitle, String taskText, String selectedPriority, String selectedDate);
        void onTaskUpdated(String taskId, String taskTitle, String taskText, String selectedPriority, String selectedDate);
    }

    private DatabaseReference tasksRef;
    private String selectedPriority = "Low"; // Default priority
    private String selectedDate = ""; // Default empty date
    private TaskListener taskListener; // Declare the listener
    private ImageView taskPriorityIcon; // Reference to the priority icon ImageView
    private TextView taskPriorityText; // Reference to the priority text View
    private EditText taskInputTitle;
    private EditText taskDescription;
    private Button btnDate;
    private Button btnPriority;
    private Button btnSaveTask;
    private String taskId; // Task ID to identify the task
    private String selectedCategory = "Personal"; // Default category
    private Button btnCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_task, container, false);

        // Initialize your views
        btnSaveTask = view.findViewById(R.id.btnSaveTask);
        taskInputTitle = view.findViewById(R.id.taskInputTitle);
        taskDescription = view.findViewById(R.id.taskDescription);
        btnDate = view.findViewById(R.id.btnDate);
        btnPriority = view.findViewById(R.id.btnPriority);
        taskPriorityIcon = view.findViewById(R.id.taskPriorityIcon);
        taskPriorityText = view.findViewById(R.id.taskPriorityText);

        // Initially hide the priority icon and text
        taskPriorityIcon.setVisibility(View.GONE);
        taskPriorityText.setVisibility(View.GONE);

        btnCategory = view.findViewById(R.id.btnCategory);
        btnCategory.setOnClickListener(v -> showCategorySelectionDialog());

        // Initialize Firebase Database reference
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        btnSaveTask.setOnClickListener(v -> {
            String taskTitle = taskInputTitle.getText().toString();
            String taskText = taskDescription.getText().toString();

            if (TextUtils.isEmpty(taskTitle)) {
                Toast.makeText(getContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
            } else if (taskId != null) {
                // If editing an existing task
                editTaskToFirebase(taskTitle, taskText);
            } else {
                saveTaskToFirebase(taskTitle, taskText);
            }
            dismiss(); // Close the bottom sheet
        });

        btnPriority.setOnClickListener(v -> showPrioritySelectionDialog());
        btnDate.setOnClickListener(v -> showDatePickerDialog()); // Use btnDate here

        return view;
    }

    // Method to create a new instance of the fragment for adding a task
    public static TaskBottomSheetFragment newInstance() {
        return new TaskBottomSheetFragment();
    }

    // Method to create a new instance for editing a task
    public static TaskBottomSheetFragment newInstance(Task task) {
        TaskBottomSheetFragment fragment = new TaskBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("taskId", task.getId());
        args.putString("taskTitle", task.getTitle());
        args.putString("taskDescription", task.getDescription());
        args.putString("taskPriority", task.getPriority());
        args.putString("taskDate", task.getDate());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if we are editing a task
        if (getArguments() != null) {
            taskId = getArguments().getString("taskId");
            taskInputTitle.setText(getArguments().getString("taskTitle"));
            taskDescription.setText(getArguments().getString("taskDescription"));
            selectedPriority = getArguments().getString("taskPriority");
            selectedDate = getArguments().getString("taskDate");
            taskPriorityText.setText(selectedPriority); // Show selected priority
            taskPriorityIcon.setVisibility(View.VISIBLE); // Show the icon
            taskPriorityText.setVisibility(View.VISIBLE); // Show the text

            // Pre-fill the date if available
            if (!selectedDate.isEmpty()) {
                btnDate.setText(selectedDate); // Set the date button text to the selected date
            }
        }
    }

    private void showPrioritySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.priority_selection_modal, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        ImageButton backButton = dialogView.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.priority1).setOnClickListener(v -> {
            selectedPriority = "High Priority";
            updatePriorityIcon();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.priority2).setOnClickListener(v -> {
            selectedPriority = "Medium Priority";
            updatePriorityIcon();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.priority3).setOnClickListener(v -> {
            selectedPriority = "Low Priority";
            updatePriorityIcon();
            dialog.dismiss();
        });
    }

    private void updatePriorityIcon() {
        taskPriorityIcon.setImageResource(R.drawable.flag_24px); // Update the icon (you can customize based on priority)
        taskPriorityText.setText(selectedPriority); // Update the text
        taskPriorityIcon.setVisibility(View.VISIBLE); // Show the icon
        taskPriorityText.setVisibility(View.VISIBLE); // Show the text
    }

    private void showCategorySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Category")
                .setItems(new String[]{"Personal", "School", "Healthcare"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            selectedCategory = "Personal";
                            break;
                        case 1:
                            selectedCategory = "School";
                            break;
                        case 2:
                            selectedCategory = "Healthcare";
                            break;
                    }
                    btnCategory.setText(selectedCategory); // Update the button text
                })
                .show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = dateFormat.format(calendar.getTime());
                    // Update the button text with the selected date
                    btnDate.setText(selectedDate); // Use btnDate here
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveTaskToFirebase(String taskTitle, String taskText) {
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username");

        // Reference to the global "tasks" node
        DatabaseReference globalTasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        // Reference to the user's "tasks" node
        DatabaseReference userTasksRef = FirebaseDatabase.getInstance().getReference("users").child(loggedInUsername).child("tasks");

        // Generate a unique task ID
        String taskId = globalTasksRef.push().getKey();

        // Create a map to store task data
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", taskId);
        taskMap.put("title", taskTitle);
        taskMap.put("description", taskText);
        taskMap.put("priority", selectedPriority);
        taskMap.put("date", selectedDate);
        taskMap.put("status", "In Progress");  // Initial status
        taskMap.put("username", loggedInUsername);
        taskMap.put("category", selectedCategory);

        if (taskId != null) {
            // Save the task to the global "tasks" table
            globalTasksRef.child(taskId).setValue(taskMap)
                    .addOnSuccessListener(aVoid -> {
                        // Save the task to the user's specific "tasks" table
                        userTasksRef.child(taskId).setValue(taskMap)
                                .addOnSuccessListener(aVoid2 -> {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                                        // Notify listener about the new task
                                        if (taskListener != null) {
                                            taskListener.onTaskAdded(taskTitle, taskText, selectedPriority, selectedDate);
                                        }
                                        // Update the "Complete" button visibility based on task status
                                        updateCompleteButtonVisibility("In Progress");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Failed to store task in user's table", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Failed to add task to global table", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateCompleteButtonVisibility(String taskStatus) {
        Button completeButton = getView().findViewById(R.id.btnComplete);  // Replace with actual button ID
        if (completeButton != null) {
            if ("In Progress".equals(taskStatus)) {
                completeButton.setVisibility(View.VISIBLE);
            } else {
                completeButton.setVisibility(View.GONE);
            }
        }
    }



    private void editTaskToFirebase(String taskTitle, String taskText) {
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username"); // Get logged-in username
        // Reference to the user's tasks
        DatabaseReference userTasksRef = FirebaseDatabase.getInstance().getReference("users").child(loggedInUsername).child("tasks");

        // Prepare the task data
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", taskTitle);
        taskMap.put("description", taskText);
        taskMap.put("priority", selectedPriority);
        taskMap.put("date", selectedDate);
        taskMap.put("category", selectedCategory);

        // Update the task data in the user's tasks node using the taskId
        if (taskId != null) {
            userTasksRef.child(taskId).updateChildren(taskMap)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
                            // Notify listener about the updated task
                            if (taskListener != null) {
                                taskListener.onTaskUpdated(taskId, taskTitle, taskText, selectedPriority, selectedDate);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void setTaskListener(TaskListener listener) {
        this.taskListener = listener; // Set the task listener
    }
}
