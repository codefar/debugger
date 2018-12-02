package com.su.debugger;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.su.debugger.component.annotation.DebugConfiguration;
import com.su.debugger.net.DataCollectorInterceptor;
import com.su.debugger.net.EnvironmentInterceptor;
import com.su.debugger.net.MockInterceptor;
import com.su.debugger.ui.test.DebuggerMainActivity;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.SpHelper;

import java.io.File;

/**
 * Created by su on 18-1-2.
 */

public class Debugger {

    private static final String TAG = Debugger.class.getSimpleName();
    private static Debugger sDebugger;
    private Configuration mConfiguration;
    private static File sDebuggerSdcardDir = new File(Environment.getExternalStorageDirectory(), "debugger");

    private Debugger() {}

    public static void init(Application app) {
        final DebugConfiguration debugConfiguration = app.getClass().getAnnotation(DebugConfiguration.class);
        if (debugConfiguration == null) {
            throw new IllegalStateException("no configuration found.");
        }
        Configuration configuration = new Configuration();
        configuration.setRequestSupplierClass(debugConfiguration.requestSupplier());
        configuration.setHttpLibrary(debugConfiguration.httpLibrary());
        init(app, configuration);
    }

    public static void init(Application app, @NonNull Configuration configuration) {
        if (sDebugger != null) {
            throw new IllegalStateException("don't init debugger twice.");
        }
        SpHelper.initSharedPreferences(app);
        GeneralInfoHelper.init(app);
        sDebugger = new Debugger();
        sDebugger.mConfiguration = configuration;
        if (sDebugger.mConfiguration.getRequestSupplierClass() == null) {
            throw new IllegalArgumentException("requestSupplier must not be null.");
        }

        DebuggerSupplier.newInstance();
    }

    public static Object getMockInterceptor() {
        return new MockInterceptor();
    }

    public static Object getDataCollectorInterceptor() {
        return new DataCollectorInterceptor();
    }

    public static Object getEnvironmentInterceptor() {
        return new EnvironmentInterceptor();
    }

    @NonNull
    public static String urlMapping(@NonNull String url, @NonNull String newHost) {
        return EnvironmentInterceptor.urlMapping(url, newHost);
    }

    public static File getDebuggerSdcardDir() {
        return sDebuggerSdcardDir;
    }

    /**
     * app需要使用此方法切换域名，切换域名后需要重启app
     * */
    @NonNull
    public static String getHost() {
        return SpHelper.getDebuggerSharedPreferences().getString(SpHelper.COLUMN_HOST, "");
    }

    public static Intent getDebuggerMainIntent() {
        return new Intent(GeneralInfoHelper.getContext(), DebuggerMainActivity.class);
    }

    public static Debugger getInstance() {
        return sDebugger;
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }
}
