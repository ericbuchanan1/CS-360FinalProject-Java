package com.example.ericbuchananproject2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.security.Permissions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class homePage extends Activity {
    // Declare UI components
    private Button addItem;
    private EditText addWeight;
    private Button setGoal;
    private DatabaseHelper dbHelper;
    private GridAdapter adapter;
    private RecyclerView recyclerView;
    private TextView viewGoalWeight;
    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // Initialize UI components
        addItem = findViewById(R.id.addItem);
        addWeight = findViewById(R.id.addDetail);
        dbHelper = new DatabaseHelper(this);
        setGoal = findViewById(R.id.setGoal);
        viewGoalWeight = findViewById(R.id.viewGoalWeight);

        // Set up bottom navigation
        setupBottomNavigation();

        // Initialize RecyclerView and adapter
        setupRecyclerView();

        // Add item button click listener
        setupAddItemButton();
        // Set Goal button click listener
        setupSetGoalButton();
        updateGoalWeightView();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.database);

        // Set the listener for item selection
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.permissions) {
                startActivity(new Intent(homePage.this, Permissions.class));
                return true;
            } else if (itemId == R.id.graph_display) {
                startActivity(new Intent(homePage.this, GraphDisplay.class));
                return true;
            }
            return false;
        });
    }


    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.data_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1)); // Grid layout with 1 column
        refreshItemsList();
    }

    private void setupAddItemButton() {
        addItem.setOnClickListener(v -> {
            String date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
            String detail = addWeight.getText().toString();

            if (detail.isEmpty()) {
                Toast.makeText(homePage.this, "Please add a weight", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int weight = Integer.parseInt(detail);
                    int userId = dbHelper.getCurrentUserId();
                    dbHelper.addDailyWeight(date, weight); // Add to database
                    refreshItemsList(); // Refresh the RecyclerView with updated data

                    // Check if the last weight matches the goal
                    int lastGoal = dbHelper.getLastGoalByUserId(userId);
                    if (weight == lastGoal) {
                        sendGoalAchievedMessage(); // Send SMS if goal is achieved
                    }

                } catch (NumberFormatException e) {
                    Toast.makeText(homePage.this, "Please enter a valid number for weight", Toast.LENGTH_SHORT).show();
                }
                addWeight.setText(""); // Clear the input field
            }
        });
    }

    private void sendGoalAchievedMessage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            String phoneNumber = getMyPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, "Congratulations on reaching your goal weight!", null, null);
                Toast.makeText(this, "SMS sent to your phone number.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Phone number not available or invalid.", Toast.LENGTH_LONG).show();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        }
    }


    private String getMyPhoneNumber() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permissions or handle the lack of permission appropriately
            return null;
        }
        return telephonyManager.getLine1Number();
    }

    private void updateGoalWeightView() {
        int userId = dbHelper.getCurrentUserId(); // Get the current user ID
        if (userId != -1) {
            int lastGoal = dbHelper.getLastGoalByUserId(userId); // Update this method to return an int
            if (lastGoal != -1) { // Check if a goal has been set
                viewGoalWeight.setText(String.format(Locale.getDefault(), "Goal Weight: %d", lastGoal));
            } else {
                viewGoalWeight.setText("Enter a goal weight!"); // No goal has been set
            }
        } else {
            viewGoalWeight.setText("No user logged in.");
        }
    }

    private void setupSetGoalButton() {
        setGoal.setOnClickListener(v -> {
            String goalWeightStr = addWeight.getText().toString();
            if (goalWeightStr.isEmpty()) {
                Toast.makeText(homePage.this, "Please enter a goal weight", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int goalWeight = Integer.parseInt(goalWeightStr);
                    int userId = dbHelper.getCurrentUserId();
                    if (userId != -1) {
                        dbHelper.addGoal(userId, goalWeight);
                        Toast.makeText(homePage.this, "Goal set successfully!", Toast.LENGTH_SHORT).show();
                        updateGoalWeightView(); // Update the TextView immediately after setting a new goal
                    } else {
                        Toast.makeText(homePage.this, "No user logged in", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(homePage.this, "Please enter a valid number for the goal weight", Toast.LENGTH_SHORT).show();
                }
                addWeight.setText(""); // Clear the input field
            }
        });
    }

    private void refreshItemsList() {
        int userID = dbHelper.getCurrentUserId(); // Get the current user's ID
        if (userID != -1) {
            List<GridItem> myData = dbHelper.getItemsByUser(userID); // Load data specific to the current user
            if (adapter == null) {
                adapter = new GridAdapter(myData, position -> {
                    GridItem itemToRemove = myData.get(position);
                    dbHelper.deleteItem(itemToRemove.getId()); // Delete the item from the database
                    myData.remove(position); // Remove the item from the list
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, myData.size());
                });
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(myData); // Assuming GridAdapter has an updateData method
            }
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_LONG).show();
        }
    }
}
