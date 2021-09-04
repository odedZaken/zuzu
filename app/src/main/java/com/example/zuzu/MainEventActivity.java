package com.example.zuzu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.zuzu.ui.main.DiscoverFragment;
import com.example.zuzu.ui.main.EventChatFragment;
import com.example.zuzu.ui.main.EventFragment;
import com.example.zuzu.ui.main.SectionsPagerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MainEventActivity extends AppCompatActivity {


    private static final int IMAGE_PICK_CODE = 1000;
    private static final int CHAT_INDEX = 2;

    private MaterialToolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ExtendedFloatingActionButton fabAction;
    private UserModel currUser;
    private DatabaseReference databaseRefEventMessages;
    private EventModel event;



    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_event);
        event = EventChatFragment.getCurrentEvent();
        storageRef = FirebaseStorage.getInstance().getReference();
        databaseRefEventMessages = FirebaseDatabase.getInstance().getReference().child("events").child(event.getId()).child("messages");
        currUser = ApplicationGlobal.getCurrentUser();
        initializeEventPage();
        //Get intent argument for event title (event name)
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);  //Enable up button in the top toolbar
    }


    private void initializeEventPage() {
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fabAction = findViewById(R.id.actionEventFab);
        ArrayList<String> tabText = new ArrayList<>();
        //Add tab names to array
        tabText.add("Details");
        tabText.add("Participants");
        tabText.add("Chat");

        prepareViewPager(tabText);
        viewPager.setAdapter(sectionsPagerAdapter);       //Supply the viewpager with fragments added to the adapter
        tabLayout.setupWithViewPager(viewPager);      //Links the tab layout with viewpager
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 2) {
                    fabAction.hide();
                } else {
                    fabAction.show();
                }
            }
        });
    }

    private void prepareViewPager(ArrayList<String> tabText) {
        //Initialize main adapter
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //Initialize discover events fragment

        for (int i = 0; i < tabText.size() - 1; i++) {
            EventFragment fragment = new EventFragment();
            Bundle bundle = new Bundle();       //This bundle contains parameters for the fragment
            bundle.putString("title", tabText.get(i));
            fragment.setArguments(bundle);
            sectionsPagerAdapter.addFragment(fragment, tabText.get(i));
        }
        EventChatFragment chatFragment = new EventChatFragment();
        sectionsPagerAdapter.addFragment(chatFragment, tabText.get(2));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //set image to image view
        if (data != null) {
            Uri imageUri = data.getData();
            String imageID = UUID.randomUUID().toString();
//            try {
//                Bitmap imageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), imageUri));
//                EventChatFragment.getProfilePicCache().put(imageID,imageBitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            //Adding the posted message to list
            MessageModel message = new MessageModel(currUser.getId(), currUser.getEmail(), currUser.getDisplayName(),
                    imageID, MessageModel.MessageType.IMAGE);
            uploadChatPic(imageUri,imageID, message);

//            addMessageToDB(message);
        }
    }

    private void uploadChatPic(Uri imageUri, String imageID, MessageModel message) {
        //Show loading progress bar
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        byte[] bitmapData = getCompressedBitmapData(imageUri);
        if (bitmapData != null) {
            UploadTask uploadProfilePicTask = storageRef.child("chat_pics").child(imageID).putBytes(bitmapData);
            uploadProfilePicTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    addMessageToDB(message);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainEventActivity.this, "Picture upload failed!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }


    private byte[] getCompressedBitmapData(Uri imageUri) {
        //Compress image and convert to byte[]
        Bitmap fullSizeBitmap = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                fullSizeBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), imageUri));
            } else {
                fullSizeBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fullSizeBitmap != null) {
            Bitmap reducedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap, 240000);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } else {
            Toast.makeText(this, "Error compressing image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void addMessageToDB(MessageModel message) {
        databaseRefEventMessages.push().setValue(message);
        MediaPlayer messageSentSE = MediaPlayer.create(MainEventActivity.this,R.raw.messagelongpop);
        messageSentSE.start();
    }

}