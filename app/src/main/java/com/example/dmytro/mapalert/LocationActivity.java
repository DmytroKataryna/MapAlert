package com.example.dmytro.mapalert;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class LocationActivity extends ActionBarActivity implements OnMapReadyCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        TimePicker.OnTimeChangedListener, View.OnFocusChangeListener {

    private GoogleMap mGoogleMap;
    private EditText mSearchEditText;
    private ImageView mLocPhoto;
    private TimePicker mTimePicker;
    private TextView mRepeatTextView;
    private Switch mTimeSwitch;
    private CustomMapFragment mapFragment;
    private ScrollView scrollView;
    private Marker locationMarker;
    private LatLng coordinates;
    private String mTime;
    private Button mSearchButton;

    //layouts
    private LinearLayout mHeadLayout, mTimeLayout;
    private RelativeLayout mRepeatLayout;

    //dialog items
    private TreeSet<Integer> selectedItems = new TreeSet<>();
    private boolean checkedDialogItems[] = new boolean[7];


    private static final int SELECT_FILE = 1111;
    private static final int REQUEST_CAMERA = 9999;
    private static final int FIRST = 0, SECOND = 1, THIRD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

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

        mSearchButton = (Button) findViewById(R.id.imageButton);
        mSearchButton.setOnClickListener(this);

        mTimeSwitch = (Switch) findViewById(R.id.timeSwitcher);
        mTimeSwitch.setOnCheckedChangeListener(this);

        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mTimePicker.setOnTimeChangedListener(this);

        mLocPhoto = (ImageView) findViewById(R.id.locationImageView);
        mLocPhoto.setOnClickListener(this);

        mapFragment = (CustomMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        ((CustomMapFragment) getFragmentManager().findFragmentById(R.id.map)).setListener(new CustomMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
                scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
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
                //save coordinates to DB
                Log.d("Location", "Coordinates" + coordinates);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.repeatLayout:
                createRepeatDialog();
                break;

            case R.id.locationImageView:
                createPhotoDialog();
                break;

            case R.id.imageButton:
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

    //Switcher listener (Show/Hide time layout)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            mTimeLayout.setVisibility(View.VISIBLE);
        else
            mTimeLayout.setVisibility(View.GONE);
        //if unChecked also delete time and repeat from object (Location)
    }

    //TimePicker listener
    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mTime = hourOfDay + " : " + minute;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get all fields data and save location to DB
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

    /////----------------------------------Photo Dialog -------------------------------

    private void createPhotoDialog() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case FIRST:
                        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        startActivityForResult(i, REQUEST_CAMERA);
                        break;
                    case SECOND:
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(
                                Intent.createChooser(intent, "Select File"),
                                SELECT_FILE);
                        break;
                    case THIRD:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.create().show();
    }

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
                    mLocPhoto.setImageBitmap(decodeFile(f.getPath()));
                    break;
                case SELECT_FILE:
                    String selectedImagePath = getAbsolutePath(data.getData());
                    mLocPhoto.setImageBitmap(decodeFile(selectedImagePath));
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


    /////----------------------------------Repeat Dialog -------------------------------
    private void createRepeatDialog() {
        final CharSequence[] items =
                {"Every Monday", "Every Tuesday ", "Every Wednesday ", "Every Thursday", "Every Friday", "Every Saturday", "Every Sunday"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Repeat");
        builder.setMultiChoiceItems(items, checkedDialogItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedDialogItems[which] = isChecked;
                if (isChecked) {
                    selectedItems.add(which);
                } else if (selectedItems.contains(which)) {
                    selectedItems.remove(Integer.valueOf(which));
                }
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mRepeatTextView.setText(convertDays(selectedItems));

            }
        });
        //show dialog
        builder.create().show();
    }

    private String convertDays(TreeSet<Integer> selectedItems) {
        final CharSequence[] items =
                {"Mon ", "Tue ", "Wed ", "Thu ", "Fri ", "Sat ", "Sun "};

        if (selectedItems.size() == 0) return "Never >";
        else if (selectedItems.size() == 7) return "Every Day >";

        StringBuilder builder = new StringBuilder();

        for (Integer integer : selectedItems) {
            builder.append(items[integer]);
        }
        builder.append(">");

        //+add Weekdays & Weekends when 0,1,2,3,4 selected and 5,6  selected
        return builder.toString();
    }


    /////---------------------------------Map Search focus Listener-----------------------------------------
    //clear text when search is focused and scroll to bottom
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.searchEditText && hasFocus) {
            scrollView.scrollTo(scrollView.getBottom(), scrollView.getBottom());
            mSearchEditText.setText("");
        }
    }
    /////--------------------------------------------------------------------------
}


