package com.example.zuzu.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zuzu.ApplicationGlobal;
import com.example.zuzu.EventModel;
import com.example.zuzu.MainEventActivity;
import com.example.zuzu.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    ArrayList<EventModel> eventList;
    Context context;

    public RecyclerViewAdapter(ArrayList<EventModel> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView eventTypeIcon;
        TextView eventName, eventDate, eventTime, eventDistance, eventNumParticipants;
        MaterialCardView eventCardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View
            eventCardView = itemView.findViewById(R.id.eventCardView);
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
        final EventModel event = eventList.get(position);
        holder.eventName.setText(event.getTitle());
        holder.eventDate.setText(getUniqueDateString(event.getDateInCalendar()));
        holder.eventTime.setText(event.getTime());
        holder.eventDistance.setText(event.getDistanceStr());
        //Set a click listener for the event card, launching a new event activity
        holder.eventCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventChatFragment.setEvent(event);
                EventFragment.setEvent(event);
                Intent intent = new Intent(context, MainEventActivity.class);
                intent.putExtra("title", event.getTitle());
                context.startActivity(intent);
            }
        });
        holder.eventNumParticipants.setTextColor(context.getResources().getColor(R.color.textDefault, null));
        holder.eventCardView.setBackgroundColor(context.getResources().getColor(android.R.color.white, null));
        if (event.isFull()) {
            holder.eventNumParticipants.setTextColor(context.getResources().getColor(R.color.lightOrange, null));
        }
        if (event.getCreatorId().equals(ApplicationGlobal.getCurrentUser().getId())) {
            holder.eventCardView.setBackgroundColor(context.getResources().getColor(R.color.backgroundCreator, null));
        }
        holder.eventNumParticipants.setText(event.getParticipantsStr());
        setDrawableType(holder, event);
    }

    private String getUniqueDateString(Calendar eventDate) {
        Calendar now = Calendar.getInstance();
        String result;
        if (eventDate.get(Calendar.DATE) == now.get(Calendar.DATE) && eventDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
            result = "Today";
        } else if (eventDate.get(Calendar.DATE) == now.get(Calendar.DATE) + 1 && eventDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
            result = "Tomorrow";
        } else {
            result = eventDate.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT, Locale.getDefault()) + ", " + eventDate.getDisplayName(Calendar.MONTH,Calendar.LONG,Locale.getDefault())
            + " " + eventDate.get(Calendar.DATE);
        }
        return result;
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    // -------- Set Drawable for sport type -------------------
    private void setDrawableType(MyViewHolder holder, EventModel event) {
        switch (event.getType()) {
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
