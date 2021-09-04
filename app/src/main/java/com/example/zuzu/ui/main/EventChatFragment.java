package com.example.zuzu.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.zuzu.ApplicationGlobal;
import com.example.zuzu.EditProfileActivity;
import com.example.zuzu.EventModel;
import com.example.zuzu.MainEventActivity;
import com.example.zuzu.MessageModel;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class EventChatFragment extends Fragment implements View.OnClickListener {

    private static final int IMAGE_PICK_CODE = 1000;


    private ArrayList<MessageModel> messageList;
    private static HashMap<String, Bitmap> profilePicCache;
    private HashSet<String> messageKeyPool;
    private UserModel currUser;
    private RecyclerView chatRecyclerView;
    private ImageButton addPictureButton;
    private TextInputEditText editTextMessage;
    private static ChatRecViewAdapter mAdapter;
    private MaterialButton sendButton;
    private ProgressBar chatProgressBar;
    private static EventModel event;
    private DatabaseReference databaseRefEventMessages;
    private StorageReference storageRef;
    private MediaPlayer messageSentSE;


    public EventChatFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currUser = ApplicationGlobal.getCurrentUser();
        databaseRefEventMessages = FirebaseDatabase.getInstance().getReference().child("events").child(event.getId()).child("messages");
        storageRef = FirebaseStorage.getInstance().getReference();
        messageList = new ArrayList<>();
        messageKeyPool = new HashSet<>();
        //Create sound effect for message sending
        messageSentSE = MediaPlayer.create(getContext(),R.raw.messagelongpop);
    }

    private void initializeChatFragment(View chatView) {
        chatRecyclerView = chatView.findViewById(R.id.chatRecycleView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(bottom < oldBottom) {
                    chatRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!messageList.isEmpty()) {
                                chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    },50);
                }
            }
        });
        addPictureButton = chatView.findViewById(R.id.addPhotoButton);
        addPictureButton.setOnClickListener(this);
        editTextMessage = chatView.findViewById(R.id.editTextMessage);
        sendButton = chatView.findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(this);
        chatProgressBar = chatView.findViewById(R.id.progressBarChat);
        mAdapter = new ChatRecViewAdapter(messageList,getActivity(),profilePicCache);
        chatRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.fragment_event_chat, container, false);
        initializeChatFragment(chatView);
        getMessagesFromDB();
        return chatView;
    }

    public static void setProfilePicCache(HashMap<String, Bitmap> profileCache){
        profilePicCache = profileCache;
    }

    public static EventModel getEvent() {
        return event;
    }

    public static void setEvent(EventModel event) {
        EventChatFragment.event = event;
    }

    private void getMessagesFromDB() {

        databaseRefEventMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                chatProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });


        databaseRefEventMessages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot dataSnapshot, @Nullable @org.jetbrains.annotations.Nullable String s) {
                String key = dataSnapshot.getKey();
                if(!messageKeyPool.contains(key)) {
                    addMessageToList(dataSnapshot);
                    mAdapter.notifyItemInserted(messageList.size() - 1);
                    messageKeyPool.add(key);
                }
                chatProgressBar.setVisibility(View.INVISIBLE);
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot dataSnapshot, @Nullable @org.jetbrains.annotations.Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot dataSnapshot, @Nullable @org.jetbrains.annotations.Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    public static void notifyAdapter() {
        mAdapter.notifyDataSetChanged();
    }

    private void addMessageToList(DataSnapshot message) {
        String authorEmail = message.child("authorEmail").getValue(String.class);
        String authorID = message.child("authorID").getValue(String.class);
        String authorName = message.child("authorName").getValue(String.class);
        String messageContent = message.child("messageContent").getValue(String.class);
        MessageModel.MessageType messageType = message.child("type").getValue(MessageModel.MessageType.class);
//        MessageModel newMessage = new MessageModel(authorID, authorEmail,authorName,messageContent, MessageModel.MessageType.TEXT);
        MessageModel newMessage = new MessageModel(authorID, authorEmail,authorName,messageContent, messageType);

        messageList.add(newMessage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addPhotoButton:
                //TODO: Post message in chat!
                pickImageFromGallery();
                break;
            case R.id.sendMessageButton:
                if(editTextMessage.getText().length() < 1) {
                    Toast.makeText(getActivity(), "Message field is empty", Toast.LENGTH_SHORT).show();
                } else {
                    //Adding the posted message to list
                    MessageModel message = new MessageModel(currUser.getId(),currUser.getEmail(),currUser.getDisplayName(),
                            editTextMessage.getText().toString(), MessageModel.MessageType.TEXT);
                    editTextMessage.setText("");
                    addMessageToDB(message);
//                    messageSentSE.start();
                }
                break;
        }
    }

    private void pickImageFromGallery() {
        //intent to pick an image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    public static EventModel getCurrentEvent() {
        return event;
    }


    private void addMessageToDB(MessageModel message) {
        databaseRefEventMessages.push().setValue(message);
        messageSentSE.start();
    }

    public static HashMap<String, Bitmap> getProfilePicCache() {
        return profilePicCache;
    }
}