package com.example.zuzu.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zuzu.EventModel;
import com.example.zuzu.LoginActivity;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;


public class EventFragment extends Fragment {

    private static EventModel event;
    private TextView eventName, eventType, eventDescription, eventDate, eventTime, eventDistance, eventNumParticipants;
    private UserModel currUser;
    private ImageView eventGoogleMap;
    private ExtendedFloatingActionButton fabAction;
    private String tabTitle;
    private RelativeLayout detailsLayout;


    public EventFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currUser = LoginActivity.getCurrentUser();
        tabTitle = this.getArguments().getString("title");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        initializeEventDetails(view);
//        initializeParticipantsList();
        if(tabTitle.equals("Participants")) {
            detailsLayout.setVisibility(View.GONE);
        }
        return view;
    }

    private void initializeEventDetails(View view) {
        detailsLayout = view.findViewById(R.id.detailsLayout);
        eventName = view.findViewById(R.id.textEventName);
        eventDescription = view.findViewById(R.id.textEventDescription);
        eventType = view.findViewById(R.id.textEventType);
        eventDate = view.findViewById(R.id.textEventDate);
        eventTime = view.findViewById(R.id.textEventTime);
        eventDistance = view.findViewById(R.id.textEventDistance);
        eventNumParticipants = view.findViewById(R.id.textNumParticipants);
        eventName.setText(event.getTitle());
        if(event.getDescription().isEmpty())
            eventDescription.setVisibility(View.GONE);

        eventDescription.setText(event.getDescription());
        eventType.setText(event.getType());
        eventDate.setText(event.getDate());
        eventTime.setText(event.getTime());
        eventDistance.setText(event.getDistanceStr());
        eventNumParticipants.setText(event.getParticipantsStr());
    }

    public static EventModel getEvent() {
        return event;
    }

    public static void setEvent(EventModel event) {
        EventFragment.event = event;
    }
}