package com.opendashcam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class WelcomeActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    /**
     * First fragment with basic app info
     */
    public static class FirstFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_LAYOUT_ID = "layout_id";

        public FirstFragment() {
        }

        /**
         * Returns a new instance of this fragment
         */
        public static FirstFragment newInstance(int layoutID) {
            FirstFragment fragment = new FirstFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_ID, layoutID);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(getArguments().getInt(ARG_LAYOUT_ID), container, false);

            // Check if we have "end welcome" button on this view
            Button endWelcomeButton = (Button) rootView.findViewById(R.id.end_welcome);

            if (endWelcomeButton != null) {
                endWelcomeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Set DB flag that first launch complete
                        // Access shared references file
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                                getString(R.string.db_first_launch_complete_flag),
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString(
                                getString(R.string.db_first_launch_complete_flag),
                                "true");
                        editor.apply();

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }

            // When user clicks on the demo "REC" widget, go to the next slide
            ImageView demoRecButton = (ImageView) rootView.findViewById(R.id.demo_rec_widget);

            if (demoRecButton != null) {
                demoRecButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((WelcomeActivity)getActivity())
                                .mViewPager
                                .setCurrentItem(getArguments().getInt(ARG_LAYOUT_ID) + 1, true);
                    }
                });
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return FirstFragment.newInstance(R.layout.fragment_welcome);
                case 1:
                    return FirstFragment.newInstance(R.layout.fragment_welcome_2);
                case 2:
                    return FirstFragment.newInstance(R.layout.fragment_welcome_3);
            }

            // default
            return FirstFragment.newInstance(R.layout.fragment_welcome);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
