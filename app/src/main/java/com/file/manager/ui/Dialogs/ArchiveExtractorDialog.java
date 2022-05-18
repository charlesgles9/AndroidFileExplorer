package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.BackgroundServices.ArchiveDService;
import com.file.manager.OnTaskCompleteListener;
import com.file.manager.R;
import com.file.manager.helpers.MIMETypesHelper;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Adapters.ZipEntryAdapter;
import com.file.manager.ui.Adapters.ZipEntryDirectoryAdapter;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.utils.ArchiveDecompressUtil;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.Selector;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;

public class ArchiveExtractorDialog extends Dialog  {


    private ArchiveDecompressUtil.OnExtractCompleteCallback onExtractCompleteCallback;
    private ZipEntryAdapter zipEntryAdapter;
    private ZipEntryDirectoryAdapter entryDirectoryAdapter;
    private CustomFile file;
    private ArchiveDecompressUtil archiveDecompressUtil;
    private Context context;
    public ArchiveExtractorDialog(Context context, CustomFile file){
        super(context);
        this.file=file;
        this.context=context;
    }


    public void setOnExtractCompleteCallback(ArchiveDecompressUtil.OnExtractCompleteCallback onExtractCompleteCallback) {
        this.onExtractCompleteCallback = onExtractCompleteCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.archive_content_layout);
        RecyclerView fileList = findViewById(R.id.fileList);
        RecyclerView pathList = findViewById(R.id.pathList);
        Spinner spinner = findViewById(R.id.charSet);
        TextView name=findViewById(R.id.name);
        final TextView path=findViewById(R.id.path);
        final View back=findViewById(R.id.back);
        final Button extract=findViewById(R.id.extract);
        final Button cancel=findViewById(R.id.cancel);
        final ProgressBar progress=findViewById(R.id.progress);
        final ImageView more=findViewById(R.id.more);
        name.setText(file.getName());
        path.setText(file.getParent());
        final LinearLayoutManager manager1= new LinearLayoutManager(getContext());
        manager1.setOrientation(RecyclerView.VERTICAL);
        final LinearLayoutManager manager2=new LinearLayoutManager(getContext());
        manager2.setOrientation(RecyclerView.HORIZONTAL);
        fileList.setLayoutManager(manager1);
        pathList.setLayoutManager(manager2);

        String startDir= DiskUtils.getInstance().getStartDirectory(file);
            Uri uri= PermissionsHelper.getInstance().getUriFromSharedPreference(new File(startDir));
            archiveDecompressUtil= new ArchiveDecompressUtil(context,uri,file);
            archiveDecompressUtil.setDestination(path.getText().toString());
            archiveDecompressUtil.setOnExtractCompleteCallback(onExtractCompleteCallback);


        zipEntryAdapter= new ZipEntryAdapter(context,archiveDecompressUtil);
        fileList.setAdapter(zipEntryAdapter);
        zipEntryAdapter.setOnItemClickListener(new ZipEntryAdapter.OnItemClickListener() {
            @Override
            public void onClick(final FileHeader  header) {
                if(header.isDirectory()) {
                    archiveDecompressUtil.moveNext();
                    entryDirectoryAdapter.setDirectory(header.getFileName());
                    entryDirectoryAdapter.notifyDataSetChanged();
                    manager2.scrollToPosition(entryDirectoryAdapter.getItemCount());
                    zipEntryAdapter.notifyDataSetChanged();
                    if(archiveDecompressUtil.getDirKey()>archiveDecompressUtil.getFirstKey())
                        back.setVisibility(View.VISIBLE);
                }else {
                    final LoadingDialog loadingDialog= new LoadingDialog(getContext());
                    final ArchiveDecompressUtil.ExtractSingleTask task=archiveDecompressUtil.extract(header);
                    archiveDecompressUtil.setDestination(path.getText().toString());
                    task.setOnComplete(new ArchiveDecompressUtil.OnExtractCompleteCallback() {
                        @Override
                        public void onComplete(String dest) {
                            CustomFile file=new CustomFile(header.getFileName(),new File(dest));
                            Toast.makeText(getContext(),file.getPath(),Toast.LENGTH_SHORT).show();
                            Toast.makeText(getContext(),"Refresh folder to view extracted file",Toast.LENGTH_LONG).show();
                           if(file.exists())
                              new MIMETypesHelper(context,file).startDefault();
                              loadingDialog.dismiss();
                        }

                        @Override
                        public void onError() {
                            loadingDialog.dismiss();
                        }
                    });
                    loadingDialog.setDismissListener(new LoadingDialog.DismissListener() {
                        @Override
                        public void onDismiss() {
                            task.cancel();
                        }
                    });
                    loadingDialog.show();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            @Override
            public void onSelect(int position){
                archiveDecompressUtil.toggleSelect(position);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                archiveDecompressUtil.movePrev();
                zipEntryAdapter.notifyDataSetChanged();
                int position=archiveDecompressUtil.getDirKey();
                entryDirectoryAdapter.setDirectory(entryDirectoryAdapter.getString(position-1));
                entryDirectoryAdapter.notifyDataSetChanged();
                manager2.scrollToPosition(entryDirectoryAdapter.getItemCount());
                if(archiveDecompressUtil.getDirKey()<=archiveDecompressUtil.getFirstKey())
                    back.setVisibility(View.GONE);
            }
        });
        entryDirectoryAdapter= new ZipEntryDirectoryAdapter(getContext());
        entryDirectoryAdapter.setOnItemClickListener(new ZipEntryDirectoryAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
               archiveDecompressUtil.setDirKey(position+1);
               zipEntryAdapter.notifyDataSetChanged();
               entryDirectoryAdapter.setDirectory(entryDirectoryAdapter.getString(position));
               entryDirectoryAdapter.notifyDataSetChanged();
                manager2.scrollToPosition(entryDirectoryAdapter.getItemCount());
                if(position==0)
                    back.setVisibility(View.GONE);
            }
        });

        pathList.setAdapter(entryDirectoryAdapter);
        entryDirectoryAdapter.setDirectory("");
        final ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(getContext(),R.array.char_sets,R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  String charset=adapter.getItem(position).toString();
                  int start=charset.indexOf("(");
                  int end=charset.indexOf(")");
                  if(start==-1){
                      start=0;
                      end=charset.length();
                  }else start+=1;
                 archiveDecompressUtil.setCharset(charset.substring(start,end));
                zipEntryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        extract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(archiveDecompressUtil.isEncrypted()){
                        EnterPasswordDialog passwordDialog= new EnterPasswordDialog(context);
                        passwordDialog.setPasswordCallback(new EnterPasswordDialog.PasswordCallback() {
                            @Override
                            public void accept(String password) {
                                if(archiveDecompressUtil !=null){
                                    archiveDecompressUtil.setPassword(password);
                                    archiveDecompressUtil.setDestination(path.getText().toString());
                                    start();
                                }
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                        passwordDialog.show();
                    }else {
                        if(archiveDecompressUtil !=null){
                            archiveDecompressUtil.setDestination(path.getText().toString());
                          start();
                        }
                    }
                } catch (ZipException e) {
                    e.printStackTrace();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        findViewById(R.id.extractTo).setOnClickListener(new View.OnClickListener() {
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


       more.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               selectAllPopUp(v);
           }
       });

        archiveDecompressUtil.listFileHeadersTask(new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete() {
                if(!archiveDecompressUtil.getZipFile().isValidZipFile()){
                    Toast.makeText(getContext(),"This archive is corrupt!",Toast.LENGTH_LONG).show();
                }
                progress.setVisibility(View.GONE);
                zipEntryAdapter.notifyDataSetChanged();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

   private void selectAllPopUp(View anchor){
       final LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       assert inflater != null;
       View view=inflater.inflate(R.layout.dialog_select_all,null);
       final PopupWindow popupWindow= new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
       popupWindow.showAsDropDown(anchor,0,0, Gravity.RIGHT);
       final TextView textView=view.findViewById(R.id.selectAll);
       textView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
            for(int i=0;i<archiveDecompressUtil.getCurrentDirList().size();i++)
                archiveDecompressUtil.toggleSelect(i);
               zipEntryAdapter.notifyDataSetChanged();
              popupWindow.dismiss();
           }
       });
   }
    private void start(){
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
                        Intent intent= new Intent(context,ArchiveDService.class);
                        Selector.getInstance().add(archiveDecompressUtil);
                        context.startService(intent);
                        choice=taskDialogPrompt.rememberChoice()?"Background Always":choice;
                    }else {
                        ZipExtractDialog zipExtractDialog= new ZipExtractDialog(context, archiveDecompressUtil);
                        zipExtractDialog.show();
                        choice=taskDialogPrompt.rememberChoice()?"Foreground Always":choice;
                    }
                    editor.putString("zip", choice);
                    editor.apply();
                    dismiss();
                }
            });
            taskDialogPrompt.show();
        }else if(copy.equals("Background Always")){
            Intent intent= new Intent(context,ArchiveDService.class);
            Selector.getInstance().add(archiveDecompressUtil);
            context.startService(intent);
            dismiss();
        }else {
            ZipExtractDialog zipExtractDialog= new ZipExtractDialog(context, archiveDecompressUtil);
            zipExtractDialog.show();
            dismiss();
        }
    }
}
