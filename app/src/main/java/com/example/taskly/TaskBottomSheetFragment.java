package com.example.taskly;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Ensure this import is included
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TaskBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet_task, container, false);

        // Initialize views
        Button btnSaveTask = view.findViewById(R.id.btnSaveTask);
        EditText taskDescription = view.findViewById(R.id.taskDescription);
        Button btnPriority = view.findViewById(R.id.btnPriority); // Ensure this is a Button

        // Set click listener for the save button
        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the save task logic here
                String taskText = taskDescription.getText().toString();
                // Save taskText to your data source

                dismiss(); // Close the bottom sheet
            }
        });

        // Set click listener for the priority button
        btnPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the priority selection dialog
                showPrioritySelectionDialog();
            }
        });

        return view;
    }

    // Method to display the priority selection dialog
    private void showPrioritySelectionDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the priority selection layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.priority_selection_modal, null);
        builder.setView(dialogView);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Back button to close the dialog
        ImageButton backButton = dialogView.findViewById(R.id.backButton); // Use ImageButton
        backButton.setOnClickListener(v -> dialog.dismiss());

        // Priority buttons with click listeners
        dialogView.findViewById(R.id.priority1).setOnClickListener(v -> {
            // Handle Priority 1 selection
            dialog.dismiss();
            // Additional logic for Priority 1
        });

        dialogView.findViewById(R.id.priority2).setOnClickListener(v -> {
            // Handle Priority 2 selection
            dialog.dismiss();
            // Additional logic for Priority 2
        });

        dialogView.findViewById(R.id.priority3).setOnClickListener(v -> {
            // Handle Priority 3 selection
            dialog.dismiss();
            // Additional logic for Priority 3
        });
    }
}
