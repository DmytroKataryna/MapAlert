package com.example.dmytro.mapalert.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class LocationActivity extends ActionBarActivity implements OnMapReadyCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        TimePicker.OnTimeChangedListener, View.OnFocusChangeListener {

    private static Integer dataBaseId;

    //Image intent constants
    private static final int SELECT_FILE = 1111;
    private static final int REQUEST_CAMERA = 9999;

    private LocationDataSource dataSource;
    private GoogleMap mGoogleMap;
    private EditText mSearchEditText, mTitleEditText, mDescriptionEditText;
    private ImageView mLocPhoto;
    private TimePicker mTimePicker;
    private TextView mRepeatTextView;
    private Switch mTimeSwitch;
    private CustomMapFragment mapFragment;
    private ScrollView scrollView;
    private Marker locationMarker;
    private Bitmap bitmap;
    private ImageButton mSearchButton;

    //Location object
    private LocationItem loc;
    private String mTitle, mDescription;
    private double latitude;
    private double longitude;
    private boolean mTimeSelected;
    private String mTime;
    private byte[] mPhoto;

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

        mTimeSwitch = (Switch) findViewById(R.id.timeSwitcher);
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

        //save image by default to variable (if user didn't chose any picture , this img will be saved to DB)
        bitmap = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_action_house);
        mPhoto = bitmapToByteArray(bitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //start edit mode (restore data )
        if (getIntent().getBooleanExtra(RecyclerViewAdapter.ITEM_EDIT_MODE, false)) {
            restoreData();
        }
    }

    private void restoreData() {
        loc = ((CursorLocation) getIntent().getSerializableExtra(RecyclerViewAdapter.ITEM_KEY)).getItem();
        dataBaseId = ((CursorLocation) getIntent().getSerializableExtra(RecyclerViewAdapter.ITEM_KEY)).getId();
        mTitleEditText.setText(loc.getTitle());
        mDescriptionEditText.setText(loc.getDescription());
        bitmap = byteArrayToBitmap(loc.getPhoto());

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

    @Override
    protected void onResume() {
        super.onResume();

        mLocPhoto.setImageBitmap(bitmap);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.mGoogleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setMyLocationEnabled(true);

        /////----------------------------------Move camera to user position or move to selected position--------------------------

        if (longitude == 0d & longitude == 0d) {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(getUserLocation().latitude, getUserLocation().longitude)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(getUserLocation().latitude, getUserLocation().longitude), 15));
        } else {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude), 15));
        }
        /////-----------------------------On Map click & On Location Change---------------------------

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //hide keyboard
                hideSoftKeyboard();

                //remove previous marker (on screen should be placed just single marker)
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

            case R.id.searchImageButton:
                LatLng location = findLocation(mSearchEditText.getText().toString());
                if (locationMarker != null) {
                    locationMarker.remove();
                    latitude = 0d;
                    longitude = 0d;
                }
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

                if (mTimeSelected) {
                    loc = new LocationItem(mTitle, mDescription, mTimeSelected, mPhoto, selectedItems, mTime, latitude, longitude);
                } else {
                    loc = new LocationItem(mTitle, mDescription, mTimeSelected, mPhoto, latitude, longitude);
                }

                if (dataBaseId == null) {
                    try { //depends on time switcher selection , it is saved different object
                        dataSource.createLocation(loc);
                        startActivity(new Intent(this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    try {
                        dataSource.updateLocation(dataBaseId, loc);
                        startActivity(new Intent(this, ListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
        this.mTimeSelected = isChecked;
        //if switcher ON TimePicker and Repeat editText is visible
        if (mTimeSelected) {
            mTimeLayout.setVisibility(View.VISIBLE);
            mTime = mTimePicker.getCurrentHour() + " : " + mTimePicker.getCurrentMinute();
        } else
            mTimeLayout.setVisibility(View.GONE);
    }

    /////------------------------ TimePicker listener ---------------------------------------------------------------

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mTime = hourOfDay + " : " + minute;
    }

    /////---------------------------------Map Search editText focus Listener & Tittle edit Text Listener---------------------
    //clear text when search is focused and scroll to bottom
    //Title field is required , so if text is empty display toast and change background color to red
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.searchEditText:
                if (hasFocus) {
                    scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
                    mSearchEditText.setText("");
                }
                break;
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

    /////----------------------------------Photo Dialog  onActivityResult------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    File f = new File(Environment.getExternalStorageDirectory()
                            .toString());
                    for (File temp : f.listFiles()) {
                        if (temp.getName().equals("temp.jpg")) {
                            f = temp;
                            break;
                        }
                    }
                    bitmap = decodeFile(f.getPath());      // create Bitmap
                    mLocPhoto.setImageBitmap(bitmap);
                    break;

                case SELECT_FILE:
                    String selectedImagePath = getAbsolutePath(data.getData());

                    bitmap = decodeFile(selectedImagePath);  // create Bitmap
                    mLocPhoto.setImageBitmap(bitmap);
                    break;
            }
        }
    }

    public String getAbsolutePath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 70;
            // Find the correct scale value. It should be the power of 2
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] bitmapToByteArray(Bitmap bm) {
        //convert Bitmap to byte array
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
        return blob.toByteArray();
    }

    public static Bitmap byteArrayToBitmap(byte[] array) {
        //convert  byte array to Bitmap
        return BitmapFactory.decodeByteArray(array, 0, array.length);
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
        //dataSource.close();
    }
}


