package com.file.manager.ui.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
@SuppressLint("WifiManagerPotentialLeak")
public class WifiInfoUtil {

    public static boolean isWifiEnabled(Context context){
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager!=null&&wifiManager.isWifiEnabled();
    }

    public static boolean isNetworkConnected(Context context){
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = connectivityManager.getActiveNetwork();
            if (connectivityManager.getNetworkCapabilities(network).
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return true;
        }catch (NullPointerException ignore){}

        return false;
    }

    public static String getWifiIpAddress(Context context){
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress=wifiManager.getConnectionInfo().getIpAddress();
        if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)){
            ipAddress=Integer.reverseBytes(ipAddress);
        }
        byte[]ipByteArray= BigInteger.valueOf(ipAddress).toByteArray();
        String value;
        try {
            value= InetAddress.getByAddress(ipByteArray).getHostAddress();
        }catch (UnknownHostException ex){
            value=null;
        }
        return value;
    }
}
