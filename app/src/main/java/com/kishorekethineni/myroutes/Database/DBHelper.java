package com.kishorekethineni.myroutes.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.kishorekethineni.myroutes.Models.RouteModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "MYRoutesDB";
    public static final String ROUTES_TABLE = "Routes";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + ROUTES_TABLE +
                        " (id integer primary key," +
                        "TimeStamp text," +
                        "Source text," +
                        "Destination text," +
                        "LatLngs text," +
                        "Address text)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ROUTES_TABLE);
        onCreate(db);
    }

    public void Insert(RouteModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues ntfp = new ContentValues();
        ntfp.put("TimeStamp", model.getTimeStamp());
        ntfp.put("Source", model.getSource());
        ntfp.put("Destination", model.getDestination());
        String coords = "";
        for (LatLng latLng : model.getLatLngList()) {
            coords += latLng.latitude + "," + latLng.longitude + "&&kish";
        }
        ntfp.put("LatLngs", coords);
        ntfp.put("Address", model.getAddress());
        db.insert(ROUTES_TABLE, null, ntfp);
    }

    public List<RouteModel> getRoutes() {
        List<RouteModel> routes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + ROUTES_TABLE, null);
        if (cursor.moveToFirst()) {
            do {
                List<LatLng> latLngs = new ArrayList<>();
                for (String string : cursor.getString(4).split("&&kish")) {
                    String[] latlong = string.split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    latLngs.add(new LatLng(latitude, longitude));
                }
                routes.add(new RouteModel(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        latLngs,
                        cursor.getString(5)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return routes;
    }
}
