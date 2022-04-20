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

import java.util.ArrayList;
public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder> {


    private LayoutInflater inflater;

    public PlayListAdapter(Context context){

    }



    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= inflater.inflate(R.layout.audio_playlist_item_layout,parent,false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {

    }



    @Override
    public int getItemCount() {
        return 0;
    }

    class PlayListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        ToggleButton selected;
        public PlayListViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            selected=view.findViewById(R.id.selected);
            selected.setClickable(false);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {

            return false;
        }
    }

    public interface  OnItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }
}
