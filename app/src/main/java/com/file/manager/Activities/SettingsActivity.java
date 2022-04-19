package com.file.manager.Activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.file.manager.Fragments.SettingsFragment;
import com.file.manager.R;

public class SettingsActivity extends Activity {

    final SettingsFragment settingsFragment= new SettingsFragment();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        String strTheme=getIntent().getExtras().getString("Theme");
        setTheme(strTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar=findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setToolbarColor(strTheme);
        FragmentTransaction transaction= getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment,settingsFragment);
        transaction.commit();
    }

    private void setTheme(String theme){
        if(theme.equals("Dark")) {
           setTheme(R.style.PreferenceThemeDark);
            getWindow().setStatusBarColor( getResources().getColor(R.color.darkStatusBar));

        }else {
            setTheme(R.style.PreferenceThemeLight);
            getWindow().setStatusBarColor( getResources().getColor(R.color.colorPrimaryLight));
        }
    }

    private void setToolbarColor(String theme){
        if(theme.equals("Dark")) {
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.darkStatusBar));
        }else
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
    }
}
