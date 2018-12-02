package com.su.debugger.ui.test;

import android.os.Bundle;

import com.su.debugger.R;

/**
 * Created by mahao on 17-4-10.
 * 调试功能列表
 */
public class DebuggerMainActivity extends BaseAppCompatActivity {
    private static final String TAG = DebuggerMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_activity_debug_list);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new DebugListFragment(), "debug_list")
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("调式功能列表");
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
