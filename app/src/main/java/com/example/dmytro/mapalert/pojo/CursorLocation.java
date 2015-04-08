package com.example.dmytro.mapalert.pojo;


import java.io.Serializable;

public class CursorLocation implements Serializable {

    private Integer id;
    private LocationItem item;
    private Integer inside;

    public CursorLocation() {
    }

    public CursorLocation(Integer id, LocationItem item, Integer inside) {
        this.id = id;
        this.item = item;
        this.inside = inside;
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

    public Integer getInside() {
        return inside;
    }

    public void setInside(Integer inside) {
        this.inside = inside;
    }
}
