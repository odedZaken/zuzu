package com.example.zuzu;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener,  DatePickerDialog.OnDateSetListener {


//    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ArrayList<String> sports = new ArrayList<>();
    private ArrayAdapter sportsAdapter;

    private TextView numParticipantsText;
    private int numParticipantsInt, currHour, currMinute;
    private ImageButton increaseButton, decreaseButton;
    private Button chooseLocationBtn;
    private TextInputEditText eventName, eventDescription, eventDate, eventTime;
    private TextInputLayout eventDateLayout, eventTimeLayout, eventTypeLayout, eventNameLayout, eventDescriptionLayout;
    private AutoCompleteTextView eventType;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private ExtendedFloatingActionButton createEventFab;
    private static LatLng eventLocation;
    private DatabaseReference eventsReference;

    //private FusedLocationProviderClient fusedLocationProviderClient;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        this.setTitle("New Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initializeCreateEventForm();
        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        sports.add("Soccer"); sports.add("Basketball");
        sports.add("Tennis"); sports.add("Volleyball");
        sports.add("Running"); sports.add("Exercise");
        sportsAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item_sport_type, sports);
        eventType.setAdapter(sportsAdapter);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.increaseButton:
                if (numParticipantsInt < 20) {
                    numParticipantsInt++;
                    numParticipantsText.setText(Integer.toString(numParticipantsInt));
                }
                else {
                    Toast.makeText(this, "Maximum participants reached", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.decreaseButton:
                if (numParticipantsInt > 2) {
                    numParticipantsInt--;
                    numParticipantsText.setText(Integer.toString(numParticipantsInt));
                }
                else {
                    Toast.makeText(this, "Minimum participants reached", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.chooseLocationBtn:
                Intent intent = new Intent(this,MapActivity.class);
                startActivity(intent);
                break;
            case R.id.createEventFab:
                dismissErrors();
                if (verifyProperties()) {
                    saveEventInDB();
                }
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateStr = dayOfMonth + "/" + (month + 1) + "/" + (year - 2000);
        eventDate.setText(dateStr);
    }


    private void initializeCreateEventForm() {
        eventLocation = null;
        numParticipantsInt = 2;
        eventNameLayout = findViewById(R.id.eventNameLayout);
        eventDescriptionLayout = findViewById(R.id.eventDescriptionLayout);
        createEventFab = findViewById(R.id.createEventFab);
        createEventFab.setOnClickListener(this);
        chooseLocationBtn = findViewById(R.id.chooseLocationBtn);
        chooseLocationBtn.setOnClickListener(this);
        increaseButton = findViewById(R.id.increaseButton);
        increaseButton.setOnClickListener(this);
        decreaseButton = findViewById(R.id.decreaseButton);
        decreaseButton.setOnClickListener(this);
        numParticipantsText = findViewById(R.id.numParticipants);
        eventName = findViewById(R.id.textInputEventName);
        eventDescription = findViewById(R.id.textInputEventDescription);
        eventDateLayout = findViewById(R.id.eventDateLayout);
        eventTimeLayout = findViewById(R.id.eventTimeLayout);
        eventTypeLayout = findViewById(R.id.eventTypeLayout);
        initializeTimeDateListeners();
        eventDate = findViewById(R.id.textInputEventDate);
        eventTime = findViewById(R.id.textInputEventTime);
        eventType = findViewById(R.id.textInputEventType);
    }

    private void initializeTimeDateListeners() {
        eventTimeLayout.setStartIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                currHour = c.get(Calendar.HOUR_OF_DAY);
                currMinute = c.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (minute < 10) {
                            eventTime.setText(hourOfDay + ":0" + minute);
                        } else {
                            eventTime.setText(hourOfDay + ":" + minute);
                        }
                    }
                },currHour,currMinute,true );
                timePickerDialog.show();
            }
        });
        eventDateLayout.setStartIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(CreateEventActivity.this);
                datePickerDialog.setOnDateSetListener(CreateEventActivity.this);
                datePickerDialog.show();
            }
        });
    }

    private void dismissErrors() {
        eventNameLayout.setErrorEnabled(false);
        eventDescriptionLayout.setErrorEnabled(false);
        eventDateLayout.setErrorEnabled(false);
        eventTimeLayout.setErrorEnabled(false);
        eventTypeLayout.setErrorEnabled(false);
        eventDescriptionLayout.setErrorEnabled(false);
    }

    private boolean verifyProperties() {
        boolean isVerified = true;
        if(eventName.getText().length() == 0)  {eventNameLayout.setError("Field can't be empty"); isVerified = false;}
        if(eventName.getText().length() > 30) { eventNameLayout.setError("Characters number too high");isVerified = false; }
        if(eventDescription.getText().length() > 70) { eventDescriptionLayout.setError("Characters number too high");isVerified = false; }
        if(eventTime.getText().length() == 0) { eventTimeLayout.setError("Field can't be empty");isVerified = false; }
        if(eventDate.getText().length() == 0) { eventDateLayout.setError("Field can't be empty");isVerified = false; }
        if(eventType.getText().length() == 0) { eventTypeLayout.setError("You must choose a sport");isVerified = false; }
        if(eventLocation == null) {
            Toast.makeText(this, "You must choose a location", Toast.LENGTH_SHORT).show();
            isVerified = false;
        }
        return isVerified;
    }

    public static void setEventLocation(LatLng latLng) {
        eventLocation = latLng;
    }

    private void saveEventInDB() {
        eventsReference = FirebaseDatabase.getInstance().getReference("events");
        String eventNameStr = eventName.getText().toString();
        String eventDescStr = eventDescription.getText().toString();
        String eventTypeStr = eventType.getText().toString();
        String eventDateStr = eventDate.getText().toString();
        String eventTimeStr = eventTime.getText().toString();
        String currUserEmail = LoginActivity.getCurrentUser().getEmail();
        EventModel newEvent = new EventModel(eventNameStr,eventDescStr, eventTypeStr, eventTimeStr, eventDateStr, currUserEmail, numParticipantsInt, eventLocation);
        String newEventId = newEvent.getId();
        eventsReference.child(newEventId).setValue(newEvent);
        Toast.makeText(this, "Event Created Successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
