package com.example.ericbuchananproject2;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class MainActivity extends AppCompatActivity {

    private EditText usernameAttempt, passwordAttempt;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);

        Button attemptRegister = findViewById(R.id.goToRegister);
        Button attemptLogin = findViewById(R.id.login);
        usernameAttempt = findViewById(R.id.editTextEnterUsername);
        passwordAttempt = findViewById(R.id.editTextEnterPassword);

        attemptRegister.setOnClickListener(v -> registerUser());
        attemptLogin.setOnClickListener(v -> loginUser());
    }

    private void registerUser() {
        String username = usernameAttempt.getText().toString();
        String password = passwordAttempt.getText().toString();

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            try (Cursor cursor = db.rawQuery("SELECT username FROM users WHERE username = ?", new String[]{username})) {
                if (cursor.moveToFirst()) {
                    Toast.makeText(MainActivity.this, "Username has been taken.", Toast.LENGTH_SHORT).show();
                } else {
                    // Insert new user and retrieve the new user ID
                    db.execSQL("INSERT INTO users (username, password) VALUES (?, ?)", new Object[]{username, password});
                    try (Cursor idCursor = db.rawQuery("SELECT last_insert_rowid()", null)) {
                        if (idCursor.moveToFirst()) {
                            int userId = idCursor.getInt(0);
                            dbHelper.setCurrentUserId(userId);
                            Intent intent = new Intent(MainActivity.this, homePage.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        } catch (SQLiteException e) {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
        }
    }



    private void loginUser() {
        String username = usernameAttempt.getText().toString();
        String password = passwordAttempt.getText().toString();

        // Get the database
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            // Query to select the 'id' from the database where username and password match
            try (Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ? AND password = ?", new String[]{username, password})) {
                if (cursor.moveToFirst()) {
                    int userIdColumnIndex = cursor.getColumnIndex("id"); // Get the column index for 'id'
                    if (userIdColumnIndex != -1) {
                        int userId = cursor.getInt(userIdColumnIndex); // Retrieve user ID from the cursor
                        dbHelper.setCurrentUserId(userId); // Set the user ID in session
                        Intent intent = new Intent(MainActivity.this, homePage.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "User ID not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
            // Ensure cursor is closed after operation
        } catch (SQLiteException e) {
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        // Ensure database is closed to avoid memory leaks
    }


}
