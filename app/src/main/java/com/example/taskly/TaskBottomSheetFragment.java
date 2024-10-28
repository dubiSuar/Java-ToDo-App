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
    }

    private DatabaseReference tasksRef;
    private String selectedPriority = "Low"; // Default priority
    private String selectedDate = ""; // Default empty date
    private TaskListener taskListener; // Declare the listener
    private ImageView taskPriorityIcon; // Reference to the priority icon ImageView
    private TextView taskPriorityText; // Reference to the priority text View

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_task, container, false);

        // Initialize your views
        Button btnSaveTask = view.findViewById(R.id.btnSaveTask);
        EditText taskInputTitle = view.findViewById(R.id.taskInputTitle);
        EditText taskDescription = view.findViewById(R.id.taskDescription);
        Button btnPriority = view.findViewById(R.id.btnPriority);
        Button btnDate = view.findViewById(R.id.btnDate);
        taskPriorityIcon = view.findViewById(R.id.taskPriorityIcon); // Initialize the ImageView
        taskPriorityText = view.findViewById(R.id.taskPriorityText); // Initialize the TextView

        // Initially hide the priority icon and text
        taskPriorityIcon.setVisibility(View.GONE);
        taskPriorityText.setVisibility(View.GONE);

        // Initialize Firebase Database reference
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        btnSaveTask.setOnClickListener(v -> {
            String taskTitle = taskInputTitle.getText().toString();
            String taskText = taskDescription.getText().toString();

            if (TextUtils.isEmpty(taskTitle)) {
                Toast.makeText(getContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
            } else {
                saveTaskToFirebase(taskTitle, taskText);
                dismiss(); // Close the bottom sheet
            }
        });

        btnPriority.setOnClickListener(v -> showPrioritySelectionDialog());

        btnDate.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    // Setter for the task listener
    public void setTaskListener(TaskListener listener) {
        this.taskListener = listener;
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
            selectedPriority = "High";
            taskPriorityIcon.setImageResource(R.drawable.flag_24px); // Update the icon
            taskPriorityText.setText(selectedPriority); // Update the text
            taskPriorityIcon.setVisibility(View.VISIBLE); // Show the icon
            taskPriorityText.setVisibility(View.VISIBLE); // Show the text
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.priority2).setOnClickListener(v -> {
            selectedPriority = "Medium";
            taskPriorityIcon.setImageResource(R.drawable.flag_24px); // Update the icon
            taskPriorityText.setText(selectedPriority); // Update the text
            taskPriorityIcon.setVisibility(View.VISIBLE); // Show the icon
            taskPriorityText.setVisibility(View.VISIBLE); // Show the text
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.priority3).setOnClickListener(v -> {
            selectedPriority = "Low";
            taskPriorityIcon.setImageResource(R.drawable.flag_24px); // Update the icon
            taskPriorityText.setText(selectedPriority); // Update the text
            taskPriorityIcon.setVisibility(View.VISIBLE); // Show the icon
            taskPriorityText.setVisibility(View.VISIBLE); // Show the text
            dialog.dismiss();
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = dateFormat.format(calendar.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveTaskToFirebase(String taskTitle, String taskText) {
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username"); // Get logged-in username
        DatabaseReference userTasksRef = FirebaseDatabase.getInstance().getReference("users").child(loggedInUsername).child("tasks");
        String taskId = userTasksRef.push().getKey();

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", taskId);
        taskMap.put("title", taskTitle);
        taskMap.put("description", taskText);
        taskMap.put("priority", selectedPriority);
        taskMap.put("date", selectedDate);

        if (taskId != null) {
            userTasksRef.child(taskId).setValue(taskMap)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                            // Notify listener about the new task
                            if (taskListener != null) {
                                taskListener.onTaskAdded(taskTitle, taskText, selectedPriority, selectedDate);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
