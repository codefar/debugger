package com.su.debugger.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.su.debugger.entity.MockResponseEntity;
import com.su.debugger.ui.test.mock.MockUtil;
import com.su.debugger.utils.GeneralInfoHelper;

/**
 * Created by su on 2018/5/28.
 */

public class MockContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.su.debugger.MockContentProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    private static final String TABLE_RESPONSE = "response";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_RESPONSE);
    public static final Uri HOST_URI = Uri.withAppendedPath(CONTENT_URI, "host");
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int RESPONSE = 1;
    public static final int RESPONSES = 2;
    public static final int HOST = 3;
    public static final Object LOCK = new Object();
    private SQLiteDatabase sqLiteDatabase;

    static {
        uriMatcher.addURI(AUTHORITY, TABLE_RESPONSE, RESPONSES);
        uriMatcher.addURI(AUTHORITY, TABLE_RESPONSE + "/#", RESPONSE);
        uriMatcher.addURI(AUTHORITY, TABLE_RESPONSE + "/host", HOST);
    }

    @Override
    public boolean onCreate() {
        sqLiteDatabase = new CollectorSQLiteHelper(getContext()).getWritableDatabase();
        return false;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return TABLE_RESPONSE;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String  groupBy = null;
        queryBuilder.setTables(TABLE_RESPONSE);
        switch (uriMatcher.match(uri)) {
            case HOST:
                groupBy = "host";
                break;
            case RESPONSES:
                break;
            case RESPONSE:
                queryBuilder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            default:
                break;
        }
        Cursor cursor = queryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, groupBy, null, sortOrder, null);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = uriMatcher.match(uri);
        long id;
        switch (uriType) {
            case RESPONSES:
                id = sqLiteDatabase.insert(TABLE_RESPONSE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(TABLE_RESPONSE + "/" + id);
    }

    //只在自动中收集？
    //当自动变为手动时，手动可能冲突
    //使用url, method, contentType, requestHeaders, requestBody, auto 联合判断
    public static boolean isUnique(@NonNull ContentResolver resolver, @NonNull ContentValues values) {
        String url = values.getAsString("url");
        String method = values.getAsString("method");
        String contentType = values.getAsString("contentType");
        String requestHeaders = values.getAsString("requestHeaders");
        String requestBody = values.getAsString("requestBody");
        boolean auto = values.getAsBoolean("auto");

        Cursor cursor = resolver.query(MockContentProvider.CONTENT_URI, null,
                MockResponseEntity.COLUMN_URL + "=?" +
                        " AND " + MockResponseEntity.COLUMN_METHOD + "=?" +
                        " AND " + MockResponseEntity.COLUMN_CONTENT_TYPE + "=?" +
                        " AND " + MockResponseEntity.COLUMN_REQUEST_HEADERS + "=?" +
                        " AND " + MockResponseEntity.COLUMN_REQUEST_BODY + "=?" +
                        " AND " + MockResponseEntity.COLUMN_AUTO + "=?",
                new String[]{url, method, contentType, requestHeaders, requestBody, auto ? "1" : "0"},
                null);
        if (cursor != null) {
            boolean unique = cursor.getCount() == 0;
            cursor.close();
            return unique;
        }
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        int rowsDeleted;
        switch (uriType) {
            case RESPONSE:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqLiteDatabase.delete(TABLE_RESPONSE, "_id=" + id, null);
                } else {
                    rowsDeleted = sqLiteDatabase.delete(TABLE_RESPONSE, "_id=" + id + " and " + selection, selectionArgs);
                }
                break;
            case RESPONSES:
                rowsDeleted = sqLiteDatabase.delete(TABLE_RESPONSE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        int rowsUpdated;
        switch (uriType) {
            case RESPONSE:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqLiteDatabase.update(TABLE_RESPONSE, values, "_id=" + id, null);
                } else {
                    rowsUpdated = sqLiteDatabase.update(TABLE_RESPONSE, values, "_id=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    public static Uri saveResponseEntity(MockResponseEntity entity) {
        ContentValues values = new ContentValues();
        values.put("url", entity.getUrl());
        values.put("host", entity.getHost());
        values.put("path", entity.getPath());
        values.put("method", entity.getMethod());
        values.put("contentType", entity.getContentType());
        values.put("requestHeaders", entity.getRequestHeaders());
        values.put("requestBody", entity.getRequestBody());
        values.put("auto", entity.isAuto());
        values.put("responseHeaders", entity.getResponseHeaders());
        values.put("response", entity.getResponse());
        values.put("inUse", false);
        String md5 = MockUtil.makeMd5(entity.getUrl(), entity.getMethod(), entity.getContentType(), entity.getRequestHeaders(), entity.getRequestBody(), entity.isAuto());
        values.put("md5", md5);
        ContentResolver resolver = GeneralInfoHelper.getContext().getContentResolver();
        return resolver.insert(MockContentProvider.CONTENT_URI, values);
    }
}
