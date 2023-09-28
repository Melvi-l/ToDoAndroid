package com.example.todolist;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

public class CreateTodoDialog extends AppCompatDialogFragment {

    private EditText editTextTitle;
    private Button buttonCreate;
    private CreateTodoDialogListener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_create_todo, null);

        editTextTitle = view.findViewById(R.id.editTextTitle);

        builder.setView(view)
                .setTitle("Add a Todo")
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("Create", (dialogInterface, i) -> {
                    String title = editTextTitle.getText().toString();
                    listener.addTodo(title);
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (CreateTodoDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CreateTodoDialogListener");
        }
    }

    public interface CreateTodoDialogListener {
        void addTodo(String title);
    }
}