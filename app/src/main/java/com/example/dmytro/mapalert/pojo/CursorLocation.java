package com.example.dmytro.mapalert.pojo;


import java.io.Serializable;

public class CursorLocation implements Serializable {

    private Integer id;
    private LocationItem item;

    public CursorLocation() {
    }

    public CursorLocation(Integer id, LocationItem item) {
        this.id = id;
        this.item = item;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocationItem getItem() {
        return item;
    }

    public void setItem(LocationItem item) {
        this.item = item;
    }
}
