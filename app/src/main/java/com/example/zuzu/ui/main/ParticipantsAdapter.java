package com.example.zuzu.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.zuzu.ParticipantModel;
import com.example.zuzu.R;

import java.util.ArrayList;

public class ParticipantsAdapter extends ArrayAdapter<String> {
    Context context;
    ArrayList<ParticipantModel> participantsList;

    public ParticipantsAdapter(@NonNull Context context, ArrayList<ParticipantModel> participantsList) {
        super(context, R.layout.fragment_participants, R.id.textView1);
        this.participantsList = participantsList;

        // Todo: delete!
        Toast.makeText(context, participantsList.get(0).getFirstName(), Toast.LENGTH_SHORT).show();
        Toast.makeText(context, participantsList.get(1).getFirstName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View singleItem = convertView;
        ParticipantViewHolder holder = null;

        if (singleItem == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            singleItem = layoutInflater.inflate(R.layout.fragment_participants, parent, false);
            holder = new ParticipantViewHolder(singleItem);
            singleItem.setTag(holder);
        }
        else {
            holder = (ParticipantViewHolder) singleItem.getTag();
        }

        holder.participantImage.setImageBitmap(participantsList.get(position).getProfilePic());
        holder.participantName.setText(participantsList.get(position).getDisplayName());
        holder.participantGenderAge.setText(participantsList.get(position).getGenderAgeString());

        return singleItem;
    }
}
