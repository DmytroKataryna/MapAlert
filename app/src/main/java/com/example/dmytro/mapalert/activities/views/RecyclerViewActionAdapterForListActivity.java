package com.example.dmytro.mapalert.activities.views;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.pojo.LocationItem;
import com.example.dmytro.mapalert.pojo.LocationItemAction;
import com.example.dmytro.mapalert.utils.LocationDataSource;

import java.io.IOException;
import java.util.List;

public class RecyclerViewActionAdapterForListActivity extends BaseAdapter {

    private Activity activity;
    private List<LocationItemAction> items;
    private Integer locationID;
    private LocationDataSource dataSource;
    private LocationItem locationItem;
    private LinearLayout itemLayout;


    public RecyclerViewActionAdapterForListActivity() {

    }

    public RecyclerViewActionAdapterForListActivity(Activity activity, List<LocationItemAction> items, Integer locationID, LocationItem locationItem, LinearLayout itemLayout) {
        this.activity = activity;
        this.items = items;
        this.locationID = locationID;
        this.locationItem = locationItem;
        this.itemLayout = itemLayout;
        dataSource = LocationDataSource.get(activity);
        dataSource.open();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(activity, R.layout.list_location_action_layout, null);
            holder = new ViewHolder();
            holder.action = (TextView) convertView.findViewById(R.id.listActivityActionTextView);
            holder.actionCheckBox = (CheckBox) convertView.findViewById(R.id.listActivityActionCheckBox);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final LocationItemAction action = items.get(position);

        holder.action.setText(action.getActionText());
        holder.action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemLayout.callOnClick();
            }
        });

        holder.actionCheckBox.setChecked(action.isDone());
        //check box listener / onCheckedChange Listener incorrectly , so i use OnClickListener
        holder.actionCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (action.isDone()) {
                        action.setDone(false);
                        holder.actionCheckBox.setChecked(false);
                        dataSource.updateLocation(locationID, locationItem);
                    } else {
                        action.setDone(true);
                        holder.actionCheckBox.setChecked(true);
                        dataSource.updateLocation(locationID, locationItem);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView action;
        CheckBox actionCheckBox;
    }
}
