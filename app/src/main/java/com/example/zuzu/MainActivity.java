package com.example.zuzu;

import android.os.Bundle;

import com.example.zuzu.ui.main.DiscoverFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.zuzu.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FloatingActionButton addEventFab;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeMainActivity();
    }

    private void initializeMainActivity() {
        ArrayList<String> tabText = new ArrayList<>();
        //Add tab names to array
        tabText.add("Discover");
        tabText.add("My Events");

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        addEventFab = findViewById(R.id.addEventFab);
        addEventFab.setOnClickListener(this);
        prepareViewPager(tabText);
        viewPager.setAdapter(sectionsPagerAdapter);       //Supply the viewpager with fragments added to the adapter
        tabLayout.setupWithViewPager(viewPager);      //Links the tab layout with viewpager
    }

    private void prepareViewPager(ArrayList<String> tabText) {
        //Initialize main adapter
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //Initialize discover events fragment
        DiscoverFragment fragment = new DiscoverFragment();

        for (int i=0; i < tabText.size(); i++) {
            Bundle bundle = new Bundle();       //This bundle contains parameters for the fragment
            bundle.putString("title",tabText.get(i));
            fragment.setArguments(bundle);
            sectionsPagerAdapter.addFragment(fragment, tabText.get(i));
            fragment = new DiscoverFragment();      //Create a new fragment for 'My Events'
        }
    }

    @Override
    public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}