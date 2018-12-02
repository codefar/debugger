package com.su.debugger.ui.test.web;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.su.debugger.R;
import com.su.debugger.entity.NoteWebViewEntity;
import com.su.debugger.ui.test.BaseAppCompatActivity;

/**
 * Created by su on 2018/1/10.
 */
public class WebViewActivity extends BaseAppCompatActivity {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    private String mTitle;
    private String mUrl;
    private boolean mSharable;
    private boolean mClearable;

    private WebViewFragment mWebViewFragment;
    private NoteWebViewEntity mEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (savedInstanceState == null) {
            mUrl = intent.getStringExtra("url");
            mTitle = intent.getStringExtra("title");
            mSharable = intent.getBooleanExtra("sharable", false);
            mClearable = intent.getBooleanExtra("clearable", false);
            mEntity = intent.getParcelableExtra("entity");
        } else {
            mUrl = savedInstanceState.getString("url");
            mTitle = savedInstanceState.getString("title");
            mSharable = savedInstanceState.getBoolean("sharable");
            mClearable = savedInstanceState.getBoolean("clearable");
            mEntity = savedInstanceState.getParcelable("entity");
        }
        if (mEntity != null) {
            mUrl = mEntity.getUrl();
            mTitle = mEntity.getTitle();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_activity_webview);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("web_view_fragment");
        if (fragment != null) {
            mWebViewFragment = (WebViewFragment) fragment;
        } else {
            if (mEntity == null) {
                mWebViewFragment = WebViewFragment.newInstance(mTitle, mUrl, mSharable);
            } else {
                mWebViewFragment = WebViewFragment.newInstance(mEntity, mSharable);
            }
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(R.id.container, mWebViewFragment, "web_view_fragment");
            transaction.commit();
        }

        initDebug();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTitle);
        Menu menu = mToolbar.getMenu();
        menu.findItem(R.id.share).setVisible(mSharable);
        menu.findItem(R.id.clean_up).setVisible(mClearable);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", mUrl);
        outState.putString("title", mTitle);
        outState.putBoolean("sharable", mSharable);
        outState.putBoolean("clearable", mClearable);
        outState.putParcelable("entity", mEntity);
    }

    @Override
    public int menuRes() {
        return R.menu.debugger_webview_menu;
    }

    public void share(MenuItem menuItem) {
        WebView webView = mWebViewFragment.getWebView();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, webView.getOriginalUrl());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "分享当前地址"));
    }

    public void cleanUp(MenuItem menuItem) {
        mWebViewFragment.clearWebViewCache();
    }

    @Override
    public void onBackPressed() {
        mWebViewFragment.onBackPressed();
    }

    private void initDebug() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
