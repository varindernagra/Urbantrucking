package com.trucklog.Utils;


import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by rock on 2/1/17.
 */

public class ConvertUtils {
    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("HH:mm a, MMM dd yyyy", cal).toString();
        return date;
    }
    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }
}
