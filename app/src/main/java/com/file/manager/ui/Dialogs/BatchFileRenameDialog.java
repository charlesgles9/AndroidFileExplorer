package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Models.BatchRenameModel;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileHandleUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;

public class BatchFileRenameDialog extends Dialog implements View.OnClickListener {


    private ArrayList<BatchRenameModel>models= new ArrayList<>();
    private OnRenameComplete complete;
    public BatchFileRenameDialog(Context context, ArrayList<CustomFile>files){
        super(context);
        for ( CustomFile customFile : files) {
            models.add(new BatchRenameModel(customFile));
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setContentView(R.layout.dialog_batch_file_rename);

        final Button preview=findViewById(R.id.preview);
        final Button cancel=findViewById(R.id.cancel);
        final Button okay=findViewById(R.id.okay);
        preview.setOnClickListener(this);
        cancel.setOnClickListener(this);
        okay.setOnClickListener(this);
        final TextInputEditText prefix= findViewById(R.id.prefix);
        final TextInputEditText postfix=findViewById(R.id.postfix);
        final TextInputEditText findText=findViewById(R.id.findText);
        final TextInputEditText replaceText=findViewById(R.id.replaceWithText);
        final TextInputEditText startNumber=findViewById(R.id.startNumber);
        final TextInputEditText numberFormatText=findViewById(R.id.numberFormatText);
        prefix.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPostFixPrefix(s.toString(),postfix.getText().toString());
                findAndReplaceText(findText.getText().toString(),replaceText.getText().toString());
                try {
                    numbering(Integer.parseInt(s.toString()),numberFormatText.getText().toString());
                }catch (NumberFormatException ne){}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        postfix.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               setPostFixPrefix(prefix.getText().toString(),s.toString());
               findAndReplaceText(findText.getText().toString(),replaceText.getText().toString());
                try {
                    numbering(Integer.parseInt(s.toString()),numberFormatText.getText().toString());
                }catch (NumberFormatException ne){}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPostFixPrefix(prefix.getText().toString(),postfix.getText().toString());
                findAndReplaceText(s.toString(),replaceText.getText().toString());
                try {
                    numbering(Integer.parseInt(s.toString()),numberFormatText.getText().toString());
                }catch (NumberFormatException ne){}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        replaceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPostFixPrefix(prefix.getText().toString(),postfix.getText().toString());
                findAndReplaceText(findText.getText().toString(),s.toString());
                try {
                    numbering(Integer.parseInt(s.toString()),numberFormatText.getText().toString());
                }catch (NumberFormatException ne){}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        startNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPostFixPrefix(prefix.getText().toString(),postfix.getText().toString());
                findAndReplaceText(findText.getText().toString(),replaceText.getText().toString());
                try {
                    numbering(Integer.parseInt(s.toString()),numberFormatText.getText().toString());
                }catch (NumberFormatException ne){}

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        numberFormatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPostFixPrefix(prefix.getText().toString(),postfix.getText().toString());
                findAndReplaceText(findText.getText().toString(),replaceText.getText().toString());
                try {
                    numbering(Integer.parseInt(s.toString()),numberFormatText.getText().toString());
                }catch (NumberFormatException ne){}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void setPostFixPrefix(String pre,String post){
        resetName();
        setPostFix(post);
        setPrefix(pre);
    }
    private void setPrefix(String s){

        for(BatchRenameModel model:models){
            model.setRename( s+model.getRename());
        }
    }
    final StringBuffer buffer= new StringBuffer();
    private void setPostFix(String s){
        for(BatchRenameModel model:models){
            buffer.append(model.getRename());
            // in case it has a file extension name
            if(buffer.toString().contains(".")&model.getFile().isFile()) {
                buffer.insert(buffer.lastIndexOf("."), s);
            }else{
                buffer.append(s);
            }

            model.setRename(buffer.toString());
            buffer.delete(0,buffer.length());
        }
    }

    public void findAndReplaceText(String find,String replace){
       if(find!="")
         for(BatchRenameModel model:models){
             model.setRename(model.getRename().replace(find,replace));
         }
    }


    public void numbering(int start,String format){

        try {
            format=Integer.parseInt(format)==0?"1":format;
        }catch (NumberFormatException ne){
            format="1";
        }
        for(BatchRenameModel model:models){
            buffer.append(model.getRename());
            String s=String.format("%0"+format+"d",start);
            // in case it has a file extension name
            if(buffer.toString().contains(".")&model.getFile().isFile()) {
                buffer.insert(buffer.lastIndexOf("."), s);
            }else{
                buffer.append(s);
            }

            model.setRename(buffer.toString());
            buffer.delete(0,buffer.length());
            start++;
        }
    }

    public void resetName(){
        for(BatchRenameModel model:models){
            model.setRename(model.getFile().getName());
        }
    }

    private void rename(){
        for(BatchRenameModel model:models) {
            if(model.getRename().equals(model.getFile().getName()))
                continue;
            CustomFile nFile = new CustomFile(model.getFile().getPath().replaceFirst(model.getFile().getName(), model.getRename()));
            if(nFile.canWrite()) {
                FileHandleUtil.renameTo(model.getFile(), nFile);
            }else {
                Uri uri = PermissionsHelper.getInstance().getUriFromSharedPreference(
                        new File(DiskUtils.getInstance().getStartDirectory(nFile)));
                if (uri != null) {
                    FileHandleUtil.SAFRenameTo(DocumentFile.fromTreeUri(getContext(), uri), model.getFile(), model.getRename());
                } else {
                    Toast.makeText(getContext(), "Invalid storage choose proper directory!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
            if(nFile.exists()){
                nFile.setLocalThumbnail(model.getFile().getLocalThumbnail());
            }
            complete.update(model.getFile(),nFile);
        }

        complete.complete();
    }

    public void setOnCompleteListener(BatchFileRenameDialog.OnRenameComplete complete){
        this.complete=complete;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.preview:
                final BatchRenamePreviewDialog previewDialog= new BatchRenamePreviewDialog(getContext(),models);
                previewDialog.show();
                break;
            case R.id.cancel:
                cancel();
                break;
            case R.id.okay:
                rename();
                cancel();
                break;
        }
    }
   public interface OnRenameComplete{
        void update(CustomFile oFile,CustomFile nFile);
        void complete();
    }
}
