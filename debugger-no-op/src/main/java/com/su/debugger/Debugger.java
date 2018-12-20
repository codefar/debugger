package com.su.debugger;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by su on 18-1-2.
 */

public class Debugger {

    private Debugger() {}

    public static void init(Application app) {

    }

    public static void init(Application app, Configuration configuration) {

    }

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

    public static Object getEnvironmentInterceptor() {
        return null;
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return url;
    }

    public static Intent getDebuggerMainIntent() {
        return null;
    }

    public static void startDataExportActivity(@NonNull Context context) {}

    public static void startPermissionsActivity(@NonNull Context context) {}

    public static void startActivitiesActivity(@NonNull Context context) {}

    public static void startMockDataActivity(@NonNull Context context) {}

    public static void startJsInterfacesActivity(@NonNull Context context) {}

    public static void startAppInfoActivity(@NonNull Context context) {}
}
