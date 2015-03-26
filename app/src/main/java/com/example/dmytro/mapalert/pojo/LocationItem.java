package com.example.dmytro.mapalert.pojo;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.TreeSet;

public class LocationItem implements Serializable {


    private String mTitle;
    private String mDescription;
    private byte[] mPhoto;

    private boolean timeSelected;
    private TreeSet<Integer> mRepeat;
    private String mTime;

    private LatLng coordinates;


    public LocationItem() {
    }

    public LocationItem(String mTitle, String mDescription, byte[] mPhoto, LatLng coordinates) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mPhoto = mPhoto;
        this.coordinates = coordinates;
    }

    public LocationItem(String mTitle, String mDescription, byte[] mPhoto, TreeSet<Integer> mRepeat, String mTime, LatLng coordinates) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mPhoto = mPhoto;
        this.mRepeat = mRepeat;
        this.mTime = mTime;
        this.coordinates = coordinates;
    }

    public boolean isTimeSelected() {
        return timeSelected;
    }

    public void setTimeSelected(boolean timeSelected) {
        this.timeSelected = timeSelected;
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
