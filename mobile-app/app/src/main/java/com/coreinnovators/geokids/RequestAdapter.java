package com.coreinnovators.geokids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<RideRequest> requests;
    private OnRequestActionListener acceptListener;
    private OnRequestActionListener rejectListener;

    public interface OnRequestActionListener {
        void onAction(RideRequest request, int position);
    }

    public RequestAdapter(List<RideRequest> requests,
                          OnRequestActionListener acceptListener,
                          OnRequestActionListener rejectListener) {
        this.requests = requests;
        this.acceptListener = acceptListener;
        this.rejectListener = rejectListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RideRequest request = requests.get(position);

        // Set route text (pickup to dropoff)
        String route = request.getPickupLocation() + " to " + request.getDropoffLocation();
        holder.routeText.setText(route);

        // Set requester name (prefer child name, fallback to parent name)
        String requesterName = request.getChildName();
        if (requesterName == null || requesterName.isEmpty()) {
            requesterName = request.getParentName();
        }
        if (requesterName == null || requesterName.isEmpty()) {
            requesterName = "Unknown";
        }
        holder.requesterName.setText(requesterName);

        // Set click listeners with current position
        holder.acceptButton.setOnClickListener(v -> {
            if (acceptListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    acceptListener.onAction(request, adapterPosition);
                }
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (rejectListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    rejectListener.onAction(request, adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView routeText;
        TextView requesterName;
        ImageButton acceptButton;
        ImageButton rejectButton;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            routeText = itemView.findViewById(R.id.route_text);
            requesterName = itemView.findViewById(R.id.requester_name);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}