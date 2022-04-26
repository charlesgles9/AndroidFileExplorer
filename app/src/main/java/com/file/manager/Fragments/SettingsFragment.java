package com.file.manager.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.file.manager.Activities.MainActivity;
import com.file.manager.R;
import com.file.manager.helpers.AuthenticationHelper;
import com.file.manager.ui.Dialogs.FingerPrintAuthDialog;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    private boolean authenticated =false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        String[]keys={"theme","zip","hidden","thumbRatio","copy","version"};
        // set up list preferences
        for (String key : keys) {
            SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());
            Preference preference=findPreference(key);
            if(preference instanceof ListPreference)
            preference.setSummary(sharedPreferences.getString(key,preference.getSummary().toString()));
        }
        // get version
        Preference preference=findPreference("version");
        try {
            PackageInfo info=getContext().getPackageManager().getPackageInfo(getActivity().getPackageName(),0);
            preference.setSummary(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setTheme(String theme){
        if(theme.equals("Dark")) {
            getActivity().setTheme(R.style.PreferenceThemeDark);
            getActivity().getWindow().setStatusBarColor( getResources().getColor(R.color.darkStatusBar));
        }else {
            getActivity().setTheme(R.style.PreferenceThemeLight);
            getActivity().getWindow().setStatusBarColor( getResources().getColor(R.color.colorPrimaryLight));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference=findPreference(key);
        if(preference instanceof ListPreference) {
            String value = sharedPreferences.getString(key, "Light");
            preference.setSummary(value);
            if(key.equals("theme")){
                if(getActivity()==null)
                    return;
                getActivity().finishAffinity();
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
            }
        } else if(preference instanceof SwitchPreference){
            if(key.equals("fingerPrint")){
                // for some reason at times the context will return nullptr
                if(getActivity()==null)
                    return;
                boolean isEnrolled=new AuthenticationHelper().isFingerPrintEnrolled(getActivity());
                boolean checked=isEnrolled&((SwitchPreference) preference).isChecked();
                if(!isEnrolled){
                    Toast.makeText(getActivity(),"set fingerPrint in settings",Toast.LENGTH_LONG).show();
                }else {
                    if(!authenticated&checked)
                        fingerPrintAuthentication((SwitchPreference) preference,getActivity());
                }
            }
        }
    }

    private void fingerPrintAuthentication(final SwitchPreference preference,final Context context){
        final FingerPrintAuthDialog authDialog= new FingerPrintAuthDialog(context,!authenticated );
        authDialog.setOnAuthSuccess(new AuthenticationHelper.OnAuthSuccess() {
            @Override
            public void onSuccess() {
                authDialog.cancel();
                authenticated =true;
            }

            @Override
            public void onFailed(String message) {

            }

            @Override
            public void onError(String message) {

            }
        });
        authDialog.show();
        authDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                preference.setChecked(authenticated);
            }
        });
    }
}

