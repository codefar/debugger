package com.su.debugger.ui.test.mock;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.Toast;

import com.su.debugger.R;
import com.su.debugger.db.MockContentProvider;
import com.su.debugger.entity.MockResponseEntity;
import com.su.debugger.ui.test.BaseAppCompatActivity;

public class MockGroupHostActivity extends BaseAppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = MockGroupHostActivity.class.getSimpleName();
    private String mHostListTitle;
    private GroupHostAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mHostListTitle = intent.getStringExtra("title");
        } else {
            mHostListTitle = savedInstanceState.getString("title");
        }
        setContentView(R.layout.debugger_template_page_list_view);
        mAdapter = new GroupHostAdapter(this, null, false);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mAdapter);
        listView.setOnItemLongClickListener(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Mock数据分组");
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new CursorLoader(this, MockContentProvider.HOST_URI,
                //CursorAdapter要求cursor必须包含_id字段
                new String[]{MockResponseEntity.COLUMN_HOST + " as _id", MockResponseEntity.COLUMN_HOST, "count(host)"},
                null,
                null,
                "host ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int count;
        if (cursor == null) {
            count = 0;
        } else {
            count = cursor.getCount();
        }
        if (count == 1) {
            cursor.moveToFirst();
            final String host = cursor.getString(1);
            Intent newFakeIntent = new Intent(this, MockUrlListActivity.class);
            newFakeIntent.putExtra("title", host);
            startActivity(newFakeIntent);
            finish();
        } else if (count == 0) { //删除之后没有模拟数据
            Toast.makeText(this, "没有找到mock数据", Toast.LENGTH_LONG).show();
            finish();
        } else {
            mAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", mHostListTitle);
    }

    public String getHostListTitle() {
        return mHostListTitle;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
