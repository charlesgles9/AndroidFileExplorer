package com.file.manager.ui.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Models.PlayListHeader;

import java.util.ArrayList;
import java.util.List;

public class PlayListHeaderAdapter extends RecyclerView.Adapter<PlayListHeaderAdapter.PlayListViewHolder> {

    private LayoutInflater inflater;
    private List<PlayListHeader> headers;
    private OnItemClickListener onItemClickListener;
    public PlayListHeaderAdapter(Context context){
        this.inflater=LayoutInflater.from(context);
        this.headers=new ArrayList<>();
    }


    public void setHeaders(List<PlayListHeader> headers) {
        this.headers = headers;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= inflater.inflate(R.layout.audio_playlist_title_layout,parent,false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        PlayListHeader header=headers.get(position);
        holder.name.setText(header.getKey());
        holder.name.setTextColor(MusicHelperSingleton.getInstance().getCurrent()==position? Color.argb(200,69,155,241):
                Color.argb(200,255,255,255));
        holder.date.setText(header.getDate());
        holder.date.setTextColor(MusicHelperSingleton.getInstance().getCurrent()==position? Color.argb(200,69,155,241):
                Color.argb(200,255,255,255));
    }

    @Override
    public int getItemCount() {
        return headers.size();
    }

     class PlayListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        TextView date;
        public PlayListViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            date=view.findViewById(R.id.date);
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
    public interface  OnItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }
}