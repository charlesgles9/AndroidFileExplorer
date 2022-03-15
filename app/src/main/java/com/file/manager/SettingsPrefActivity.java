package com.file.manager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.file.manager.helpers.AuthenticationHelper;
import com.file.manager.ui.FingerPrintAuthDialog;


public class SettingsPrefActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private boolean authenticated =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getIntent().getExtras().getString("Theme").equals("Dark")) {
           setTheme("Dark");
            getWindow().setStatusBarColor( getResources().getColor(R.color.darkStatusBar));
        }else {
            setTheme("Light");
            getWindow().setStatusBarColor( getResources().getColor(R.color.colorPrimaryLight));
        }
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        String[]keys={"theme","zip","hidden","thumbRatio","copy","version"};
        // set up list preferences
        for (String key : keys) {
            SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Preference preference=findPreference(key);
            if(preference instanceof ListPreference)
            preference.setSummary(sharedPreferences.getString(key,preference.getSummary().toString()));
        }
        // get version
        Preference preference=findPreference("version");
        try {
            PackageInfo info=getApplicationContext().getPackageManager().getPackageInfo(getPackageName(),0);
            preference.setSummary(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference=findPreference(key);
        if(preference instanceof ListPreference) {
            String value = (String) sharedPreferences.getString(key, "Light");
            preference.setSummary(value);
            if(key.equals("theme")){
                finishAffinity();
                startActivity(new Intent(this,MainActivity.class));
            }
        } else if(preference instanceof SwitchPreference){
            if(key.equals("fingerPrint")){
                boolean isEnrolled=new AuthenticationHelper().isFingerPrintEnrolled(getApplicationContext());
                boolean checked=isEnrolled&((SwitchPreference) preference).isChecked();
                ((SwitchPreference) preference).setChecked(checked);
                if(!isEnrolled){
                    Toast.makeText(getApplicationContext(),"set fingerPrint in settings",Toast.LENGTH_LONG).show();
                }else {
                    if(checked&!authenticated){
                        ((SwitchPreference) preference).setChecked(false);
                        if(!authenticated)
                        fingerPrintAuthentication((SwitchPreference) preference);
                        else
                         ((SwitchPreference) preference).setChecked(true);
                    }
                }
            }
        }
    }

    private void fingerPrintAuthentication(final SwitchPreference preference){
        final FingerPrintAuthDialog authDialog= new FingerPrintAuthDialog(this,true );
        authDialog.setOnAuthSuccess(new AuthenticationHelper.OnAuthSuccess() {
            @Override
            public void onSuccess() {
                authDialog.dismiss();
                authenticated =true;
                preference.setChecked(true);
            }

            @Override
            public void onFailed(String message) {

            }

            @Override
            public void onError(String message) {

            }
        });
        authDialog.show();
    }
}

