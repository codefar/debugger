package com.su.debugger.ui.test.mock;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.su.debugger.db.MockContentProvider;
import com.su.debugger.entity.MockResponseEntity;
import com.su.debugger.utils.IOUtil;

public class EntityHelper {

    @Nullable
    public static MockResponseEntity getById(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(id)), null, null, null, "url, auto ASC");
            if (cursor != null) {
                cursor.moveToFirst();

                MockResponseEntity entity = new MockResponseEntity();
                final int entityId = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_ID));
                String md5 = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_MD5));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_URL));
                String method = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_METHOD));
                String contentType = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_CONTENT_TYPE));
                String headers = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_REQUEST_HEADERS));
                String parameters = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_REQUEST_BODY));
                String responseHeaders = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_RESPONSE_HEADERS));
                String response = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_RESPONSE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_DESCRIPTION));
                int inUse = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_IN_USE));
                entity.setId(entityId);
                entity.setMd5(md5);
                entity.setUrl(url);
                entity.setMethod(method);
                entity.setContentType(contentType);
                entity.setRequestHeaders(headers);
                entity.setRequestBody(parameters);
                entity.setResponseHeaders(responseHeaders);
                entity.setResponse(response);
                entity.setDescription(description);
                entity.setInUse(inUse == 1);
                return entity;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeQuietly(cursor);
        }
        return null;
    }

    public static int update(Context context, long id, ContentValues values) {
        ContentResolver resolver = context.getContentResolver();
        return resolver.update(ContentUris.withAppendedId(MockContentProvider.CONTENT_URI, id), values, null, null);
    }
}
