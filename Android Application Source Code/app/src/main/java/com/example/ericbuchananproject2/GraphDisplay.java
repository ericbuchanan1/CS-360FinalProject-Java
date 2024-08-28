package com.example.ericbuchananproject2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class GraphDisplay extends Activity {
    private BarChart chart;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_display);
        dbHelper = new DatabaseHelper(this);
        BarChart chart = findViewById(R.id.weight_graph);
        int currentUserId = dbHelper.getCurrentUserId();

        // Fetch data from database and prepare the chart
        List<BarEntry> entries = getItemData(currentUserId);
        BarDataSet dataSet = new BarDataSet(entries, "Weight");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        BarData BarData = new BarData(dataSet);
        chart.setData(BarData);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(getDates(currentUserId)));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.animateX(100);


        chart.invalidate(); // refresh the chart



        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.graph_display);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.database) {
                Intent intent = new Intent(GraphDisplay.this, homePage.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.permissions) {
                Intent intent = new Intent(GraphDisplay.this, permissions.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
    public List<BarEntry> getItemData(int userId) {
        List<GridItem> items = dbHelper.getItemsByUser(userId);
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            // Assuming the X-axis is item IDs or sequential numbers
            entries.add(new BarEntry(i, items.get(i).getQuantity()));
        }

        return entries;
    }

    public List<String> getDates(int userId) {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT dates FROM items WHERE user_id = ? ORDER BY id ASC", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(0)); // Fetch dates only
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dates;
    }

}
