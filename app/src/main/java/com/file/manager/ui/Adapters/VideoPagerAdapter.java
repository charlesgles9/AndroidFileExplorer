package com.file.manager.ui.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.file.manager.Fragments.VideoTabFragment;

import java.util.List;

public class VideoPagerAdapter extends FragmentPagerAdapter {

    private List<VideoTabFragment> fragments;
    public VideoPagerAdapter(Context context, FragmentManager manager,List<VideoTabFragment> fragments){
        super(manager);
        this.fragments=fragments;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getName();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
