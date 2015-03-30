package com.example.dmytro.mapalert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.dmytro.mapalert.DBUtils.LocationDataSource;
import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.views.RecyclerViewAdapter;
import com.example.dmytro.mapalert.pojo.CursorLocation;

import java.io.IOException;
import java.util.List;

//here i will place list of locations
//temporarily, activity have just button that displays the number of  that are saved in DB
public class ListActivity extends ActionBarActivity {

    private LocationDataSource dataSource;
    private List<CursorLocation> locationItems;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_location);

        dataSource = LocationDataSource.get(getApplicationContext());
        dataSource.open();

        //get Data from DB
        try {
            locationItems = dataSource.getAllLocationItems();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        recyclerView = (RecyclerView) findViewById(R.id.locationRecycleList);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, locationItems);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_location_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                startActivity(new Intent(this, LocationActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
