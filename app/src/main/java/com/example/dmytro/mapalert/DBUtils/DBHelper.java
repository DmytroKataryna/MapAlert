package com.example.dmytro.mapalert.DBUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locationDataBase.db";
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_TABLE = "locationtb";
    public static final String ID_COLUMN = "_id";
    public static final String LOCATION_COLUMN = "location";

    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + ID_COLUMN
            + " integer primary key autoincrement, " + LOCATION_COLUMN
            + " BLOB not null);";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF IT EXIST " + DATABASE_TABLE);
        onCreate(db);
    }
}
