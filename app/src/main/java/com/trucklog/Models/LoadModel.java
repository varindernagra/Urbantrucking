package com.trucklog.Models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by rock on 1/31/17.
 */

public class LoadModel implements Serializable{
    private String comment, work, container, point, company, ramp, po, length, address, date, uid;
    private long created;
    private int cost,status;
    private DriverModel driverModel;
    private ShipperModel shipperModel;
    private ArrayList<LogModel> logModel;
    private boolean is_new;

    public boolean is_new() {
        return is_new;
    }

    public void setIs_new(boolean is_new) {
        this.is_new = is_new;
    }
        private int seq_index;


    public ArrayList<LogModel> getLogModel() {
        return logModel;
    }

    public void setLogModel(ArrayList<LogModel> logModel) {
        this.logModel = logModel;
    }

    public int getSeq_index() {
        return seq_index;
    }
//
    public void setSeq_index(int seq_index) {
        this.seq_index = seq_index;
    }

    public LoadModel(JSONObject object){
        try{
            status = Integer.parseInt(object.getString("status"));
            comment = object.getString("comment");
            work = object.getString("work");
            container = object.getString("container");
            point = object.getString("point");
            company = object.getString("company");
            ramp = object.getString("ramp");
            po = object.getString("po");
            length = object.getString("length");
            address = object.getString("address");
            date = object.getString("date");
            uid = object.getString("id");
            if(object.has("created") == true)
                created = object.getLong("created");
            else
                created = 0;
            cost = object.getInt("cost");
            driverModel = new DriverModel(object.getJSONObject("driver"));

            logModel = new ArrayList<>();
            JSONArray json_logs_array = new JSONArray();
            json_logs_array = object.getJSONArray("logs");
            for(int i = 0 ; i < json_logs_array.length(); i++){
                LogModel tmp_model = new LogModel(json_logs_array.getJSONObject(i));
                logModel.add(tmp_model);
            }
            is_new = false;
            shipperModel = new ShipperModel(object.getJSONObject("shipper"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRamp() {
        return ramp;
    }

    public void setRamp(String ramp) {
        this.ramp = ramp;
    }

    public String getPo() {
        return po;
    }

    public void setPo(String po) {
        this.po = po;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public DriverModel getDriverModel() {
        return driverModel;
    }

    public void setDriverModel(DriverModel driverModel) {
        this.driverModel = driverModel;
    }

    public ShipperModel getShipperModel() {
        return shipperModel;
    }

    public void setShipperModel(ShipperModel shipperModel) {
        this.shipperModel = shipperModel;
    }
}
