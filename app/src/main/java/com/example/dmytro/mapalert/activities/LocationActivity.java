
package com.example.dmytro.mapalert.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.views.CustomMapFragment;
import com.example.dmytro.mapalert.activities.views.PhotoDialog;
import com.example.dmytro.mapalert.activities.views.RecyclerViewAdapter;
import com.example.dmytro.mapalert.activities.views.RepeatDialog;
import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationItem;
import com.example.dmytro.mapalert.utils.ImageUtil;
import com.example.dmytro.mapalert.utils.LocationDataSource;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

//Я старався повиносити частини коду в інші класи , бо цей вийшов дуже занадто громіздкий
//ще не чистив код (будуть попадатися зміні які не використовують і т.д.)
public class LocationActivity extends ActionBarActivity implements OnMapReadyCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        TimePicker.OnTimeChangedListener, View.OnFocusChangeListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "LocationActivity";

    private static Integer dataBaseId;
    private GoogleApiClient mApiClient;

    //Image intent constants
    private static final int SELECT_FILE = 1111;
    private static final int REQUEST_CAMERA = 9999;

    private LocationDataSource dataSource;
    private GoogleMap mGoogleMap;
    private EditText mSearchEditText, mTitleEditText, mDescriptionEditText;
    private TimePicker mTimePicker;
    private TextView mRepeatTextView;
    private SwitchCompat mTimeSwitch;
    private CustomMapFragment mapFragment;
    private ScrollView scrollView;
    private Marker locationMarker;
    private ImageButton mSearchButton;

    //Image
    private ImageView mLocPhoto;
    private Bitmap bitmap;
    private File imagePathFile;
    private String imagePath;

    //Location object
    private LocationItem loc;
    private String mTitle, mDescription;
    private double latitude;
    private double longitude;
    private boolean mTimeSelected;
    private String mTime;

    //layouts
    private LinearLayout mHeadLayout, mTimeLayout;
    private RelativeLayout mRepeatLayout;

    //dialog items
    private TreeSet<Integer> selectedItems = new TreeSet<>();
    private boolean checkedDialogItems[] = new boolean[7];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        dataSource = LocationDataSource.get(getApplicationContext());
        dataSource.open();

        mHeadLayout = (LinearLayout) findViewById(R.id.headLayout);
        mHeadLayout.setOnClickListener(this);

        mRepeatLayout = (RelativeLayout) findViewById(R.id.repeatLayout);
        mRepeatLayout.setOnClickListener(this);
        mTimeLayout = (LinearLayout) findViewById(R.id.timeLayout);
        mTimeLayout.setVisibility(View.GONE);

        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mSearchEditText.setOnFocusChangeListener(this);

        mTitleEditText = (EditText) findViewById(R.id.titleEditText);
        mTitleEditText.setOnFocusChangeListener(this);

        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        mRepeatTextView = (TextView) findViewById(R.id.repeatTextView);

        mSearchButton = (ImageButton) findViewById(R.id.searchImageButton);
        mSearchButton.setOnClickListener(this);

        mTimeSwitch = (SwitchCompat) findViewById(R.id.timeSwitcher);
        mTimeSwitch.setOnCheckedChangeListener(this);

        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mTimePicker.setOnTimeChangedListener(this);

        mLocPhoto = (ImageView) findViewById(R.id.locationImageView);
        mLocPhoto.setOnClickListener(this);

        mapFragment = (CustomMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();


        //start edit mode (restore data )
        if (getIntent().getBooleanExtra(RecyclerViewAdapter.ITEM_EDIT_MODE, false))
            restoreData();

    }

    //------------------------------------------------- RESTORE DATA (location object from DB )---------------------
    private void restoreData() {
        loc = ((CursorLocation) getIntent().getSerializableExtra(RecyclerViewAdapter.ITEM_KEY)).getItem();
        dataBaseId = ((CursorLocation) getIntent().getSerializableExtra(RecyclerViewAdapter.ITEM_KEY)).getId();
        mTitleEditText.setText(loc.getTitle());
        mDescriptionEditText.setText(loc.getDescription());
        imagePath = loc.getImagePath();  // get image path from db

        Picasso.with(getApplicationContext()).load(new File(imagePath))
                .placeholder(R.mipmap.ic_action_house)
                .into(mLocPhoto);

        if (loc.isTimeSelected()) {
            mTimeSwitch.setChecked(true);
            //parse Time string to Hour  & Minute
            String time[] = loc.getTime().split(" : ");
            mTimePicker.setCurrentHour(Integer.valueOf(time[0]));
            mTimePicker.setCurrentMinute(Integer.valueOf(time[1]));

            selectedItems = loc.getRepeat();
            for (Integer i : selectedItems) {
                checkedDialogItems[i] = true;
            }
            mRepeatTextView.setText(new RepeatDialog().convertDays(selectedItems));
        }
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
    }

    /////----------------------------------Photo Dialog  onActivityResult------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    //f.getPath() завжди незміний / тому можна захаркодити /storage/emulated/0/temp.jpg
                    File f = new File(Environment.getExternalStorageDirectory()
                            .toString());
                    for (File temp : f.listFiles()) {
                        if (temp.getName().equals("temp.jpg")) {
                            f = temp;
                            break;
                        }
                    }
                    //delete previous image
                    if (imagePath != null) {
                        new File(imagePath).delete();
                    }
                    bitmap = ImageUtil.decodeFile(f.getPath());      // create Bitmap
                    imagePathFile = ImageUtil.saveToInternalStorage(getApplicationContext(), bitmap);

                    mLocPhoto.post(new Runnable() {
                        @Override
                        public void run() {
                            mLocPhoto.setImageBitmap(bitmap);
                        }
                    });
                    imagePath = imagePathFile.getPath();

//                    Picasso picasso = Picasso.with(getApplicationContext());
//                    picasso.invalidate(imagePathFile);
//                    picasso.load(imagePathFile).placeholder(R.mipmap.ic_action_house)
//                            .into(mLocPhoto);

                    break;

                case SELECT_FILE:
                    String selectedImagePath = ImageUtil.getAbsolutePath(this, data.getData());

                    //delete previous image
                    if (imagePath != null) {
                        new File(imagePath).delete();
                    }

                    bitmap = ImageUtil.decodeFile(selectedImagePath);  // create Bitmap
                    imagePathFile = ImageUtil.saveToInternalStorage(getApplicationContext(), bitmap);

                    //mLocPhoto.setImageBitmap(bitmap);

                    mLocPhoto.post(new Runnable() {
                        @Override
                        public void run() {
                            mLocPhoto.setImageBitmap(bitmap);
                        }
                    });
                    imagePath = imagePathFile.getPath();

//                    Picasso.with(getApplicationContext()).load(imagePathFile)
//                            .placeholder(R.mipmap.ic_action_house)
//                            .into(mLocPhoto);

                    break;
            }
        }
    }


    @Override
    public void onClick(View view) {
        //hide soft keyboard
        hideSoftKeyboard();

        switch (view.getId()) {
            //repeat dialog
            case R.id.repeatLayout:
                new RepeatDialog(this, mRepeatTextView, selectedItems, checkedDialogItems)
                        .createRepeatDialog();
                break;
            //photo dialog
            case R.id.locationImageView:
                new PhotoDialog(this).createPhotoDialog();
                break;
            //search button (placed on MapFragment)
            case R.id.searchImageButton:
                LatLng location = findLocation(mSearchEditText.getText().toString());
                //first delete previous marker and clear coordinates
                if (locationMarker != null) {
                    locationMarker.remove();
                    latitude = 0d;
                    longitude = 0d;
                }
                //add marker ,animate map and save coordinates
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                locationMarker = mGoogleMap.addMarker(new MarkerOptions().position(location));
                latitude = location.latitude;
                longitude = location.longitude;
                break;

            //change scroll view position
            case R.id.headLayout:
                scrollView.scrollTo(scrollView.getTop(), scrollView.getTop());
                mHeadLayout.setFocusable(true);
                break;
        }
    }

    /////---------------------------------- Activity Menu -------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:

                if (!checkIfAvailableToLogin()) return false;

                if (imagePath == null) //if nothing selected , save default img
                    imagePath = "drawable://" + R.mipmap.ic_action_house;

                if (mTimeSelected) { //depends on time switcher selection , it is saved different object
                    loc = new LocationItem(mTitle, mDescription, mTimeSelected, imagePath, selectedItems, mTime, latitude, longitude);
                } else {
                    loc = new LocationItem(mTitle, mDescription, mTimeSelected, imagePath, latitude, longitude);
                }


                try { //create or update location
                    if (!getIntent().getBooleanExtra(RecyclerViewAdapter.ITEM_EDIT_MODE, false)) {
                        dataSource.createLocation(loc);
                        startActivity(new Intent(this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } else {
                        dataSource.updateLocation(dataBaseId, loc);
                        startActivity(new Intent(this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //check Title field and save location to  DB just if title isn't empty
    private boolean checkIfAvailableToLogin() {
        mTitle = mTitleEditText.getText().toString();
        mDescription = mDescriptionEditText.getText().toString();
        if (mTitle == null || mTitle.length() < 1) {
            mTitleEditText.setBackgroundResource(R.drawable.text_view_red_background);
            Toast toast = Toast.makeText(this, "This field is required", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
            return false;
        }
        return true;
    }


    /////------------------------ Switcher listener (Show/Hide time layout) ----------------------------------------------

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        hideSoftKeyboard();
        //if switcher ON TimePicker and RepeatEditText is visible
        if (mTimeSwitch.isChecked()) {
            mTimeSelected = true;
            mTimeLayout.setVisibility(View.VISIBLE);
            mTime = mTimePicker.getCurrentHour() + " : " + mTimePicker.getCurrentMinute();
        } else {
            mTimeSelected = false;
            mTimeLayout.setVisibility(View.GONE);
        }

    }

    /////------------------------ TimePicker listener ---------------------------------------------------------------

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mTime = hourOfDay + " : " + minute;
    }

    /////---------------------------------Map Search editText focus Listener & Tittle edit Text Listener---------------------

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            //clear text when search is focused and scroll to bottom
            case R.id.searchEditText:
                if (hasFocus) {
                    scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
                }
                break;

            //Title field is required , so if text is empty display toast and change background color to red
            case R.id.titleEditText:
                if (hasFocus) {
                    mTitleEditText.setBackgroundResource(R.drawable.text_view_background);
                } else {
                    if (mTitleEditText.getText().length() < 1) {
                        Toast toast = Toast.makeText(this, "This field is required", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
                        toast.show();
                        mTitleEditText.setBackgroundResource(R.drawable.text_view_red_background);
                    } else {
                        mTitleEditText.setBackgroundResource(R.drawable.text_view_background);
                    }

                }
                break;
        }
    }
    /////------------------------ Hide KeyBoard --------------------------------------------------------------------------

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
    }


    /////--------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //dataSource.close();  //make bug
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Custom Map Fragment , where i handle scroll UP/DOWn problems
        ((CustomMapFragment) getFragmentManager().findFragmentById(R.id.map)).setListener(new CustomMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
                //scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
            }
        });
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mApiClient.connect();
    }

    //  --------------------------------- Google Maps -----------------------------------------
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.mGoogleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setMyLocationEnabled(true);

        ///////////Move camera to user position or move to selected position

        if (longitude == 0d & longitude == 0d) {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(getUserLocation().latitude, getUserLocation().longitude)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(getUserLocation().latitude, getUserLocation().longitude), 15));
        } else {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude), 15));
        }
        ////////// On Map click & On Location Change

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //hide keyboard
                hideSoftKeyboard();

                //remove previous marker (on screen should be placed just single(one) marker)
                if (locationMarker != null) {
                    locationMarker.remove();
                    latitude = 0d;
                    longitude = 0d;
                }
                //set marker
                locationMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                latitude = latLng.latitude;
                longitude = latLng.longitude;
            }
        });

        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                //compare this coordinates with location coordinate and if user is out of area send notification
            }
        });
    }

    /////----------------------------------Find location by address  -------------------------------

    public LatLng findLocation(String address) {
        Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gc.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //if no addresses found move to user current location
        Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
        return new LatLng(getUserLocation().latitude, getUserLocation().longitude);
    }

    /////--------------------------------Get User Location--------------------------------

    public LatLng getUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
////        Criteria criteria = new Criteria();

        final Location location = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        if (location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        //if location null move camera to London
        return new LatLng(51.50722, -0.12750);
    }
}


//package com.example.dmytro.mapalert.activities;
//
//import android.app.Activity;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.os.Bundle;
//import android.os.Environment;
//import android.support.v7.app.ActionBarActivity;
//import android.support.v7.widget.SwitchCompat;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.ScrollView;
//import android.widget.TextView;
//import android.widget.TimePicker;
//import android.widget.Toast;
//
//import com.example.dmytro.mapalert.R;
//import com.example.dmytro.mapalert.activities.views.CustomMapFragment;
//import com.example.dmytro.mapalert.activities.views.PhotoDialog;
//import com.example.dmytro.mapalert.activities.views.RecyclerViewAdapter;
//import com.example.dmytro.mapalert.activities.views.RepeatDialog;
//import com.example.dmytro.mapalert.geofencing.GeofenceTransitionsIntentService;
//import com.example.dmytro.mapalert.pojo.CursorLocation;
//import com.example.dmytro.mapalert.pojo.LocationItem;
//import com.example.dmytro.mapalert.utils.Constants;
//import com.example.dmytro.mapalert.utils.ImageUtil;
//import com.example.dmytro.mapalert.utils.LocationDataSource;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.Geofence;
//import com.google.android.gms.location.GeofencingRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.squareup.picasso.Picasso;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.TreeSet;
//
////location activity (almost done)
//
////Я старався повиносити частини коду в інші класи , бо цей вийшов дуже занадто громіздкий
////ще не чистив код (будуть попадатися зміні які не використовують і т.д.)
//public class LocationActivity extends ActionBarActivity
//        implements OnMapReadyCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
//        TimePicker.OnTimeChangedListener, View.OnFocusChangeListener, GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
//
//    private static final String TAG = "LocationActivity";
//    private static Integer dataBaseId;
//
//    //Image intent constants
//    private static final int SELECT_FILE = 1111;
//    private static final int REQUEST_CAMERA = 9999;
//
//    private LocationDataSource dataSource;
//    private GoogleApiClient mApiClient;
//    private GoogleMap mGoogleMap;
//    private EditText mSearchEditText, mTitleEditText, mDescriptionEditText;
//    private TimePicker mTimePicker;
//    private TextView mRepeatTextView;
//    private SwitchCompat mTimeSwitch;
//    private CustomMapFragment mapFragment;
//    private ScrollView scrollView;
//    private Marker locationMarker;
//    private ImageButton mSearchButton;
//    //Image
//    private ImageView mLocPhoto;
//    private Bitmap bitmap;
//    private File imagePathFile;
//    private String imagePath;
//
//    //Location object
//    private LocationItem loc;
//    private String mTitle, mDescription;
//    private double latitude;
//    private double longitude;
//    private boolean mTimeSelected;
//    private String mTime;
//
//    //layouts
//    private LinearLayout mHeadLayout, mTimeLayout;
//    private RelativeLayout mRepeatLayout;
//
//    //dialog items
//    private TreeSet<Integer> selectedItems = new TreeSet<>();
//    private boolean checkedDialogItems[] = new boolean[7];
//
//    //GeoFence
//    private PendingIntent mGeofencePendingIntent;
//    protected ArrayList<Geofence> mGeofenceList;
//
//    private Button mAddGeofencesButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_location);
//
//        mApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//
//        mApiClient.connect();
//        mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
//        mGeofencePendingIntent = null;
//        mGeofenceList = new ArrayList<Geofence>();
//        populateGeofenceList();
//
//        dataSource = LocationDataSource.get(getApplicationContext());
//        dataSource.open();
//
//        mHeadLayout = (LinearLayout) findViewById(R.id.headLayout);
//        mHeadLayout.setOnClickListener(this);
//
//        mRepeatLayout = (RelativeLayout) findViewById(R.id.repeatLayout);
//        mRepeatLayout.setOnClickListener(this);
//        mTimeLayout = (LinearLayout) findViewById(R.id.timeLayout);
//        mTimeLayout.setVisibility(View.GONE);
//
//        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
//        mSearchEditText.setOnFocusChangeListener(this);
//
//        mTitleEditText = (EditText) findViewById(R.id.titleEditText);
//        mTitleEditText.setOnFocusChangeListener(this);
//
//        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
//
//        scrollView = (ScrollView) findViewById(R.id.scrollView);
//        mRepeatTextView = (TextView) findViewById(R.id.repeatTextView);
//
//        mSearchButton = (ImageButton) findViewById(R.id.searchImageButton);
//        mSearchButton.setOnClickListener(this);
//
//        mTimeSwitch = (SwitchCompat) findViewById(R.id.timeSwitcher);
//        mTimeSwitch.setOnCheckedChangeListener(this);
//
//        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
//        mTimePicker.setOnTimeChangedListener(this);
//
//        mLocPhoto = (ImageView) findViewById(R.id.locationImageView);
//        mLocPhoto.setOnClickListener(this);
//
//
//        //start edit mode (restore data )
//        if (getIntent().getBooleanExtra(RecyclerViewAdapter.ITEM_EDIT_MODE, false))
//            restoreData();
//
//    }
//
//    //------------------------------------------------- RESTORE DATA (location object from DB )---------------------
//    private void restoreData() {
//        loc = ((CursorLocation) getIntent().getSerializableExtra(RecyclerViewAdapter.ITEM_KEY)).getItem();
//        dataBaseId = ((CursorLocation) getIntent().getSerializableExtra(RecyclerViewAdapter.ITEM_KEY)).getId();
//        mTitleEditText.setText(loc.getTitle());
//        mDescriptionEditText.setText(loc.getDescription());
//        imagePath = loc.getImagePath();  // get image path from db
//
//        Picasso.with(getApplicationContext()).load(new File(imagePath))
//                .placeholder(R.mipmap.ic_action_house)
//                .into(mLocPhoto);
//
//        if (loc.isTimeSelected()) {
//            mTimeSwitch.setChecked(true);
//            //parse Time string to Hour  & Minute
//            String time[] = loc.getTime().split(" : ");
//            mTimePicker.setCurrentHour(Integer.valueOf(time[0]));
//            mTimePicker.setCurrentMinute(Integer.valueOf(time[1]));
//
//            selectedItems = loc.getRepeat();
//            for (Integer i : selectedItems) {
//                checkedDialogItems[i] = true;
//            }
//            mRepeatTextView.setText(new RepeatDialog().convertDays(selectedItems));
//        }
//        latitude = loc.getLatitude();
//        longitude = loc.getLongitude();
//    }
//
//    /////----------------------------------Photo Dialog  onActivityResult------------------------------------------
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            switch (requestCode) {
//                case REQUEST_CAMERA:
//                    //f.getPath() завжди незміний / тому можна захаркодити /storage/emulated/0/temp.jpg
//                    File f = new File(Environment.getExternalStorageDirectory()
//                            .toString());
//                    for (File temp : f.listFiles()) {
//                        if (temp.getName().equals("temp.jpg")) {
//                            f = temp;
//                            break;
//                        }
//                    }
//                    //delete previous image
//                    if (imagePath != null) {
//                        new File(imagePath).delete();
//                    }
//                    bitmap = ImageUtil.decodeFile(f.getPath());      // create Bitmap
//                    imagePathFile = ImageUtil.saveToInternalStorage(getApplicationContext(), bitmap);
//
//                    mLocPhoto.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mLocPhoto.setImageBitmap(bitmap);
//                        }
//                    });
//                    imagePath = imagePathFile.getPath();
//
//                   Picasso picasso = Picasso.with(getApplicationContext());
//                   picasso.invalidate(imagePathFile);
//                    picasso.load(imagePathFile).placeholder(R.mipmap.ic_action_house)
//                            .into(mLocPhoto);
//
//                    break;
//
//                case SELECT_FILE:
//                    String selectedImagePath = ImageUtil.getAbsolutePath(this, data.getData());
//
//                    //delete previous image
//                    if (imagePath != null) {
//                        new File(imagePath).delete();
//                    }
//
//                    bitmap = ImageUtil.decodeFile(selectedImagePath);  // create Bitmap
//                    imagePathFile = ImageUtil.saveToInternalStorage(getApplicationContext(), bitmap);
//
//                    //mLocPhoto.setImageBitmap(bitmap);
//
//                    mLocPhoto.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mLocPhoto.setImageBitmap(bitmap);
//                        }
//                    });
//                    imagePath = imagePathFile.getPath();
//
//                    Picasso.with(getApplicationContext()).load(imagePathFile)
//                            .placeholder(R.mipmap.ic_action_house)
//                            .into(mLocPhoto);
//
//                    break;
//            }
//        }
//    }
//    // _____-----------------------------On Click __------------------------
//
//    @Override
//    public void onClick(View view) {
//        //hide soft keyboard
//        hideSoftKeyboard();
//
//        switch (view.getId()) {
//            //repeat dialog
//            case R.id.repeatLayout:
//                new RepeatDialog(this, mRepeatTextView, selectedItems, checkedDialogItems)
//                        .createRepeatDialog();
//                break;
//            //photo dialog
//            case R.id.locationImageView:
//                new PhotoDialog(this).createPhotoDialog();
//                break;
//            //search button (placed on MapFragment)
//            case R.id.searchImageButton:
//                LatLng location = findLocation(mSearchEditText.getText().toString());
//                //first delete previous marker and clear coordinates
//                if (locationMarker != null) {
//                    locationMarker.remove();
//                    latitude = 0d;
//                    longitude = 0d;
//                }
//                //add marker ,animate map and save coordinates
//                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
//                locationMarker = mGoogleMap.addMarker(new MarkerOptions().position(location));
//                latitude = location.latitude;
//                longitude = location.longitude;
//                break;
//
//            //change scroll view position
//            case R.id.headLayout:
//                scrollView.scrollTo(scrollView.getTop(), scrollView.getTop());
//                mHeadLayout.setFocusable(true);
//                break;
//        }
//    }
//
//    /////---------------------------------- Activity Menu -------------------------------
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.location_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.done:
//
//                if (!checkIfAvailableToLogin()) return false;
//
//                if (imagePath == null) //if nothing selected , save default img
//                    imagePath = "drawable://" + R.mipmap.ic_action_house;
//
//                if (mTimeSelected) { //depends on time switcher selection , it is saved different object
//                    loc = new LocationItem(mTitle, mDescription, mTimeSelected, imagePath, selectedItems, mTime, latitude, longitude);
//                } else {
//                    loc = new LocationItem(mTitle, mDescription, mTimeSelected, imagePath, latitude, longitude);
//                }
//
//
//                try { //create or update location
//                    if (!getIntent().getBooleanExtra(RecyclerViewAdapter.ITEM_EDIT_MODE, false)) {
//                        dataSource.createLocation(loc);
//                        startActivity(new Intent(this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
//                    } else {
//                        dataSource.updateLocation(dataBaseId, loc);
//                        startActivity(new Intent(this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
//                    }
//                } catch (IOException | ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    //check Title field and save location to  DB just if title isn't empty
//    private boolean checkIfAvailableToLogin() {
//        mTitle = mTitleEditText.getText().toString();
//        mDescription = mDescriptionEditText.getText().toString();
//        if (mTitle == null || mTitle.length() < 1) {
//            mTitleEditText.setBackgroundResource(R.drawable.text_view_red_background);
//            Toast toast = Toast.makeText(this, "This field is required", Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
//            toast.show();
//            return false;
//        }
//        return true;
//    }
//
//    /////----------------------------------Find location by address  -------------------------------
//
//    public LatLng findLocation(String address) {
//        Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
//        List<Address> addresses;
//        try {
//            addresses = gc.getFromLocationName(address, 1);
//            if (addresses.size() > 0) {
//                return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //if no addresses found move to user current location
//        Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
//        return new LatLng(getUserLocation().latitude, getUserLocation().longitude);
//    }
//    /////------------------------ Switcher listener (Show/Hide time layout) ----------------------------------------------
//
//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        hideSoftKeyboard();
//        //if switcher ON TimePicker and RepeatEditText is visible
//        if (mTimeSwitch.isChecked()) {
//            mTimeSelected = true;
//            mTimeLayout.setVisibility(View.VISIBLE);
//            mTime = mTimePicker.getCurrentHour() + " : " + mTimePicker.getCurrentMinute();
//        } else {
//            mTimeSelected = false;
//            mTimeLayout.setVisibility(View.GONE);
//        }
//
//    }
//
//    /////------------------------ TimePicker listener ---------------------------------------------------------------
//
//    @Override
//    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//        mTime = hourOfDay + " : " + minute;
//    }
//
//    /////---------------------------------Map Search editText focus Listener & Tittle edit Text Listener---------------------
//
//    @Override
//    public void onFocusChange(View v, boolean hasFocus) {
//        switch (v.getId()) {
//            //clear text when search is focused and scroll to bottom
//            case R.id.searchEditText:
//                if (hasFocus) {
//                    scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
//                }
//                break;
//
//            //Title field is required , so if text is empty display toast and change background color to red
//            case R.id.titleEditText:
//                if (hasFocus) {
//                    mTitleEditText.setBackgroundResource(R.drawable.text_view_background);
//                } else {
//                    if (mTitleEditText.getText().length() < 1) {
//                        Toast toast = Toast.makeText(this, "This field is required", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
//                        toast.show();
//                        mTitleEditText.setBackgroundResource(R.drawable.text_view_red_background);
//                    } else {
//                        mTitleEditText.setBackgroundResource(R.drawable.text_view_background);
//                    }
//
//                }
//                break;
//        }
//    }
//    /////------------------------ Hide KeyBoard --------------------------------------------------------------------------
//
//    public void hideSoftKeyboard() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
//    }
//
//
//    /////------------------On Connection to Google Api callback and Get User Location method --------------------------------
//
//
//    @Override
//    public void onConnected(Bundle bundle) {
//
//        mapFragment = (CustomMapFragment) getFragmentManager()
//                .findFragmentById(R.id.map);
//
//        //Custom Map Fragment , where i handle scroll UP/DOWn problems
//        ((CustomMapFragment) getFragmentManager().findFragmentById(R.id.map)).setListener(new CustomMapFragment.OnTouchListener() {
//            @Override
//            public void onTouch() {
//                scrollView.requestDisallowInterceptTouchEvent(true);
//                //scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
//            }
//        });
//        mapFragment.getMapAsync(this);
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        // The connection to Google Play services was lost for some reason. We call connect() to
//        // attempt to re-establish the connection.
//        Log.i(TAG, "Connection suspended");
//        mApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//    }
//
//    public LatLng getUserLocation() {
////        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
////        Criteria criteria = new Criteria();
//
//        final Location location = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
//        if (location != null) {
//            return new LatLng(location.getLatitude(), location.getLongitude());
//        }
//        //if location null move camera to London
//        return new LatLng(51.50722, -0.12750);
//    }
//
//
//    //  --------------------------------- Google Maps -----------------------------------------
//    @Override
//    public void onMapReady(final GoogleMap googleMap) {
//        this.mGoogleMap = googleMap;
//
//        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//        googleMap.setMyLocationEnabled(true);
//
//        ///////////Move camera to user position or move to selected position
//
//        if (longitude == 0d & longitude == 0d) {
//            LatLng latLng = getUserLocation();
//            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)));
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                    new LatLng(latLng.latitude, latLng.longitude), 15));
//        } else {
//            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                    new LatLng(latitude, longitude), 15));
//        }
//        ////////// On Map click & On Location Change
//
//        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                //hide keyboard
//                hideSoftKeyboard();
//
//                //remove previous marker (on screen should be placed just single(one) marker)
//                if (locationMarker != null) {
//                    locationMarker.remove();
//                    latitude = 0d;
//                    longitude = 0d;
//                }
//                //set marker
//                locationMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
//                latitude = latLng.latitude;
//                longitude = latLng.longitude;
//
//
//            }
//        });
//
//        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//                //compare this coordinates with location coordinate and if user is out of area send notification
//            }
//        });
//    }
//
//    /////--------------------------------------------------------------------------------------
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //dataSource.close();  //make bug
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        mApiClient.connect();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mApiClient.disconnect();
//    }
//
//    @Override
//    public void onResult(Status status) {
//        if (status.isSuccess()) {
//            // Update state and save in shared preferences.
////            mGeofencesAdded = !mGeofencesAdded;
////            SharedPreferences.Editor editor = mSharedPreferences.edit();
////            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
////            editor.commit();
//
//            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
//            // geofences enables the Add Geofences button.
////            setButtonsEnabledState();
//
////            Toast.makeText(
////                    this,
////                    getString(mGeofencesAdded ? R.string.geofences_added :
////                            R.string.geofences_removed),
////                    Toast.LENGTH_SHORT
////            ).show();
////        } else {
////            // Get the status code for the error and log it using a user-friendly message.
////            String errorMessage = GeofenceErrorMessages.getErrorString(this,
////                    status.getStatusCode());
////            Log.e(TAG, errorMessage);
//        }
//    }
//
//    private PendingIntent getGeofencePendingIntent() {
//        // Reuse the PendingIntent if we already have it.
//        if (mGeofencePendingIntent != null) {
//            return mGeofencePendingIntent;
//        }
//        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
//        // addGeofences() and removeGeofences().
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
//    public void addGeofencesButtonHandler(View view) {
//        if (!mApiClient.isConnected()) {
//            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            LocationServices.GeofencingApi.addGeofences(
//                    mApiClient,
//                    // The GeofenceRequest object.
//                    getGeofencingRequest(),
//                    // A pending intent that that is reused when calling removeGeofences(). This
//                    // pending intent is used to generate an intent when a matched geofence
//                    // transition is observed.
//                    getGeofencePendingIntent()
//            ).setResultCallback(this); // Result processed in onResult().
//            //startService(new Intent(this, GeofenceTransitionsIntentService.class));
//        } catch (SecurityException securityException) {
//            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//            Log.e(TAG, securityException.toString());
//        }
//    }
//
//    /**
//     * Removes geofences, which stops further notifications when the device enters or exits
//     * previously registered geofences.
//     */
//    public void removeGeofencesButtonHandler(View view) {
//        if (!mApiClient.isConnected()) {
//            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
//            return;
//        }
//        try {
//            // Remove geofences.
//            LocationServices.GeofencingApi.removeGeofences(
//                    mApiClient,
//                    // This is the same pending intent that was used in addGeofences().
//                    getGeofencePendingIntent()
//            ).setResultCallback(this); // Result processed in onResult().
//        } catch (SecurityException securityException) {
//            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//            Log.e(TAG, securityException.toString());
//        }
//    }
//
//    public void populateGeofenceList() {
//        // for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {
//
//        Log.d(TAG, "Lat" + latitude + " long " + longitude);
//
//        Geofence geo = new Geofence.Builder().setRequestId("ID1").setCircularRegion(37.621313, -122.378955, 100).setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build();
//        mGeofenceList.add(geo);
//
////            mGeofenceList.add(new Geofence.Builder()
////                    // Set the request ID of the geofence. This is a string to identify this
////                    // geofence.
////                    .setRequestId(entry.getKey())
////
////                            // Set the circular region of this geofence.
////                    .setCircularRegion(
////                            entry.getValue().latitude,
////                            entry.getValue().longitude,
////                            Constants.GEOFENCE_RADIUS_IN_METERS
////                    ).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
////                            Geofence.GEOFENCE_TRANSITION_EXIT).build());
//
//        // Set the expiration duration of the geofence. This geofence gets automatically
//        // removed after this period of time.
//        //.setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
//
//        // Set the transition types of interest. Alerts are only generated for these
//        // transition. We track entry and exit transitions in this sample.
//
//
//        // Create the geofence.
//        //}
//    }
//
//    private GeofencingRequest getGeofencingRequest() {
//        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//
//        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
//        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
//        // is already inside that geofence.
//        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
//
//        // Add the geofences to be monitored by geofencing service.
//        builder.addGeofences(mGeofenceList);
//
//        // Return a GeofencingRequest.
//        return builder.build();
//    }
//}
//
//
