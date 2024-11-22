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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Task> tasks;
    private OnTaskInteractionListener listener;

    public HistoryAdapter(List<Task> tasks, OnTaskInteractionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom history_item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_task, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Task task = tasks.get(position);

        // Bind task data to the views
        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskPriority.setText(task.getPriority());
        holder.taskDate.setText(task.getDate());
        holder.taskStatus.setText(task.getStatus());

        // Optionally, set up interaction handlers like Update Status or Edit
        holder.itemView.setOnClickListener(v -> listener.onUpdateTaskStatus(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // Update the list of tasks
    public void updateTaskList(List<Task> updatedTasks) {
        this.tasks = updatedTasks;
        notifyDataSetChanged();
    }

    // ViewHolder to hold references to the views
    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskPriority, taskDate, taskStatus;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskStatus = itemView.findViewById(R.id.taskStatus);
        }
    }

    // Listener interface to handle task interactions
    public interface OnTaskInteractionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
        void onUpdateTaskStatus(Task task);
    }
}
