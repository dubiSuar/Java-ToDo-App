package com.example.taskly;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
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
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskPriority, taskDate, btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskDate = itemView.findViewById(R.id.taskDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Interface to handle Edit and Delete actions
    public interface OnTaskInteractionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }
}
