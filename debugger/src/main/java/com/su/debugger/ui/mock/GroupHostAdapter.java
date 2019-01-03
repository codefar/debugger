package com.su.debugger.ui.mock;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.su.debugger.R;
import com.su.debugger.database.MockContentProvider;
import com.su.debugger.entity.MockResponseEntity;

public class GroupHostAdapter extends CursorAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private MockGroupHostActivity mActivity;

    GroupHostAdapter(MockGroupHostActivity activity, Cursor c, boolean autoRequery) {
        super(activity, c, autoRequery);
        mActivity = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.debugger_item_mock_group_host, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String host = cursor.getString(1);
        int count = cursor.getInt(2);
        holder.hostView.setText(host);
        holder.countView.setText(String.valueOf(count));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String host = cursor.getString(1);
        Intent intent = new Intent(mActivity, MockUrlListActivity.class);
        intent.putExtra("title", mActivity.getHostListTitle());
        intent.putExtra("host", host);
        mActivity.startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        final String host = cursor.getString(1);
        new AlertDialog.Builder(mActivity)
                .setMessage("确定要将" + host + "下的所有数据删除吗？")
                .setPositiveButton(R.string.debugger_confirm, (dialog, which) -> {
                    deleteByHost(host);
                })
                .setNegativeButton(R.string.debugger_cancel, null)
                .show();
        return true;
    }

    private boolean deleteByHost(String host) {
        ContentResolver resolver = mActivity.getContentResolver();
        int count = resolver.delete(MockContentProvider.getContentUri(), MockResponseEntity.COLUMN_HOST + "=?", new String[]{host});
        return count > 0;
    }

    private static class ViewHolder {
        private TextView hostView;
        private TextView countView;

        private ViewHolder(View itemView) {
            hostView = itemView.findViewById(R.id.host);
            countView = itemView.findViewById(R.id.count);
        }
    }
}
