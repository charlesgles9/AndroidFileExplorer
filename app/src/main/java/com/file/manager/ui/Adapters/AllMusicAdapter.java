package com.file.manager.ui.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.MusicHelperSingleton;

import java.util.ArrayList;

public class AllMusicAdapter extends RecyclerView.Adapter<AllMusicAdapter.ListViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<CustomFile>files;
    private OnItemClickListener onItemClickListener;
    private boolean activateSelect;
    private int highlight=-1;
    private int selectCount=0;
    public AllMusicAdapter(Context context, ArrayList<CustomFile>files){
        this.inflater=LayoutInflater.from(context);
        this.files=files;
        this.highlight= MusicHelperSingleton.getInstance().getCurrent();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.audio_playlist_item_layout,parent,false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        CustomFile file=files.get(position);
        holder.name.setText(file.getName());
        holder.name.setTextColor(highlight==position? Color.argb(200,69,155,241):
                Color.argb(200,255,255,255));
        holder.selected.setChecked(file.IsSelected());
        holder.selected.setVisibility(isActivateSelect()?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public int getSelectCount() {
        return selectCount;
    }

    public void setSelectCount(int selectCount) {
        this.selectCount = selectCount;
    }


    public void setActivateSelect(boolean activateSelect) {
        this.activateSelect = activateSelect;
    }

    public boolean isActivateSelect() {
        return activateSelect;
    }

    public void setHighlight(int highlight) {
        this.highlight = highlight;
    }

    public int getHighlight() {
        return highlight;
    }

    public void reset(){
        for(CustomFile file:files){
            file.setSelected(false);
        }
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        ToggleButton selected;
        public ListViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            selected=view.findViewById(R.id.selected);
            selected.setClickable(false);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener!=null)
                onItemClickListener.onClick(getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View v) {
            if(onItemClickListener!=null)
                onItemClickListener.onLongClick(getAdapterPosition());
            return false;
        }
    }

    public interface OnItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }
}
