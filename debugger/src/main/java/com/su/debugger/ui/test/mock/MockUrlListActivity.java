package com.su.debugger.ui.test.mock;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.widget.ListView;

import com.su.debugger.R;
import com.su.debugger.db.MockContentProvider;
import com.su.debugger.entity.MockResponseEntity;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.utils.LogUtil;
import com.su.debugger.utils.SearchableHelper;

public class MockUrlListActivity extends BaseAppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    public static final String TAG = MockUrlListActivity.class.getSimpleName();
    private MockAdapter mAdapter;
    private String mHost;
    private String mTitle;
    private String mQueryText = "";
    private SearchableHelper mSearchableHelper = new SearchableHelper();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_page_list_view);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mTitle = intent.getStringExtra("title");
            mHost = intent.getStringExtra("host");
        } else {
            mTitle = savedInstanceState.getString("title");
            mHost = savedInstanceState.getString("host");
        }
        mAdapter = new MockAdapter(this, null, false);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTitle);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String likeStatement = "%" + mQueryText + "%";
        String selection = "(" + MockResponseEntity.COLUMN_PATH + " like ?" +
                " OR " + MockResponseEntity.COLUMN_REQUEST_HEADERS + " like ?" +
                " OR " + MockResponseEntity.COLUMN_REQUEST_BODY + " like ?" +
                " OR " + MockResponseEntity.COLUMN_RESPONSE_HEADERS + " like ?" +
                " OR " + MockResponseEntity.COLUMN_RESPONSE + " like ?)";
        String[] selectionArgs = new String[]{likeStatement, likeStatement, likeStatement, likeStatement, likeStatement};
        if (!TextUtils.isEmpty(mHost)) {
            selection += " AND host=?";
            selectionArgs = new String[]{likeStatement, likeStatement, likeStatement, likeStatement, likeStatement, mHost};
        }
        return new CursorLoader(this, MockContentProvider.getContentUri(), MockResponseEntity.PROJECTION, selection, selectionArgs, "url ASC, method ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        LogUtil.logHeaders(data);
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public int menuRes() {
        return R.menu.debugger_search_menu;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mQueryText = s;
        getSupportLoaderManager().restartLoader(0, null, this);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (TextUtils.isEmpty(s)) {
            mQueryText = s;
            getSupportLoaderManager().restartLoader(0, null, this);
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", mTitle);
        outState.putString("host", mHost);
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
