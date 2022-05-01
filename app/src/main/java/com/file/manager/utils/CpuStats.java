package com.file.manager.utils;

import android.os.AsyncTask;
import com.file.manager.ui.Models.CpuModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class CpuStats {

    private ArrayList<CpuModel>cpuInfo;
    private boolean running=false;
    public CpuStats(){
        this.cpuInfo=new ArrayList<>();
    }

    private void fetchCpuFreq() throws Exception {
        int count=getCpuCoreCount();
        if(cpuInfo.isEmpty()) {
            for (int i = 0; i < count; i++) {
                CpuModel cpu = new CpuModel(i);
                cpu.setCur_freq(((getCurFreq(i) + 1) / 1000) );
                cpu.setMin_freq(((getMinFreq(i) + 1) / 1000));
                cpu.setMax_freq(((getMaxFreq(i) + 1) / 1000));
                cpuInfo.add(cpu);
            }
        }else {
            for (int i = 0; i < count; i++) {
                CpuModel cpu = cpuInfo.get(i);
                cpu.setCur_freq(((getCurFreq(i) + 1) / 1000) );
                cpu.setMin_freq(((getMinFreq(i) + 1) / 1000));
                cpu.setMax_freq(((getMaxFreq(i) + 1) / 1000));
            }
        }
    }

    private int getCurFreq(int core){
        return readIntegerFile("/sys/devices/system/cpu/cpu"+core+"/cpufreq/scaling_cur_freq");
    }

    private int getMinFreq(int core){
        return readIntegerFile("/sys/devices/system/cpu/cpu"+core+"/cpufreq/cpuinfo_min_freq");
    }

    private int getMaxFreq(int core){
        return readIntegerFile("/sys/devices/system/cpu/cpu"+core+"/cpufreq/cpuinfo_max_freq");
    }
    private int readIntegerFile(String path){
        final BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(path)),1000);
            final String line;
            line = reader.readLine();
            reader.close();
            return Integer.parseInt(line);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    private int getCpuCoreCount() throws Exception{
        if(!cpuInfo.isEmpty())
            return cpuInfo.size();
        // fetch the directory containing the cpu information
        final File directory= new File("/sys/devices/system/cpu/");
        final File[] files=directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches("cpu[0-9]", new File(dir, name).getName());
            }
        });
        return files.length;
    }

    class FetchCpuInfoTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            running=true;
            try {
                fetchCpuFreq();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            running=false;
        }
    }

    public void execute(){
        new FetchCpuInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean isRunning() {
        return running;
    }

    public ArrayList<CpuModel> getCpuInfo() {
        return cpuInfo;
    }
}
