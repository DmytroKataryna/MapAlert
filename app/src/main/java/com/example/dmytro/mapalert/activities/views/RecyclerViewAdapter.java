package com.example.dmytro.mapalert.activities.views;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dmytro.mapalert.DBUtils.LocationDataSource;
import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.LocationActivity;
import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationItem;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    public static final String ITEM_KEY = "LocationItemKey";
    public static final String ITEM_EDIT_MODE = "LocationEditMode";

    private List<CursorLocation> items;
    private LocationDataSource dataSource;
    private Activity activity;

    public RecyclerViewAdapter(Activity activity, List<CursorLocation> items) {
        this.activity = activity;
        dataSource = LocationDataSource.get(activity);
        dataSource.open();
        this.items = items;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        LocationItem locationItem = items.get(i).getItem();

        viewHolder.title.setText(trimText(locationItem.getTitle()));
        viewHolder.description.setText(trimText(locationItem.getDescription()));
        viewHolder.editButtonListener.setLocation(items.get(i));
        viewHolder.deleteButtonListener.setLocation(items.get(i));
        viewHolder.layoutListener.setLocation(items.get(i));

        Picasso.with(activity).load(new File(locationItem.getImagePath()))
                .placeholder(R.mipmap.ic_action_house)
                .into(viewHolder.photo);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void delete(CursorLocation item) {
        //delete from DB
        dataSource.deleteLocation(item.getId());

        int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout itemLayout;
        private ItemLayoutListener layoutListener;

        private TextView title, description;
        private ImageView photo;

        private ImageButton deleteButton, editButton;
        private DeleteButtonListener deleteButtonListener;
        private EditButtonListener editButtonListener;

        public ViewHolder(View itemView) {
            super(itemView);

            itemLayout = (LinearLayout) itemView.findViewById(R.id.itemLayout);
            layoutListener = new ItemLayoutListener();
            itemLayout.setOnClickListener(layoutListener);

            title = (TextView) itemView.findViewById(R.id.titleItemTextView);
            description = (TextView) itemView.findViewById(R.id.descItemTextView);
            photo = (ImageView) itemView.findViewById(R.id.locPhotoView);
            deleteButton = (ImageButton) itemView.findViewById(R.id.deleteItemButton);
            deleteButtonListener = new DeleteButtonListener();
            deleteButton.setOnClickListener(deleteButtonListener);

            editButton = (ImageButton) itemView.findViewById(R.id.editItemButton);
            editButtonListener = new EditButtonListener();
            editButton.setOnClickListener(editButtonListener);
        }
    }

    private class EditButtonListener implements View.OnClickListener {
        private CursorLocation item;

        @Override
        public void onClick(View v) {
            //start Detail activity with Item ID and Boolean field (Edit Mode)
            activity.startActivity(new Intent(activity, LocationActivity.class)
                    .putExtra(ITEM_KEY, item).putExtra(ITEM_EDIT_MODE, true));
        }

        public void setLocation(CursorLocation locationItem) {
            this.item = locationItem;
        }
    }

    private class DeleteButtonListener implements View.OnClickListener {
        private CursorLocation item;

        @Override
        public void onClick(View v) {
            delete(item);
        }

        public void setLocation(CursorLocation locationItem) {
            this.item = locationItem;
        }
    }


    private class ItemLayoutListener implements View.OnClickListener {
        private CursorLocation location;

        @Override
        public void onClick(View v) {
            //start another unchanged screen activity
            //zапускатu detail активи з бандлом в якому знаходіться наш обєкт
        }

        public void setLocation(CursorLocation location) {
            this.location = location;
        }
    }

    //convert text (delete spaces and line separators)
    public String trimText(String text) {
        String[] stringArray = text.trim().split("\\r?\\n");
        StringBuilder builder = new StringBuilder();

        for (String aStringArray : stringArray) {
            builder.append(aStringArray).append(" ");
        }
        return builder.toString();
    }
}
