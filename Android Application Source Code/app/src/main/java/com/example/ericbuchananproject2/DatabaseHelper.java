package com.example.ericbuchananproject2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AppDatabase.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL UNIQUE," +
                        "password TEXT NOT NULL);"
        );

        // Create Items Table with a Foreign Key
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "dates TEXT NOT NULL," +
                        "weights TEXT NOT NULL," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);"
        );

        // Create Goal Table with a Foreign Key
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS goal (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "goal INTEGER NOT NULL," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);"
        );

        // Create Session Table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS session (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE SET NULL);"
        );
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS goal");
        onCreate(db);
    }



    // CRUD Operations for items
    public void addDailyWeight(String date, int weight) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            // Handle the case where no user is logged in
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("dates", date);
        values.put("weights", weight);
        db.insert("items", null, values);
        db.close();
    }

    public void addGoal(int goal) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            // Handle the case where no user is logged in
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("goal", goal);
        db.insert("goal", null, values);
        db.close();
    }


    public List<GridItem> getItemsByUser(int userId) {
        List<GridItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, dates, weights FROM items WHERE user_id = ?", new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            items.add(new GridItem(cursor.getInt(0), cursor.getString(1), cursor.getInt(2)));
        }
        cursor.close();
        db.close();
        return items;
    }


    // Method to set the current user ID
    public void setCurrentUserId(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        db.delete("session", null, null); // Clear previous sessions
        db.insert("session", null, values); // Insert new session data
        db.close();
    }

    // Method to get the current user ID
    public int getCurrentUserId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM session LIMIT 1", null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex("user_id"));
            }
            return -1; // Return -1 if no session is found
        } finally {
            cursor.close();
            db.close();
        }
    }


    public List<GridItem> getItems() {
        List<GridItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, dates, weights FROM items", null);
        if (cursor.moveToFirst()) {
            do {
                GridItem item = new GridItem(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }
    public List<String> getDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT dates FROM items ORDER BY id ASC", null);
        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dates;
    }


    public void updateItem(int id, String name, int weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dates", name);
        values.put("weights", weight);
        db.update("items", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getLastItemId() {
        SQLiteDatabase db = this.getReadableDatabase();
        int itemId = -1; // Initialize to -1 to indicate no item found
        Cursor cursor = db.rawQuery("SELECT id FROM items ORDER BY id DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            itemId = cursor.getInt(0); // Get the first column's value
        }
        cursor.close();
        db.close();
        return itemId;
    }

    // Goal CRUD Methods
    public void addGoal(int userId, int goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("goal", goal);
        db.insert("goal", null, values);
        db.close();
    }

    public GridItem getLastGoal(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, goal FROM goal WHERE user_id = ? ORDER BY id DESC LIMIT 1", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            GridItem item = new GridItem(cursor.getInt(0), null, cursor.getInt(1)); // Assuming GridItem(id, times, goal)
            cursor.close();
            db.close();
            return item;
        }
        cursor.close();
        db.close();
        return null;  // Return null if no goal found
    }

    // In DatabaseHelper class
    public int getLastGoalByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT goal FROM goal WHERE user_id = ? ORDER BY id DESC LIMIT 1", new String[]{String.valueOf(userId)});
        int goal = -1; // Initialize to -1 to indicate no goal was found
        if (cursor.moveToFirst()) {
            goal = cursor.getInt(0); // Assume the goal is the first column in the cursor
        }
        cursor.close();
        db.close();
        return goal; // Return the last goal or -1 if not found
    }




    public void deleteGoal(int userId, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("goal", "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)});
        db.close();
    }

    public List<GridItem> getAllGoals() {
        List<GridItem> goals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, goal, times FROM goal", null);
        while (cursor.moveToNext()) {
            goals.add(new GridItem(cursor.getInt(0), cursor.getString(2), cursor.getInt(1)));  // Assuming GridItem(id, times, goal)
        }
        cursor.close();
        db.close();
        return goals;
    }


}
