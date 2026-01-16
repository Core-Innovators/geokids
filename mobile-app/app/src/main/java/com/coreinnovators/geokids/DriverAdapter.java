package com.coreinnovators.geokids;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private Context context;
    private List<Driver> driverList;
    private OnDriverClickListener listener;
    private int selectedPosition = -1;

    public interface OnDriverClickListener {
        void onDriverClick(Driver driver, int position);
    }

    public DriverAdapter(Context context, OnDriverClickListener listener) {
        this.context = context;
        this.driverList = new ArrayList<>();
        this.listener = listener;
    }

    public void setDriverList(List<Driver> drivers) {
        this.driverList = drivers;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    public Driver getSelectedDriver() {
        if (selectedPosition >= 0 && selectedPosition < driverList.size()) {
            return driverList.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_driver_card, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);

        // Set driver name
        holder.driverName.setText(driver.getFullName());

        // Set profile image
        if (driver.getProfileImageUrl() != null && !driver.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(driver.getProfileImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.driverImage);
        } else {
            holder.driverImage.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Set available seats (default 10 for now)
        holder.availableSeats.setText("-10");

        // Set route information
        if (driver.getRouteData() != null && driver.getRouteData().getStartPoint() != null) {
            // For now, showing "passara to buttala" as placeholder
            // You can customize this based on actual location data
            holder.routeInfo.setText("- passara to buttala");
        } else {
            holder.routeInfo.setText("- Route not available");
        }

        // Highlight selected card
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.orange_primary));
            holder.driverName.setTextColor(context.getResources().getColor(R.color.white));
            holder.availableSeats.setTextColor(context.getResources().getColor(R.color.white));
            holder.routeInfo.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.driverName.setTextColor(context.getResources().getColor(R.color.dark_blue));
            holder.availableSeats.setTextColor(context.getResources().getColor(R.color.dark_blue));
            holder.routeInfo.setTextColor(context.getResources().getColor(R.color.dark_blue));
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                setSelectedPosition(position);
                listener.onDriverClick(driver, position);
            }
        });

        // Arrow click listener for viewing profile
        holder.arrowIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDriverClick(driver, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView driverImage;
        TextView driverName;
        TextView availableSeats;
        TextView routeInfo;
        ImageView arrowIcon;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.driver_card);
            driverImage = itemView.findViewById(R.id.driver_image);
            driverName = itemView.findViewById(R.id.driver_name);
            availableSeats = itemView.findViewById(R.id.available_seats);
            routeInfo = itemView.findViewById(R.id.route_info);
            arrowIcon = itemView.findViewById(R.id.arrow_icon);
        }
    }
}