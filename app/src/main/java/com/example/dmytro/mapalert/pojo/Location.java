package com.example.dmytro.mapalert.pojo;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Location implements Serializable {

    private int id;
    private LatLng coordinates;
    private String mTitle;
    private String mDescription;
    private String mPhoto;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String mPhoto) {
        this.mPhoto = mPhoto;
    }
}
