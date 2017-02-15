package com.trucklog.Models;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by rock on 2/1/17.
 */

public class ShipperModel implements Serializable{
    private String name, email, image_url, id;
    private long created;
    private int admin, editor;

    public ShipperModel(JSONObject object){
        try{
            name = object.getString("name");
            email = object.getString("email");
            image_url = object.getString("image_url");
            id = object.getString("id");
            created = object.getLong("created");
            admin = object.getInt("admin");
            editor = object.getInt("editor");
        }catch (Exception e){

            e.printStackTrace();
            name = new String();
            email = new String();
            image_url = new String();
            id = new String();
            created = 0;
            admin = 0;
            editor = 0;
        }
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
}
