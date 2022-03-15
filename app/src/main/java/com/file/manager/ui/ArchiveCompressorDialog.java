package com.file.manager.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.file.manager.BackgroundServices.ArchiveCService;
import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.utils.ArchiveCompressUtil;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.Selector;
import com.google.android.material.textfield.TextInputEditText;

import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ArchiveCompressorDialog extends Dialog {

    private Context context;
    private ArchiveCompressUtil archiveCompressUtil;
    private ArrayList<File>files= new ArrayList<>();
    private ArchiveCompressUtil.OnCompressionCompleteCallback completeCallback;
    public ArchiveCompressorDialog(Context context, ArrayList<CustomFile> list){
        super(context);
        this.context=context;
        for(CustomFile file:list){
            this.files.add(new File(file.getPath()));
        }
        String startDir= DiskUtils.getInstance().getStartDirectory(files.get(0));
        Uri uri= PermissionsHelper.getInstance().getUriFromSharedPreference(
                new File(startDir));
        archiveCompressUtil = new ArchiveCompressUtil(context,uri,files);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.compress_layout);
        final ArrayAdapter<CharSequence> levelAdapter;
        final ArrayAdapter<CharSequence> methodAdapter;
        final TextInputEditText name=findViewById(R.id.name);
        final TextInputEditText password=findViewById(R.id.password);
        final ToggleButton showPassword=findViewById(R.id.showPassword);
        final TextView path=findViewById(R.id.path);
        final Button cancel=findViewById(R.id.cancel);
        final Button okay=findViewById(R.id.okay);
        final Button extractTo=findViewById(R.id.extractTo);
        final Spinner level=findViewById(R.id.level);
        final Spinner method=findViewById(R.id.method);

        int lastIndex=files.get(0).getName().lastIndexOf(".");
        lastIndex=lastIndex!=-1?lastIndex:files.get(0).getName().length();
        name.setText(files.get(0).getName().substring(0,lastIndex));
        path.setText(files.get(0).getParent());

        final Map<String, CompressionLevel>lmap=ArchiveCompressUtil.getCompressionLevels();
        String[]adapterItems=new String[lmap.size()];
        Iterator<Map.Entry<String,CompressionLevel>> iterator=lmap.entrySet().iterator();
        for(int i=0;i<lmap.size();i++){
            adapterItems[i]=iterator.next().getKey();
        }
        levelAdapter= new ArrayAdapter<CharSequence>(context,R.layout.spinner_item,adapterItems);
        levelAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        level.setAdapter(levelAdapter);

        final Map<String, CompressionMethod>cmap=ArchiveCompressUtil.getCompressionMethod();
        String[]cAdapterItems=new String[cmap.size()];
        Iterator<Map.Entry<String,CompressionMethod>>cIterator=cmap.entrySet().iterator();
        for (int i=0;i<cmap.size();i++){
            cAdapterItems[i]=cIterator.next().getKey();
        }
        methodAdapter= new ArrayAdapter<CharSequence>(context,R.layout.spinner_item,cAdapterItems);
        methodAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        method.setAdapter(methodAdapter);
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.setInputType(showPassword.isChecked()?InputType.TYPE_TEXT_VARIATION_NORMAL:
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                archiveCompressUtil.setToLocation( new File(path.getText().toString()));
                archiveCompressUtil.setName(name.getText().toString());
                archiveCompressUtil.setPassword(password.getText().toString());
                archiveCompressUtil.setCompressionLevel(lmap.get( levelAdapter.getItem(0)));
                archiveCompressUtil.setOnCompressionCompleteCallback(completeCallback);
               /* ZipCompressDialog zipCompressDialog= new ZipCompressDialog(context,archiveCompressUtil);
                zipCompressDialog.show();*/
                final SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getContext());
                final SharedPreferences.Editor editor=preferences.edit();
                final String copy=preferences.getString("zip","Ask me Always");
                boolean askMeAlways=copy.equals("Ask me Always");
                if(askMeAlways){
                    final TaskDialogPrompt taskDialogPrompt = new TaskDialogPrompt(getContext());
                    taskDialogPrompt.setOnItemClickListener(new TaskDialogPrompt.OnItemClickListener() {
                        @Override
                        public void onClick() {
                            String choice="Ask me Always";
                            if(taskDialogPrompt.isForeground()){
                                Intent intent= new Intent(context, ArchiveCService.class);
                                Selector.getInstance().add(archiveCompressUtil);
                                context.startService(intent);
                                choice=taskDialogPrompt.rememberChoice()?"Background Always":choice;
                            }else {
                                ZipCompressDialog zipCompressDialog= new ZipCompressDialog(context,archiveCompressUtil);
                                zipCompressDialog.show();
                                choice=taskDialogPrompt.rememberChoice()?"Foreground Always":choice;
                            }
                            editor.putString("zip", choice);
                            editor.apply();
                            dismiss();
                        }
                    });
                    taskDialogPrompt.show();
                }else if(copy.equals("Background Always")){
                    Intent intent= new Intent(context, ArchiveCService.class);
                    Selector.getInstance().add(archiveCompressUtil);
                    context.startService(intent);
                    dismiss();
                }else {
                    ZipCompressDialog zipCompressDialog= new ZipCompressDialog(context,archiveCompressUtil);
                    zipCompressDialog.show();
                    dismiss();
                }


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              dismiss();
            }
        });

        extractTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderPickerDialog pickerDialog= new FolderPickerDialog(context,2);
                pickerDialog.setOnDirPickedListener(new FolderPickerDialog.OnDirPickedListener() {
                    @Override
                    public void picked(String str) {
                        path.setText(str);
                    }

                    @Override
                    public void cancelled(String str) {

                    }
                });
                pickerDialog.show();
            }
        });
        level.setSelection(0);
        level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                archiveCompressUtil.setCompressionLevel(lmap.get(levelAdapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        method.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        archiveCompressUtil.setMethod(cmap.get(methodAdapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void setCompleteCallback(ArchiveCompressUtil.OnCompressionCompleteCallback completeCallback) {
        this.completeCallback = completeCallback;
    }
}
