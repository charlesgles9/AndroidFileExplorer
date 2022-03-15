package com.file.manager.Tools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.file.manager.BackgroundServices.FTPService;
import com.file.manager.IOnBackPressed;
import com.file.manager.Activities.MainActivity;
import com.file.manager.R;
import com.file.manager.ui.FolderPickerDialog;
import com.file.manager.ui.Models.FtpServerInstance;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.SoftwareKeyboardListener;
import com.file.manager.ui.utils.WifiInfoUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FtpServerFragment extends Fragment implements IOnBackPressed {


    private Fragment parent;
    private MainActivity activity;
    private View root;
    public FtpServerFragment(Fragment parent){
        this.parent=parent;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // create only one instance
       if(root==null)
        root=inflater.inflate(R.layout.fragment_ftp_layout,container,false);
        Toolbar toolbar=root.findViewById(R.id.toolbar);
        activity=(MainActivity)getContext();
        activity.toolbar.setVisibility(View.GONE);
        final TextInputEditText name=root.findViewById(R.id.name);
        final TextInputEditText port=root.findViewById(R.id.port);
        final TextInputEditText pass=root.findViewById(R.id.password);
        final TextView dir=root.findViewById(R.id.path);
        final Button pick_dir=root.findViewById(R.id.pick_dir);
        final Button startServer=root.findViewById(R.id.start_server);
        dir.setText(DiskUtils.getInstance().getStorageDirs()[0].getPath());
        final SoftwareKeyboardListener softwareKeyboardListener=(SoftwareKeyboardListener)root;
        final String[]prev={dir.getText().toString()};
        pick_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderPickerDialog folderPickerDialog= new FolderPickerDialog(getContext(),2);
                folderPickerDialog.setOnDirPickedListener(new FolderPickerDialog.OnDirPickedListener() {
                    @Override
                    public void picked(String path) {
                        dir.setText(path);
                        prev[0]=path;
                    }

                    @Override
                    public void cancelled(String path) {
                         dir.setText(prev[0]);
                    }
                });
                folderPickerDialog.show();
            }
        });


        final List<View> views=new ArrayList<>(Arrays.asList((View)name,port,pass,pick_dir));
        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(FtpServerInstance.getInstance().isRunning()) {
                    FtpServerInstance.getInstance().stop();
                    toggleStartServerUI(startServer);
                    disableViews(!FtpServerInstance.getInstance().isRunning(),views);
                    return;
                }

                if(!WifiInfoUtil.isWifiEnabled(getContext().getApplicationContext())){
                    Toast.makeText(getContext(),"WIFI is disabled!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!WifiInfoUtil.isNetworkConnected(getContext().getApplicationContext())){
                    Toast.makeText(getContext(),"No Connection Available!",Toast.LENGTH_SHORT).show();
                    return;
                }

                FtpServerInstance serverInstance=FtpServerInstance.getInstance();
                serverInstance.setName(name.getText().toString());
                serverInstance.setPath(dir.getText().toString());
                serverInstance.setIpAddress(WifiInfoUtil.getWifiIpAddress(getContext().getApplicationContext()));
                try {
                    serverInstance.setPort(Integer.parseInt(port.getText().toString()));
                }catch (NumberFormatException ignore){
                    Toast.makeText(getContext(),"invalid port",Toast.LENGTH_SHORT).show();
                    return;
                }
                serverInstance.setPassword(pass.getText().toString());
                serverInstance.getLiveData().removeObservers(getViewLifecycleOwner());
                serverInstance.getLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean stopped) {
                        if(stopped){
                            disableViews(!FtpServerInstance.getInstance().isRunning(),views);
                            toggleStartServerUI(startServer);
                        }
                    }
                });
                try {
                    serverInstance.start();
                }catch (Exception ignore){
                    Toast.makeText(getContext(),"Failed to create server!",Toast.LENGTH_SHORT).show();
                   return;
                }
                toggleStartServerUI(startServer);
                Intent intent= new Intent(getContext(), FTPService.class);
                getContext().startService(intent);
                disableViews(!FtpServerInstance.getInstance().isRunning(),views);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        softwareKeyboardListener.setListener(new SoftwareKeyboardListener.Listener() {
            @Override
            public void onSoftKeyboardShown(boolean showing) {
               startServer.setVisibility(showing?View.GONE:View.VISIBLE);
            }
        });

        FtpServerInstance instance=FtpServerInstance.getInstance();
        // in case the server is still running
        toggleStartServerUI(startServer);
        disableViews(!instance.isRunning(),views);
        if(instance.isRunning()) {
            name.setText(instance.getName());
            port.setText(String.valueOf(instance.getPort()));
            pass.setText(instance.getPassword());
            dir.setText(instance.getPath());
        }
        return root;
    }

    private void toggleStartServerUI(Button startServer){
        if(!FtpServerInstance.getInstance().isRunning()) {
            startServer.setText("START SERVER");
            startServer.setBackgroundResource(R.drawable.drawable_highlight6);
        }else {
            startServer.setText("STOP SERVER");
            startServer.setBackgroundResource(R.drawable.drawable_highlight8);
        }

    }

    private void disableViews(boolean enabled, List<View>views){
        for (View view:views){
            view.setEnabled(enabled);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onBackPressed() {
        if(parent!=null)
            activity.setFragment(parent);
        if(activity!=null)
            activity.toolbar.setVisibility(View.VISIBLE);
    }
}
