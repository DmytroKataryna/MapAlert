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
import android.widget.TextView;

import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.views.RecyclerViewAdapter;
import com.example.dmytro.mapalert.geofencing.BackgroundLocationService;
import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.utils.LocationDataSource;
import com.example.dmytro.mapalert.utils.PreferencesUtils;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.util.List;


public class ListActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener {

    private PreferencesUtils utils;
    private LocationDataSource dataSource;
    private List<CursorLocation> locationItems;
    private RecyclerView recyclerView;

    private FloatingActionButton mAddButton;

    //menu items
    private SwitchCompat mTrackSwitcher;
    private TextView mTrackTextView;

    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_location);

        utils = PreferencesUtils.get(getApplicationContext());
        dataSource = LocationDataSource.get(getApplicationContext());
        dataSource.open();

        //get List Location Data from DB
        try {
            locationItems = dataSource.getAllLocationItems();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        mAddButton = (FloatingActionButton) findViewById(R.id.fab_add_location);
        recyclerView = (RecyclerView) findViewById(R.id.locationRecycleList);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, locationItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        mAddButton.attachToRecyclerView(recyclerView);

        if (!utils.isServiceAlive())  //start geoLocation Service
            startService(new Intent(this, BackgroundLocationService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        started = false;
        if (mTrackSwitcher != null) {  //if service is alive set menu switcher checked to true
            mTrackSwitcher.setSelected(utils.isServiceAlive());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_location_menu, menu);
        //get my custom menu item layout
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
            if (item.getItemId() == R.id.track) {
                View view = MenuItemCompat.getActionView(item).findViewById(R.id.textViewForActionBar);
                if (view != null) {
                    mTrackTextView = (TextView) view;
                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    //floating button listener
    public void addButtonListener(View view) {
        //to prevent double click / set such construction
        if (!started) {
            startActivity(new Intent(this, LocationActivity.class));
        }
        started = true;
    }

    //menu switcher listener
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            startService(new Intent(this, BackgroundLocationService.class));
            mTrackTextView.setTextColor(getResources().getColor(R.color.positive_button_red));
            utils.setServiceState(true);
        } else {
            stopService(new Intent(this, BackgroundLocationService.class));
            mTrackTextView.setTextColor(getResources().getColor(R.color.grey_50));
            utils.setServiceState(false);
        }
    }
}