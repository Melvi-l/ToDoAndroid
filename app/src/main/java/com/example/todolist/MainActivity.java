package com.example.todolist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements CreateTodoDialog.CreateTodoDialogListener, EditNameDialog.EditNameDialogListener {
    private Database database;
    private ArrayList<Todo> todoList;
    private TodoListAdapter adapter;
    private ListView listView;

    private String username = "Toolbar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fetchUsername();
        setUsername();

        database = new Database(this);
        todoList = database.getAllTodos();

        initalizeListView();

    }
    public void initalizeListView() {
        adapter = new TodoListAdapter(this, todoList);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Todo todo = todoList.get(position);
            deleteTodo(todo);
            Log.e("DELETE_TODO", "delete todo ("+ todo.toString() + "), remaining todoList: "+ todoList.toString());
            adapter.notifyDataSetChanged();

            Toast.makeText(MainActivity.this, todo.getTitle() + " deleted !", Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.edit_name) {
            EditNameDialog editNameDialog = new EditNameDialog(username);
            editNameDialog.show(getSupportFragmentManager(), "edit_name_dialog");
            return true;
        }
        if(item.getItemId() == R.id.save) {
            save();
            return true;
        }
        if(item.getItemId() == R.id.load_save) {
            loadSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setUsername() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(username);
    }

    private void fetchUsername() {
        username = getSharePreferences("username");
    }
    private void saveUsername() {
        setSharePreferences("username", username);
    }

    @Override
    public void editName(String name) {
        username = name;
        setUsername();
        saveUsername();
    }
    public void askReadPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
    public void askWritePermission() {
        requestPermissions(new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        },2);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                File directory = getExternalFilesDir(null);
                chooseFile(directory);
            } else {
                Toast.makeText(this, "Read permission is needed to store data", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                // File directory = getFilesDir();
                File directory = getExternalFilesDir(null);
                saveTodoListAsFile(directory);
            } else {
                Toast.makeText(this, "Write permission is needed to store data", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.persistTodoList(todoList);
    }


    public void onClick(View view) {
        CreateTodoDialog createTodoDialog = new CreateTodoDialog();
        createTodoDialog.show(getSupportFragmentManager(), "create_todo_dialog");
    }

    public void addTodo(String title) {
        Todo todo = new Todo(title);
        adapter.add(todo);
        database.persistTodo(todo);
    }

    public void deleteTodo(Todo todo) {
        adapter.remove(todo);
        database.removeTodo(todo);
    }

    public void setSharePreferences(String key, String value) {
        SharedPreferences sp= getApplicationContext().getSharedPreferences("todo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public String getSharePreferences(String key) {
        SharedPreferences sp= getApplicationContext().getSharedPreferences("todo", Context.MODE_PRIVATE);
        String value = sp.getString(key,"unknown");
        return value;
    }

    public void loadSave() { askReadPermission(); }
    public void chooseFile(File directory) {
        File[] files = directory.listFiles();
        final String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a save");
        builder.setItems(fileNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedFileName = fileNames[which];
                loadFileToTodoList(selectedFileName, directory);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public void loadFileToTodoList(String fileName, File directory) {
        Log.e("SELECTED_FILE", fileName);
        String todoListString = database.importFromFile(fileName, directory);
        Log.e("TODOLIST_STRING", todoListString);
        todoList = TodoListFromString(todoListString);
        Log.e("NEW_TODOLIST", todoList.toString());
        initalizeListView();
    }
    public ArrayList<Todo> TodoListFromString(String input) {
        ArrayList<Todo> todoList = new ArrayList<Todo>();
        String[] lines = input.split("\n");
        for (String line : lines) {
            Todo todo = Todo.fromString(line);
            if (todo != null) {
                todoList.add(todo);
            }
        }
        return todoList;
    }

    public void save() {
        askWritePermission();
    }
    private void saveTodoListAsFile(File directory) {
        Log.e("FOLDER", directory.toString());
        // Clamp at 5 save
        File[] fileList;
        while ((fileList = directory.listFiles()).length > 4) {
            File fileToDelete = fileList[0];
            if (fileToDelete.delete()) {
                Log.e("REMOVE_SAVE", fileToDelete.getName() + " deleted !");
            }
        }
        String filename = username.toLowerCase() + "_todo_" + new SimpleDateFormat("HH:mm:ss_dd-MM-yyyy").format(new Date());
        String todoListString = TodoListToString();
        Log.e("FOLDER", todoListString);
        database.exportToFile(filename, todoListString, directory);
        Toast.makeText(this, "Save as " + filename, Toast.LENGTH_SHORT).show();
    }
    public String TodoListToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Todo todo : todoList) {
            stringBuilder.append(todo.toString()).append("\n");
        }
        return stringBuilder.toString();
    }


    //private ActivityResultLauncher<Intent> getFilename = registerForActivityResult(
    //        new ActivityResultContracts.StartActivityForResult(),
    //        result -> {
    //            result.getResultCode();
    //            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
    //                Uri uri = result.getData().getData();
    //                String displayName = null;
    //                if (uri.getScheme().equals("content")) {
    //                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
    //                    if (cursor != null && cursor.moveToFirst()) {
    //                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
    //                        if (nameIndex != -1) {
    //                            displayName = cursor.getString(nameIndex);
    //                        }
    //                        cursor.close();
    //                    }
    //                }
    //                if (displayName == null) {
    //                    displayName = uri.getLastPathSegment();
    //                }
    //                Log.e("FILENAME", displayName);
    //            }
    //        });


}

//Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//intent.addCategory(Intent.CATEGORY_OPENABLE);
//intent.setType("*/*");
//getFilename.launch(intent);
// startActivityForResult(intent, PICK_FILE_REQUEST);
// database.importFromFile("", directory);