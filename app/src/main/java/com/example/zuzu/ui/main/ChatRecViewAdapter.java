package com.example.zuzu.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zuzu.ApplicationGlobal;
import com.example.zuzu.EditProfileActivity;
import com.example.zuzu.EventModel;
import com.example.zuzu.MainEventActivity;
import com.example.zuzu.MessageModel;
import com.example.zuzu.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class ChatRecViewAdapter extends RecyclerView.Adapter<ChatRecViewAdapter.MyViewHolder> {

    ArrayList<MessageModel> messageList;
    Context context;
    HashMap<String, Bitmap> profilePicCache;
    HashSet<String> requestedPics;
    HashSet<Integer> positionsSet;
    private final StorageReference storageProfilePicsRef, storageChatPicsRef;


    public ChatRecViewAdapter(ArrayList<MessageModel> eventList, Context context, HashMap<String, Bitmap> profilePicCache) {
        this.messageList = eventList;
        this.context = context;
        this.profilePicCache = profilePicCache;
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("profile_pics");
        storageChatPicsRef = FirebaseStorage.getInstance().getReference().child("chat_pics");
        requestedPics = new HashSet<>();
        positionsSet = new HashSet<>();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView authorProfilePic, messageImage;
        TextView authorFullName, messageContent;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View
            authorProfilePic = itemView.findViewById(R.id.authorImage);
            authorFullName = itemView.findViewById(R.id.authorFullName);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageImage = itemView.findViewById(R.id.messageImage);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Define a new View of the message card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat_message, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Bind the data from the array list into the message card parameters
        MessageModel message = messageList.get(position);
        holder.authorFullName.setText(message.getAuthorName());

        if(!positionsSet.contains(position)) {
            holder.authorProfilePic.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_person_24));
        }
        if (profilePicCache.containsKey(message.getAuthorID())) {
            Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fadein);
            holder.authorProfilePic.setImageBitmap(profilePicCache.get(message.getAuthorID()));
            if(!positionsSet.contains(position)) {
                holder.authorProfilePic.startAnimation(fadeIn);
                positionsSet.add(position);
            }
        }
        else if (!requestedPics.contains(message.getAuthorID())) {
            requestedPics.add(message.getAuthorID());
            getProfilePicFromDB(message.getAuthorEmail(), holder.authorProfilePic, message.getAuthorID());
        }
        if (message.getType() == MessageModel.MessageType.TEXT) {
            holder.messageContent.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageContent.setText(message.getMessageContent());
        } else {
            holder.messageContent.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            if(profilePicCache.containsKey(message.getMessageContent())) {
                holder.messageImage.setImageBitmap(profilePicCache.get(message.getMessageContent()));
            } else {
                getMessageImageFromDB(message.getMessageContent(), holder.messageImage);
                holder.messageImage.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_image_24));
//                Toast.makeText(context, "Getting image from db!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getMessageImageFromDB(String imageID, ImageView messageImage) {
        storageChatPicsRef.child(imageID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                StorageReference chatPicRef = storageChatPicsRef.child(imageID);
                chatPicRef.getBytes(EditProfileActivity.MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fadein);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profilePicCache.put(imageID, bitmap);
                        messageImage.setImageBitmap(bitmap);
                        messageImage.startAnimation(fadeIn);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to fetch chat picture..", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private void getProfilePicFromDB(final String email, final ImageView authorImage, final String authorID) {
        storageProfilePicsRef.child(email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                StorageReference userProfilePic = storageProfilePicsRef.child(email);
                userProfilePic.getBytes(EditProfileActivity.MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fadein);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profilePicCache.put(authorID, bitmap);
                        authorImage.setImageBitmap(bitmap);
                        authorImage.startAnimation(fadeIn);
                        EventChatFragment.notifyAdapter();
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



    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
