package com.lilincpp.github.libezftp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lilincpp.github.libezftp.callback.OnEZFtpCallBack;
import com.lilincpp.github.libezftp.callback.OnEZFtpDataTransferCallback;

import java.util.List;

/**
 * Proxy for {@link EZFtpClientImpl}
 *
 * @author lilin
 */
public final class EZFtpClient implements IEZFtpClient {

    private static final String TAG = "EZFtpClient";

    private IEZFtpClient ftpClientIml;

    public EZFtpClient() {
        ftpClientIml = new EZFtpClientImpl();
    }

    @Override
    public void connect(@NonNull String serverIp, @NonNull int port, @NonNull String userName, @NonNull String password) {
        connect(serverIp, port, userName, password, null);
    }

    @Override
    public void connect(@NonNull String serverIp, @NonNull int port, @NonNull String userName, @NonNull String password, @Nullable OnEZFtpCallBack<Void> callBack) {
        ftpClientIml.connect(serverIp, port, userName, password, callBack);
    }

    @Override
    public void disconnect() {
        ftpClientIml.disconnect();
    }

    @Override
    public void disconnect(@Nullable OnEZFtpCallBack<Void> callBack) {
        ftpClientIml.disconnect(callBack);
    }

    @Override
    public boolean isConnected() {
        return ftpClientIml.isConnected();
    }

    @Override
    public void getCurDirFileList(@Nullable OnEZFtpCallBack<List<EZFtpFile>> callBack) {
        ftpClientIml.getCurDirFileList(callBack);
    }

    @Override
    public void getCurDirPath(@Nullable OnEZFtpCallBack<String> callBack) {
        ftpClientIml.getCurDirPath(callBack);
    }

    @Override
    public void changeDirectory(@NonNull String path, @Nullable OnEZFtpCallBack<String> callBack) {
        ftpClientIml.changeDirectory(path, callBack);
    }

    @Override
    public void backup(@Nullable OnEZFtpCallBack<String> callBack) {
        ftpClientIml.backup(callBack);
    }

    @Override
    public void downloadFile(@NonNull EZFtpFile remoteFile, @NonNull String localFilePath, @Nullable OnEZFtpDataTransferCallback callback) {
        ftpClientIml.downloadFile(remoteFile, localFilePath, callback);
    }

    @Override
    public void uploadFile(@NonNull String localFilePath, @Nullable OnEZFtpDataTransferCallback callback) {
        ftpClientIml.uploadFile(localFilePath, callback);
    }

    @Override
    public boolean curDirIsHomeDir() {
        return ftpClientIml != null && ftpClientIml.curDirIsHomeDir();
    }

    @Override
    public void backToHomeDir(OnEZFtpCallBack<String> callBack) {
        ftpClientIml.backToHomeDir(callBack);
    }

    @Override
    public void release() {
        ftpClientIml.release();
    }
}
