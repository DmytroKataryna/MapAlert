package com.example.dmytro.mapalert.activities.views;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.pojo.LocationItemAction;

import java.util.List;


public class RecyclerViewActionAdapter extends RecyclerView.Adapter<RecyclerViewActionAdapter.ViewHolder> {


    private RecyclerView recyclerView;
    private List<LocationItemAction> items;

    public RecyclerViewActionAdapter(List<LocationItemAction> items, RecyclerView recyclerView) {
        this.items = items;
        this.recyclerView = recyclerView;
    }

    @Override
    public RecyclerViewActionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_action_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LocationItemAction locationItemAction = items.get(position);

        holder.action.setText(locationItemAction.getActionText());

        holder.actionCheckBox.setChecked(locationItemAction.isDone());
        holder.actionCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationItemAction.isDone()) {
                    locationItemAction.setDone(false);
                    holder.actionCheckBox.setChecked(false);
                } else {
                    locationItemAction.setDone(true);
                    holder.actionCheckBox.setChecked(true);
                }

                notifyDataSetChanged();
            }
        });
        holder.deleteButtonListener.setAction(locationItemAction);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView action;
        private CheckBox actionCheckBox;
        private ImageButton deleteButton;
        private DeleteButtonListener deleteButtonListener;

        public ViewHolder(View itemView) {
            super(itemView);

            action = (TextView) itemView.findViewById(R.id.actionTextView);

            actionCheckBox = (CheckBox) itemView.findViewById(R.id.actionCheckBox);

            deleteButton = (ImageButton) itemView.findViewById(R.id.deleteActionImageButton);
            deleteButtonListener = new DeleteButtonListener();
            deleteButton.setOnClickListener(deleteButtonListener);
        }
    }

    private class DeleteButtonListener implements View.OnClickListener {
        private LocationItemAction action;

        @Override
        public void onClick(View v) {
            delete(action);
        }

        public void setAction(LocationItemAction action) {
            this.action = action;
        }
    }

    private void delete(LocationItemAction action) {
        int position = items.indexOf(action);
        items.remove(position);
        recyclerView.getLayoutParams().height -= 100;
        notifyItemRemoved(position);
    }
}