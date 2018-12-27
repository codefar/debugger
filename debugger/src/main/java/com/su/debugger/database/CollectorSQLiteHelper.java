package com.su.debugger.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CollectorSQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = CollectorSQLiteHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "debugger-responses.db";
    private static final int DATABASE_VERSION = 8;

    CollectorSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableSql = "CREATE TABLE IF NOT EXISTS response (\n"
                + "_id INTEGER primary key AUTOINCREMENT,\n"
                + "md5 text NOT NULL,\n"
                + "url text NOT NULL,\n"
                + "host text NOT NULL,\n"
                + "path text,\n"
                + "method text NOT NULL,\n"
                + "contentType text NOT NULL,\n"
                + "requestHeaders text,\n"
                + "requestBody text,\n"
                + "responseHeaders text,\n"
                + "response text,\n"
                + "description text,\n"
                + "pages text,\n"
                + "test integer,\n"
                + "auto integer,\n"
                + "inUse integer);\n";
        String indexSql = "CREATE UNIQUE INDEX locate_request" +
                " ON response(url, method, contentType, requestHeaders, requestBody, auto);";
        db.execSQL(tableSql);
        db.execSQL(indexSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
