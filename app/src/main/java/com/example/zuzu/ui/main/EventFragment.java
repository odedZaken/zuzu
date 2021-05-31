package com.example.zuzu.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.example.zuzu.ApplicationGlobal;
import com.example.zuzu.EventModel;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class EventFragment extends Fragment implements View.OnClickListener{

    private static EventModel event;
    private TextView eventName, eventType, eventDescription, eventDate, eventTime, eventDistance, eventNumParticipants;
    private UserModel currUser;
    private ImageView eventMapImageView;
    private MaterialCardView eventMapCard;
    private ExtendedFloatingActionButton fabAction;
    private String tabTitle;
    private Activity context;
//    private Context context;
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
        getEventDetailsFromDB();

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
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle("Are you sure?")
                .setMessage("Event wil be deleted for all users")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReferenceEvent.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(context, "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }
                        });
                        context.finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        fabAction.setEnabled(true);
                    }
                });
        dialogBuilder.show();
    }

    private void leaveEvent() {
        //delete user from event list in database and refresh fab action and list
        event.removeUser(currUser.getId());
        databaseReferenceEvent.child("usersIDs").setValue(event.getUsersIDs()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    configureActionButton();
                    fabAction.setEnabled(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(context, "Something went wrong..", Toast.LENGTH_SHORT).show();
                    fabAction.setEnabled(true);
                }
            });
        databaseReferenceEvent.child("currParticipants").setValue(event.getCurrParticipants());
    }

    private void joinEvent() {
        //add user to event list in database and refresh fab action and list
        if(!event.isFull()) {
            event.addUser(currUser.getId());
            databaseReferenceEvent.child("usersIDs").setValue(event.getUsersIDs()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    configureActionButton();
                    fabAction.setEnabled(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(context, "Something went wrong..", Toast.LENGTH_SHORT).show();
                    fabAction.setEnabled(true);
                }
            });
            databaseReferenceEvent.child("currParticipants").setValue(event.getCurrParticipants());
        } else {
            Toast.makeText(context, "Event is currently full", Toast.LENGTH_SHORT).show();
            fabAction.setEnabled(true);
        }
    }

    private void configureActionButton() {
        //Initialize association variables
        isUserCreator = false;
        isUserParticipate = false;
        if(currUser.getId().equals(event.getCreatorId()))
        {
            isUserCreator = true;
            isUserParticipate = true;
        } else {
            for (String id : event.getUsersIDs()) {
                if (currUser.getId().equals(id)) {
                    isUserParticipate = true;
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
        } else {
            fabAction.setText("Join");
            fabAction.setIcon(ContextCompat.getDrawable(context,R.drawable.ic_baseline_person_join_24));
            fabAction.setBackgroundColor(getResources().getColor(R.color.design_default_color_secondary,null));
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

    private void getEventDetailsFromDB() {
        databaseReferenceEvent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    ArrayList<String> usersId = new ArrayList<>();
                    //Fetch current users Ids of this event and number of participants from DB
                    //Will be updated on every data change
                    int currParticipantsFromDB = dataSnapshot.child("currParticipants").getValue(int.class);
                    for (DataSnapshot id : dataSnapshot.child("usersIDs").getChildren()) {
                        usersId.add(id.getValue(String.class));
                    }
                    event.setCurrParticipants(currParticipantsFromDB);
                    eventNumParticipants.setText(event.getParticipantsStr());
                    event.setUsersIDs(usersId);
                    //Here a call to initialize users list
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {
                Toast.makeText(context, "Operation canceled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static EventModel getEvent() {
        return event;
    }

    public static void setEvent(EventModel event) {
        EventFragment.event = event;
    }
}