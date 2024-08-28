package com.example.ericbuchananproject2;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class permissions extends Activity {
    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permissions);

        Button buttonAskPermissions = findViewById(R.id.button_ask_permissions);
        buttonAskPermissions.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE);
            } else {
                sendSmsAlert();
            }
        });

        setupBottomNavigation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSmsAlert();  // Permission was granted, continue with sending SMS
            } else {
                Toast.makeText(this, "SMS permission denied. Cannot send SMS alerts.", Toast.LENGTH_SHORT).show();
                // Continue with other functionalities of your app that do not require this permission
            }
        }
    }

    private void sendSmsAlert() {
        // Simulate sending an SMS. This is where you would handle SMS sending if the app had the capability.
        Toast.makeText(this, "Sending SMS...", Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.permissions);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.database) {
                startActivity(new Intent(permissions.this, homePage.class));
                return true;
            } else if (itemId == R.id.graph_display) {
                startActivity(new Intent(permissions.this, GraphDisplay.class));
                return true;
            }
            return false;
        });
    }
}
