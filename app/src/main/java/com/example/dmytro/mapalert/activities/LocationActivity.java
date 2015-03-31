package com.example.dmytro.mapalert.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
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

import com.example.dmytro.mapalert.DBUtils.ImageUtil;
import com.example.dmytro.mapalert.DBUtils.LocationDataSource;
import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.views.CustomMapFragment;
import com.example.dmytro.mapalert.activities.views.PhotoDialog;
import com.example.dmytro.mapalert.activities.views.RecyclerViewAdapter;
import com.example.dmytro.mapalert.activities.views.RepeatDialog;
import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationItem;
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

//location activity (almost done)

//Я старався повиносити частини коду в інші класи , бо цей вийшов дуже занадто громіздкий
//ще не чистив код (будуть попадатися зміні які не використовують і т.д.)
public class LocationActivity extends ActionBarActivity implements OnMapReadyCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        TimePicker.OnTimeChangedListener, View.OnFocusChangeListener {

    private static Integer dataBaseId;

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

        //Custom Map Fragment , where i handle scroll UP/DOWn problems
        ((CustomMapFragment) getFragmentManager().findFragmentById(R.id.map)).setListener(new CustomMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
                //scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
            }
        });
        mapFragment.getMapAsync(this);

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


    /////--------------------------------Get User Location--------------------------------

    public LatLng getUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        final Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        //if location null move camera to London
        return new LatLng(51.50722, -0.12750);
    }

    /////--------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //dataSource.close();  //make bug
    }
}


