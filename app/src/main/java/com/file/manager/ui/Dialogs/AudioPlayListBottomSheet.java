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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import com.file.manager.Fragments.MusicListFragment;
import com.file.manager.Fragments.PlayListFragment;
import com.file.manager.R;
import com.file.manager.ui.Adapters.PlayListPagerAdapter;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Models.PlayListHierarchy;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AudioPlayListBottomSheet extends BottomSheetDialogFragment {
    private ViewPager pager;
    private List<Fragment> fragments;
    private View navigationLayout;
    private View selectLayout;
    private Button addToPlayList;
    private TextView select_count;
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
        TextView music_count = root.findViewById(R.id.count);
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
        final MutableLiveData<String> musicListLiveData=musicListFragment.getMode();
        final MutableLiveData<String> playListLiveData=playListFragment.getMode();

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pages[position].setChecked(true);
                musicListFragment.updateMode("Navigation");
                addToPlayList.setText(position==0?"ADD TO PLAYLIST":"REMOVE FROM PLAYLIST");
                if(position==0){
                    MusicHelperSingleton.getInstance().getData().clear();
                    MusicHelperSingleton.getInstance().setPlayList("All Songs");
                    musicListFragment.updateAdapter();
                    musicListFragment.startAdapterUITimer();
                }else {
                    playListFragment.setSongs();
                    playListFragment.showMessageIfEmpty();
                    musicListFragment.stopAdapterUITimer();
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

        musicListLiveData.removeObservers(this);
        musicListLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
              toggleLayout(s);
            }
        });
        playListLiveData.removeObservers(this);
        playListLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                toggleLayout(s);
            }
        });

        addToPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if current fragment is music fragment
              if(pager.getCurrentItem()==0) {
                  AddPlayListDialog addPlayListDialog = new AddPlayListDialog(getContext(), musicListFragment.getSelected());
                  addPlayListDialog.setOnTaskCompleteListener(new AddPlayListDialog.OnItemCompleteListener() {
                      @Override
                      public void onComplete(PlayListHierarchy.PlayListModel parent, ArrayList<PlayListHierarchy.PlayListModel> array) {
                       playListFragment.add(parent.getName(),array);
                      }
                  });
                  addPlayListDialog.show();
              }else {
                  playListFragment.removeSelected();
              }
                musicListFragment.updateMode("Navigation");
            }
        });

        exitSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pager.getCurrentItem()==0)
                musicListFragment.updateMode("Navigation");
                else
                playListFragment.updateMode("Navigation");
            }
        });
        music_count.setText("Music("+MusicHelperSingleton.getInstance().getAllSongs().size()+")");

        // if the file name changes update the UI in the two fragments
        MutableLiveData<String> currentFileName= MusicHelperSingleton.getInstance().getCurrentFileName();
        currentFileName.removeObservers(this);
        currentFileName.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                int position=MusicHelperSingleton.getInstance().getCurrent();
                if(!MusicHelperSingleton.getInstance().getPlayList().equals("All Songs"))
                    playListFragment.highlightItem(position);
                else
                    musicListFragment.highlightItem(position);

            }
        });

        return root;
    }


    @SuppressLint("SetTextI18n")
    private void toggleLayout(String s){
        if(s.equals("Navigation")){
            navigationLayout.setVisibility(View.VISIBLE);
            selectLayout.setVisibility(View.INVISIBLE);
        }else if(s.equals("Select")){
            navigationLayout.setVisibility(View.INVISIBLE);
            selectLayout.setVisibility(View.VISIBLE);
            if(pager.getCurrentItem()==0){
                select_count.setText("SELECTED ITEMS("+musicListFragment.getSelectCount()+"/"+MusicHelperSingleton.getInstance().getAllSongs().size()+")");
            }else {
                select_count.setText("SELECTED ITEMS("+playListFragment.getSelectCount()+"/"+playListFragment.size()+")");
            }
        }
        addToPlayList.setEnabled(s.equals("Select"));
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
