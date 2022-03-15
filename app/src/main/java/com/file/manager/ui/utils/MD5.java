package com.file.manager.ui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class MD5 {
    public static String fileToMD5(String filePath){
        InputStream stream=null;
        try {
            stream= new FileInputStream(filePath);
            int size=(int)Math.min(new File(filePath).length(),DiskUtils.SIZE_KB*20);
            byte[]buffer=new byte[size];
            final MessageDigest digest=MessageDigest.getInstance("MD5");
            int num=0;
            while (num!=-1) {
                num = stream.read(buffer, 0, size);
                if (num > 0)
                    digest.update(buffer, 0, num);
            }

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

    public static String fileToMD5Fast(String filePath){
        InputStream stream=null;
        try {
            stream= new FileInputStream(filePath);
            int size=(int)Math.min(new File(filePath).length(),DiskUtils.SIZE_KB*20);
            byte[]buffer=new byte[size];
            final MessageDigest digest=MessageDigest.getInstance("MD5");
            int num=stream.read(buffer,0,size);
            if(num>0)
                digest.update(buffer,0,num);
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
    private static String bytesToString(byte[]md5Bytes){
        String hash=new BigInteger(1,md5Bytes).toString(16);
        hash=String.format("%32s",hash).replace(' ','0');
        return hash;
    }
}
