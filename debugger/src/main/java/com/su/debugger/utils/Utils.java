package com.su.debugger.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by su on 17-12-11.
 */

public class Utils {

    public static void restartApp(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 600L, pendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
