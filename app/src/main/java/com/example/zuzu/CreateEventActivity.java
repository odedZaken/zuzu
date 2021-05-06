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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private TextInputLayout eventDateLayout, eventTimeLayout, eventTypeLayout;
    private AutoCompleteTextView eventType;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private LatLng eventLocation;

    private FusedLocationProviderClient fusedLocationProviderClient;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        this.setTitle("New Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initializeCreateEventForm();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateStr = dayOfMonth + "/" + (month + 1) + "/" + (year - 2000);
        eventDate.setText(dateStr);
    }


    private void initializeCreateEventForm() {
        numParticipantsInt = 2;
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
                        eventTime.setText(hourOfDay + ":" + minute);
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
}
