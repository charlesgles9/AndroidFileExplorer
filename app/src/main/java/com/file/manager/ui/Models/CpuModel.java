package com.file.manager.ui.Models;

public class CpuModel {

    private String name;
    private int cur_freq;
    private int max_freq;
    private int min_freq;
    private int core;
    public CpuModel(int core){
       this.name="CPU "+core;
       this.core=core;
    }

    public void setCur_freq(int cur_freq) {
        this.cur_freq = cur_freq;
    }
    public String getCur_freq() {
        return cur_freq+"MHz";
    }

    public String getMax_freq() {
        return max_freq+"MHz";
    }

    public void setMax_freq(int max_freq) {
        this.max_freq = max_freq;
    }

    public String getMin_freq() {
        return min_freq+"MHz";
    }

    public void setMin_freq(int min_freq) {
        this.min_freq = min_freq;
    }

    public String getName() {
        return name;
    }


    public int getCore() {
        return core;
    }

    public int getCurUsage(){
        int usage=0;
        if(max_freq-min_freq>0&&max_freq>0&&cur_freq>0){
            usage=(cur_freq-min_freq)*100/(max_freq-min_freq);
        }
        return usage;
    }
}
