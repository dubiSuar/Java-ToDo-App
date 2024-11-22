package com.example.taskly;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskInteractionListener taskInteractionListener;

    // Constructor to initialize task list and interaction listener
    public TaskAdapter(List<Task> taskList, OnTaskInteractionListener taskInteractionListener) {
        this.taskList = taskList;
        this.taskInteractionListener = taskInteractionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);



        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskDate.setText(task.getDate());
        holder.taskStatus.setText(task.getStatus());

        if (task.getStatus().equals("Completed")) {
            holder.btnComplete.setVisibility(View.GONE); // Hide button if completed
        }
        // Set priority color based on the priority level
        switch (task.getPriority()) {
            case "High Priority":
                holder.taskPriority.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.priority_high));
                break;
            case "Medium Priority":
                holder.taskPriority.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.priority_medium));
                break;
            case "Low Priority":
                holder.taskPriority.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.priority_low));
                break;
        }
        holder.taskPriority.setText(task.getPriority());

        // Set up Edit button click listener
        holder.btnEdit.setOnClickListener(v -> {
            if (taskInteractionListener != null) {
                taskInteractionListener.onEditTask(task);
            }
        });

        // Set up Delete button click listener with confirmation dialog
        holder.btnDelete.setOnClickListener(v -> {
            if (taskInteractionListener != null) {
                // Create confirmation dialog
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            taskInteractionListener.onDeleteTask(task); // Proceed with deletion
                        })
                        .setNegativeButton("No", null) // Do nothing if "No" is clicked
                        .show();
            }
        });

        holder.btnComplete.setOnClickListener(v -> {
            // Update the task status locally
            task.setStatus("Completed");
            holder.taskStatus.setText("Status: Completed");
            holder.btnComplete.setVisibility(View.GONE); // Hide the Complete button

            // Update the status in Firebase
            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(task.getId());
            taskRef.child("status").setValue("Completed")
                    .addOnSuccessListener(aVoid -> {
                        // Notify interaction listener (optional)
                        if (taskInteractionListener != null) {
                            taskInteractionListener.onUpdateTaskStatus(task); // Notify listener if required
                        }
                        // Show feedback
                        Toast.makeText(holder.itemView.getContext(), "Task marked as Completed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to update status in Firebase
                        Toast.makeText(holder.itemView.getContext(), "Failed to update task status", Toast.LENGTH_SHORT).show();
                    });

            // Update user-specific task node (optional if using user-specific tasks)
            DatabaseReference userTaskRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(task.getUsername())
                    .child("tasks")
                    .child(task.getId());
            userTaskRef.child("status").setValue("Completed");
        });


    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Method to update the task list dynamically
    public void updateTaskList(List<Task> updatedTaskList) {
        this.taskList = updatedTaskList;
        notifyDataSetChanged();  // Notify the adapter that the dataset has changed
    }

    // Method to filter tasks by date
    public void filterTasksByDate(String selectedDate) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getDate().equals(selectedDate)) {
                filteredList.add(task);  // Add tasks with matching date
            }
        }
        updateTaskList(filteredList);  // Update the list with the filtered tasks
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskPriority, taskDate, btnEdit, btnDelete, taskStatus, btnComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskDate = itemView.findViewById(R.id.taskDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }

    // Interface to handle Edit and Delete actions
    public interface OnTaskInteractionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
        void onUpdateTaskStatus(Task task);
    }
}
