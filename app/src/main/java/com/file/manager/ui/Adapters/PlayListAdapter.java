package com.file.manager.ui.Adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.file.manager.R;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Models.PlayListChild;
import com.file.manager.ui.Models.PlayListHeader;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder> {


    private LayoutInflater inflater;
    private PlayListHeader header;
    private OnItemClickListener onItemClickListener;
    public PlayListAdapter(Context context,PlayListHeader header){
       this.header=header;
       this.inflater=LayoutInflater.from(context);
    }



    public void setHeader(PlayListHeader header) {
        this.header = header;
    }

    public PlayListHeader getHeader() {
        return header;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= inflater.inflate(R.layout.audio_playlist_item_layout,parent,false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        PlayListChild child=header.get(position);
        holder.name.setText(child.getName());
        holder.name.setTextColor(MusicHelperSingleton.getInstance().getCurrent()==position? Color.argb(200,69,155,241):
                Color.argb(200,255,255,255));
        holder.selected.setChecked(child.isSelected());
        holder.delete.setVisibility(View.VISIBLE);
    }


    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return header!=null?header.size():0;
    }


     class PlayListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        ToggleButton selected;
        ImageView delete;
        public PlayListViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            selected=view.findViewById(R.id.selected);
            delete=view.findViewById(R.id.delete);
            selected.setClickable(false);
            delete.setOnClickListener(this);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position=getAdapterPosition();
           if(onItemClickListener!=null) {
               if(v.getId()==R.id.delete)
                   onItemClickListener.onDelete(position);
               else
                  onItemClickListener.onClick(position);
           }
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
        void onDelete(int position);
    }
}
