package com.example.dmytro.mapalert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.dmytro.mapalert.DBUtils.LocationDataSource;
import com.example.dmytro.mapalert.R;

import java.io.IOException;

public class ListActivity extends ActionBarActivity {

    private LocationDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_location);

        dataSource = LocationDataSource.get(getApplicationContext());
        dataSource.open();
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

    public void getRows(View v) throws IOException, ClassNotFoundException {
        // new DBHelper().onUpgrade();
        Toast.makeText(this, " TXT " + dataSource.getAllLocationItems().size(), Toast.LENGTH_SHORT).show();
    }
}
