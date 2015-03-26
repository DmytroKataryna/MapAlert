package com.example.dmytro.mapalert.pojo;

import java.io.Serializable;
import java.util.TreeSet;

public class LocationItem implements Serializable {

    static final long serialVersionUID = 5716885753290653044L;

    private String mTitle;
    private String mDescription;
    private byte[] mPhoto;

    private boolean timeSelected;
    private TreeSet<Integer> mRepeat;
    private String mTime;

    private double latitude;
    private double longitude;


    public LocationItem() {
    }

    public LocationItem(String mTitle, String mDescription, byte[] mPhoto, double latitude, double longitude) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mPhoto = mPhoto;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationItem(String mTitle, String mDescription, byte[] mPhoto, TreeSet<Integer> mRepeat, String mTime, double latitude, double longitude) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mPhoto = mPhoto;
        this.mRepeat = mRepeat;
        this.mTime = mTime;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public boolean isTimeSelected() {
        return timeSelected;
    }

    public void setTimeSelected(boolean timeSelected) {
        this.timeSelected = timeSelected;
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

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public byte[] getPhoto() {
        return mPhoto;
    }

    public void setPhoto(byte[] mPhoto) {
        this.mPhoto = mPhoto;
    }

    public TreeSet<Integer> getRepeat() {
        return mRepeat;
    }

    public void setRepeat(TreeSet<Integer> mRepeat) {
        this.mRepeat = mRepeat;
    }
}
