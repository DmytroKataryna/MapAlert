package com.example.dmytro.mapalert.activities.views;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;

import java.util.TreeSet;

/////----------------------------------Repeat Dialog -------------------------------------
public class RepeatDialog {

    private Context context;
    private TextView mRepeatTextView;

    //dialog items
    private TreeSet<Integer> selectedItems;
    private boolean checkedDialogItems[];

    public RepeatDialog(Context context, TextView repeatTextView, TreeSet<Integer> selectedItems, boolean checkedDialogItems[]) {
        this.context = context;
        this.mRepeatTextView = repeatTextView;
        this.selectedItems = selectedItems;
        this.checkedDialogItems = checkedDialogItems;
    }

    public void createRepeatDialog() {
        final CharSequence[] items =
                {"Every Monday", "Every Tuesday ", "Every Wednesday ", "Every Thursday", "Every Friday", "Every Saturday", "Every Sunday"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
}
