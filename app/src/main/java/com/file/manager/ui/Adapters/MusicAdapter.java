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
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Models.PlayListChild;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ListViewHolder> {

    private LayoutInflater inflater;
    private List<PlayListChild> files;
    private List<PlayListChild>selectedFiles;
    private OnItemClickListener onItemClickListener;
    private boolean activateSelect;

    public MusicAdapter(Context context, List<PlayListChild>files){
        this.inflater=LayoutInflater.from(context);
        this.files=files;
        this.selectedFiles= new ArrayList<>();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.audio_playlist_item_layout,parent,false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        PlayListChild file=files.get(position);
        holder.name.setText(file.getName());
        holder.name.setTextColor(MusicHelperSingleton.getInstance().getCurrent()==position? Color.argb(200,69,155,241):
                Color.argb(200,255,255,255));
        holder.selected.setChecked(file.isSelected());
        holder.selected.setVisibility(isActivateSelect()?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public PlayListChild get(int position){
        return files.get(position);
    }


    public  void resetSelectedFiles(){
        for (PlayListChild child : selectedFiles) {
            child.setSelected(false);
        }
        selectedFiles.clear();
        notifyDataSetChanged();
    }
    public List<PlayListChild> getSelectedFiles() {
        return selectedFiles;
    }

    public void setActivateSelect(boolean activateSelect) {
        this.activateSelect = activateSelect;
    }

    public boolean isActivateSelect() {
        return activateSelect;
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
