package com.example.dmytro.mapalert.geofencing;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.dmytro.mapalert.pojo.LocationServiceItem;
import com.example.dmytro.mapalert.pojo.LocationServiceItemConverted;
import com.example.dmytro.mapalert.utils.PreferencesUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = "BackgroundServiceTAG";

    public static final String BROADCAST_ACTION = "com.example.mapalert.location.receiver";
    public static final String NOTIF_DESCRIPTION_EXTRA = "notification_receiver_description";
    public static final String NOTIF_TITLE_EXTRA = "notification_receiver_title";

    public static final Integer RADIUS_IN_METERS = 50;

    private PreferencesUtils utils;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<LocationServiceItemConverted> locationItems;

    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    AlarmManager alarmManager;
    NotificationManager nm;
    Intent i;

    @Override
    public void onCreate() {
        super.onCreate();

        utils = PreferencesUtils.get(getApplicationContext());
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        i = new Intent(BROADCAST_ACTION);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(50000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // utils.setServiceState(true);
        if (intent != null && intent.getSerializableExtra("LocationServiceArray") != null) {
            locationItems = new ArrayList<>(convertItemLatLngToLocation((ArrayList<LocationServiceItem>) intent.getSerializableExtra("LocationServiceArray")));
            Log.d(TAG, "SIZE " + locationItems.size());
        } else {
            Log.d(TAG, "Intent or array is null");
        }
        mGoogleApiClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = getUserLocation();
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //utils.setServiceState(false);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        Log.d(TAG, "service destroyed");
    }


    public Location getUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            return location;
        }
        //if location null move camera to London
        location = new Location("provider");
        location.setLatitude(51.50722);
        location.setLongitude(-0.12750);
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (locationItems != null)
            Log.d(TAG, "SIZE " + locationItems.size());
        else
            Log.d(TAG, "LocationItems is null");

        checkForBelongingToArea(location, locationItems);
    }

    private void checkForBelongingToArea(Location userCurrentLocation, ArrayList<LocationServiceItemConverted> listOfLocations) {
        if (listOfLocations == null) return;

        for (LocationServiceItemConverted location : listOfLocations) {
            float distance = userCurrentLocation.distanceTo(location.getLocation());

            //send notification that user enter area
            if (distance < RADIUS_IN_METERS && !location.isInside()) {
                location.setInside(true);
                sendBroadcast(new Intent(BROADCAST_ACTION)
                        .putExtra(NOTIF_TITLE_EXTRA, "Entered " + location.getTitle() + " area")
                        .putExtra(NOTIF_DESCRIPTION_EXTRA, location.getDescription()));
            }
            //send notification that user leave area
            if (distance > RADIUS_IN_METERS && location.isInside()) {
                location.setInside(false);
                sendBroadcast(new Intent(BROADCAST_ACTION)
                        .putExtra(NOTIF_TITLE_EXTRA, "Exited " + location.getTitle() + " area")
                        .putExtra(NOTIF_DESCRIPTION_EXTRA, location.getDescription()));
            }
        }
    }


    public ArrayList<LocationServiceItemConverted> convertItemLatLngToLocation(ArrayList<LocationServiceItem> locationServiceItems) {
        ArrayList<LocationServiceItemConverted> result = new ArrayList<>();
        for (LocationServiceItem location : locationServiceItems) {
            Location loc = new Location("provider");
            loc.setLatitude(location.getLocationItem().getLatitude());
            loc.setLongitude(location.getLocationItem().getLongitude());
            result.add(new LocationServiceItemConverted(location.getLocationItem().getTitle(), location.getLocationItem().getDescription(), loc, location.isInside()));
        }
        return result;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
