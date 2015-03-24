package com.example.dmytro.mapalert;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

public class LocationActivity extends ActionBarActivity implements OnMapReadyCallback, View.OnClickListener, View.OnFocusChangeListener {

    private static EditText mTimeEditText;
    private CustomMapFragment mapFragment;
    private ScrollView scrollView;
    private Marker locationMarker;
    private LatLng coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mapFragment = (CustomMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        mTimeEditText = (EditText) findViewById(R.id.timeEditText);
        mTimeEditText.setOnFocusChangeListener(this);

        ((CustomMapFragment) getFragmentManager().findFragmentById(R.id.map)).setListener(new CustomMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setMyLocationEnabled(true);

        /////----------------------------------Move camera to position user-----------------

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        final Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(16)                 // Sets camera zoom to 16
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

        /////----------------------------------------------------------------------------------

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //remove previous marker (on screen should be placed just single marker)
                if (locationMarker != null) {
                    locationMarker.remove();
                    coordinates = null;
                }

                //set marker
                locationMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                coordinates = latLng;
                //save coordinates to sharedPref
                Log.d("Location", "Coordinates" + latLng.toString());
            }
        });


        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                //save this coordinates to sharedPref and then Intent will compare it and send notification
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    //display TimePickerDialog when TimeEditText is in focus
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.timeEditText:
                if (hasFocus) {
                    DialogFragment newFragment = new TimePickerFragment();
                    newFragment.show(getFragmentManager(), "timePicker");
                    break;
                }
        }
    }

    //TimePicker Dialog
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mTimeEditText.setText(hourOfDay + " : " + minute);
        }
    }
}
