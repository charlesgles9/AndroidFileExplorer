package com.file.manager.ui.Dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.viewpager.widget.ViewPager;

import com.file.manager.Fragments.MusicListFragment;
import com.file.manager.Fragments.PlayListFragment;
import com.file.manager.R;
import com.file.manager.ui.Adapters.MusicAdapter;
import com.file.manager.ui.Adapters.PlayListPagerAdapter;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Models.PlayListChild;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class AudioPlayListBottomSheet extends BottomSheetDialogFragment {
    private ViewPager pager;
    private List<Fragment> fragments;
    private View navigationLayout;
    private View selectLayout;
    private Button addToPlayList;
    private TextView select_count;
    private  TextView titleCount;
    private MusicListFragment musicListFragment;
    private PlayListFragment  playListFragment;
    private int currentPage=0;
    public AudioPlayListBottomSheet(@NonNull Context context,List<Fragment>fragments) {
        this.fragments=fragments;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(true);
        View root=inflater.inflate(R.layout.audio_play_list_bottom_sheet,container,false);
        navigationLayout=root.findViewById(R.id.navLayout);
        selectLayout=root.findViewById(R.id.selectLayout);
        addToPlayList=root.findViewById(R.id.addToPlayList);
        titleCount = root.findViewById(R.id.title_count);
        select_count=root.findViewById(R.id.selectText);
        final View exitSelect=root.findViewById(R.id.exitSelect);
        pager=root.findViewById(R.id.pager);
        ImageView close=root.findViewById(R.id.close);

        PlayListPagerAdapter adapter = new PlayListPagerAdapter(getChildFragmentManager(), fragments);
        pager.setAdapter(adapter);
        pager.setCurrentItem(currentPage);
        final RadioButton[]pages ={root.findViewById(R.id.firstPage),root.findViewById(R.id.secondPage)};
        musicListFragment=(MusicListFragment)fragments.get(0);
        playListFragment=(PlayListFragment)fragments.get(1);


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pages[position].setChecked(true);
                if(position==0){
                    if(musicListFragment.getAdapter().isActivateSelect()) {
                        setTitle("SELECTED(" + musicListFragment.getAdapter().getSelectedFiles().size() + "/"
                                + musicListFragment.getAdapter().getItemCount() + ")");
                    }else
                        setTitle("Music("+musicListFragment.getAdapter().getItemCount()+")");
                }else {
                    setTitle("PlayList("+musicListFragment.getAdapter().getItemCount()+")");
                }
                currentPage=position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        for(int i=0;i<pages.length;i++){
            radioButtonPageClickListener(i,pages[i]);
        }


        musicListFragment.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
               if(!musicListFragment.getAdapter().isActivateSelect()) {
                   MusicHelperSingleton.getInstance().play(position);
               }else {
                   PlayListChild child= musicListFragment.getAdapter().get(position);
                   child.setSelected(!child.isSelected());
                   if(child.isSelected()){
                       musicListFragment.getAdapter().getSelectedFiles().add(child);
                   }else
                       musicListFragment.getAdapter().getSelectedFiles().remove(child);

               }
                musicListFragment.getAdapter().notifyItemChanged(position);

            }

            @Override
            public void onLongClick(int position) {
              musicListFragment.getAdapter().setActivateSelect(true);
              selectLayout.setVisibility(View.VISIBLE);
              navigationLayout.setVisibility(View.INVISIBLE);
            }
        });

        exitSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLayout.setVisibility(View.INVISIBLE);
                navigationLayout.setVisibility(View.VISIBLE);
                musicListFragment.getAdapter().getSelectedFiles().clear();
            }
        });
        return root;
    }


    private void setTitle(String title){
        titleCount.setText(title);
    }

    private void radioButtonPageClickListener(final int position,RadioButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(position);
            }
        });
    }

}
