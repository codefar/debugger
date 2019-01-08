package com.su.debugger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.su.debugger.net.interceptor.DataCollectorInterceptor;
import com.su.debugger.net.interceptor.HostInterceptor;
import com.su.debugger.net.interceptor.MockInterceptor;
import com.su.debugger.ui.DebuggerMainActivity;
import com.su.debugger.ui.HostsActivity;
import com.su.debugger.ui.JsInterfaceListActivity;
import com.su.debugger.ui.app.AppInfoListActivity;
import com.su.debugger.ui.app.ComponentListActivity;
import com.su.debugger.ui.app.DataExportActivity;
import com.su.debugger.ui.app.DatabaseListActivity;
import com.su.debugger.ui.app.PermissionListActivity;
import com.su.debugger.ui.mock.MockGroupHostActivity;
import com.su.debugger.ui.ui.RulerActivity;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.SpHelper;

import java.io.File;
import java.util.Map;

/**
 * Created by su on 18-1-2.
 */

public class Debugger {

    private static final String TAG = Debugger.class.getSimpleName();
    private static Debugger sDebugger;
    private static File sDebuggerSdcardDir = new File(Environment.getExternalStorageDirectory(), "debugger");

    private Debugger() {}

    public static void init(Application app, @NonNull String className) {
        if (sDebugger != null) {
            throw new IllegalStateException("don't init debugger twice.");
        }
        SpHelper.initSharedPreferences(app);
        GeneralInfoHelper.init(app);
        sDebugger = new Debugger();
        if (TextUtils.isEmpty(className)) {
            throw new IllegalArgumentException("requestSupplier must not be null.");
        }

        DebuggerSupplier.newInstance(className);
    }

    public static Object getMockInterceptor() {
        return new MockInterceptor();
    }

    public static Object getDataCollectorInterceptor() {
        return new DataCollectorInterceptor();
    }

    public static Object getHostInterceptor() {
        return new HostInterceptor();
    }

    public static File getDebuggerSdcardDir() {
        return sDebuggerSdcardDir;
    }

    @NonNull
    public static String getHost() {
        return SpHelper.getDebuggerSharedPreferences().getString(SpHelper.COLUMN_HOST, "");
    }

    @NonNull
    public static String getWebViewHost() {
        return SpHelper.getDebuggerSharedPreferences().getString(SpHelper.COLUMN_WEB_VIEW_HOST, "");
    }

    public static Intent getDebuggerMainIntent() {
        return new Intent(GeneralInfoHelper.getContext(), DebuggerMainActivity.class);
    }

    public static Debugger getInstance() {
        return sDebugger;
    }

    public static void startDataExportActivity(@NonNull Context context) {
        DataExportActivity.startActivity(context);
    }

    public static void startPermissionsActivity(@NonNull Context context) {
        PermissionListActivity.startActivity(context);
    }

    public static void startActivitiesActivity(@NonNull Context context) {
        ComponentListActivity.startActivity(context, "activity");
    }

    public static void startMockDataActivity(@NonNull Context context) {
        MockGroupHostActivity.startActivity(context, "数据模拟接口列表");
    }

    public static void startJsInterfacesActivity(@NonNull Context context) {
        JsInterfaceListActivity.startActivity(context);
    }

    public static void startAppInfoActivity(@NonNull Context context) {
        AppInfoListActivity.startActivity(context);
    }

    public static void startDatabaseListActivity(@NonNull Context context) {
        DatabaseListActivity.startActivity(context);
    }

    public static void startHostsActivity(@NonNull Context context, int type) {
        HostsActivity.startActivity(context, type);
    }

    public static void startRulerActivity(@NonNull Context context) {
        RulerActivity.startActivity(context);
    }

    @Nullable
    public static byte[] toPostData(@Nullable String content) {
        return DebuggerSupplier.getInstance().toPostData(content);
    }

    @Nullable
    public static String toCookies(@NonNull String host) {
        return DebuggerSupplier.getInstance().toCookies(host);
    }

    @NonNull
    public static Map<String, Object> jsObjectList(Activity activity) {
        return DebuggerSupplier.getInstance().jsObjectList(activity);
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return DebuggerSupplier.getInstance().urlMapping(url, newHost);
    }

    public static boolean isLogin() {
        return DebuggerSupplier.getInstance().isLogin();
    }
}
