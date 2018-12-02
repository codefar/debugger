package com.su.debugger.ui.test.mock;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.su.debugger.AppHelper;
import com.su.debugger.DebuggerSupplier;
import com.su.debugger.db.MockContentProvider;
import com.su.debugger.entity.MockResponseEntity;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParseHelper {
    private static final String TAG = ParseHelper.class.getSimpleName();

    static String makeQueryContent(Uri uri, @NonNull String separator) {
        Set<String> set = uri.getQueryParameterNames();
        HashMap<String, Object> map = new HashMap<>();
        for (String key : set) {
            map.put(key, uri.getQueryParameter(key));
        }
        List<String> queryList = new ArrayList<>(set);
        if (queryList.isEmpty()) {
            return "";
        }
        StringBuilder queryContent = new StringBuilder();
        Collections.sort(queryList);
        for (String key : queryList) {
            queryContent.append(key);
            queryContent.append(": ");
            queryContent.append(map.get(key));
            queryContent.append(separator);
        }
        return queryContent.deleteCharAt(queryContent.length() - separator.length()).toString();
    }

    static List<MockDetailActivity.Item> makeQueryList(Uri uri, String parent) {
        List<MockDetailActivity.Item> itemList = new ArrayList<>();
        Set<String> set = uri.getQueryParameterNames();
        HashMap<String, Object> map = new HashMap<>();
        for (String key : set) {
            map.put(key, uri.getQueryParameter(key));
        }
        List<String> queryList = new ArrayList<>(set);
        if (queryList.isEmpty()) {
            return itemList;
        }
        Collections.sort(queryList);
        for (String key : queryList) {
            MockDetailActivity.Item item = new MockDetailActivity.Item(map.get(key).toString(), key, parent, false);
            itemList.add(item);
        }
        return itemList;
    }

    static String makeParametersContent(String parameters, @NonNull String separator) {
        if (TextUtils.isEmpty(parameters)) {
            return parameters;
        }
        StringBuilder parametersContent = new StringBuilder();
        Map<String, Object> map = JSON.parseObject(parameters);
        List<String> parameterList = new ArrayList<>(map.keySet());
        if (parameterList.isEmpty()) {
            return parameters;
        }
        Collections.sort(parameterList);
        for (String key : parameterList) {
            parametersContent.append(key);
            parametersContent.append(": ");
            parametersContent.append(map.get(key));
            parametersContent.append(separator);
        }
        return parametersContent.deleteCharAt(parametersContent.length() - separator.length()).toString();
    }

    static List<MockDetailActivity.Item> makeParametersList(String parameters, String parent) {
        List<MockDetailActivity.Item> itemList = new ArrayList<>();
        if (TextUtils.isEmpty(parameters)) {
            return itemList;
        }
        Map<String, Object> map = JSON.parseObject(parameters);
        List<String> parameterList = new ArrayList<>(map.keySet());
        if (parameterList.isEmpty()) {
            return itemList;
        }
        Collections.sort(parameterList);
        for (String key : parameterList) {
            MockDetailActivity.Item item = new MockDetailActivity.Item(map.get(key).toString(), key, parent, false);
            itemList.add(item);
        }
        return itemList;
    }

    //更新description，更新数据库
    static int updateDescription(@NonNull Context context, @NonNull MockResponseEntity entity, String description) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_DESCRIPTION, description);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setDescription(description);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新content type，更新数据库
    static int updateAuto(@NonNull Context context, @NonNull MockResponseEntity entity, boolean auto) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_AUTO, auto);
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            entity.setAuto(rowsUpdated > 0 == auto);
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新request headers中一个key，更新数据库
    static int updateRequestHeader(@NonNull Context context, @NonNull MockResponseEntity entity, String key, String value, String action) {
        Map<String, Object> map = JSON.parseObject(entity.getRequestHeaders());
        if (map == null) {
            map = new HashMap<>();
        }
        if (TextUtils.equals(action, "remove")) {
            map.remove(key);
        } else if (TextUtils.equals(action, "add")) {
            map.put(key, "");
        } else if (TextUtils.equals(action, "update")) {
            map.put(key, value);
        } else {
            throw new IllegalArgumentException("wrong action!");
        }

        String newHeaders;
        if (map.isEmpty()) {
            newHeaders = "";
        } else {
            newHeaders = JSON.toJSONString(map, true);
        }

        String md5 = makeMd5(entity.getUrl(), entity.getMethod(), entity.getContentType(), newHeaders, entity.getRequestBody(), entity.isAuto());
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_MD5, md5);
        values.put(MockResponseEntity.COLUMN_REQUEST_HEADERS, newHeaders);
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setRequestHeaders(newHeaders);
                entity.setMd5(md5);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新method，更新数据库
    static int updateMethod(@NonNull Context context, @NonNull MockResponseEntity entity, String value) {
        String md5 = makeMd5(entity.getUrl(), value, entity.getContentType(), entity.getRequestHeaders(), entity.getRequestBody(), entity.isAuto());
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_MD5, md5);
        values.put(MockResponseEntity.COLUMN_METHOD, value);
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setMethod(value);
                entity.setMd5(md5);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新content type，更新数据库
    static int updateContentType(@NonNull Context context, @NonNull MockResponseEntity entity, String value) {
        String md5 = makeMd5(entity.getUrl(), entity.getMethod(), value, entity.getRequestHeaders(), entity.getRequestBody(), entity.isAuto());
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_MD5, md5);
        values.put(MockResponseEntity.COLUMN_CONTENT_TYPE, value);
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setMethod(value);
                entity.setMd5(md5);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新query中一个key，更新数据库
    static int updateQueryKey(@NonNull Context context, @NonNull MockResponseEntity entity, String queryKey, String value, String action) {
        ContentResolver resolver = context.getContentResolver();
        String url = entity.getUrl();
        Uri uri = Uri.parse(url);
        //移除key
        Set<String> queryKeySet = new HashSet<>(uri.getQueryParameterNames());
        if (TextUtils.equals(action, "remove")) {
            queryKeySet.remove(queryKey);
        } else if (TextUtils.equals(action, "add")) {
            queryKeySet.add(queryKey);
        }
        //调整query字段顺序
        List<String> queryKeyList = new ArrayList<>(queryKeySet);
        Collections.sort(queryKeyList);
        Uri.Builder builder = uri.buildUpon();
        builder.clearQuery();
        for (String key : queryKeyList) {
            if (TextUtils.equals(key, queryKey)) {
                builder.appendQueryParameter(key, value);
            } else {
                builder.appendQueryParameter(key, uri.getQueryParameter(key));
            }
        }
        String newUrl = builder.build().toString();
        //使用新url生成md5
        String md5 = makeMd5(newUrl, entity.getMethod(), entity.getContentType(), entity.getRequestHeaders(), entity.getRequestBody(), entity.isAuto());

        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_MD5, md5);
        values.put(MockResponseEntity.COLUMN_URL, newUrl);
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setUrl(newUrl);
                entity.setMd5(md5);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新parameters中一个key，更新数据库
    static int updateParameter(@NonNull Context context, @NonNull MockResponseEntity entity, String parameter, String value, String action) {
        JSONObject jsonObject = JSON.parseObject(entity.getRequestBody());
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        if (TextUtils.equals(action, "remove")) {
            jsonObject.remove(parameter);
        } else if (TextUtils.equals(action, "add")) {
            jsonObject.put(parameter, "");
        } else if (TextUtils.equals(action, "update")) {
            String contentType = entity.getContentType();
            if (!TextUtils.isEmpty(contentType) && contentType.contains("application/json")) {
                JSONObject jsonValue = JSON.parseObject(value);
                jsonObject.put(parameter, jsonValue);
            } else {
                jsonObject.put(parameter, value);
            }
        } else {
            throw new IllegalArgumentException("wrong action!");
        }

        String newParameters;
        if (jsonObject.isEmpty()) {
            newParameters = "";
        } else {
            newParameters = JSON.toJSONString(jsonObject, true);
        }

        String md5 = makeMd5(entity.getUrl(), entity.getMethod(), entity.getContentType(), entity.getRequestHeaders(), newParameters, entity.isAuto());
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_MD5, md5);
        values.put(MockResponseEntity.COLUMN_REQUEST_BODY, newParameters);
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setRequestBody(newParameters);
                entity.setMd5(md5);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    //更新response headers中一个key，更新数据库
    static int updateResponseHeader(@NonNull Context context, @NonNull MockResponseEntity entity, String key, String value, String action) {
        JSONObject jsonObject = JSON.parseObject(entity.getResponseHeaders());
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        if (TextUtils.equals(action, "remove")) {
            jsonObject.remove(key);
        } else if (TextUtils.equals(action, "add")) {
            jsonObject.put(key, "");
        } else if (TextUtils.equals(action, "update")) {
            jsonObject.put(key, value);
        } else {
            throw new IllegalArgumentException("wrong action!");
        }

        String newHeaders;
        if (jsonObject.isEmpty()) {
            newHeaders = "";
        } else {
            newHeaders = JSON.toJSONString(jsonObject, true);
        }

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_RESPONSE_HEADERS, newHeaders);
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setResponseHeaders(newHeaders);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    static int updateResponse(@NonNull Context context, @NonNull MockResponseEntity entity, String value) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_RESPONSE, value);
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        values.put(MockResponseEntity.COLUMN_IN_USE, true);
        int rowsUpdated = 0;
        try {
            rowsUpdated = resolver.update(Uri.withAppendedPath(MockContentProvider.CONTENT_URI, String.valueOf(entity.getId())), values, null, null);
            if (rowsUpdated > 0) {
                entity.setResponse(value);
            }
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, "已存在同样条件的request", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        }
        return rowsUpdated;
    }

    public static String makeMd5(String url, String method, String contentType, String requestHeaders, String requestBody, boolean auto) {
        String key = MockResponseEntity.COLUMN_URL + "=" + url
                + "&" + MockResponseEntity.COLUMN_METHOD + "=" + method
                + "&" + MockResponseEntity.COLUMN_CONTENT_TYPE + "=" + contentType
                + "&" + MockResponseEntity.COLUMN_REQUEST_HEADERS + "=" + AppHelper.encodeString(requestHeaders)
                + "&" + MockResponseEntity.COLUMN_REQUEST_BODY + "=" + AppHelper.encodeString(requestBody)
                + "&" + MockResponseEntity.COLUMN_AUTO + "=" + auto;
        String md5Key = Md5Util.md5Hex(key);
        return md5Key;
    }

    public static String makeMd5(@NonNull MockResponseEntity entity) {
        return makeMd5(entity.getUrl(), entity.getMethod(), entity.getContentType(), entity.getRequestHeaders(), entity.getRequestBody(), entity.isAuto());
    }

    //是否为单一元素
    static boolean singleElement(@NonNull String type) {
        switch (type) {
            case MockResponseEntity.TYPE_REQUEST_QUERY:
            case MockResponseEntity.TYPE_REQUEST_BODY:
            case MockResponseEntity.TYPE_REQUEST_HEADERS:
            case MockResponseEntity.TYPE_RESPONSE_HEADERS:
                return false;
            default:
                return true;
        }
    }

    static boolean copy(@NonNull Context context, @NonNull MockResponseEntity entity) {
        String md5 = makeMd5(entity);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_MD5, md5);
        values.put(MockResponseEntity.COLUMN_URL, entity.getUrl());
        values.put(MockResponseEntity.COLUMN_HOST, entity.getHost());
        values.put(MockResponseEntity.COLUMN_METHOD, entity.getMethod());
        values.put(MockResponseEntity.COLUMN_CONTENT_TYPE, entity.getContentType());
        values.put(MockResponseEntity.COLUMN_REQUEST_HEADERS, entity.getRequestHeaders());
        values.put(MockResponseEntity.COLUMN_REQUEST_BODY, entity.getRequestBody());
        values.put(MockResponseEntity.COLUMN_AUTO, false);
        values.put(MockResponseEntity.COLUMN_IN_USE, entity.isInUse());
        values.put(MockResponseEntity.COLUMN_DESCRIPTION, entity.getDescription());
        Uri uri = resolver.insert(MockContentProvider.CONTENT_URI, values);
        return uri != null;
    }

    public static boolean recorded(@NonNull MockResponseEntity entity) {
        ContentResolver resolver = GeneralInfoHelper.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MockResponseEntity.COLUMN_URL, entity.getUrl());
        values.put(MockResponseEntity.COLUMN_METHOD, entity.getMethod());
        values.put(MockResponseEntity.COLUMN_CONTENT_TYPE, entity.getContentType());
        values.put(MockResponseEntity.COLUMN_REQUEST_HEADERS, entity.getRequestHeaders());
        values.put(MockResponseEntity.COLUMN_REQUEST_BODY, entity.getRequestBody());
        values.put(MockResponseEntity.COLUMN_AUTO, entity.isAuto());
        return !MockContentProvider.isUnique(resolver, values);
    }

//    /storage/emulated/0/Android/data/packageName/files/mock
//    /storage/emulated/0/mock
    public static void process(Activity activity) {
        File mockCacheDir = activity.getExternalFilesDir("mock");
        if (mockCacheDir == null) {
            return;
        }
        if (!mockCacheDir.exists()) {
            mockCacheDir.mkdirs();
        }
        Log.d(TAG, "file: " + mockCacheDir.getAbsolutePath());
        processAllFiles(mockCacheDir.getAbsolutePath());
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File mockDir = new File(externalStorageDirectory, "mock");
        if (!mockDir.exists()) {
            mockDir.mkdirs();
        }
        Log.d(TAG, "mockDir: " + mockDir.getAbsolutePath());
        processAllFiles(mockDir.getAbsolutePath());
    }

    public static void processAllFiles(@NonNull String filepath) {
        File file = new File(filepath);
        IOUtil.processAllFiles(file, f -> {
            if (!f.getAbsolutePath().endsWith(".json")) {
                Log.i(TAG, "ignore file: " + f.getAbsolutePath());
                return;
            }
            String responsesString = IOUtil.readFile(f);
            List<MockResponseEntity> list = new ArrayList<>();
            try {
                list = JSON.parseArray(responsesString, MockResponseEntity.class);
            } catch (JSONException e) {
                MockResponseEntity entity;
                try {
                    entity = JSON.parseObject(responsesString, MockResponseEntity.class);
                    list.add(entity);
                } catch (JSONException jsonException) {
                    Log.e(TAG, "filepath: " + filepath, e);
                    return;
                }
            }
            Log.d(TAG, "filepath: " + filepath + " size: " + list.size());
            for (MockResponseEntity entity : list) {
                String url = entity.getUrl();
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                String path = uri.getPath();
                entity.setHost(host);
                entity.setPath(path);
                entity.setAuto(false);
                updateDbWithMockResponseEntity(entity);
            }
        });
        //update数据库
    }

    //收集sd卡数据时使用
    private static void updateDbWithMockResponseEntity(@NonNull MockResponseEntity entity) {
        String url = entity.getUrl();
        String method = entity.getMethod();
        String contentType = entity.getContentType();
        String requestHeaders = entity.getRequestHeaders();
        String requestBody = entity.getRequestBody();
        boolean auto = entity.isAuto();

        if (!TextUtils.isEmpty(requestBody)) {
            JSONObject jsonObject = JSON.parseObject(requestBody);
            ParseHelper.removeKeysFromJson(jsonObject);
            requestBody = JSON.toJSONString(jsonObject,
                                            SerializerFeature.DisableCircularReferenceDetect,
                                            SerializerFeature.PrettyFormat);
            entity.setRequestBody(requestBody);
        }

        synchronized (MockContentProvider.LOCK) {
            ContentResolver resolver = GeneralInfoHelper.getContext().getContentResolver();
            Cursor cursor = resolver.query(MockContentProvider.CONTENT_URI, null,
                    MockResponseEntity.COLUMN_URL + "=?" +
                            " AND " + MockResponseEntity.COLUMN_METHOD + "=?" +
                            " AND " + MockResponseEntity.COLUMN_CONTENT_TYPE + "=?" +
                            " AND " + MockResponseEntity.COLUMN_REQUEST_HEADERS + "=?" +
                            " AND " + MockResponseEntity.COLUMN_REQUEST_BODY + "=?" +
                            " AND " + MockResponseEntity.COLUMN_AUTO + "=?",
                            new String[]{url, method, contentType, requestHeaders, requestBody, auto ? "1" : "0"},
                    null);

            //如果已经收集过并且存在于数据库中，则更新
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_ID));
                cursor.close();
                ContentValues values = new ContentValues();
                values.put(MockResponseEntity.COLUMN_URL, url);
                values.put(MockResponseEntity.COLUMN_HOST, entity.getHost());
                values.put(MockResponseEntity.COLUMN_PATH, entity.getPath());
                values.put(MockResponseEntity.COLUMN_METHOD, method);
                values.put(MockResponseEntity.COLUMN_CONTENT_TYPE, contentType);
                values.put(MockResponseEntity.COLUMN_REQUEST_HEADERS, requestHeaders);
                values.put(MockResponseEntity.COLUMN_REQUEST_BODY, requestBody);
                values.put(MockResponseEntity.COLUMN_AUTO, auto);
                values.put(MockResponseEntity.COLUMN_RESPONSE_HEADERS, entity.getResponseHeaders());
                values.put(MockResponseEntity.COLUMN_RESPONSE, entity.getResponse());
                values.put(MockResponseEntity.COLUMN_DESCRIPTION, entity.getDescription());
                values.put(MockResponseEntity.COLUMN_IN_USE, entity.isInUse());
                resolver.update(ContentUris.withAppendedId(MockContentProvider.CONTENT_URI, id), values, null, null);
            } else {
                MockContentProvider.saveResponseEntity(entity);
            }
        }
    }

    /**
     * remove指定字段
     * 可以指定层级
     */
    public static void removeKeysFromJson(JSONObject jsonObject) {
        DebuggerSupplier debugger = DebuggerSupplier.getInstance();
        List<List<String>> excludeKeys = debugger.getRequestBodyExcludeKeys();
        for (List<String> key : excludeKeys) {
            JSONObject temp = jsonObject;
            int size = key.size();
            if (size == 1) {
                temp.put(key.get(0), null);
            } else {
                for (int i = 0; i < size - 1; i++) {
                    temp = temp.getJSONObject(key.get(i));
                }
                temp.put(key.get(size - 1), null);
            }
        }
        Log.d(TAG, "jsonObject: " + jsonObject);
    }
}
