package com.file.manager.ui.Models;

import com.file.manager.utils.DiskUtils;

public class CopyProgressMonitor {


    private long workToBeDone;
    private long workCompleted;
    private String source;
    private String destination;
    private long totalFilesCount;
    private long totalFilesCopied;

    public long getWorkToBeDone() {
        return workToBeDone;
    }

    public void setWorkToBeDone(long workToBeDone) {
        this.workToBeDone = workToBeDone;
    }

    public long getWorkCompleted() {
        return workCompleted;
    }

    public void setWorkCompleted(long workCompleted) {
        this.workCompleted = workCompleted;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public long getTotalFilesCount() {
        return totalFilesCount;
    }

    public void setTotalFilesCount(long totalFilesCount) {
        this.totalFilesCount = totalFilesCount;
    }

    public long getTotalFilesCopied() {
        return totalFilesCopied;
    }

    public void setTotalFilesCopied(long totalFilesCopied) {
        this.totalFilesCopied = totalFilesCopied;
    }

    public void addTotalFilesCount(int count){
        this.totalFilesCount+=count;
    }

    public void addTotalFilesCopied(int count){
        this.totalFilesCopied+=count;
    }

    public void addWorkToBeDone(long count){
        this.workToBeDone+=count;
    }

    public void addWorkCompleted(long count){
        this.workCompleted+=count;
    }

    public String getWriteSpeedString(long elapsed){
        long bytes=Math.abs(workCompleted+1)/elapsed;
        return DiskUtils.getInstance().getSize(bytes)+"/s";
    }
    public int getWriteSpeedInt(long elapsed){
        long bytes=Math.abs(workCompleted)/elapsed;
        return (int)bytes;
    }
    public int getWriteSpeedInt(long elapsed,long size){
        long bytes=Math.abs(size)/elapsed;
        return (int)bytes;
    }
    public static String getWriteSpeedFromBytes(long size){
        return DiskUtils.getInstance().getSize(size)+"/s";
    }
    public boolean isFinished(){
        return workCompleted>=workToBeDone;
    }
    public int getPercent() {
        int a=(int)((float)(workCompleted+totalFilesCopied)/(float)(workToBeDone+totalFilesCount)*100);
        return a;
    }
}
