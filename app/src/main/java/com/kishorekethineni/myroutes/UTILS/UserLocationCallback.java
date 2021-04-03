package com.kishorekethineni.myroutes.UTILS;

import android.location.Address;
import android.location.Location;

import java.util.List;

public interface  UserLocationCallback {
    void onLocationResult(Location location);
    void onAddressResult(List<Address> addressList);
    void onFailedRequest(String result);
}
