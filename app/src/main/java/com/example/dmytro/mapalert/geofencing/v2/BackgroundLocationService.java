package com.example.dmytro.mapalert.geofencing.v2;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = "BackgroundServiceTAG";
    public static final String BROADCAST_ACTION = "com.example.mapalert.location.receiver";
    public static final Integer RADIUS_IN_METERS = 50;

    private GoogleApiClient mGoogleApiClient;
    private final Handler handler = new Handler();

    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    AlarmManager alarmManager;
    NotificationManager nm;
    Intent i;

    @Override
    public void onCreate() {
        super.onCreate();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        i = new Intent(BROADCAST_ACTION);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(50000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        //get data from intent  (latitude and longitude ) and add it to List

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = getUserLocation();
        startLocationUpdates();
    }


    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }


    public Location getUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            Log.d(TAG, "LAT " + location.getLatitude() + " LNG " + location.getLongitude());
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
        //SFO hard coding
        Location center = new Location("provider");
        center.setLatitude(37.621313);
        center.setLongitude(-122.378955);

        float distance = location.distanceTo(center);

        if (distance < RADIUS_IN_METERS) {
            sendBroadcast(new Intent(BROADCAST_ACTION));
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
}
