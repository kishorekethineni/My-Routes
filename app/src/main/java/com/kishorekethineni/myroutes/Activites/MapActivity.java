
package com.kishorekethineni.myroutes.Activites;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;
import com.kishorekethineni.myroutes.Database.DBHelper;
import com.kishorekethineni.myroutes.Models.RouteModel;
import com.kishorekethineni.myroutes.R;
import com.kishorekethineni.myroutes.UTILS.UserLocationCallback;
import com.kishorekethineni.myroutes.UTILS.UserLocationUtility;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private EditText source,desti;
    private GoogleMap googleMap;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.submit).setVisibility(View.GONE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        source=findViewById(R.id.source_et);
        desti=findViewById(R.id.destination_et);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        Gson gson = new Gson();
        String strObj = getIntent().getStringExtra("routeObject");
        RouteModel model = gson.fromJson(strObj, RouteModel.class);
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_dest));
        desti.setText(model.getDestination());
        source.setText(model.getSource());
        for (int i=0; i<model.getLatLngList().size();i++){
            googleMap.addMarker(new MarkerOptions()
                    .position(model.getLatLngList().get(i))
                    .title("Point: "+i)
                    .visible(true)
                    .icon(markerIcon));
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(model.getLatLngList().get(0), 20));
        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(model.getLatLngList().toArray(new LatLng[0])));
        polyline1.setTag("Way");

    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
