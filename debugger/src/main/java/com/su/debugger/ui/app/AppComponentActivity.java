package com.su.debugger.ui.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;

import com.su.debugger.R;
import com.su.debugger.ui.BaseAppCompatActivity;
import com.su.debugger.ui.XmlViewerActivity;
import com.su.debugger.utils.ManifestParser;
import com.su.debugger.widget.recycler.PreferenceItemDecoration;

/**
 * Created by su on 17-5-27.
 * 调试功能列表 - 应用信息
 */
public class AppComponentActivity extends BaseAppCompatActivity {
    private static final String TAG = AppComponentActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_preference_activity_template);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new InfoListFragment(), "app_info").commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("App信息");
    }

    public static class InfoListFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private FragmentActivity mActivity;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.debugger_preference_app_info);
            mActivity = getActivity();
            findPreference("activity").setOnPreferenceClickListener(this);
            findPreference("service").setOnPreferenceClickListener(this);
            findPreference("receiver").setOnPreferenceClickListener(this);
            findPreference("provider").setOnPreferenceClickListener(this);
            findPreference("manifest").setOnPreferenceClickListener(this);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDividerHeight(-1);
            PreferenceItemDecoration decoration = new PreferenceItemDecoration(mActivity, 0, 0);
            getListView().addItemDecoration(decoration);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (TextUtils.equals(key, "manifest")) {
                startTextViewerActivity();
                return true;
            }
            ComponentListActivity.startActivity(mActivity, key);
            return true;
        }

        private void startTextViewerActivity() {
            ManifestParser parser = new ManifestParser(mActivity);
            Intent intent = new Intent(mActivity, XmlViewerActivity.class);
            intent.putExtra("title", "Manifest文件");
            intent.putExtra("content", parser.getManifest());
            startActivity(intent);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
