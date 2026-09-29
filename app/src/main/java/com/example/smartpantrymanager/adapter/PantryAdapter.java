package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

/**
 * Binds the list of pantry items to a RecyclerView on the Pantry List screen.
 * Tapping a row opens it for editing; the overflow/delete button removes it.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {


    private List<PantryItem> items;

    public PantryAdapter(List<PantryItem> items) {
        this.items = items;
    }

    public void updateItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.name.setText(item.getName());

        String qty = trimTrailingZero(item.getQuantity()) + " " + item.getUnit();
        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            qty += " · expires " + item.getExpiryDate();
        }
        holder.details.setText(qty);

    }

    private String trimTrailingZero(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView details;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textIngredientName);
            details = itemView.findViewById(R.id.textIngredientDetails);
        }
    }
}
