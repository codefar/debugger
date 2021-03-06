package com.su.debugger.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.su.debugger.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by su on 17-2-8.
 */

public class GeneralInfoHelper {

    private static final String TAG = GeneralInfoHelper.class.getSimpleName();

    //application context
    private static Context sContext;
    private static String sAndroidId;
    private static int sScreenWidth;
    private static int sScreenHeight;
    private static double sAspectRatio;

    private static int sAvailableWidth;
    private static int sAvailableHeight;
    private static ViewConfiguration sViewConfiguration;

    private static String sVersionName = "";
    private static int sVersionCode;
    private static String sPackageName = "";
    private static String sAppName = "";
    private static String sApplicationLabel = "";
    private static String sProcessName = "";
    private static int sProcessId = -1;
    private static boolean sDebuggable;
    private static long sLaunchTime;

    private static int sActionBarHeight;
    private static int sStatusBarHeight;
    private static int sNavigationBarHeight;
    private static int sTargetSdkVersion;
    private static int sMinSdkVersion;
    private static int sCompileSdkVersion;
    private static int sUid;
    private static String sApplicationClassName;
    private static long sInstallTime;
    private static long sUpdateTime;
    private static String sSourceDir;
    private static String[] sSplitSourceDirs;
    private static String sDeviceProtectedDataDir;
    private static String sNativeLibraryDir;
    private static String sDataDir;
    private static String sLibName;

    private GeneralInfoHelper() {}

    public static void init(Context context) {
        sContext = context.getApplicationContext();
        Resources resources = sContext.getResources();
        sLibName = resources.getString(R.string.debugger_name);
        initPackageInfo();
        initAndroidId();
        initScreenSize();
        sProcessId = Process.myPid();
        sProcessName = getCurrentProcessName();
        sStatusBarHeight = UiHelper.getStatusBarHeight(sContext);
        sActionBarHeight = UiHelper.getActionBarHeight(sContext);
        sNavigationBarHeight = UiHelper.getNavigationBarHeight(sContext);
        sViewConfiguration = ViewConfiguration.get(context);
        SharedPreferences sharedPreferences = SpHelper.getDebuggerSharedPreferences();
        long now = System.currentTimeMillis();
        sLaunchTime = sharedPreferences.getLong("launch_time", now);
    }

    private static void initPackageInfo() {
        if (TextUtils.isEmpty(sVersionName)) {
            try {
                PackageManager pm = sContext.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(sContext.getPackageName(), 0);
                sVersionName = pi.versionName;
                sVersionCode = pi.versionCode;
                sPackageName = pi.packageName;
                sAppName = pi.applicationInfo.loadLabel(pm).toString();
                ApplicationInfo applicationInfo = pm.getApplicationInfo(getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                sUid = applicationInfo.uid;
                sApplicationClassName = applicationInfo.className;
                sTargetSdkVersion = applicationInfo.targetSdkVersion;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    sMinSdkVersion = applicationInfo.minSdkVersion;
                    sDeviceProtectedDataDir = applicationInfo.deviceProtectedDataDir;
                }
                Object compileSdkVersion = ReflectUtil.getFieldValue(applicationInfo.getClass(), applicationInfo, "compileSdkVersion");
                if (compileSdkVersion != null) {
                    sCompileSdkVersion = (Integer) compileSdkVersion;
                }
                sApplicationLabel = pm.getApplicationLabel(applicationInfo).toString();
                sDebuggable = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
                sSourceDir = applicationInfo.sourceDir;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sSplitSourceDirs = applicationInfo.splitSourceDirs;
                }
                sNativeLibraryDir = applicationInfo.nativeLibraryDir;
                sDataDir = applicationInfo.dataDir;
                sUpdateTime = new File(sSourceDir).lastModified();
                sInstallTime = pi.firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, e);
            }
        }
    }

    @SuppressLint("HardwareIds")
    private static void initAndroidId() {
        sAndroidId = Settings.Secure.getString(sContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private static void initScreenSize() {
        WindowManager wm = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point realSize = new Point();
        display.getRealSize(realSize);
        sScreenWidth = Math.min(realSize.x, realSize.y);
        sScreenHeight = Math.max(realSize.x, realSize.y);
        sAspectRatio = BigDecimal.valueOf(getScreenHeight())
                .divide(BigDecimal.valueOf(getScreenWidth()), 2, BigDecimal.ROUND_DOWN)
                .doubleValue();
        Point availableSize = new Point();
        display.getSize(availableSize);
        sAvailableWidth = Math.min(availableSize.x, availableSize.y);
        sAvailableHeight = Math.max(availableSize.x, availableSize.y);
    }

    public static double getAspectRatio() {
        return sAspectRatio;
    }

    public static int getProcessId() {
        return sProcessId;
    }

    @NonNull
    private static String getCurrentProcessName() {
        try {
            return IOUtil.streamToString(new FileInputStream("/proc/self/cmdline")).trim();
        } catch (IOException e) {
            Log.e(TAG, "can't get current process name!", e);
            return "";
        }
    }

    public static Context getContext() {
        return sContext;
    }

    public static ViewConfiguration getViewConfiguration() {
        return sViewConfiguration;
    }

    public static String getVersionName() {
        return sVersionName;
    }

    public static int getVersionCode() {
        return sVersionCode;
    }

    public static String getPackageName() {
        return sPackageName;
    }

    public static String getAppName() {
        return sAppName;
    }

    public static String getAndroidId() {
        return sAndroidId;
    }

    public static int getScreenWidth() {
        return sScreenWidth;
    }

    public static int getScreenHeight() {
        return sScreenHeight;
    }

    public static int getAvailableWidth() {
        return sAvailableWidth;
    }

    public static int getAvailableHeight() {
        return sAvailableHeight;
    }

    public static int getNavigationBarHeight() {
        return sNavigationBarHeight;
    }

    public static String getApplicationLabel() {
        return sApplicationLabel;
    }

    public static String getProcessName() {
        return sProcessName;
    }

    public static boolean isDebuggable() {
        return sDebuggable;
    }

    public static long getLaunchTime() {
        return sLaunchTime;
    }

    public static int getTargetSdkVersion() {
        return sTargetSdkVersion;
    }

    public static int getMinSdkVersion() {
        return sMinSdkVersion;
    }

    public static int getCompileSdkVersion() {
        return sCompileSdkVersion;
    }

    public static int getUid() {
        return sUid;
    }

    public static String getApplicationClassName() {
        return sApplicationClassName;
    }

    public static long getInstallTime() {
        return sInstallTime;
    }

    public static long getUpdateTime() {
        return sUpdateTime;
    }

    public static String getSourceDir() {
        return sSourceDir;
    }

    public static String[] getSplitSourceDirs() {
        return sSplitSourceDirs;
    }

    public static String getDeviceProtectedDataDir() {
        return sDeviceProtectedDataDir;
    }

    public static String getNativeLibraryDir() {
        return sNativeLibraryDir;
    }

    public static String getDataDir() {
        return sDataDir;
    }

    public static int getStatusBarHeight() {
        return sStatusBarHeight;
    }

    public static int getActionBarHeight() {
        return sActionBarHeight;
    }

    public static String getLibName() {
        return sLibName;
    }

    @NonNull
    public static String infoToString() {
        return "GeneralInfoHelper{" +
                "libName=" + sLibName +
                ", debuggable=" + sDebuggable +
                ", processId=" + sProcessId +
                ", processName=" + sProcessName +
                ", versionName=" + sVersionName +
                ", versionCode=" + sVersionCode +
                ", packageName=" + sPackageName +
                ", appName=" + sAppName +
                ", deviceId=" + sAndroidId +
                ", screenWidth=" + sScreenWidth +
                ", screenHeight=" + sScreenHeight +
                ", statusBarHeight=" + sStatusBarHeight +
                ", actionBarHeight=" + sActionBarHeight +
                ", navigationBarHeight=" + sNavigationBarHeight +
                ", aspectRatio=" + sAspectRatio +
                ", availableWidth=" + sAvailableWidth +
                ", availableHeight=" + sAvailableHeight +
                ", applicationLabel=" + sApplicationLabel +
                ", uid=" + sUid +
                ", applicationClassName=" + sApplicationClassName +
                ", installTime=" + sInstallTime +
                ", updateTime=" + sUpdateTime +
                ", sourceDir=" + sSourceDir +
                ", sSplitSourceDirs=" + Arrays.toString(sSplitSourceDirs) +
                ", nativeLibraryDir=" + sNativeLibraryDir +
                ", dataDir=" + sDataDir +
                ", deviceProtectedDataDir=" + sDeviceProtectedDataDir +
                ", launchTime=" + sLaunchTime +
                ", targetSdkVersion=" + sTargetSdkVersion +
                ", minSdkVersion=" + sMinSdkVersion +
                ", compileSdkVersion=" + sCompileSdkVersion +
                '}';
    }
}
