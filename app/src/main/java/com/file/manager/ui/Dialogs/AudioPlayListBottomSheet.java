package com.file.manager.ui.Dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import java.util.ArrayList;
import java.util.List;

public class AudioPlayListBottomSheet extends BottomSheetDialogFragment {
    private ViewPager pager;
    private View navigationLayout;
    private View selectLayout;
    private View addToPlayList;
    private TextView select_count;
    private  TextView titleCount;
    private MusicListFragment musicListFragment;
    private PlayListFragment  playListFragment;
    private List<Fragment> fragments= new ArrayList<>();
    private int currentPage=0;
    public AudioPlayListBottomSheet() { }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(true);
        View root=inflater.inflate(R.layout.audio_play_list_bottom_sheet,container,false);
        navigationLayout=root.findViewById(R.id.navLayout);
        selectLayout=root.findViewById(R.id.selectLayout);
        addToPlayList=root.findViewById(R.id.bottom_options);
        titleCount = root.findViewById(R.id.title_count);
        select_count=root.findViewById(R.id.selectText);
        final View exitSelect=root.findViewById(R.id.exitSelect);
        pager=root.findViewById(R.id.pager);
        ImageView close=root.findViewById(R.id.close);
        addPlayListFragment();
        PlayListPagerAdapter adapter = new PlayListPagerAdapter(getChildFragmentManager(), fragments);
        pager.setAdapter(adapter);
        pager.setCurrentItem(currentPage);
        final RadioButton[]pages ={root.findViewById(R.id.firstPage),root.findViewById(R.id.secondPage)};

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pages[position].setChecked(true);
                updateTitle();
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
                   int previous=MusicHelperSingleton.getInstance().getCurrent();
                   MusicHelperSingleton.getInstance().play(position);
                   musicListFragment.getAdapter().notifyItemChanged(previous);
               }else {
                   PlayListChild child= musicListFragment.getAdapter().get(position);
                   child.setSelected(!child.isSelected());
                   if(child.isSelected()){
                       musicListFragment.getAdapter().getSelectedFiles().add(child);
                   }else
                       musicListFragment.getAdapter().getSelectedFiles().remove(child);
                   updateTitle();

               }
                musicListFragment.getAdapter().notifyItemChanged(position);

            }

            @Override
            public void onLongClick(int position) {
              musicListFragment.getAdapter().setActivateSelect(true);
              musicListFragment.getAdapter().notifyDataSetChanged();
              navigationLayout.setVisibility(View.INVISIBLE);
              animateView(selectLayout,true);
              animateView(addToPlayList,true);
              addToPlayList.setVisibility(View.VISIBLE);
              updateTitle();
            }
        });

        exitSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicListFragment.getAdapter().setActivateSelect(false);
                selectLayout.setVisibility(View.INVISIBLE);
                animateView(navigationLayout,true);
                addToPlayList.setVisibility(View.GONE);
                musicListFragment.getAdapter().resetSelectedFiles();
                updateTitle();
            }
        });

        return root;
    }


    @SuppressLint("SetTextI18n")
    private void updateTitle(){
        if(musicListFragment.getAdapter().isActivateSelect()) {
            select_count.setText("SELECTED(" + musicListFragment.getAdapter().getSelectedFiles().size() + "/"
                    + musicListFragment.getAdapter().getItemCount() + ")");
        }else
            titleCount.setText("Music("+musicListFragment.getAdapter().getItemCount()+")");
    }

    private void addPlayListFragment(){
        if(!fragments.isEmpty())
            return;
        MusicHelperSingleton.getInstance().setPlayList("All Songs");
        musicListFragment=new MusicListFragment(new Updatable() {
            @Override
            public void update() {
                updateTitle();
            }});
        playListFragment=new PlayListFragment();
        fragments.add(musicListFragment);
        fragments.add(playListFragment);
    }

    private void radioButtonPageClickListener(final int position,RadioButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(position);
            }
        });
    }

    private void animateView(final View view, final boolean visible){
        Animation animation;
        if(visible)
            animation= AnimationUtils.loadAnimation(getContext(),R.anim.slide_down);
        else
            animation= AnimationUtils.loadAnimation(getContext(),R.anim.slide_up);
        view.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    public interface Updatable{
        void update();
    }
}
