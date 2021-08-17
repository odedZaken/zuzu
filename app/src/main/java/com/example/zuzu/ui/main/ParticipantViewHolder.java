package com.example.zuzu.ui.main;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zuzu.R;

public class ParticipantViewHolder {

    ImageView participantImage;
    TextView participantName;
    TextView participantGenderAge;
    LinearLayout adminCardViewLayout;

    ParticipantViewHolder(View v) {
        participantImage = v.findViewById(R.id.imageView1);
        participantName = v.findViewById(R.id.textView1);
        participantGenderAge = v.findViewById(R.id.textView2);
        adminCardViewLayout = v.findViewById(R.id.adminCardViewLayout);
    }
}
