package com.trucklog.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Created by rock on 2/8/17.
 */

public class NewLoadManage {
    private static String LOAD_SIZE = "load_size";
    private static String LOAD = "load_";

    public static int[] getNewLoads(Context context) {
        int[] return_value;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int size_new_load = sp.getInt(LOAD_SIZE,0);
        if(size_new_load == 0){
            return null;
        }
        else{
            return_value = new int[size_new_load];
            for(int i = 0 ; i < size_new_load; i++){
                return_value[i] = sp.getInt(LOAD + (i+1), 0);
            }
        }
        return return_value;
    }

    public static void setNewLoad(Context context, int new_load_id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        int size_new_load = sp.getInt(LOAD_SIZE,0);
        size_new_load += 1;
        ed.putInt(LOAD_SIZE, size_new_load);
        ed.putInt(LOAD+size_new_load, new_load_id);
        ed.commit();
    }


    public static void removeNewLoad(Context context, int load_id){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        boolean is_removed = false;
        int size_new_load = sp.getInt(LOAD_SIZE,0);
        if(size_new_load != 0) {
            for (int i = 0; i < size_new_load; i++) {
                int tmp_load_id = sp.getInt(LOAD + (i+1), 0);
                if (tmp_load_id == load_id) {
                    ed.remove(LOAD + (i+1));
                    ed.commit();
                    is_removed = true;
                }
            }
            if(is_removed == true) {
                ed.putInt(LOAD_SIZE, size_new_load - 1);
                ed.commit();
            }
        }
    }

    public static void removeAllNewLoad(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sp.edit();
        int size_new_load = sp.getInt(LOAD_SIZE,0);
        if(size_new_load != 0){
            for(int i = 0 ; i < size_new_load ; i++){
                ed.remove(LOAD+i);
            }
        }
        ed.remove(LOAD_SIZE);
        ed.commit();
    }
}
