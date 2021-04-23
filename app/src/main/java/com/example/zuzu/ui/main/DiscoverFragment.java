package com.example.zuzu.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.zuzu.R;


public class DiscoverFragment extends Fragment {

    //private static final String ARG_SECTION_NUMBER = "section_number";
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//    private String mParam1;
//    private String mParam2;

    //private PageViewModel pageViewModel;
    public DiscoverFragment() {
        //Empty constructor
    }

//    public static DiscoverFragment newInstance(String param1, String param2) {
////        DiscoverFragment fragment = new DiscoverFragment();
////        Bundle bundle = new Bundle();
////        bundle.putInt(ARG_SECTION_NUMBER, index);
////        fragment.setArguments(bundle);
////        return fragment;
//        DiscoverFragment fragment = new DiscoverFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM1, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
//        int index = 1;
//        if (getArguments() != null) {
//            index = getArguments().getInt(ARG_SECTION_NUMBER);
//        }
//        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        final TextView textView = root.findViewById(R.id.section_label);
//        pageViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        //Initialize view and inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        //Initialize and assign variable
        TextView textView = view.findViewById(R.id.section_label);

        //Get title
        String sTitle = this.getArguments().getString("title");

        //Set title on text view
        textView.setText(sTitle);

        return view;
    }
}