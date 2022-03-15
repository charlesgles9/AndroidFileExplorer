package com.file.manager.ui.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
;

import java.util.List;

public class PlayListPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    public PlayListPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments){
        super(fragmentManager);
        this.fragments=fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }
}
