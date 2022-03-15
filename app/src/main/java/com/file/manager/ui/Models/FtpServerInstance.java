package com.file.manager.ui.Models;
import android.text.TextUtils;
import androidx.lifecycle.MutableLiveData;
import com.lilincpp.github.libezftp.EZFtpServer;
import com.lilincpp.github.libezftp.user.EZFtpUser;
import com.lilincpp.github.libezftp.user.EZFtpUserPermission;

public class FtpServerInstance {
    private EZFtpUser user;
    private EZFtpServer ftpServer;
    private String name;
    private String password;
    private String path;
    private String IpAddress;
    private int port;
    private static FtpServerInstance instance=new FtpServerInstance();
    private MutableLiveData<Boolean> liveData;
    private FtpServerInstance(){
        liveData= new MutableLiveData<>(false);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setIpAddress(String ipAddress) {
        IpAddress = ipAddress;
    }

    public String getIpAddress() {
        return IpAddress;
    }

    public static FtpServerInstance getInstance() {
        return instance;
    }

    public void start(){
        if(isRunning()){
           return;
        }
        if(!(TextUtils.isEmpty(name)& TextUtils.isEmpty(path)&
             TextUtils.isEmpty(String.valueOf(port)))){
        user= new EZFtpUser(name,password,path, EZFtpUserPermission.WRITE);
        ftpServer=new EZFtpServer.Builder().addUser(user)
                .setListenPort(port)
                .create();
        //start server
         ftpServer.start();

        }
    }


    public void stop(){
     if(ftpServer!=null)
      ftpServer.stop();
    }


    public boolean isRunning() {
        if(ftpServer==null)
            return false;
        return !ftpServer.isStopped();
    }

    public MutableLiveData<Boolean> getLiveData() {
        return liveData;
    }
}
