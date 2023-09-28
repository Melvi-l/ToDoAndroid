package com.example.todolist;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

public class EditNameDialog extends AppCompatDialogFragment {

    private EditText editTextTitle;
    private Button buttonCreate;
    private EditNameDialogListener listener;
    private String oldName;

    public EditNameDialog(String oldName) {
        this.oldName = oldName;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_name, null);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextTitle.setText(oldName);

        builder.setView(view)
                .setTitle("Edit name")
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("Edit", (dialogInterface, i) -> {
                    String name = editTextTitle.getText().toString();
                    listener.editName(name);
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (EditNameDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CreateTodoDialogListener");
        }
    }

    public interface EditNameDialogListener {
        void editName(String name);
    }
}