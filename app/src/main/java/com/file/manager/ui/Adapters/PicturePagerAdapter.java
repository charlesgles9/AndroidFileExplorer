package com.file.manager.ui.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.file.manager.Fragments.PictureTabFragment;

import java.io.File;
import java.util.List;

public class PicturePagerAdapter extends FragmentPagerAdapter {

    private List<PictureTabFragment>fragments;

    public PicturePagerAdapter(FragmentManager manager, List<PictureTabFragment>fragments){
        super(manager);
       this.fragments=fragments;
    }



    public String getResolution(int position){
        return fragments.get(position).getResolution();
    }

    public File getFile(int position){
        return fragments.get(position).getFile();
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
