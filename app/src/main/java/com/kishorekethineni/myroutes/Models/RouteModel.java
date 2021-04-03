package com.kishorekethineni.myroutes.Models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

public class RouteModel implements Serializable {

    private String TimeStamp;
    private String Source;
    private String Destination;
    private List<LatLng> latLngList;
    private String address;

    public RouteModel(String timeStamp, String source, String destination, List<LatLng> latLngList, String address) {
        TimeStamp = timeStamp;
        Source = source;
        Destination = destination;
        this.latLngList = latLngList;
        this.address = address;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public String getDestination() {
        return Destination;
    }

    public void setDestination(String destination) {
        Destination = destination;
    }

    public List<LatLng> getLatLngList() {
        return latLngList;
    }

    public void setLatLngList(List<LatLng> latLngList) {
        this.latLngList = latLngList;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
