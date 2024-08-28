package com.example.ericbuchananproject2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    public interface ItemClickListener {
        void onDeleteClick(int position);
    }

    private final List<GridItem> gridData;
    private final ItemClickListener itemClickListener;

    public GridAdapter(List<GridItem> gridData, ItemClickListener itemClickListener) {
        this.gridData = gridData;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_layout, parent, false);
        return new ViewHolder(view);
    }
    public void updateData(List<GridItem> newData) {
        gridData.clear();
        gridData.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull GridAdapter.ViewHolder holder, int position) {
        GridItem item = gridData.get(position);

        // Set the text for the title and detail views
        holder.titleTextView.setText(item.getName());  // Sets the name of the item
        holder.detailTextView.setText(String.valueOf(item.getQuantity()));  // Converts quantity to String

        // Set the click listener for the delete button
        holder.delete_entry.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if(adapterPosition != RecyclerView.NO_POSITION) {
                itemClickListener.onDeleteClick(adapterPosition);
            }
        });
    }



    @Override
    public int getItemCount() {
        return gridData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView detailTextView;
        Button delete_entry;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.date_text_view);
            detailTextView = itemView.findViewById(R.id.weight_text_view);
            delete_entry = itemView.findViewById(R.id.delete_entry);
        }
    }
}
