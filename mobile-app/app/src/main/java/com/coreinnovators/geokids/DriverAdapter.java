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

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private final Context context;
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
        if (previousPosition != -1) notifyItemChanged(previousPosition);
        if (selectedPosition != -1) notifyItemChanged(selectedPosition);
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
        boolean isSelected = (position == selectedPosition);

        // ── Name ─────────────────────────────────────────────────────
        holder.driverName.setText(driver.getFullName());

        // ── Profile image ─────────────────────────────────────────────
        String profileUrl = driver.getProfileImageUrl();
        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(context)
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(holder.driverImage);
        } else {
            holder.driverImage.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // ── Route label ───────────────────────────────────────────────
        // Firestore driver document has:
        //   address: "Hulandawa, Monaragala"  ← human readable start town
        //   routeData.distance: "17.7 km"     ← route length
        //   routeData.duration: "18 min"      ← travel time
        // We show "Hulandawa · 17.7 km" which is accurate and scannable.
        holder.routeInfo.setText(buildRouteLabel(driver));

        // ── Selected state ────────────────────────────────────────────
        applySelectionState(holder, isSelected);

        // ── Click ─────────────────────────────────────────────────────
        holder.itemView.setOnClickListener(v -> {
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80)
                    .withEndAction(() -> {
                        float target = (position == selectedPosition) ? 1f : 1.02f;
                        v.animate().scaleX(target).scaleY(target).setDuration(120).start();
                    }).start();
            setSelectedPosition(holder.getAdapterPosition());
            if (listener != null) listener.onDriverClick(driver, holder.getAdapterPosition());
        });

        holder.arrowIcon.setOnClickListener(v -> {
            if (listener != null) listener.onDriverClick(driver, holder.getAdapterPosition());
        });
    }

    /**
     * Builds route label from driver.address + routeData.distance.
     *
     * Driver Firestore structure:
     *   address: "Hulandawa, Monaragala"
     *   routeData: { distance: "17.7 km", duration: "18 min",
     *                startPoint: {lat, lng}, endPoint: {lat, lng} }
     *
     * Result examples:
     *   "Hulandawa · 17.7 km"
     *   "Hulandawa · 18 min"
     *   "Hulandawa"
     *   "Route not set"
     */
    private String buildRouteLabel(Driver driver) {
        String address = driver.getAddress(); // "Hulandawa, Monaragala"

        // Start town: first segment before the comma
        String startTown = null;
        if (address != null && !address.isEmpty()) {
            startTown = address.contains(",")
                    ? address.split(",")[0].trim()
                    : address.trim();
        }

        // Read distance and duration safely — Driver.RouteData is an inner class
        // so we call getRouteData() and access fields via the standalone RouteData
        // interface (both classes expose getDistance() and getDuration())
        String distance = null;
        String duration = null;
        Driver.RouteData route = driver.getRouteData();
        if (route != null) {
            distance = route.getDistance();
            duration = route.getDuration();
        }

        if (startTown != null) {
            if (distance != null && !distance.isEmpty()) return startTown + " · " + distance;
            if (duration != null && !duration.isEmpty()) return startTown + " · " + duration;
            return startTown;
        }
        if (distance != null && !distance.isEmpty()) return distance + " route";
        return "Route not set";
    }

    private void applySelectionState(DriverViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.selectionAccent.setVisibility(View.VISIBLE);
            holder.selectionAccent.setAlpha(0f);
            holder.selectionAccent.animate().alpha(1f).setDuration(200).start();
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.arrowIcon.setVisibility(View.GONE);
            holder.cardView.setCardElevation(dpToPx(holder, 8));
            holder.itemView.setScaleX(1.02f);
            holder.itemView.setScaleY(1.02f);
        } else {
            holder.selectionAccent.setVisibility(View.GONE);
            holder.checkIcon.setVisibility(View.GONE);
            holder.arrowIcon.setVisibility(View.VISIBLE);
            holder.cardView.setCardElevation(dpToPx(holder, 3));
            holder.itemView.setScaleX(1f);
            holder.itemView.setScaleY(1f);
        }
    }

    private float dpToPx(DriverViewHolder holder, int dp) {
        return dp * holder.itemView.getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View selectionAccent;
        CircleImageView driverImage;
        View statusDot;
        TextView driverName;
        TextView routeInfo;
        ImageView checkIcon;
        ImageView arrowIcon;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView        = itemView.findViewById(R.id.driver_card);
            selectionAccent = itemView.findViewById(R.id.selection_accent);
            driverImage     = itemView.findViewById(R.id.driver_image);
            statusDot       = itemView.findViewById(R.id.status_dot);
            driverName      = itemView.findViewById(R.id.driver_name);
            routeInfo       = itemView.findViewById(R.id.route_info);
            checkIcon       = itemView.findViewById(R.id.check_icon);
            arrowIcon       = itemView.findViewById(R.id.arrow_icon);
        }
    }
}