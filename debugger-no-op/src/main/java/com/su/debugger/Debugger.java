package com.su.debugger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by su on 18-1-2.
 */

public class Debugger {

    private Debugger() {}

    public static void init(Application app) {}

    public static void init(Application app, @NonNull String className) {}

    @NonNull
    public static String getHost() {
        return "";
    }

    public static Object getMockInterceptor() {
        return null;
    }

    public static Object getDataCollectorInterceptor() {
        return null;
    }

    public static Object getHostInterceptor() {
        return null;
    }

    @Nullable
    public static Intent getDebuggerMainIntent() {
        return null;
    }

    @NonNull
    public static String getWebViewHost() {
        return "";
    }

    public static void startDataExportActivity(@NonNull Context context) {}

    public static void startPermissionsActivity(@NonNull Context context) {}

    public static void startActivitiesActivity(@NonNull Context context) {}

    public static void startMockDataActivity(@NonNull Context context) {}

    public static void startJsInterfacesActivity(@NonNull Context context) {}

    public static void startAppInfoActivity(@NonNull Context context) {}

    public static void startDatabaseListActivity(@NonNull Context context) {}

    public static void startHostsActivity(@NonNull Context context, int type) {}

    public static void startRulerActivity(@NonNull Context context) {}

    @Nullable
    public static byte[] toPostData(@Nullable String content) {
        return null;
    }

    @Nullable
    public static String toCookies(@NonNull String host) {
        return null;
    }

    @NonNull
    public static Map<String, Object> jsObjectList(Activity activity) {
        return new HashMap<>();
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return url;
    }

    public static boolean isLogin() {
        return false;
    }
}
