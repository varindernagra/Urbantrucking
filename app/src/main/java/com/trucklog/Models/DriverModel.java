package com.trucklog.Models;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by rock on 2/1/17.
 */

public class DriverModel implements Serializable{
    private String name, email, image_url, id;
    private long created;
    private double longitude, latitude;
    private int admin, editor;
    public DriverModel(JSONObject object) {
        try{
            name = object.getString("name");
            created = object.getLong("created");
            email = object.getString("email");
            image_url = object.getString("image_url");
            id = object.getString("id");
            longitude = object.getDouble("longitude");
            latitude = object.getDouble("latitude");
            admin = object.getInt("admin");
            editor = object.getInt("editor");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getAdmin() {
        return admin;
    }

    public void setAdmin(int admin) {
        this.admin = admin;
    }

    public int getEditor() {
        return editor;
    }

    public void setEditor(int editor) {
        this.editor = editor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
