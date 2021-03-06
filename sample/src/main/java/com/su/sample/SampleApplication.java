package com.su.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.su.debugger.Debugger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by su on 18-1-2.
 */

public class SampleApplication extends Application {

    private static final String TAG = SampleApplication.class.getSimpleName();
    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        initDebugger(this);
        initSharedPreference();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyDeath()
                .penaltyLog()
                .build());
    }

    private static void initDebugger(Application application) {
        if (BuildConfig.DEBUG) {
            Debugger.init(application, "com.su.sample.MySupplier");
        }
    }

    private void initSharedPreference() {
        Set<String> set = new HashSet<>();
        set.add("a");
        set.add("b");
        set.add("c");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putInt("int_test", 1)
                .putLong("long_test", 0x80000000)
                .putFloat("float_test", 1.1f)
                .putBoolean("boolean_test", true)
                .putString("string_test", "string...")
                .putStringSet("set_test", set)
                .apply();
    }

    public static Context getContext() {
        return sContext;
    }
}
