package com.example.dmytro.mapalert.DBUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.dmytro.mapalert.pojo.LocationItem;

import java.io.IOException;

public class LocationDataSource {

    private static LocationDataSource sDataSource;
    public DBHelper dbHelper;
    public SQLiteDatabase sdb;

    public static final String[] COLUMNS = {DBHelper.ID_COLUMN, DBHelper.LOCATION_COLUMN};

    public LocationDataSource(Context context) {
        dbHelper = new DBHelper(context);
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

    public LocationItem createLocation(LocationItem locationItem) throws IOException, ClassNotFoundException {

        ContentValues values = new ContentValues();
        values.put(DBHelper.LOCATION_COLUMN, Serializer.serialize(locationItem));

        long insertId = sdb.insert(DBHelper.DATABASE_TABLE, null,
                values);
        Cursor cursor = sdb.query(DBHelper.DATABASE_TABLE,
                COLUMNS, DBHelper.ID_COLUMN + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        LocationItem loc = cursorToLocation(cursor);
        cursor.close();
        return loc;
    }

    private LocationItem cursorToLocation(Cursor cursor) throws IOException, ClassNotFoundException {
        return (LocationItem) Serializer.deserialize(cursor.getBlob(1));
    }
}
