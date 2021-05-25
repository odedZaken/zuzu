package com.example.zuzu.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zuzu.EventModel;
import com.example.zuzu.LoginActivity;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;


public class EventFragment extends Fragment implements View.OnClickListener{

    private static EventModel event;
    private TextView eventName, eventType, eventDescription, eventDate, eventTime, eventDistance, eventNumParticipants;
    private UserModel currUser;
    private ImageView eventMapImageView;
    private MaterialCardView eventMapCard;
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
//                case R.id.action
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
        fabAction.setText("hello");
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