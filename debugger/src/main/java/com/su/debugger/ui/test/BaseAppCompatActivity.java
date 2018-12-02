package com.su.debugger.ui.test;

import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;

import com.su.debugger.R;
import com.su.debugger.ui.test.web.WebViewActivity;
import com.su.debugger.utils.UiHelper;

/**
 * Created by su on 18-1-2.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    public void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleRes) {
        setTitle(getString(titleRes));
    }

    protected abstract String getTag();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupToolbar();
    }

    private void setupToolbar() {
        mToolbar = findViewById(R.id.id_toolbar);
        //抢标页面/转让页面等没有toolbar
        if (mToolbar != null) {
            int menuRes = menuRes();
            if (menuRes != 0) {
                mToolbar.inflateMenu(menuRes);
                prepareOptionsMenu(mToolbar.getMenu());
            }
            mToolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
            mToolbar.setNavigationOnClickListener(v -> popStackIfNeeded(BaseAppCompatActivity.this));
        }
    }

    @MenuRes
    public int menuRes() {
        return 0;
    }

    public void prepareOptionsMenu(Menu menu) {}

    private static void popStackIfNeeded(BaseAppCompatActivity activity) {
        int count = activity.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (activity instanceof WebViewActivity) {
                activity.finish();
            } else {
                activity.onBackPressed();
            }
        } else {
            activity.getSupportFragmentManager().popBackStack();
        }
    }

    protected int getStatusBarColor() {
        return UiHelper.getThemeColorByAttrId(getTheme(), R.attr.colorPrimaryDark);
    }

    protected boolean isAddStatusBarView() {
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mToolbar != null) {
                mToolbar.showOverflowMenu();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
