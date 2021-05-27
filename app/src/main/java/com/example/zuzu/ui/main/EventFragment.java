package com.example.zuzu.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zuzu.ApplicationGlobal;
import com.example.zuzu.EventModel;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class EventFragment extends Fragment implements View.OnClickListener{

    private static EventModel event;
    private TextView eventName, eventType, eventDescription, eventDate, eventTime, eventDistance, eventNumParticipants;
    private UserModel currUser;
    private ImageView eventMapImageView;
    private MaterialCardView eventMapCard;
    private ExtendedFloatingActionButton fabAction;
    private String tabTitle;
    private Context context;
    private RelativeLayout detailsLayout;
    private DatabaseReference databaseReferenceEvent;
    private boolean isUserParticipate;
    private boolean isUserCreator;


    public EventFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currUser = ApplicationGlobal.getCurrentUser();
        tabTitle = this.getArguments().getString("title");
        isUserParticipate = false;
        isUserCreator = false;
        databaseReferenceEvent = FirebaseDatabase.getInstance().getReference().child("events").child(event.getId());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        initializeEventDetails(view);
        context = getActivity();
//        initializeParticipantsList();
        configureActionButton();
//        if(tabTitle.equals("Participants")) {
//            detailsLayout.setVisibility(View.GONE);
//        }
        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.mapEventCard:
                //Redirect to GoogleMaps with event exact location
                String url = "https://www.google.com/maps/search/?api=1&query="+ event.getLocation().latitude + "," + event.getLocation().longitude;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.actionEventFab:
                fabAction.setEnabled(false);
                if(isUserCreator) {
                    deleteEvent();
                } else if(isUserParticipate) {
                    leaveEvent();
                } else {
                    joinEvent();
                }
                break;
        }
    }

    private void deleteEvent() {
        //delete event from database and finish
        Toast.makeText(context, "Will be deleted..", Toast.LENGTH_SHORT).show();
    }

    private void leaveEvent() {
        //delete user from event list in database and refresh fab action and list
    }

    private void joinEvent() {
        //add user to event list in database and refresh fab action and list
    }

    private void configureActionButton() {
        //Initialize association variables
        if(currUser.getId().equals(event.getCreatorId()))
        {
            isUserCreator = true;
            isUserParticipate = true;
        } else {
            for (String id : event.getUsersIDs()) {
                if (currUser.getId().equals(id)) {
                    isUserParticipate = true;
//                    Toast.makeText(context, "User is NOT creator!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(isUserCreator) {
            fabAction.setText("Delete");
            fabAction.setIcon(ContextCompat.getDrawable(context,R.drawable.ic_baseline_delete_24));
            fabAction.setBackgroundColor(getResources().getColor(R.color.red,null));
        } else if(isUserParticipate) {
            fabAction.setText("Leave");
            fabAction.setIcon(ContextCompat.getDrawable(context,R.drawable.ic_baseline_person_leave_24));
            fabAction.setBackgroundColor(getResources().getColor(R.color.orange,null));
        }
    }

    private void initializeEventDetails(View view) {
        eventMapCard = view.findViewById(R.id.mapEventCard);
        eventMapImageView = view.findViewById(R.id.mapEventImage);
        String staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=500x400&markers=color:red%7C"+ event.getLocation().latitude+","+event.getLocation().longitude +"&key=" + getResources().getString(R.string.google_maps_api_key) ;
        Glide.with(this).load(staticMapUrl).into(eventMapImageView);
        eventMapCard.setOnClickListener(this);
        detailsLayout = view.findViewById(R.id.detailsLayout);
        eventName = view.findViewById(R.id.textEventName);
        eventDescription = view.findViewById(R.id.textEventDescription);
        eventType = view.findViewById(R.id.textEventType);
        eventDate = view.findViewById(R.id.textEventDate);
        eventTime = view.findViewById(R.id.textEventTime);
        eventDistance = view.findViewById(R.id.textEventDistance);
        eventNumParticipants = view.findViewById(R.id.textNumParticipants);
        fabAction = getActivity().findViewById(R.id.actionEventFab);
        fabAction.setOnClickListener(this);
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