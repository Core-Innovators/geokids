package com.coreinnovators.geokids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<RideRequest> requestList;
    private OnRequestActionListener acceptListener;
    private OnRequestActionListener rejectListener;

    public interface OnRequestActionListener {
        void onAction(RideRequest request, int position);
    }

    public RequestAdapter(List<RideRequest> requestList,
                          OnRequestActionListener acceptListener,
                          OnRequestActionListener rejectListener) {
        this.requestList = requestList;
        this.acceptListener = acceptListener;
        this.rejectListener = rejectListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_item, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RideRequest request = requestList.get(position);

        // Set child name
        holder.childNameText.setText(request.getChildName());

        // Set parent name
        holder.parentNameText.setText("Parent: " + request.getParentName());

        // Set pickup address (shortened)
        String pickupAddress = request.getPickupAddress();
        if (pickupAddress != null && !pickupAddress.isEmpty()) {
            // Show only first part of address
            String[] parts = pickupAddress.split(",");
            holder.pickupAddressText.setText(parts[0].trim());
        } else {
            holder.pickupAddressText.setText("Address not provided");
        }

        // Set school
        String school = request.getChildSchool();
        if (school != null && !school.isEmpty()) {
            holder.schoolText.setText(school);
        } else {
            holder.schoolText.setText("School not provided");
        }

        // Load child profile image
        if (request.getChildProfileImageUrl() != null &&
                !request.getChildProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(request.getChildProfileImageUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(holder.childProfileImage);
        } else {
            holder.childProfileImage.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Accept button click
        holder.acceptButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                acceptListener.onAction(request, adapterPosition);
            }
        });

        // Reject button click
        holder.rejectButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                rejectListener.onAction(request, adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        CircleImageView childProfileImage;
        TextView childNameText;
        TextView parentNameText;
        TextView pickupAddressText;
        TextView schoolText;
        CardView acceptButton;
        CardView rejectButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            childProfileImage = itemView.findViewById(R.id.child_profile_image);
            childNameText = itemView.findViewById(R.id.child_name_text);
            parentNameText = itemView.findViewById(R.id.parent_name_text);
            pickupAddressText = itemView.findViewById(R.id.pickup_address_text);
            schoolText = itemView.findViewById(R.id.school_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}