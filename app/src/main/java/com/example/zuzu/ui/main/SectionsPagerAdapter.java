package com.example.zuzu.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.zuzu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the tabs.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

//    @StringRes
//    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
//    private final Context mContext;
    private ArrayList<String> arrayList = new ArrayList<>();
    private List<Fragment> fragmentList = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
//        mContext = context;
    }

    public void addFragment(Fragment fragment, String title) {
        arrayList.add(title);
        fragmentList.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        //return DiscoverFragment.newInstance(position + 1);
        return fragmentList.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //Return array list position
//        return mContext.getResources().getString(TAB_TITLES[position]);
        return arrayList.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        //return 2;
        return fragmentList.size();
    }
}