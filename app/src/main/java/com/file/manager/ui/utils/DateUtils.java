package com.file.manager.ui.utils;


import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private DateUtils(){

    }

    public static String getDateString(long ll){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat= new SimpleDateFormat("MM/dd/yyy");
        return dateFormat.format(ll);
    }
    @SuppressLint("SimpleDateFormat")
    public static String getDateStringHHMMSS(long ll){
        String pattern=((ll+1)/1000)<60*60?"mm:ss":"hh:mm:ss";
        DateFormat dateFormat= new SimpleDateFormat(pattern);
        Date date= new Date(ll);
        return dateFormat.format(date);
    }

    public static String getHoursMinutesSec(long ll){
     if(ll<=59){
         return ll+"sec";
     }else if(ll>=60&ll<=(60*60)){
         return ll/60+" min";
     }else {
         return ll/360+"hrs";
     }
    }

}
