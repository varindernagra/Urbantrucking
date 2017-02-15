package com.trucklog.Models;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by rock on 2/3/17.
 */

public class LogModel implements Serializable{
    //"status": 2, "id": "19", "load_id": "1", "location": "", "time": 1486100084
    private int status, id, load_id;
    private long time;
    private String location;

    public LogModel(JSONObject object){
        try{
            status = object.getInt("status");
            id = Integer.parseInt(object.getString("id"));
            load_id = Integer.parseInt(object.getString("load_id"));
            location = new String();
            location = object.getString("location");
            time = object.getLong("time");
        }catch (Exception e){

        }
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLoad_id() {
        return load_id;
    }

    public void setLoad_id(int load_id) {
        this.load_id = load_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
