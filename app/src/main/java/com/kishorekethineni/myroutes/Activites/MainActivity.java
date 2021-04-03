
package com.kishorekethineni.myroutes.Activites;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    Location location=null;
    private EditText source,desti;
    private GoogleMap googleMap;
    private final List<LatLng> waypoints=new ArrayList<>();
    private UserLocationUtility locationUtility;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        source=findViewById(R.id.source_et);
        desti=findViewById(R.id.destination_et);
        locationUtility=new UserLocationUtility(MainActivity.this);
        if (locationUtility.checkPermissionGranted()){
            locationUtility.getCurrentLocationOneTime(new UserLocationCallback() {
                @Override
                public void onLocationResult(Location loc) {
                    location=loc;
                    BitmapDescriptor markerIcon = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_source));
                    LatLng latLng=new LatLng(loc.getLatitude(),loc.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                            .title("Current Location")
                            .snippet("Save your Navigation's...")
                            .icon(markerIcon);
                    googleMap.addMarker(markerOptions);
                    source.setText(locationUtility.getAddressElement(9,loc));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                    waypoints.add(latLng);
                }
                @Override
                public void onAddressResult(List<Address> addressList) {
                }
                @Override
                public void onFailedRequest(String result) {
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waypoints.size()>1){
                    Toast.makeText(MainActivity.this, "Last point will be considered as your destination", Toast.LENGTH_SHORT).show();
                    Date date = new Date();
                    Timestamp ts=new Timestamp(date.getTime());
                    new saveTask(new RouteModel(ts.toString(),source.getText().toString(),desti.getText().toString(),waypoints,"")).execute();
                }else{
                    showDailog("Failed","Destination required to complete a voyage");
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_dest));
                desti.setText(locationUtility.getAddressElement(9,latLng));
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Point: "+waypoints.size())
                        .visible(true)
                        .icon(markerIcon));
                waypoints.add(latLng);
                Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(waypoints.toArray(new LatLng[0])));
                polyline1.setTag("Way");
            }
        });
    }

    public void showDailog(String title,String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(title);
        alert.show();
    }
    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private class saveTask extends AsyncTask<String,String,String>{

        private final RouteModel model;
        public saveTask(RouteModel model){
            this.model=model;
        }
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd=new ProgressDialog(MainActivity.this);
            pd.setMessage("Saving...");
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            DBHelper db=new DBHelper(MainActivity.this);
            db.Insert(model);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            startActivity(new Intent(MainActivity.this, Home.class));
        }
    }
}
