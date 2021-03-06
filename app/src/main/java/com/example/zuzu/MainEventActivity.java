package com.example.zuzu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.zuzu.ui.main.DiscoverFragment;
import com.example.zuzu.ui.main.EventChatFragment;
import com.example.zuzu.ui.main.EventFragment;
import com.example.zuzu.ui.main.SectionsPagerAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

public class MainEventActivity extends AppCompatActivity {


    private MaterialToolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ExtendedFloatingActionButton fabAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_event);
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
                if(position == 2) {
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

        for (int i=0; i < tabText.size() - 1; i++) {
            EventFragment fragment = new EventFragment();
            Bundle bundle = new Bundle();       //This bundle contains parameters for the fragment
            bundle.putString("title",tabText.get(i));
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
}