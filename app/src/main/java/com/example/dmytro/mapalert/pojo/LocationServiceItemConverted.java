package com.example.dmytro.mapalert.pojo;

import android.location.Location;

import java.io.Serializable;

public class LocationServiceItemConverted implements Serializable {

    private Integer dataBaseId;
    private String title;
    private String description;
    private Location location;
    private String mImagePath;

    private boolean inside;

    public LocationServiceItemConverted() {
    }

    public LocationServiceItemConverted(Integer dataBaseId, String title, String description, Location location, String mImagePath, Integer inside) {
        this.dataBaseId = dataBaseId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.mImagePath = mImagePath;

        //if inside equals 1 than true in all other situation false
        this.inside = inside.equals(1);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isInside() {
        return inside;
    }

    public void setInside(boolean inside) {
        this.inside = inside;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public Integer getDataBaseId() {
        return dataBaseId;
    }

    public void setDataBaseId(Integer dataBaseId) {
        this.dataBaseId = dataBaseId;
    }
}
