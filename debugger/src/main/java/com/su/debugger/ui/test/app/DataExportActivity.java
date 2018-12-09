package com.su.debugger.ui.test.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.su.debugger.AppHelper;
import com.su.debugger.Debugger;
import com.su.debugger.R;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.IOUtil;
import com.su.debugger.utils.ManifestParser;
import com.su.debugger.widget.SimpleBlockedDialogFragment;

import java.io.File;
import java.io.FilenameFilter;

public class DataExportActivity extends BaseAppCompatActivity {
    private static final String TAG = DataExportActivity.class.getSimpleName();
    private static final SimpleBlockedDialogFragment DIALOG_FRAGMENT = SimpleBlockedDialogFragment.newInstance();
    private static final File EXPORTED_APK_FILE = new File(Debugger.getDebuggerSdcardDir(), GeneralInfoHelper.getVersionName() + "-" + GeneralInfoHelper.getAppName() + ".apk");
    private static final File EXPORTED_MANIFEST_FILE = new File(Debugger.getDebuggerSdcardDir(), GeneralInfoHelper.getVersionName() + "-manifest.xml");
    private static final File EXPORTED_SO_DIR_FILE = new File(Debugger.getDebuggerSdcardDir(), GeneralInfoHelper.getVersionName() + "-native");
    private static final File EXPORTED_DATABASE_DIR_FILE = new File(Debugger.getDebuggerSdcardDir(), GeneralInfoHelper.getVersionName() + "-databases");
    private static final File EXPORTED_SHARED_PREFERENCE_DIR_FILE = new File(Debugger.getDebuggerSdcardDir(), GeneralInfoHelper.getVersionName() + "-shared_prefs");
    private static File EXPORTED_SHARED_PRIVATE_DIR_FILE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_data_export);
        EXPORTED_SHARED_PRIVATE_DIR_FILE = new File(Debugger.getDebuggerSdcardDir(), getPackageName());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new ItemListFragment(), "app_data_export").commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("应用数据导出");
    }

    public static class ItemListFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private FragmentActivity mActivity;
        private String mDataDirPath;
        private FilenameFilter mDbFilenameFilter = (dir, name) -> name.endsWith(".db");
        private FilenameFilter mSpFilenameFilter = (dir, name) -> name.endsWith(".xml");

        private void exportApkFile() {
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

        private void exportSoFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            new Thread() {
                @Override
                public void run() {
                    File nativeLibraryDir = new File(GeneralInfoHelper.getNativeLibraryDir());
                    if (!EXPORTED_SO_DIR_FILE.exists()) {
                        EXPORTED_SO_DIR_FILE.mkdirs();
                    }
                    File[] sos = nativeLibraryDir.listFiles();
                    for (File so : sos) {
                        IOUtil.copyFile(so, new File(EXPORTED_SO_DIR_FILE, so.getName()));
                    }
                    mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "已将so导出到" + EXPORTED_SO_DIR_FILE.getAbsolutePath(), Toast.LENGTH_LONG).show());
                    DIALOG_FRAGMENT.dismissAllowingStateLoss();
                }
            }.start();
        }

        private void exportManifestFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            new Thread() {
                @Override
                public void run() {
                    File dir = EXPORTED_MANIFEST_FILE.getParentFile();
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    ManifestParser parser = new ManifestParser(mActivity);
                    IOUtil.writeFile(EXPORTED_MANIFEST_FILE.getAbsolutePath(), parser.getManifest());
                    mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "已将apk导出到" + EXPORTED_MANIFEST_FILE.getAbsolutePath(), Toast.LENGTH_LONG).show());
                    DIALOG_FRAGMENT.dismissAllowingStateLoss();
                }
            }.start();
        }

        private void exportDatabaseFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            new Thread() {
                @Override
                public void run() {
                    File databasesDir = new File(mDataDirPath, "databases");
                    if (!EXPORTED_DATABASE_DIR_FILE.exists()) {
                        EXPORTED_DATABASE_DIR_FILE.mkdirs();
                    }
                    File[] databases = databasesDir.listFiles(mDbFilenameFilter);
                    for (File database : databases) {
                        IOUtil.copyFile(database, new File(EXPORTED_DATABASE_DIR_FILE, database.getName()));
                    }
                    mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "已将数据库文件导出到" + EXPORTED_DATABASE_DIR_FILE.getAbsolutePath(), Toast.LENGTH_LONG).show());
                    DIALOG_FRAGMENT.dismissAllowingStateLoss();
                }
            }.start();
        }

        private void exportSharedPreferenceFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            new Thread() {
                @Override
                public void run() {
                    File databasesDir = new File(mDataDirPath, "databases");
                    if (!EXPORTED_SHARED_PREFERENCE_DIR_FILE.exists()) {
                        EXPORTED_SHARED_PREFERENCE_DIR_FILE.mkdirs();
                    }
                    File[] sharedPreferences = databasesDir.listFiles(mSpFilenameFilter);
                    for (File sharedPreference : sharedPreferences) {
                        IOUtil.copyFile(sharedPreference, new File(EXPORTED_SHARED_PREFERENCE_DIR_FILE, sharedPreference.getName()));
                    }
                    mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "已将SharedPreference文件导出到" + EXPORTED_SHARED_PREFERENCE_DIR_FILE.getAbsolutePath(), Toast.LENGTH_LONG).show());
                    DIALOG_FRAGMENT.dismissAllowingStateLoss();
                }
            }.start();
        }

        private void exportPrivateDirFile() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DIALOG_FRAGMENT.show(ft, "导出中...");
            new Thread() {
                @Override
                public void run() {
                    if (!EXPORTED_SHARED_PRIVATE_DIR_FILE.exists()) {
                        EXPORTED_SHARED_PRIVATE_DIR_FILE.mkdirs();
                    }
                    IOUtil.copyDirectory(new File(mDataDirPath), EXPORTED_SHARED_PRIVATE_DIR_FILE);
                    mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "已将应用私有文件导出到" + EXPORTED_SHARED_PRIVATE_DIR_FILE.getAbsolutePath(), Toast.LENGTH_LONG).show());
                    DIALOG_FRAGMENT.dismissAllowingStateLoss();
                }
            }.start();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.debugger_preference_data_export);
            mActivity = getActivity();
            mDataDirPath = mActivity.getApplicationInfo().dataDir;
            Preference apkPreference = findPreference("apk");
            apkPreference.setOnPreferenceClickListener(this);
            String[] splitSourceDirs = GeneralInfoHelper.getSplitSourceDirs();
            if (splitSourceDirs != null && splitSourceDirs.length > 0) {
                apkPreference.setSummary("请关闭instant run后重新编译安装应用");
                apkPreference.setEnabled(false);
            } else {
                apkPreference.setSummary("可将您现在使用中的App导出到sdcard中");
            }
            Preference soPreference = findPreference("so");
            soPreference.setOnPreferenceClickListener(this);
            File nativeLibraryDir = new File(GeneralInfoHelper.getNativeLibraryDir());
            if (IOUtil.hasFilesInDir(nativeLibraryDir)) {
                soPreference.setEnabled(true);
                soPreference.setSummary("共" + nativeLibraryDir.list().length + "个So文件");
            } else {
                soPreference.setEnabled(false);
                soPreference.setSummary("暂无So文件");
            }
            findPreference("manifest").setOnPreferenceClickListener(this);

            Preference databasePreference = findPreference("database");
            databasePreference.setOnPreferenceClickListener(this);
            File databasesDir = new File(mDataDirPath, "databases");
            if (IOUtil.hasFilesInDir(databasesDir, mDbFilenameFilter)) {
                databasePreference.setEnabled(true);
                databasePreference.setSummary("共" + databasesDir.list(mDbFilenameFilter).length + "个数据库文件");
            } else {
                databasePreference.setEnabled(false);
                databasePreference.setSummary("暂无数据库文件");
            }

            File sharedPreferenceDir = new File(mDataDirPath, "shared_prefs");
            Preference sharedPreferencePreference = findPreference("shared_preference");
            sharedPreferencePreference.setOnPreferenceClickListener(this);
            if (IOUtil.hasFilesInDir(sharedPreferenceDir, mSpFilenameFilter)) {
                sharedPreferencePreference.setEnabled(true);
                sharedPreferencePreference.setSummary("共" + sharedPreferenceDir.list(mSpFilenameFilter).length + "个数据库文件");
            } else {
                sharedPreferencePreference.setEnabled(false);
                sharedPreferencePreference.setSummary("暂无SharedPreference文件");
            }

            findPreference("private_dir").setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (!AppHelper.checkPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(mActivity, "没有外存写入权限", Toast.LENGTH_LONG).show();
                startActivity(new Intent(mActivity, PermissionListActivity.class));
                return true;
            }
            switch (preference.getKey()) {
                case "apk":
                    exportApkFile();
                    break;
                case "so":
                    exportSoFile();
                    break;
                case "manifest":
                    exportManifestFile();
                    break;
                case "database":
                    exportDatabaseFile();
                    break;
                case "shared_preference":
                    exportSharedPreferenceFile();
                    break;
                case "private_dir":
                    exportPrivateDirFile();
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    public String getTag() {
        return TAG;
    }
}
