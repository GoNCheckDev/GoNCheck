package com.example.vuquang.goncheck.model;


import android.location.Address;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by VuQuang on 4/25/2017.
 */

public class CheckedPlace extends RealmObject{

    @PrimaryKey
    private int id;

    private String placeAddr;

    private double latitude;
    private double longitude;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaceAddr() {
        return placeAddr;
    }

    public void setPlaceAddr(String placeAddr) {
        this.placeAddr = placeAddr;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
