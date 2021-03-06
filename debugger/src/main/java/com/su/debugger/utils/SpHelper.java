package com.su.debugger.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by su on 18-1-2.
 */

public class SpHelper {
    public static final String SHARED_PREFERENCE_BASE_DIRNAME = "shared_prefs";
    public static final String NAME = "debugger";
    public static final String COLUMN_DEBUG_DOWNTIME = "debug_downtime";
    public static final String COLUMN_MOCK_POLICY = "mock_policy";
    public static final String COLUMN_HOST = "host";
    public static final String COLUMN_WEB_VIEW_HOST = "web_view_host";
    public static final String COLUMN_GRID_LINE_SIZE = "grid_line_size";
    public static final String COLUMN_GRID_LINE_UNIT = "grid_line_unit";
    public static final String COLUMN_GRID_LINE_COLOR_STRING = "grid_line_color";
    public static final String COLUMN_GRID_LINE_STATUS_BAR = "grid_line_status_bar";
    public static final String COLUMN_MEASURE_COLOR_STRING = "measure_color";
    public static final String COLUMN_MEASURE_RESULT_COLOR_STRING = "measure_result_color";
    public static final String COLUMN_MEASURE_STATUS_BAR = "measure_status_bar";
    public static final String COLUMN_MEASURE_NAVIGATION_BAR = "measure_navigation_bar";

    private static SharedPreferences sDefaultSharedPreferences;

    private SpHelper() {
    }

    public static SharedPreferences getDebuggerSharedPreferences() {
        return sDefaultSharedPreferences;
    }

    public static void initSharedPreferences(@NonNull Context context) {
        sDefaultSharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static int sharedPreferenceCount(@NonNull Context context) {
        File sharedPreferenceDir = new File(context.getApplicationInfo().dataDir, SHARED_PREFERENCE_BASE_DIRNAME);
        if(sharedPreferenceDir.exists() && sharedPreferenceDir.isDirectory()){
            return  sharedPreferenceDir.list().length;
        }
        return 0;
    }

    @NonNull
    public static String getOnlySharedPreferenceFileName(@NonNull Context context) {
        File sharedPreferenceDir = new File(context.getApplicationInfo().dataDir, SHARED_PREFERENCE_BASE_DIRNAME);
        if(sharedPreferenceDir.exists() && sharedPreferenceDir.isDirectory()){
            return IOUtil.getFileNameWithoutExtension(sharedPreferenceDir.listFiles((dir, name) -> name.endsWith(".xml"))[0]);
        }
        throw new IllegalStateException("there's no any shared preference files.");
    }

    public static List<File> getAllSharedPreferenceFiles(@NonNull Context context) {
        File sharedPreferenceDir = new File(context.getApplicationInfo().dataDir, SHARED_PREFERENCE_BASE_DIRNAME);
        if(sharedPreferenceDir.exists() && sharedPreferenceDir.isDirectory()){
            return Arrays.asList(sharedPreferenceDir.listFiles((dir, name) -> name.endsWith(".xml")));
        }
        return new ArrayList<>();
    }
}
