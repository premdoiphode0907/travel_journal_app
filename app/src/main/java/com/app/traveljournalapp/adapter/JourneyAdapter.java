package com.app.traveljournalapp.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.traveljournalapp.R;
import com.app.traveljournalapp.activity.JourneyDetailActivity;  // Import JourneyDetailActivity
import com.app.traveljournalapp.data.db.entity.Journey;

import java.util.List;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    private List<Journey> journeyList;

    @Override
    public JourneyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journey, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JourneyViewHolder holder, int position) {
        Journey journey = journeyList.get(position);
        holder.title.setText(journey.getTitle());
        holder.date.setText(journey.getDate());
        holder.address.setText(journey.getAddress());

        // Set a click listener on the item
        holder.itemView.setOnClickListener(v -> {
            // Open the JourneyDetailActivity when an item is clicked
            System.out.println("Journey Id: "+journey.getId());
            Intent intent = new Intent(v.getContext(), JourneyDetailActivity.class);
            intent.putExtra("journeyId", journey.getId());
            intent.putExtra("position", position);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return journeyList != null ? journeyList.size() : 0;
    }

    public void setJourneys(List<Journey> journeys) {
        this.journeyList = journeys;
        notifyDataSetChanged();
    }

    public static class JourneyViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, address;

        public JourneyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.journeyTitle);
            date = itemView.findViewById(R.id.journeyDate);
            address = itemView.findViewById(R.id.journeyAddress);
        }
    }
}
