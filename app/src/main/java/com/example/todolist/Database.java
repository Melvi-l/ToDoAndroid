package com.example.todolist;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static android.provider.Telephony.Mms.Part.FILENAME;

public class Database {


    private DatabaseHelper dbHelper;

    public Database(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void exportToFile(String filename, String content, File directory) {
        try {
            File file = new File(directory, filename);
            FileWriter fw = new FileWriter(file);
            PrintWriter writer = new PrintWriter(fw);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(content);
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
        }
    }

    public String importFromFile(String filename, File directory) {
        String line="";
        String content = "";
        try {
            File file = new File(directory, filename);
            FileReader fr= new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                content += line + "\n";
                Log.e("PERSIST", line);
            }
            br.close();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
        }

        return content;
    }

    private void createTodo(Todo todo) {
        SQLiteDatabase db= dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", todo.getTitle());
        Log.e("PERSIST CREATE", todo.getTitle());
        db.insert("todos", null , values);

        db.close();
    }

    private boolean updateTodo(Todo todo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", todo.getTitle());
        Log.e("PERSIST UPDATE", todo.getTitle());
        int n = db.update("todos", values, "id=?", new String[]{String.valueOf(todo.getId())});

        return 0 < n;
    }

    public void persistTodo(Todo todo) {
        if (!updateTodo(todo) && todo.getId()==-1) {
            createTodo(todo);
        }
    }
    
    public void persistTodoList(List<Todo> todoList) {
        for (Todo todo: todoList) {
            persistTodo(todo);
        }
    }

    public void removeTodo(Todo todo) {
        SQLiteDatabase db= dbHelper.getWritableDatabase();
        db.delete("todos", "id=?", new String[]{String.valueOf(todo.getId())});
    }

    public ArrayList<Todo> getAllTodos() {
        ArrayList<Todo> todoList = new ArrayList<Todo>();

        SQLiteDatabase db= dbHelper.getWritableDatabase();

        String[] projection = {
                "id",
                "title"
        };

        Log.e("PERSIST", "get that shit");
        Cursor cursor = db.query("todos", projection, null, null, null, null, null);

        Log.e("PERSIST", String.valueOf(cursor.getCount()));
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            Log.e("PERSIST", title);
            Todo todo = new Todo(id, title);
            todoList.add(todo);
        }

        cursor.close();
        db.close();

        Log.e("PERSIST", String.valueOf(todoList.size()));
        return todoList;
    }

}
