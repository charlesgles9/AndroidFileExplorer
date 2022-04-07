package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.utils.FileHandleUtil;

import java.io.File;

public class NewFolderDialog extends Dialog implements View.OnClickListener {

    private CustomFile directory;
    private TextView name;
    private TextView title;
    private onCompleteListener onCompleteListener;
    public NewFolderDialog(Context context, CustomFile directory){
        super(context);
        this.directory=directory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dilaog_single_file_rename);
        name=findViewById(R.id.name);
        title=findViewById(R.id.title);
        Button cancel=findViewById(R.id.cancel);
        Button okay=findViewById(R.id.okay);
        title.setText("Create Folder");
        name.setHint("Enter Folder Name");
        cancel.setOnClickListener(this);
        okay.setOnClickListener(this);

    }

    public void setOnCompleteListener(NewFolderDialog.onCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.okay){
            if(name.getText().toString().isEmpty()) {
                Toast.makeText(getContext(),"empty field!",Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                if(new File(directory,name.getText().toString()).exists()){
                    Toast.makeText(getContext(),"folder already exists!",Toast.LENGTH_SHORT).show();
                    return;
                }
                CustomFile file=FileHandleUtil.createFolder(getContext(),directory,name.getText().toString());
                onCompleteListener.onComplete(file);
                Toast.makeText(getContext(),"folder created!",Toast.LENGTH_SHORT).show();
                dismiss();
            }catch (NullPointerException ignored){
                Toast.makeText(getContext(),"failed to create folder!",Toast.LENGTH_SHORT).show();
            }
        }else{
           dismiss();
        }
    }

    public interface onCompleteListener{
        void onComplete(CustomFile folder);
    }
}
