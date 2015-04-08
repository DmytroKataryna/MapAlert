package com.example.dmytro.mapalert.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.dmytro.mapalert.geofencing.BackgroundTimeService;
import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationDataSource {

    private static LocationDataSource sDataSource;

    private Context context;
    public DBHelper dbHelper;
    public SQLiteDatabase sdb;
    private PreferencesUtils utils;

    public static final String[] COLUMNS = {DBHelper.ID_COLUMN, DBHelper.LOCATION_COLUMN, DBHelper.LOCATION_INSIDE_COLUMN};

    public LocationDataSource(Context context) {
        dbHelper = new DBHelper(context);
        utils = PreferencesUtils.get(context);
        this.context = context;
    }

    public static LocationDataSource get(Context c) {
        if (sDataSource == null) {
            sDataSource = new LocationDataSource(c.getApplicationContext());
        }
        return sDataSource;
    }

    public void open() throws SQLException {
        sdb = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public CursorLocation createLocation(LocationItem locationItem) throws IOException, ClassNotFoundException {

        ContentValues values = new ContentValues();
        values.put(DBHelper.LOCATION_COLUMN, Serializer.serialize(locationItem));

        long insertId = sdb.insert(DBHelper.DATABASE_TABLE, null,
                values);
        Cursor cursor = sdb.query(DBHelper.DATABASE_TABLE,
                COLUMNS, DBHelper.ID_COLUMN + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        CursorLocation item = cursorToLocation(cursor);
        utils.setServiceDataChanged(true);
        context.startService(new Intent(context, BackgroundTimeService.class).putExtra(BackgroundTimeService.LOCATION_DATA, item));
        cursor.close();
        return item;
    }

    public void updateLocation(int id, LocationItem locationItem) throws IOException {

        ContentValues values = new ContentValues();
        values.put(DBHelper.LOCATION_COLUMN, Serializer.serialize(locationItem));

        sdb.update(DBHelper.DATABASE_TABLE, values, DBHelper.ID_COLUMN + "=" + id, null);
        utils.setServiceDataChanged(true);
        context.startService(new Intent(context, BackgroundTimeService.class).putExtra(BackgroundTimeService.LOCATION_DATA, new CursorLocation(id, locationItem, 0)));
    }

    public void updateInsideStatus(int id, Integer insideStatus) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.LOCATION_INSIDE_COLUMN, insideStatus);

        sdb.update(DBHelper.DATABASE_TABLE, values, DBHelper.ID_COLUMN + "=" + id, null);
    }

    //delete location from DB and also image from internal storage
    public void deleteLocation(Integer id, String imagePath) {
        new File(imagePath).delete();
        sdb.delete(DBHelper.DATABASE_TABLE, DBHelper.ID_COLUMN + " = ?", new String[]{String.valueOf(id)});
        utils.setServiceDataChanged(true);
    }

    public List<CursorLocation> getAllLocationItems() throws IOException, ClassNotFoundException {
        List<CursorLocation> allLoc = new ArrayList<>();

        Cursor cursor = sdb.query(DBHelper.DATABASE_TABLE,
                COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            CursorLocation item = cursorToLocation(cursor);
            allLoc.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return allLoc;
    }

    private CursorLocation cursorToLocation(Cursor cursor) throws IOException, ClassNotFoundException {
        return new CursorLocation(cursor.getInt(0), (LocationItem) Serializer.deserialize(cursor.getBlob(1)), cursor.getInt(2));
    }
}
