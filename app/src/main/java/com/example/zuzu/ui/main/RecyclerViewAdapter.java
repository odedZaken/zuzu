package com.example.zuzu.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zuzu.EventModel;
import com.example.zuzu.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    ArrayList<EventModel> eventList;
    Context context;

    public RecyclerViewAdapter(ArrayList<EventModel> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView eventTypeIcon;
        TextView eventName , eventDate, eventTime, eventDistance, eventNumParticipants;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View

            eventTypeIcon = itemView.findViewById(R.id.eventCardTypeIcon);
            eventName = itemView.findViewById(R.id.eventCardName);
            eventDate = itemView.findViewById(R.id.eventCardDate);
            eventTime = itemView.findViewById(R.id.eventCardTime);
            eventDistance = itemView.findViewById(R.id.eventCardDistance);
            eventNumParticipants = itemView.findViewById(R.id.eventCardParticipants);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Define a new View of the Event Card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event_card, parent, false);
        //Create a new view holder for the card (click listener for the card)
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Bind the data from the array list into the event card parameters
        EventModel event = eventList.get(position);
        holder.eventName.setText(event.getTitle());
        holder.eventDate.setText(event.getDate());
        holder.eventTime.setText(event.getTime());
        holder.eventDistance.setText(Integer.toString(event.getDistance()));
        //Create the string for current/max participants
        String participants = event.getCurrParticipants() + "/" + event.getMaxParticipants();
        holder.eventNumParticipants.setText(participants);
        setDrawableType(holder, event);
    }



    @SuppressLint("UseCompatLoadingForDrawables")
    // -------- Set Drawable for sport type -------------------
    private void setDrawableType (MyViewHolder holder ,EventModel event) {
        switch(event.getType()) {
            case "Soccer":
                holder.eventTypeIcon.setImageDrawable(this.context.getDrawable(R.drawable.ic_baseline_sports_soccer_24));
                break;
            case "Basketball":
                holder.eventTypeIcon.setImageDrawable(this.context.getDrawable(R.drawable.ic_baseline_sports_basketball_24));
                break;
            case "Volleyball":
                holder.eventTypeIcon.setImageDrawable(this.context.getDrawable(R.drawable.ic_baseline_sports_volleyball_24));
                break;
            case "Tennis":
                holder.eventTypeIcon.setImageDrawable(this.context.getDrawable(R.drawable.ic_baseline_sports_tennis_24));
                break;
            case "Exercise":
                holder.eventTypeIcon.setImageDrawable(this.context.getDrawable(R.drawable.ic_baseline_fitness_center_24));
                break;
            case "Running":
                holder.eventTypeIcon.setImageDrawable(this.context.getDrawable(R.drawable.ic_baseline_directions_run_24));
                break;
        }
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
