package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.PropertyModel;
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;

import java.io.File;
import java.util.ArrayList;

public class PhotoInfoDialog extends Dialog {

    private Context context;
    private File file;
    private String resolution;
    public PhotoInfoDialog(Context context,String resolution, File file){
        super(context);
        this.context=context;
        this.file=file;
        this.resolution=resolution;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dialog_properties_layout);
        final RecyclerView recyclerView=findViewById(R.id.list);
        final LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new PhotoPropertyAdapter());
        findViewById(R.id.okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    private void copyTextToClipBoard(String text){
        ClipboardManager clipboardManager=(ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData=ClipData.newPlainText("Text Copied",text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getContext(),"Text Copied!",Toast.LENGTH_SHORT).show();
    }

    class PhotoPropertyAdapter extends RecyclerView.Adapter<PhotoInfoViewHolder> {

        private ArrayList<PropertyModel> properties = new ArrayList<>();
        private LayoutInflater inflater;

        public PhotoPropertyAdapter() {
            inflater = LayoutInflater.from(context);
            properties.add(new PropertyModel("name",file.getName()));
            properties.add(new PropertyModel("path",file.getPath()));
            properties.add(new PropertyModel("date", DateUtils.getDateString(file.lastModified())));
            String extension= FileFilters.getExtension(file.getName());
            try {
                properties.add(new PropertyModel("mimeType", MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1))));
            }catch (Exception ignore){}
            properties.add(new PropertyModel("extension",extension));
            properties.add(new PropertyModel("size", DiskUtils.getInstance().getSize(file)));
            properties.add(new PropertyModel("Resolution",resolution));
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoInfoViewHolder holder, int position) {
           final PropertyModel model=properties.get(position);
            holder.title.setText(model.getTitle());
            holder.details.setText(model.getDetails());
        }

        @NonNull
        @Override
        public PhotoInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=inflater.inflate(R.layout.dialog_file_properties_details,parent,false);
            return new PhotoInfoViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return properties.size();
        }
    }
        class PhotoInfoViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
            TextView title;
            TextView details;
            public PhotoInfoViewHolder(@NonNull View itemView) {
                super(itemView);
                title=itemView.findViewById(R.id.title);
                details=itemView.findViewById(R.id.details);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                copyTextToClipBoard(details.getText().toString());
                return false;
            }
        }
    }

