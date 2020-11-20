package com.example.login1;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter
{

    final int pageCount = 2;
    //private String tabTitles[] = {"Image", "Profile"};
    public PagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        switch(position){
            case 0:
                Log.d("position", "0");
                return new ViewPage();
            case 1:
                Log.d("position", "1");
                return new ProfilePage();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }
}
