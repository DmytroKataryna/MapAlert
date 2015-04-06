package com.example.dmytro.mapalert.pojo;

import android.location.Location;

import java.io.Serializable;

public class LocationServiceItemConverted implements Serializable {

    private String title;
    private String description;
    private Location location;
    private String mImagePath;
    private boolean inside;

    public LocationServiceItemConverted() {
    }

    public LocationServiceItemConverted(String title, String description, Location location, String mImagePath, boolean inside) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.mImagePath = mImagePath;
        this.inside = inside;
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
}
