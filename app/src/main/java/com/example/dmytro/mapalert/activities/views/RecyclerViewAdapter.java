package com.example.dmytro.mapalert.activities.views;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.LocationActivity;
import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationItem;
import com.example.dmytro.mapalert.utils.LocationDataSource;
import com.example.dmytro.mapalert.utils.PreferencesUtils;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    public static final String ITEM_KEY = "LocationItemKey";
    public static final String ITEM_EDIT_MODE = "LocationEditMode";

    private List<CursorLocation> items;
    private LocationDataSource dataSource;
    private Activity activity;
    private FloatingActionButton mAddButton;
    private PreferencesUtils utils;

    public RecyclerViewAdapter(Activity activity, List<CursorLocation> items, FloatingActionButton mAddButton) {
        this.activity = activity;
        utils = PreferencesUtils.get(activity);
        dataSource = LocationDataSource.get(activity);
        dataSource.open();
        this.items = items;
        this.mAddButton = mAddButton;
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
        viewHolder.editButtonListener.setLocation(items.get(i));
        viewHolder.deleteButtonListener.setLocation(items.get(i));
        //viewHolder.layoutListener.setLocation(items.get(i));

//        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
//        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
//        viewHolder.recyclerView.setAdapter(new RecyclerViewActionAdapterForListActivity(activity, locationItem.getActions()));
//        viewHolder.recyclerView.setLayoutManager(layoutManager);
//        viewHolder.recyclerView.setItemAnimator(itemAnimator);
        RecyclerViewActionAdapterForListActivity adapter = new RecyclerViewActionAdapterForListActivity(activity, locationItem.getActions(), items.get(i).getId(), locationItem);
        viewHolder.listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(viewHolder.listView);

        Picasso.with(activity).load(new File(locationItem.getImagePath()))
                .placeholder(R.drawable.ic_image_camera)
                .into(viewHolder.photo);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void delete(CursorLocation item) {
        //delete location from DB and also image from internal storage
        dataSource.deleteLocation(item.getId(), item.getItem().getImagePath());

        int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);

        //after user delete location , add Button should be visible
        if (mAddButton.hasShadow()) mAddButton.show();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout itemLayout;
//        private ItemLayoutListener layoutListener;

        private TextView title;
        private ListView listView;
        private ImageView photo;

        private ImageButton deleteButton, editButton;
        private DeleteButtonListener deleteButtonListener;
        private EditButtonListener editButtonListener;

        public ViewHolder(View itemView) {
            super(itemView);

            itemLayout = (LinearLayout) itemView.findViewById(R.id.itemLayout);
//            layoutListener = new ItemLayoutListener();
//            itemLayout.setOnClickListener(layoutListener);

            title = (TextView) itemView.findViewById(R.id.titleItemTextView);
            listView = (ListView) itemView.findViewById(R.id.listLocationActionListView);
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

            new MaterialDialog.Builder(activity)
                    .title("Delete Location")
                    .content("Are you sure you want to permanently delete this location?")
                    .positiveText("Delete")
                    .negativeText("Cancel")
                    .positiveColorRes(R.color.positive_button_red)
                    .negativeColorRes(R.color.negative_button_blue)
                    .titleColorRes(R.color.dialog_blue)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            delete(item);
                        }
                    })
                    .show();
        }

        public void setLocation(CursorLocation locationItem) {
            this.item = locationItem;
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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.MATCH_PARENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
