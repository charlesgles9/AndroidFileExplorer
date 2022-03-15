package com.file.manager.ui.Models;

import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileHandleUtil;

public class CopyStats {
        private int FilesToCopy;
        private int FilesCopied;
        private String  WriteSpeed;
        private int CurrentFileCount;
        private int CurrentFileCopied;
        private long pFileBytesCopied;
        private long FileBytesCopied;
        private long FileBytesToCopy;
        private MutableLiveData<CopyStats> stats= new MutableLiveData<>();
        private FileHandleUtil.WriteSpeedTimer.WriteSpeedListener listener;
        private String source;
        private String destination;
        private boolean finished=false;
        private boolean isFolder=false;
        private int subFolderCount;
        private int subfolderCopied;
        private AsyncTask Thread;

    public AsyncTask getThread() {
        return Thread;
    }

    public CopyStats(AsyncTask Thread){
        this.Thread=Thread;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setIsFolder(boolean folder) {
        this.isFolder = folder;
    }

    public void setSubfolderCopied(int subfolderCopied) {
        this.subfolderCopied = subfolderCopied;
    }

    public void setSubFolderCount(int subFolderCount) {
        this.subFolderCount = subFolderCount;
    }

    public int getSubfolderCopied() {
        return subfolderCopied;
    }

    public int getSubFolderCount() {
        return subFolderCount;
    }

    public String CurrentFileProgress(){
            return String.valueOf(CurrentFileCopied/CurrentFileCount*100);
        }
        public String FilesCopiedProgress(){
            return String.valueOf(FilesCopied/FilesToCopy*100);
        }
        public String getWriteProgress(){
            return DiskUtils.getInstance().getSize(FileBytesCopied)+"/" +
                    DiskUtils.getInstance().getSize(FilesToCopy);
        }
        public void setFileBytesCopied(long fileBytesCopied){
            this.FileBytesCopied = fileBytesCopied;
        }
        public void setFileBytesToCopy(long fileBytesToCopy){
            this.FileBytesToCopy = fileBytesToCopy;
        }

        public String getBytesData(){
            DiskUtils diskUtils=DiskUtils.getInstance();
            return diskUtils.getSize(FileBytesCopied)+"/"+diskUtils.getSize(FileBytesToCopy);
        }

        public int getByteDataProgress(){
            float r=(float)FileBytesCopied/(float)FileBytesToCopy;
            return Math.min((int)(r*100),100);
        }
        public int getFilesToCopy() {
            return FilesToCopy;
        }

        public void setFilesToCopy(int filesToCopy) {
            FilesToCopy = filesToCopy;
        }

        public int getFilesCopied() {
            return FilesCopied;
        }

        public void setFilesCopied(int filesCopied) {
            FilesCopied = filesCopied;
        }

    public long getFileBytesCopied() {
        return FileBytesCopied;
    }

    long pbytes=0L;

    public void reset(){
        pbytes=0L;
    }
        public String getWriteSpeed() {
            long bytes=Math.abs(FileBytesCopied-pFileBytesCopied+pbytes+1)/2;
            pFileBytesCopied=FileBytesCopied;
            pbytes=bytes;
            return DiskUtils.getInstance().getSize(bytes)+"/s";
        }

        public String getRemainingTime(){
            long time=Math.abs((FileBytesToCopy-FileBytesCopied+1)/(pbytes+1));
            return " remaining time: "+DateUtils.getHoursMinutesSec(time);
        }

        public void setCurrentFileCount(int currentFileCount) {
            CurrentFileCount = currentFileCount;
        }

        public void setCurrentFileCopied(int currentFileCopied) {
            CurrentFileCopied = currentFileCopied;
        }

        public String getFileCountStats(){
          return CurrentFileCount+"/"+CurrentFileCopied;
        }

        public int getFileCountProgress(){
          float r=(float)CurrentFileCopied/(float)CurrentFileCount;
          return (int)(r*100);
        }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setListener(FileHandleUtil.WriteSpeedTimer.WriteSpeedListener listener){
            this.listener=listener;
        }

        public MutableLiveData<CopyStats> getStats() {
            return stats;
         }

        public FileHandleUtil.WriteSpeedTimer.WriteSpeedListener getListener() {
            return listener;
        }
}
