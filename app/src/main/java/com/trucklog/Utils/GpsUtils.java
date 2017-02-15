package com.trucklog.Utils;

import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;

/**
 * Created by rock on 2/1/17.
 */

public class GpsUtils {
    public static boolean checkGpsAvailable(LocationManager manager){
        boolean ret_val = false;
        try {
            ret_val = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return ret_val;
    }

    public static boolean checkNetworkAvailable(LocationManager manager){
        boolean ret_val = false;
        try {
            ret_val = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return ret_val;
    }
}
