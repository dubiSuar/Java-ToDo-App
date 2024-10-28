package com.example.taskly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
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
            // No default case needed
        }

        holder.taskPriority.setText(task.getPriority());
    }



    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskPriority, taskDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskDate = itemView.findViewById(R.id.taskDate);
        }
    }
}
