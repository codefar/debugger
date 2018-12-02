package com.su.debugger.ui.test.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.su.debugger.R;
import com.su.debugger.ui.test.BaseAppCompatActivity;

/**
 * Created by su on 17-5-27.
 * 调试功能列表 - 应用信息
 */
public class AppComponentActivity extends BaseAppCompatActivity {
    private static final String TAG = AppComponentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_activity_debug_list);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new InfoListFragment(), "app_info").commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
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
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            Intent intent = new Intent(mActivity, ComponentListActivity.class);
            switch (key) {
                case "activity":
                    intent.putExtra("type", "activity");
                    break;
                case "service":
                    intent.putExtra("type", "service");
                    break;
                case "receiver":
                    intent.putExtra("type", "receiver");
                    break;
                case "provider":
                    intent.putExtra("type", "provider");
                    break;
                default:
                    break;
            }
            startActivity(intent);
            return true;
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
