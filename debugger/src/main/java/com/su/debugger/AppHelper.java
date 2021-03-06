package com.su.debugger;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.su.debugger.entity.NoteWebViewEntity;
import com.su.debugger.utils.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 2014/8/20.
 */
public final class AppHelper {

    private static final String TAG = AppHelper.class.getSimpleName();

    private AppHelper() {}

    public static void startLauncher(@NonNull Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }

    public static boolean isPhone(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static List<FeatureInfo> getRequiredFeatures(@NonNull Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            if (packageInfo.reqFeatures == null) {
                return new ArrayList<>();
            }
            //需要去重
            ArrayList<FeatureInfo> list = new ArrayList<>();
            for (FeatureInfo featureInfo : packageInfo.reqFeatures) {
                if (list.isEmpty()) {
                    list.add(featureInfo);
                    continue;
                }

                boolean find = false;
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    FeatureInfo fi = list.get(i);
                    if (TextUtils.equals(fi.name, featureInfo.name)) {
                        find = true;
                        continue;
                    }
                }
                if (!find) {
                    list.add(featureInfo);
                }
            }

            return Arrays.asList(packageInfo.reqFeatures);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e);
        }
        return new ArrayList<>();
    }

    public static String encodeString(@Nullable String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "str: " + str, e);
        }
        return str;
    }

    public static String getHostFromUrl(@NonNull String url) {
        Uri uri = Uri.parse(url);
        return uri.getHost();
    }

    //https://stackoverflow.com/questions/4737841/urlencoder-not-able-to-translate-space-character?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    public static String encodeUrlString(@Nullable String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        return encodeString(str).replace("+", "%20");
    }

    public static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public static List<NotificationChannel> listNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return manager.getNotificationChannels();
        }
        return Collections.EMPTY_LIST;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static boolean isNotificationChannelEnabled(@NonNull Context context, @NonNull String channelId) {
        boolean enabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
        if (enabled && !TextUtils.isEmpty(channelId)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(channelId);
            return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }
        return false;
    }

    public static boolean isNotificationEnabled(@NonNull Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void goNotificationSettings(@NonNull Context context) {
        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    public static void startActivity(@NonNull Context context, @Nullable Intent intent) {
        if (intent != null) {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "路径错误,跳转失败!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "intent: " + intent, e);
            }
        }
    }

    public static void startWebView(@NonNull Context context, String title, String url, boolean sharable) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("sharable", sharable);
        context.startActivity(intent);
    }

    public static void startWebView(@NonNull Context context, @Nullable NoteWebViewEntity entity) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("entity", entity);
        intent.putExtra("sharable", true);
        intent.putExtra("clearable", true);
        context.startActivity(intent);
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic");
    }

    public static void hideSoftInputFromWindow(@Nullable Window window) {
        if (window != null && window.getCurrentFocus() != null) {
            Context context = window.getContext();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(window.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String shellExec(@NonNull String cmd) {
        Runtime runtime = Runtime.getRuntime();
        BufferedReader reader = null;
        try {
            Process process = runtime.exec(cmd);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            char[] buff = new char[1024];
            int ch;
            while ((ch = reader.read(buff)) != -1) {
                stringBuilder.append(buff, 0, ch);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            Log.e(TAG, "cmd: " + cmd, e);
        } finally {
            IOUtil.closeQuietly(reader);
        }
        return null;
    }

    public static void copyToClipboard(@NonNull Context context, String label, String text) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    public static int getDatabasesCount(@NonNull Context context) {
        String[] dbList = context.getApplicationContext().databaseList();
        if (dbList == null) {
            return 0;
        }
        int count = 0;
        for (String dbName : dbList) {
            if (dbName.endsWith("-journal")) {
                continue;
            }
            count++;
        }
        return count;
    }

    /**
     * 判断是否是魅族系统
     *
     * @return
     */
    public static boolean isFlyme() {
        try {
            // Invoke Build.hasSmartBar()
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    public static void restartApp(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 600L, pendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
