package com.example.zuzu.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.zuzu.ApplicationGlobal;
import com.example.zuzu.EditProfileActivity;
import com.example.zuzu.ParticipantModel;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ParticipantsAdapter extends ArrayAdapter<String> {
    Context context;
    ArrayList<ParticipantModel> participantsList;
    HashMap<String,Bitmap> profilePicCache;
    private final StorageReference storageProfilePicsRef;
    String eventCreatorId;

    public ParticipantsAdapter(@NonNull Context context, ArrayList<ParticipantModel> participantsList, HashMap<String,Bitmap> profilePicCache, String eventCreatorId) {
        super(context, R.layout.list_item_event_participant);
        this.context = context;
        this.participantsList = participantsList;
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("profile_pics");
        this.profilePicCache = profilePicCache;
        this.eventCreatorId = eventCreatorId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View singleItem = convertView;
        ParticipantViewHolder holder = null;

        if (singleItem == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            singleItem = layoutInflater.inflate(R.layout.list_item_event_participant, parent, false);
            holder = new ParticipantViewHolder(singleItem);
            singleItem.setTag(holder);
        }
        else {
            holder = (ParticipantViewHolder) singleItem.getTag();
        }

        holder.adminCardViewLayout.setVisibility(View.GONE);
        if(eventCreatorId.equals(participantsList.get(position).getUserID())) {
            holder.adminCardViewLayout.setVisibility(View.VISIBLE);
        }
        holder.participantName.setText(participantsList.get(position).getDisplayName());
        holder.participantGenderAge.setText(participantsList.get(position).getGenderAgeString());
        if(profilePicCache.containsKey(participantsList.get(position).getUserID())) {
            holder.participantImage.setImageBitmap(profilePicCache.get(participantsList.get(position).getUserID()));
        } else {
            getProfilePicFromDB(participantsList.get(position).getEmail(), holder.participantImage, participantsList.get(position).getUserID());
        }
        return singleItem;
    }

    @Override
    public int getCount() {
        return participantsList.size();
    }


    private void getProfilePicFromDB(final String email, final ImageView participantImage, final String participantId) {
        storageProfilePicsRef.child(email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                StorageReference userProfilePic = storageProfilePicsRef.child(email);
                userProfilePic.getBytes(EditProfileActivity.MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fadein);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profilePicCache.put(participantId,bitmap);
                        participantImage.setImageBitmap(bitmap);
                        participantImage.startAnimation(fadeIn);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to fetch profile picture..", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
