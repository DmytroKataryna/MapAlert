package com.example.dmytro.mapalert.activities.views;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
    private Activity activity;


    public RecyclerViewAdapter(Activity activity, List<CursorLocation> items) {
        this.activity = activity;
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
        viewHolder.layoutListener.setLocation(items.get(i));

        RecyclerViewActionAdapterForListActivity adapter = new RecyclerViewActionAdapterForListActivity(activity, locationItem.getActions(), items.get(i).getId(), locationItem, viewHolder.itemLayout);
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


    class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout itemLayout;
        private ItemLayoutListener layoutListener;
        private TextView title;
        private ListView listView;
        private ImageView photo;


        public ViewHolder(View itemView) {
            super(itemView);

            itemLayout = (LinearLayout) itemView.findViewById(R.id.itemLayout);
            layoutListener = new ItemLayoutListener();
            itemLayout.setOnClickListener(layoutListener);

            title = (TextView) itemView.findViewById(R.id.titleItemTextView);
            listView = (ListView) itemView.findViewById(R.id.listLocationActionListView);
            photo = (ImageView) itemView.findViewById(R.id.locPhotoView);
        }
    }

    private class ItemLayoutListener implements View.OnClickListener {

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


    //convert text (delete spaces and line separators)
    public String trimText(String text) {
        String[] stringArray = text.trim().split("\\r?\\n");
        StringBuilder builder = new StringBuilder();

        for (String aStringArray : stringArray) {
            builder.append(aStringArray).append(" ");
        }
        return builder.toString();
    }

    //as I set List View inside Recycler View , there appears problems with auto increasing List View height
    //so I use this method to manually resize List View
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
