package com.file.manager.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.file.manager.MainActivity;
import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileHandleUtil;

import java.io.File;

public class SingleFileRenameDialog extends Dialog implements View.OnClickListener {


    private CustomFile file;
    private TextView name;
    private OnRenameComplete complete;
    public SingleFileRenameDialog(Context context, CustomFile file){
        super(context);
        this.file=file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.single_file_rename_dialog);
        final Button cancel=findViewById(R.id.cancel);
        final Button okay=findViewById(R.id.okay);
        name=findViewById(R.id.name);
        name.setOnClickListener(this);
        cancel.setOnClickListener(this);
        okay.setOnClickListener(this);
        name.setText(file.getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.name:
                break;
            case R.id.okay:
                CustomFile nFile=new CustomFile(file.getPath().replaceFirst(file.getName(), name.getText().toString()));
                if(file.canWrite()) {
                    FileHandleUtil.renameTo(file, nFile);
                }else {
                    Uri uri= PermissionsHelper.getInstance().getUriFromSharedPreference(
                            new File(DiskUtils.getInstance().getStartDirectory(file)));
                    if(uri!=null) {
                        FileHandleUtil.SAFRenameTo(DocumentFile.fromTreeUri(getContext(), uri), file, nFile.getName());
                    }else {
                        Toast.makeText(getContext(),"Invalid storage choose proper directory!",Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
                nFile.setLocalThumbnail(file.getLocalThumbnail());
                complete.onComplete(nFile);
                dismiss();
                break;
            case R.id.cancel:
                cancel();
                break;
        }
    }

    public void setOnCompleteListener(OnRenameComplete complete){
        this.complete=complete;
    }
    public interface OnRenameComplete{
        void onComplete(CustomFile nFile);
    }
}
