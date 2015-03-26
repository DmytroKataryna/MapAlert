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
import com.example.dmytro.mapalert.activities.views.RepeatDialog;
import com.example.dmytro.mapalert.pojo.LocationItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class LocationActivity extends ActionBarActivity implements OnMapReadyCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        TimePicker.OnTimeChangedListener, View.OnFocusChangeListener {

    private LocationDataSource dataSource;
    private GoogleMap mGoogleMap;
    private EditText mSearchEditText;
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
    private EditText mTitleEditText, mDescriptionEditText;
    private LatLng coordinates;
    private boolean mTimeSelected;
    private String mTime;
    private byte[] mPhoto;
    //repeat also

    //layouts
    private LinearLayout mHeadLayout, mTimeLayout;
    private RelativeLayout mRepeatLayout;

    //dialog items
    private TreeSet<Integer> selectedItems = new TreeSet<>();
    private boolean checkedDialogItems[] = new boolean[7];

    private static final int SELECT_FILE = 1111;
    private static final int REQUEST_CAMERA = 9999;

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
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
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

        /////-----------------------------On Map click & On Location Change---------------------------

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //hide keyboard
                hideSoftKeyboard();

                //remove previous marker (on screen should be placed just single marker)
                if (locationMarker != null) {
                    locationMarker.remove();
                    coordinates = null;
                }
                //set marker
                locationMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                coordinates = latLng;
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
                    coordinates = null;
                }
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
                locationMarker = mGoogleMap.addMarker(new MarkerOptions().position(location));
                coordinates = location;
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
                break;
        }

        if (mTimeSelected) {
            Toast.makeText(this, "Time" + mTimeSelected, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Time ELSE " + mTimeSelected, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
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
        return new LatLng(0, 0);
    }
    /////------------------------ Switcher listener (Show/Hide time layout) -----------------------------------

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        hideSoftKeyboard();
        this.mTimeSelected = isChecked;

        if (mTimeSelected)
            mTimeLayout.setVisibility(View.VISIBLE);
        else
            mTimeLayout.setVisibility(View.GONE);
    }

    /////------------------------ TimePicker listener -----------------------------------

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mTime = hourOfDay + " : " + minute;
    }

    /////---------------------------------Map Search editText focus Listener-----------------------------------------
    //clear text when search is focused and scroll to bottom
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.searchEditText && hasFocus) {
            scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
            mSearchEditText.setText("");
        }
    }
    /////------------------------ Hide KeyBoard -----------------------------------

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
    }

    /////----------------------------------Photo Dialog  onActivityResult-------------------------------

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
                    mPhoto = bitmapToByteArray(bitmap);    // convert Bitmap to byte array
                    mLocPhoto.setImageBitmap(bitmap);
                    break;

                case SELECT_FILE:
                    String selectedImagePath = getAbsolutePath(data.getData());

                    bitmap = decodeFile(selectedImagePath);  // create Bitmap
                    mPhoto = bitmapToByteArray(bitmap);      // convert Bitmap to byte array
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
            // Find the correct scale value. It should be the power of
            // 2.
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
        // Create the buffer with the correct size
        int iBytes = bm.getWidth() * bm.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);

        // Log.e("DBG", buffer.remaining()+""); -- Returns a correct number based on dimensions
        // Copy to buffer and then into byte array
        bm.copyPixelsToBuffer(buffer);
        // Log.e("DBG", buffer.remaining()+""); -- Returns 0
        return buffer.array();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}


