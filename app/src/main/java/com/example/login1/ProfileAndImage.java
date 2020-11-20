package com.example.login1;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileAndImage extends FragmentActivity {
    boolean alreadySet = false;

    private String[] tabNames = {"Image", "Profile"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_and_image);
        //getSupportActionBar().hide();

        ViewPager2 viewPager = findViewById(R.id.viewpager);

        PagerAdapter pa = new PagerAdapter(this);
        viewPager.setAdapter(pa);



        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabNames[position]);
            }

        }).attach();
    }

}