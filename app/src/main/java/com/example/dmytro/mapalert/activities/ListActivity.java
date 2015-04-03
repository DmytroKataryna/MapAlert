package com.example.dmytro.mapalert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.views.RecyclerViewAdapter;
import com.example.dmytro.mapalert.geofencing.BackgroundLocationService;
import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationServiceItem;
import com.example.dmytro.mapalert.utils.LocationDataSource;
import com.example.dmytro.mapalert.utils.PreferencesUtils;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//here i will place list of locations
//temporarily, activity have just button that displays the number of  that are saved in DB
public class ListActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ListActivityClass";

    private PreferencesUtils utils;
    private LocationDataSource dataSource;
    private List<CursorLocation> locationItems;
    private ArrayList<LocationServiceItem> locationItemsForService;
    private RecyclerView recyclerView;

    private FloatingActionButton mAddButton;
    private SwitchCompat mTrackSwitcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_location);

        utils = PreferencesUtils.get(getApplicationContext());
        dataSource = LocationDataSource.get(getApplicationContext());
        dataSource.open();

        //get Data from DB
        try {
            locationItems = dataSource.getAllLocationItems();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        recyclerView = (RecyclerView) findViewById(R.id.locationRecycleList);
        mAddButton = (FloatingActionButton) findViewById(R.id.fab_add_location);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, locationItems, mAddButton);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        mAddButton.attachToRecyclerView(recyclerView);

        if (locationItems != null)
            startService(new Intent(this, BackgroundLocationService.class));


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTrackSwitcher != null) {
            mTrackSwitcher.setChecked(utils.isServiceAlive());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_location_menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() == R.id.myswitch) {
                View view = MenuItemCompat.getActionView(item).findViewById(R.id.switchForActionBar);
                if (view != null) {
                    mTrackSwitcher = (SwitchCompat) view;
                    mTrackSwitcher.setOnCheckedChangeListener(this);
                    mTrackSwitcher.setChecked(utils.isServiceAlive());
                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.list_location_menu, menu);
//        for (int i = 0; i < menu.size(); i++) {
//            MenuItem item = menu.getItem(i);
//            if (item.getItemId() == R.id.myswitch) {
//                View view = MenuItemCompat.getActionView(item).findViewById(R.id.switchForActionBar);
//                if (view != null) {
//                    mTrackSwitcher = (SwitchCompat) view;
//                    mTrackSwitcher.setOnCheckedChangeListener(this);
//                    mTrackSwitcher.setChecked(utils.isServiceAlive());
//                }
//            }
//        }
//        return super.onPrepareOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
////        startService(new Intent(this, BackgroundLocationService.class)
////                .putExtra("LocationServiceArray", createDataForService(locationItems)));
//        switch (item.getItemId()) {
//            case R.id.myswitch:
//                SwitchCompat switchCompat = (SwitchCompat) item.getActionView();
//                if (switchCompat.isChecked()) {
//                    switchCompat.setChecked(false);
//                    Toast.makeText(getApplicationContext(), "Cheked", Toast.LENGTH_SHORT).show();
//                } else {
//                    switchCompat.setChecked(true);
//                    Toast.makeText(getApplicationContext(), "UN Cheked", Toast.LENGTH_SHORT).show();
//                }
//
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    private ArrayList<LocationServiceItem> createDataForService(List<CursorLocation> locationCursorItems) {
//        locationItemsForService = new ArrayList<>();
//        for (CursorLocation location : locationCursorItems) {
//            locationItemsForService.add(new LocationServiceItem(location.getItem(), false));
//        }
//        return locationItemsForService;
//    }

    public void addButtonListener(View view) {
        startActivity(new Intent(this, LocationActivity.class));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            startService(new Intent(this, BackgroundLocationService.class));
        } else {
            stopService(new Intent(this, BackgroundLocationService.class));
        }
    }

}