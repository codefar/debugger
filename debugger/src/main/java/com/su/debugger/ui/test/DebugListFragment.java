package com.su.debugger.ui.test;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.su.debugger.AppHelper;
import com.su.debugger.Debugger;
import com.su.debugger.DebuggerSupplier;
import com.su.debugger.R;
import com.su.debugger.ui.test.app.AppComponentActivity;
import com.su.debugger.ui.test.app.AppInfoListActivity;
import com.su.debugger.ui.test.app.PermissionListActivity;
import com.su.debugger.ui.test.app.SharedPreferenceDetailActivity;
import com.su.debugger.ui.test.app.SharedPreferenceListActivity;
import com.su.debugger.ui.test.mock.MockGroupHostActivity;
import com.su.debugger.ui.test.mock.ParseHelper;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.IOUtil;
import com.su.debugger.utils.NetworkUtil;
import com.su.debugger.utils.ReflectUtil;
import com.su.debugger.utils.SpHelper;
import com.su.debugger.utils.SystemInfoHelper;
import com.su.debugger.utils.Utils;
import com.su.debugger.widget.SimpleBlockedDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 17-4-17.
 */

public class DebugListFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final String TAG = DebugListFragment.class.getSimpleName();
    private static final int REQUEST_HOST = 1;
    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private static final File EXPORTED_APK_FILE = new File(Debugger.getDebuggerSdcardDir(), "exported-" + GeneralInfoHelper.getVersionName() + "-" + GeneralInfoHelper.getAppName() + ".apk");
    com.su.debugger.widget.recycler.DividerDecoration mDividerDecoration;
    private Preference mProxyPreference;
    private Preference mSharedPreferencePreference;
    private Preference mNotificationPreference;
    private ListPreference mMockPolicyPreference;
    private String mHost;
    private FragmentActivity mActivity;
    private String mEntryClassName;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(SpHelper.NAME);
        addPreferencesFromResource(R.xml.debugger_preference_debug_list);
        mActivity = getActivity();
        mProxyPreference = findPreference("system_proxy");
        SwitchPreferenceCompat entryPreference = (SwitchPreferenceCompat) findPreference("debug_entry");
        mEntryClassName = Debugger.class.getPackage().getName() + ".ui.test.DebugEntryActivity";
        entryPreference.setChecked(isComponentEnabled(mActivity.getPackageManager(), mActivity.getPackageName(), mEntryClassName));
        entryPreference.setOnPreferenceChangeListener(this);
        mNotificationPreference = findPreference("system_notification");
        mNotificationPreference.setOnPreferenceClickListener(this);
        Preference appInfoPreference = findPreference("app_info");
        appInfoPreference.setOnPreferenceClickListener(this);
        appInfoPreference.setSummary("debuggable: " + GeneralInfoHelper.isDebuggable() + "    "
                                             + "版本:" + GeneralInfoHelper.getVersionName()
                                             + "(" + GeneralInfoHelper.getVersionCode() + ")");
        findPreference("app_component_info").setOnPreferenceClickListener(this);
        Preference apkExportPreference = findPreference("apk_export");
        String[] splitSourceDirs = GeneralInfoHelper.getSplitSourceDirs();
        if (splitSourceDirs != null && splitSourceDirs.length > 0) {
            apkExportPreference.setSummary("请关闭instant run后重新编译安装应用");
            apkExportPreference.setEnabled(false);
        }
        apkExportPreference.setOnPreferenceClickListener(this);

        Preference softwareInfoPreference = findPreference("software_info");
        softwareInfoPreference.setSummary("Android " + Build.VERSION.RELEASE + "    " + SystemInfoHelper.getSystemVersionName(Build.VERSION.SDK_INT) + "    "
                                                  + "API " + Build.VERSION.SDK_INT);
        Preference hardwareInfoPreference = findPreference("hardware_info");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthPixels = GeneralInfoHelper.getScreenWidth();
        int heightPixels = GeneralInfoHelper.getScreenHeight();
        hardwareInfoPreference.setSummary("分辨率: " + widthPixels + " x " + heightPixels + " px    "
                                                  + "密度: " + SystemInfoHelper.getDpiInfo(metrics.densityDpi) + " / " + metrics.density + "x   "
                                                  + "CPU 位数: " + SystemInfoHelper.getCpuBit());
        findPreference("app_component_info").setOnPreferenceClickListener(this);
        findPreference("permission").setOnPreferenceClickListener(this);
        mSharedPreferencePreference = findPreference("shared_preference");
        mSharedPreferencePreference.setOnPreferenceClickListener(this);
        findPreference("more_phone_info").setOnPreferenceClickListener(this);
        initHostPreference();
        mMockPolicyPreference = (ListPreference) findPreference("new_fake_server");
        mMockPolicyPreference.setOnPreferenceChangeListener(this);
        initMockPolicy(mMockPolicyPreference.getValue());
        findPreference("new_fake_server_list").setOnPreferenceClickListener(this);
        findPreference("import_mock_data").setOnPreferenceClickListener(this);
        findPreference("web_view_debug").setOnPreferenceClickListener(this);
        findPreference("js_interface").setOnPreferenceClickListener(this);
        Preference preference = findPreference("js_rhino");
        preference.setVisible(ReflectUtil.isUseRhino());
        preference.setOnPreferenceClickListener(this);
        findPreference("open_source_debug").setOnPreferenceClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDividerDecoration = new com.su.debugger.widget.recycler.DividerDecoration(getListView());
        setDividerHeight(-1);
        getListView().addItemDecoration(mDividerDecoration);
        mDividerDecoration.setDivider(getResources().getDrawable(R.drawable.debugger_shape_preference_list_divider_debug));
        mDividerDecoration.setDividerHeight(1);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPreferencePreference.setEnabled(SpHelper.sharedPreferenceCount(mActivity) != 0);
        setNotificationSummary();
        if (!NetworkUtil.isNetworkAvailable()) {
            mProxyPreference.setSummary("无网络连接");
            return;
        }
        String[] proxySetting = NetworkUtil.getSystemProxy(mActivity);
        if (TextUtils.isEmpty(proxySetting[0])) {
            mProxyPreference.setSummary("无代理");
        } else {
            mProxyPreference.setSummary(proxySetting[0] + ":" + proxySetting[1]);
        }
    }

    private void startCollection() {
        if (!AppHelper.checkPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            File mockCacheDir = mActivity.getExternalFilesDir("mock");
            if (mockCacheDir == null) {
                Toast.makeText(mActivity, "没有外存读取权限", Toast.LENGTH_LONG).show();
                startActivity(new Intent(mActivity, PermissionListActivity.class));
                return;
            }
            Toast.makeText(mActivity, "没有外存读取权限只能处理" + mockCacheDir.getAbsolutePath() + "下的json文件", Toast.LENGTH_LONG).show();
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DIALOG_FRAGMENT.show(ft, "收集中...");
        new Thread() {
            @Override
            public void run() {
                ParseHelper.process(mActivity);
                mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "收集完成", Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            }
        }.start();
    }

    private void exportApkFile() {
        if (!AppHelper.checkPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(mActivity, "没有外存写入权限", Toast.LENGTH_LONG).show();
            startActivity(new Intent(mActivity, PermissionListActivity.class));
            return;
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DIALOG_FRAGMENT.show(ft, "导出中...");
        new Thread() {
            @Override
            public void run() {
                File dir = EXPORTED_APK_FILE.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                IOUtil.copyFile(new File(GeneralInfoHelper.getSourceDir()), EXPORTED_APK_FILE);
                mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "已将apk导出到" + EXPORTED_APK_FILE.getAbsolutePath(), Toast.LENGTH_LONG).show());
                DIALOG_FRAGMENT.dismissAllowingStateLoss();
            }
        }.start();
    }

    private void initHostPreference() {
        mHost = Debugger.getHost();
        Preference hostsPreference = findPreference("hosts");
        DebuggerSupplier supplier = DebuggerSupplier.getInstance();
        List<Pair<String, String>> hosts = supplier.allHosts();
        if (hosts.isEmpty()) {
            ((PreferenceGroup) findPreference("server")).removePreference(hostsPreference);
        } else {
            int size = hosts.size();
            Pair<String, String> pair = null;
            for (int i = 0; i < size; i++) {
                Pair<String, String> host = hosts.get(i);
                if (TextUtils.equals(host.second, mHost)) {
                    pair = host;
                    hostsPreference.setSummary(host.first + ": " + mHost);
                }
            }
            if (pair == null) {
                hostsPreference.setSummary(mHost);
            } else {
                hostsPreference.setSummary(pair.first + " (" + pair.second + ")");
            }
        }
        hostsPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, final Object newValue) {
        String key = preference.getKey();
        if (TextUtils.equals(key, "debug_entry")) {
            boolean enable = (boolean) newValue;
            enableEntry(mActivity, mEntryClassName, enable);
            return true;
        } else if (TextUtils.equals(key, "new_fake_server")) {
            initMockPolicy(newValue.toString());
            return true;
        }
        return false;
    }

    private void initMockPolicy(String value) {
        mMockPolicyPreference.setSummary("");
        CharSequence[] values = mMockPolicyPreference.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (TextUtils.equals(values[i], value)) {
                mMockPolicyPreference.setSummary(mMockPolicyPreference.getEntries()[i]);
                break;
            }
        }
    }

    public static void enableEntry(Context context, String className, boolean enabled) {
        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, className);
        pm.setComponentEnabledSetting(componentName,
                                      enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                              : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                      PackageManager.DONT_KILL_APP);
    }

    public static boolean isComponentEnabled(PackageManager pm, String pkgName, String clsName) {
        ComponentName componentName = new ComponentName(pkgName, clsName);
        int componentEnabledSetting = pm.getComponentEnabledSetting(componentName);
        switch (componentEnabledSetting) {
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED:
                return false;
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return true;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            default:
                // We need to get the application info to get the component's default state
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES
                            | PackageManager.GET_RECEIVERS
                            | PackageManager.GET_SERVICES
                            | PackageManager.GET_PROVIDERS
                            | PackageManager.GET_DISABLED_COMPONENTS);
                    List<ComponentInfo> components = new ArrayList<>();
                    if (packageInfo.activities != null)
                        Collections.addAll(components, packageInfo.activities);
                    if (packageInfo.services != null)
                        Collections.addAll(components, packageInfo.services);
                    if (packageInfo.providers != null)
                        Collections.addAll(components, packageInfo.providers);

                    for (ComponentInfo componentInfo : components) {
                        if (componentInfo.name.equals(clsName)) {
                            return componentInfo.isEnabled();
                        }
                    }
                    // the component is not declared in the AndroidManifest
                    return false;
                } catch (PackageManager.NameNotFoundException e) {
                    // the package isn't installed on the device
                    return false;
                }
        }
    }

    private void setNotificationSummary() {
        boolean enabled = AppHelper.isNotificationEnabled(mActivity);
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                List<NotificationChannel> notificationChannels = AppHelper.listNotificationChannels(mActivity);
                if (notificationChannels != null && !notificationChannels.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (NotificationChannel channel : notificationChannels) {
                        sb.append(channel.getName());
                        sb.append(": ");
                        sb.append(AppHelper.isNotificationChannelEnabled(mActivity, channel.getId()) ? "enabled" : "disabled");
                        sb.append("  ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    mNotificationPreference.setSummary(sb.toString());
                } else {
                    mNotificationPreference.setSummary("应用未创建channel");
                }
            } else {
                mNotificationPreference.setSummary("enabled");
            }
        } else {
            mNotificationPreference.setSummary("disabled");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_HOST && resultCode == Activity.RESULT_OK) {
            final String value = data.getStringExtra("value");
            if (!TextUtils.equals(mHost, value)) {
                new AlertDialog.Builder(mActivity)
                        .setCancelable(false)
                        .setMessage("点击确认，程序自动重启")
                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                            SpHelper.getDebuggerSharedPreferences()
                                    .edit()
                                    .putString("host", value)
                                    .apply();
                            Utils.restartApp(mActivity);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case "hosts":
                Intent intent = new Intent(mActivity, HostsActivity.class);
                intent.putExtra("host", mHost);
                startActivityForResult(intent, REQUEST_HOST);
                return true;
            case "system_notification":
                AppHelper.goNotificationSettings(mActivity);
                return true;
            case "permission":
                startActivity(new Intent(mActivity, PermissionListActivity.class));
                return true;
            case "apk_export":
                exportApkFile();
                return true;
            case "shared_preference":
                if (SpHelper.sharedPreferenceCount(mActivity) == 1) {
                    Intent sharedPreferenceIntent = new Intent(mActivity, SharedPreferenceDetailActivity.class);
                    sharedPreferenceIntent.putExtra("name", SpHelper.getOnlySharedPreferenceFileName(mActivity));
                    startActivity(sharedPreferenceIntent);
                } else {
                    startActivity(new Intent(mActivity, SharedPreferenceListActivity.class));
                }
                return true;
            case "app_info":
                startActivity(new Intent(mActivity, AppInfoListActivity.class));
                return true;
            case "app_component_info":
                startActivity(new Intent(mActivity, AppComponentActivity.class));
                return true;
            case "more_phone_info":
                startActivity(new Intent(mActivity, PhoneInfoActivity.class));
                return true;
            case "import_mock_data":
                startCollection();
                return true;
            case "new_fake_server_list":
                Intent newFakeIntent = new Intent(mActivity, MockGroupHostActivity.class);
                newFakeIntent.putExtra("title", preference.getTitle());
                startActivity(newFakeIntent);
                return true;
            case "web_view_debug":
                Intent serverIntent = new Intent(mActivity, WebViewListActivity.class);
                serverIntent.putExtra("type", 0);
                serverIntent.putExtra("title", preference.getTitle());
                startActivity(serverIntent);
                return true;
            case "js_interface":
                startActivity(new Intent(mActivity, JsInterfaceTestActivity.class));
                return true;
            case "js_rhino":
                if (!AppHelper.checkPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        || !AppHelper.checkPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(mActivity, "没有外存读写权限", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(mActivity, PermissionListActivity.class));
                    return true;
                }
                startActivity(new Intent(mActivity, JsListActivity.class));
                return true;
            case "open_source_debug":
                startActivity(new Intent(mActivity, UtilsListActivity.class));
                return true;
            default:
                return false;
        }
    }
}
