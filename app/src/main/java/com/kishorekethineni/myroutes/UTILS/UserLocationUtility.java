
package com.kishorekethineni.myroutes.UTILS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class UserLocationUtility extends LocationCallback
{
    private static final String TAG = UserLocationUtility.class.getSimpleName();


    /**
     * Inner-class defining int constants for use with location requests
     */
    public static class RequestCodes
    {
        static final int CURRENT_LOCATION_ONE_TIME = 0;
        static final int CURRENT_LOCATION_UPDATES = 1;
        static final int LAST_KNOWN_LOCATION = 2;
        static final int SMART_LOCATION = 3;
    }

    /**
     * Inner-class defining int constants for use with reverse Geocoding of address data
     */
    public static class AddressCodes
    {
        static final int ADMIN_AREA = 0;
        static final int CITY_NAME = 1;
        static final int COUNTRY_CODE = 2;
        static final int COUNTRY_NAME = 3;
        static final int FEATURE_NAME = 4;
        static final int FULL_ADDRESS = 5;
        static final int PHONE_NUMBER = 6;
        static final int POST_CODE = 7;
        static final int PREMISES = 8;
        static final int STREET_ADDRESS = 9;
        static final int SUB_ADMIN_AREA = 10;
        static final int SUB_THOROUGHFARE = 11;
    }

    /**
     * Inner-class to provide alert dialog functionality for use with permissions checking
     */
    public static class RationaleAlertDialog extends FragmentActivity
    {

        // Hold a WeakReference to the host activity (allows it to be garbage-collected to prevent possible memory leak)
        private final WeakReference<Activity> weakActivity;
        private static final String TAG = RationaleAlertDialog.class.getSimpleName();


        /** Constructor */
        RationaleAlertDialog(Activity activity){
            // assign the activity to the weak reference
            this.weakActivity = new WeakReference<>(activity);
        }

        public void displayAlert(){
            // Re-acquire a strong reference to the calling activity and verify that it still exists and is active
            Activity activity = weakActivity.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                // Activity is no longer valid, don't do anything
                return;
            }

            // Build the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Location Permission Request")
                    .setMessage("This app requires permission to access device location in order " +
                               "to provide location functionality. If you do not grant permission " +
                               "then this functionality will be switched off.")
                    .setCancelable(true)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Dismiss the dialog
                            dialogInterface.cancel();
                        }
                    });

            // Display the alert
            AlertDialog alert = builder.create();
            alert.show();

        }

    }


    // Hold a WeakReference to the host activity (allows it to be garbage-collected to prevent possible memory leak)
    private final WeakReference<Activity> weakActivity;


    private FusedLocationProviderClient mLocationClient;
    private Context mContext;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean mIsReceivingUpdates;
    private Geocoder mGeocoder;


    /** Constructor */
    public UserLocationUtility(Activity activity){
        // assign the activity to the weak reference
        this.weakActivity = new WeakReference<>(activity);

        // Hold a reference to the Application Context single object
        this.mContext = activity.getApplicationContext();

        // Instantiate our location client
        this.mLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        // Set the request state flag to false by default
        mIsReceivingUpdates = false;

        // Set up the default LocationRequest parameters
        this.mLocationRequest = new LocationRequest();
        setLocationRequestParams(10000, 5000, LocationRequest.PRIORITY_HIGH_ACCURACY);
                                // Sets up the LocationRequest with an update interval of 10 seconds, a fastest
                                // update interval cap of 5 seconds and using high accuracy power priority.
    }


    /**
     * Retrieves and returns the device's cached last known location via an instance of
     * UserLocationCallback, which must be implemented by the caller.
     *
     * Location can be null in certain circumstances, for example on a new or recently
     * factory-reset device or if location services are turned off in device settings.
     */
    @SuppressLint("MissingPermission")
    public void getLastKnownLocation(final UserLocationCallback callback){
        // Re-acquire a strong reference to the calling activity and verify that it still exists and is active
        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // Activity is no longer valid, don't do anything
            return;
        }

        // Request the last known location from the location client
        mLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>()
                {
                    @Override
                    public void onSuccess(Location location){

                        if (location != null){
                            // Call back to the main thread with the location result
                            callback.onLocationResult(location);
                        } else {
                            // Call back to the main thread to advise of a null result
                            callback.onFailedRequest("Location request returned null");
                        }

                    }
                });
    }


    /**
     * Returns the device's current location via an instance of UserLocationCallback,
     * which must be implemented by the caller.
     *
     * Turns on location updates, retrieves the current device location then turns
     * location updates off again. This can be used if last location returns null
     * but location services are turned on, or if a more recent or accurate location
     * is needed than the one found in the device's cache.
     */
    @SuppressLint("MissingPermission")
    public void getCurrentLocationOneTime(final UserLocationCallback callback){
        if (mIsReceivingUpdates){
            callback.onFailedRequest("Device is already receiving updates");
            return;
        }

        // Set up the LocationCallback for the request
        mLocationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if (locationResult != null){
                    callback.onLocationResult(locationResult.getLastLocation());
                    // Stop location updates now that we have a location result
                    stopLocationUpdates();
                } else {
                    callback.onFailedRequest("Location request returned null");
                    // Stop location updates on null result
                    stopLocationUpdates();
                }
            }
        };

        // Start the request
        mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        // Update the request state flag
        mIsReceivingUpdates = true;
    }


    /**
     * Starts a location update request with the parameters specified by mLocationRequest and
     * returns the location result via an instance of UserLocationCallback, which must be
     * implemented by the caller.
     *
     * This is inherently power-intensive so care should be taken to balance the frequency
     * of requested updates with the need for accuracy.
     *
     * Location updates should be disabled using the stopLocationUpdates() method when no
     * longer needed, such as when the user closes or otherwise navigates away from the app.
     *
     * @see UserLocationUtility#stopLocationUpdates()
     */
    @SuppressLint("MissingPermission")
    public void getCurrentLocationUpdates(final UserLocationCallback callback){
        if (mIsReceivingUpdates){
            callback.onFailedRequest("Device is already receiving updates");
            return;
        }

        // Set up the LocationCallback for the request
        mLocationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if (locationResult != null){
                    callback.onLocationResult(locationResult.getLastLocation());
                } else {
                    callback.onFailedRequest("Location request returned null");
                }
            }
        };

        // Start the request
        mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        // Update the request state flag
        mIsReceivingUpdates = true;
    }


    /**
     * Returns the best available location via an instance of UserLocationCallback, which
     * must be implemented by the caller.
     *
     * Checks if last known location is available. If unavailable (null) then a single
     * location update is requested instead.
     *
     * If current location is also unavailable (due to a disabled service for example)
     * then an onFailedRequest callback is executed to be handled by the caller.
     *
     * This method should be preferred over the getLastKnownLocation() and
     * getCurrentLocationOneTime() methods in most use cases.
     *
     * @param   callback An interface which must be implemented by the caller in order to
     *          receive the results of the location request.
     */
    @SuppressLint("MissingPermission")
    public void getSmartLocation(final UserLocationCallback callback){
        // Re-acquire a strong reference to the calling activity and verify that it still exists and is active
        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // Activity is no longer valid, don't do anything
            return;
        }

        // Request the last known location from the location client
        mLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>()
                {
                    @Override
                    public void onSuccess(Location location){

                        if (location != null){

                            // Call back to the main thread with the location result
                            Log.i("getSmartLocation()", "Location provided by getLastLocation()");
                            callback.onLocationResult(location);

                        } else {
                            // Location result is null or so request location updates to attempt
                            // to get the device's current location.

                            // Set up the LocationCallback for the request
                            mLocationCallback = new LocationCallback()
                            {
                                @Override
                                public void onLocationResult(LocationResult locationResult){
                                    if (locationResult != null){
                                        callback.onLocationResult(locationResult.getLastLocation());
                                        Log.i("getSmartLocation()", "Location provided by requestLocationUpdates()");
                                        // Stop location updates now that we have a location result
                                        stopLocationUpdates();
                                    } else {
                                        callback.onFailedRequest("Location request returned null");
                                        // Stop location updates on null result
                                        stopLocationUpdates();
                                    }
                                }
                            };

                            // Start the request
                            mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                            // Update the request state flag
                            mIsReceivingUpdates = true;
                        }

                    }
                });

    }


    /**
     * Simple check to see if the required permission has been granted.
     *
     * @return  true if permission granted, false if not.
     */
    public boolean checkPermissionGranted(){
        int permissionState = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);

        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Explicitly requests permission to access the device's fine location.
     *
     * Determines if additional rationale should be provided to the user, displays it if
     * so then initiates a permission request via a call to startPermissionRequest().
     *
     * @param   requestCode A package-defined int constant to identify the request.
     *                      It is returned to the onRequestPermissionsResult callback
     *                      which must be implemented by the caller.
     */
    public void requestPermission(int requestCode){
        // Re-acquire a strong reference to the calling activity and verify that it still exists and is active
        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // Activity is no longer valid, don't do anything
            return;
        }

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale){

            // Provide additional rationale to the user. This would happen if the user denied the request
            // previously but didn't tick the "Don't ask again" checkbox.
            RationaleAlertDialog dialog = new RationaleAlertDialog(activity);
            dialog.displayAlert();

            // Request permission
            startPermissionRequest(requestCode);

        } else {

            // Request permission. It's possible this can be auto-answered if the device policy sets
            // the permission in a given state or the user denied the request previously and ticked
            // the "Don't ask again" checkbox.
            startPermissionRequest(requestCode);

        }

    }

    /**
     * Called by requestPermission() to initiate a permission request
     *
     * @param   requestCode the request code passed in by requestPermission()
     * @see UserLocationUtility#requestPermission(int)
     */
    private void startPermissionRequest(int requestCode){
        // Re-acquire a strong reference to the calling activity and verify that it still exists and is active
        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // Activity is no longer valid, don't do anything
            return;
        }

        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                requestCode);
                // requestCode is an int constant. The onRequestPermissionsResult callback
                // gets the result of the request.
    }


    /**
     * Checks if all required location settings are satisfied
     *
     * If the settings are not satisfied a dialog requesting the user enable the required
     * settings will be displayed. The result of the request can be checked in
     * onActivityResult() in the calling Activity if necessary.
     */
    public void checkDeviceSettings(){
        // Re-acquire a strong reference to the calling activity and verify that it still exists and is active
        final Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // Activity is no longer valid, don't do anything
            return;
        }

        // Create a settings request builder and pass it the LocationRequest
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // Create a settings client
        SettingsClient client= LocationServices.getSettingsClient(mContext);

        // Create a Task from the client
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        // Query the result of the Task to determine if the required location settings are satisfied
        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Location settings are satisfied, no need to take any action
                Log.i(TAG, "Location settings satisfied");
            }
        });
        task.addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    // Location settings are not satisfied, display a dialog to the user
                    // requesting the settings to be enabled
                    Log.e(TAG, "Location settings not satisfied. Attempting to resolve...");
                    try{
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        // Show the dialog
                        resolvable.startResolutionForResult(activity, 1);
                    } catch (IntentSender.SendIntentException sendException){
                        // Ignore the error.
                    }

                }
            }
        });

    }


    /**
     * Determines if location updates are currently active or not
     *
     * @return  true if receiving location updates, false if not.
     */
    public boolean isReceivingUpdates(){
        return mIsReceivingUpdates;
    }


    /**
     * Stops location updates from being received.
     *
     * This should be called in the calling Activity's onPause() method so
     * that location updates don't continue in the background when the user
     * navigates away from the app.
     */
    public void stopLocationUpdates(){

        if (mLocationCallback != null) {
            mLocationClient.removeLocationUpdates(mLocationCallback);
            mIsReceivingUpdates = false;
            Log.i(TAG, "Location updates removed");
        }

    }


    /**
     * Resumes location updates if they have previously been set up
     *
     * This should be called in the calling Activity's onResume() method if
     * you want your app to continue to receive location updates when it resumes
     *
     * @see UserLocationUtility#stopLocationUpdates()
     */
    @SuppressLint("MissingPermission")
    public void resumeLocationUpdates(){

        if (mLocationCallback != null) {
            mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            mIsReceivingUpdates = true;
            Log.i(TAG, "Location updates resumed");
        }

    }


    /**
     * Provides a way for a calling Activity to set or change the default parameters of
     * the LocationRequest object to suit its needs.
     *
     * @param interval          Set the desired interval for active location updates, in milliseconds.
     * @param fastestInterval   Explicitly set the fastest interval for location updates, in
     *                          milliseconds. This controls the fastest rate at which your
     *                          application will receive location updates.
     * @param priority          Set the priority of the request. Use with a priority constant such as
     *                          LocationRequest.PRIORITY_HIGH_ACCURACY. No other values are accepted.
     */
    public void setLocationRequestParams(long interval, long fastestInterval, int priority){

        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
        }

        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(priority);

    }


    /**
     * Returns a List object containing the address information for the supplied
     * Location object. Will be null if no address found. The caller should
     * implement a UserLocationCallback to receive and handle the result.
     *
     * @param   location    A Location object containing latitude and longitude.
     * @param   callback    An interface which must be implemented by the caller in order to
     *                      receive the results of the request.
     */
    public void getAddressList(Location location, UserLocationCallback callback){

        mGeocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addressList = null;

        try {

            addressList = mGeocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(),
                    1); // We only want one address to be returned.

        } catch (IOException e) {
            // Catch network or other IO problems
            callback.onFailedRequest("Geocoder not available");
            return;
        } catch (IllegalArgumentException e) {
            // Catch invalid latitude or longitude values
            callback.onFailedRequest("Invalid latitude or longitude");
            return;
        }

        // Handle case where no address is found
        if (addressList == null || addressList.size() == 0) {
            callback.onFailedRequest("No address found");
        } else {
            // Return the address list
            callback.onAddressResult(addressList);
        }

    }


    /**
     * Returns a String containing the requested address element or null if not found
     *
     * @param   elementCode A package-defined int constant representing the specific
     *                      address element to return.
     * @param   location    A Location object containing a latitude and longitude.
     *
     * @return  String containing the requested address element if found, a reason for
     *          failure if necessary or null if address element doesn't exist.
     */
    public String getAddressElement(int elementCode, Location location){

        mGeocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addressList;
        Address address;
        String elementString = null;

        try {

            addressList = mGeocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(),
                    1); // We only want one address to be returned.

        } catch (IOException e) {
            // Catch network or other IO problems
            return "Geocoder not available. Check network connection.";
        } catch (IllegalArgumentException e) {
            // Catch invalid latitude or longitude values
            return "Invalid latitude or longitude";
        }

        // Handle case where no address is found
        if (addressList == null || addressList.size() == 0){
            return "Sorry, no address found for this location";
        } else {
            // Create the Address object from the address list
            address = addressList.get(0);
        }

        // Get the specific address element requested by the caller
        switch (elementCode){

            case AddressCodes.ADMIN_AREA:
                elementString = address.getAdminArea();
                break;
            case AddressCodes.CITY_NAME:
                elementString = address.getLocality();
                break;
            case AddressCodes.COUNTRY_CODE:
                elementString = address.getCountryCode();
                break;
            case AddressCodes.COUNTRY_NAME:
                elementString = address.getCountryName();
                break;
            case AddressCodes.FEATURE_NAME:
                elementString = address.getFeatureName();
                break;
            case AddressCodes.FULL_ADDRESS:
                elementString = address.toString();
                break;
            case AddressCodes.PHONE_NUMBER:
                elementString = address.getPhone();
                break;
            case AddressCodes.POST_CODE:
                elementString = address.getPostalCode();
                break;
            case AddressCodes.PREMISES:
                elementString = address.getPremises();
                break;
            case AddressCodes.STREET_ADDRESS:
                elementString = address.getThoroughfare();
                break;
            case AddressCodes.SUB_ADMIN_AREA:
                elementString = address.getSubAdminArea();
                break;
            case AddressCodes.SUB_THOROUGHFARE:
                elementString = address.getSubThoroughfare();
                break;
            default:
                elementString = "Invalid element code";
                break;
        }

        return elementString;
    }

    public String getAddressElement(int elementCode, LatLng location){

        mGeocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addressList;
        Address address;
        String elementString = null;

        try {

            addressList = mGeocoder.getFromLocation(
                    location.latitude, location.longitude,
                    1); // We only want one address to be returned.

        } catch (IOException e) {
            // Catch network or other IO problems
            return "Geocoder not available. Check network connection.";
        } catch (IllegalArgumentException e) {
            // Catch invalid latitude or longitude values
            return "Invalid latitude or longitude";
        }

        // Handle case where no address is found
        if (addressList == null || addressList.size() == 0){
            return "Sorry, no address found for this location";
        } else {
            // Create the Address object from the address list
            address = addressList.get(0);
        }

        // Get the specific address element requested by the caller
        switch (elementCode){

            case AddressCodes.ADMIN_AREA:
                elementString = address.getAdminArea();
                break;
            case AddressCodes.CITY_NAME:
                elementString = address.getLocality();
                break;
            case AddressCodes.COUNTRY_CODE:
                elementString = address.getCountryCode();
                break;
            case AddressCodes.COUNTRY_NAME:
                elementString = address.getCountryName();
                break;
            case AddressCodes.FEATURE_NAME:
                elementString = address.getFeatureName();
                break;
            case AddressCodes.FULL_ADDRESS:
                elementString = address.toString();
                break;
            case AddressCodes.PHONE_NUMBER:
                elementString = address.getPhone();
                break;
            case AddressCodes.POST_CODE:
                elementString = address.getPostalCode();
                break;
            case AddressCodes.PREMISES:
                elementString = address.getPremises();
                break;
            case AddressCodes.STREET_ADDRESS:
                elementString = address.getThoroughfare();
                break;
            case AddressCodes.SUB_ADMIN_AREA:
                elementString = address.getSubAdminArea();
                break;
            case AddressCodes.SUB_THOROUGHFARE:
                elementString = address.getSubThoroughfare();
                break;
            default:
                elementString = "Invalid element code";
                break;
        }

        return elementString;
    }

}