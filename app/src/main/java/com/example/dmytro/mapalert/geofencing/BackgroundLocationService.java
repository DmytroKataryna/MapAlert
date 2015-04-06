package com.example.dmytro.mapalert.geofencing;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationServiceItemConverted;
import com.example.dmytro.mapalert.utils.LocationDataSource;
import com.example.dmytro.mapalert.utils.PreferencesUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = "BackgroundServiceTAG";

    public static final String BROADCAST_ACTION = "com.example.mapalert.location.receiver";
    public static final String NOTIF_DESCRIPTION_EXTRA = "notification_receiver_description";
    public static final String NOTIF_TITLE_EXTRA = "notification_receiver_title";
    public static final String NOTIF_IMAGE_PATH_EXTRA = "notification_receiver_image_path";

    public static final Integer RADIUS_IN_METERS = 25;

    private PreferencesUtils utils;
    private LocationDataSource dataSource;

    private GoogleApiClient mGoogleApiClient;
    private ArrayList<LocationServiceItemConverted> locationItems;

    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        utils = PreferencesUtils.get(getApplicationContext());
        dataSource = LocationDataSource.get(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30 * 1000);  //30 sec (just for test ) it should be 2 min
        mLocationRequest.setFastestInterval(5 * 1000); //5 sec (just for test ) it should be 1.5 min
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        utils.setServiceState(true);
        updateLocationData();

        Log.d(TAG, "service started command");
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
        utils.setServiceState(false);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        Log.d(TAG, "service destroyed");
    }


    public Location getUserLocation() {
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        if (utils.isDataChanged()) updateLocationData();

//        if (locationItems != null)   //temp (debug)
//            Log.d(TAG, "SIZE " + locationItems.size());
//        else
//            Log.d(TAG, "LocationItems is null");

        checkForBelongingToArea(location, locationItems);
    }

    private void checkForBelongingToArea(Location userCurrentLocation, ArrayList<LocationServiceItemConverted> listOfLocations) {
        if (listOfLocations == null) return;

        for (LocationServiceItemConverted location : listOfLocations) {
            float distance = userCurrentLocation.distanceTo(location.getLocation());

            Toast.makeText(getApplicationContext(), "DIS " + distance, Toast.LENGTH_SHORT).show();

            //send notification that user enter area
            if (distance < RADIUS_IN_METERS && !location.isInside()) {
                location.setInside(true);
                updateInsideStatus(location.getDataBaseId(), true);
                sendBroadcast(new Intent(BROADCAST_ACTION)

                        .putExtra(NOTIF_TITLE_EXTRA, "Entered " + location.getTitle() + " area")
                        .putExtra(NOTIF_DESCRIPTION_EXTRA, location.getDescription())
                        .putExtra(NOTIF_IMAGE_PATH_EXTRA, location.getImagePath()));
            }
            //send notification that user leave area
            if (distance > RADIUS_IN_METERS && location.isInside()) {
                location.setInside(false);
                updateInsideStatus(location.getDataBaseId(), false);

                sendBroadcast(new Intent(BROADCAST_ACTION)
                        .putExtra(NOTIF_TITLE_EXTRA, "Exited " + location.getTitle() + " area")
                        .putExtra(NOTIF_DESCRIPTION_EXTRA, location.getDescription())
                        .putExtra(NOTIF_IMAGE_PATH_EXTRA, location.getImagePath()));
            }
        }
    }

    public ArrayList<LocationServiceItemConverted> convertCursorItemLocationToServiceLocation(List<CursorLocation> cursorLocationItems) {
        ArrayList<LocationServiceItemConverted> result = new ArrayList<>();
        for (CursorLocation location : cursorLocationItems) {
            Location loc = new Location("provider");
            loc.setLatitude(location.getItem().getLatitude());
            loc.setLongitude(location.getItem().getLongitude());
            result.add(new LocationServiceItemConverted(
                    location.getId(),
                    location.getItem().getTitle(),
                    location.getItem().getDescription(),
                    loc,
                    location.getItem().getImagePath(),
                    location.getInside()));
        }
        return result;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void updateLocationData() {
        dataSource.open();

        //get Data from DB
        try {
            locationItems = convertCursorItemLocationToServiceLocation(dataSource.getAllLocationItems());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        utils.setServiceDataChanged(false);

        //dataSource.close();  крешиться в цьому місці
    }

    public void updateInsideStatus(Integer id, boolean inside) {
        dataSource.open();

        Integer insideInt;
        if (inside) insideInt = 1;
        else insideInt = 0;
        //get Data from DB
        dataSource.updateInsideStatus(id, insideInt);

        //dataSource.close();  крешиться в цьому місці
    }
}
