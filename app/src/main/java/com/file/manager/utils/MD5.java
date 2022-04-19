package com.file.manager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class MD5 {
    private static String fileToMD5(File file,long bytes){
        InputStream stream=null;
        try {
            stream= new FileInputStream(file);
            int size=(int)Math.min(bytes, DiskUtils.SIZE_KB*10);
            byte[]buffer=new byte[size];
            final MessageDigest digest=MessageDigest.getInstance("MD5");
            int num = stream.read(buffer, 0, size);
                if (num > 0)
                    digest.update(buffer, 0, num);
            byte[]md5Bytes=digest.digest();
            return bytesToString(md5Bytes);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }finally {
            if(stream!=null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String getHashValue(File file,long bytes){
        return fileToMD5(file,bytes);
    }
    private static String bytesToString(byte[]md5Bytes){
        String hash=new BigInteger(1,md5Bytes).toString(16);
        hash=String.format("%32s",hash).replace(' ','0');
        return hash;
    }
}
