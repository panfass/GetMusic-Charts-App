package com.example.android.getmustop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "Getmustop";
    static final String TABLE_ISTORIKO = "istoriko";
    static final String ISID = "_id";
    static final String ISLISTNAME = "listname";
    static final String ISDATE = "date";

    private SQLiteDatabase db;

    public DBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String idb = " CREATE TABLE IF NOT EXISTS " + TABLE_ISTORIKO + "(" + ISID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ISLISTNAME + " TEXT," + ISDATE + " DATE " + ")";
        db.execSQL(idb);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_ISTORIKO);
        onCreate(db);
    }

    public void addIstoriko(Istoriko istoriko) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues istorikovalues = new ContentValues();
        istorikovalues.put(ISLISTNAME, istoriko.getList());
        istorikovalues.put(ISDATE, istoriko.getDate());

        db.insert(TABLE_ISTORIKO, null, istorikovalues);

    }

    public List<Istoriko> getAllIstoriko(){
        List<Istoriko> istorikoList = new ArrayList<>();
        String selectQuery = " SELECT * FROM " + TABLE_ISTORIKO;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                Istoriko istoriko = new Istoriko();
                istoriko.setId(cursor.getInt(0));
                istoriko.setList(cursor.getString(1));
                istoriko.setDate(cursor.getString(2));
                istorikoList.add(istoriko);

            }while(cursor.moveToNext());
        }
        return istorikoList;
    }

}
