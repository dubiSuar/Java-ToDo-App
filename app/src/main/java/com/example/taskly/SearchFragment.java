package com.example.taskly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchBar;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseReference tasksRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchBar = view.findViewById(R.id.search_bar);
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, null);  // TaskAdapter without listener for now
        recyclerViewTasks.setAdapter(taskAdapter);

        // Initialize Firebase Database reference
        String loggedInUsername = requireActivity().getIntent().getStringExtra("username");
        tasksRef = FirebaseDatabase.getInstance().getReference("users").child(loggedInUsername).child("tasks");

        // Fetch all tasks for the user from Firebase
        fetchTasksFromFirebase();

        // Set up search functionality
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not needed for this functionality
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterTasks(charSequence.toString());  // Filter tasks based on the text entered
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {
                // Not needed for this functionality
            }
        });

        return view;
    }

    // Fetch tasks from Firebase and store them in taskList
    private void fetchTasksFromFirebase() {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tasks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filter tasks based on the search query
    private void filterTasks(String query) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(task);
            }
        }
        taskAdapter.updateTaskList(filteredList);  // Update the adapter with the filtered list
    }
}
