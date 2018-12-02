package com.su.debugger.ui.test.mock;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.su.debugger.R;
import com.su.debugger.db.MockContentProvider;
import com.su.debugger.entity.MockResponseEntity;

import java.util.Set;

public class MockAdapter extends CursorAdapter implements AdapterView.OnItemClickListener {
    private Context mContext;
    private String mQueryText;

    MockAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.debugger_item_mock_url, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_ID));
        String md5 = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_MD5));
        final String url = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_URL));
        final String host = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_HOST));
        final String path = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_PATH));
        final String method = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_METHOD));
        final String contentType = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_CONTENT_TYPE));
        final String headers = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_REQUEST_HEADERS));
        final String parameters = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_REQUEST_BODY));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_DESCRIPTION));
        final int auto = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_AUTO));
        int inUse = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_IN_USE));

        final MockResponseEntity entity = new MockResponseEntity();
        entity.setId(id);
        entity.setUrl(url);
        entity.setHost(host);
        entity.setPath(path);
        entity.setMethod(method);
        entity.setContentType(contentType);
        entity.setRequestHeaders(headers);
        entity.setRequestBody(parameters);
        entity.setAuto(auto == 1);
        entity.setInUse(inUse == 1);

        Uri uri = Uri.parse(url);
        Set<String> set = uri.getQueryParameterNames();
        String queryContent = ParseHelper.makeQueryContent(uri, " ");
        if (set.isEmpty()) {
            holder.queryLayout.setVisibility(View.GONE);
        } else {
            holder.queryView.setText(queryContent);
            holder.queryLayout.setVisibility(View.VISIBLE);
        }

        String parametersContent = ParseHelper.makeParametersContent(parameters, " ");
        if (TextUtils.isEmpty(parameters)) {
            holder.requestBodyLayout.setVisibility(View.GONE);
        } else {
            holder.requestBodyView.setText(parametersContent);
            holder.requestBodyLayout.setVisibility(View.VISIBLE);
        }
        holder.hostView.setText(host);
        holder.typeView.setText(auto == 0 ? "manual" : "auto");
        if (TextUtils.isEmpty(uri.getPath())) {
            holder.pathLayout.setVisibility(View.GONE);
        } else {
            holder.pathView.setText(uri.getPath());
            holder.pathLayout.setVisibility(View.VISIBLE);
        }
        holder.methodView.setText(method);
        if (TextUtils.equals(method, "GET") || TextUtils.isEmpty(contentType)) {
            holder.contentTypeTitleView.setVisibility(View.GONE);
            holder.contentTypeView.setVisibility(View.GONE);
        } else {
            holder.contentTypeTitleView.setVisibility(View.VISIBLE);
            holder.contentTypeView.setVisibility(View.VISIBLE);
            holder.contentTypeView.setText(contentType);
        }
        if (TextUtils.isEmpty(description)) {
            holder.descLayout.setVisibility(View.GONE);
        } else {
            holder.descView.setText(description);
            holder.descLayout.setVisibility(View.VISIBLE);
        }

        holder.checkBox.setChecked(inUse == 1);
        holder.checkBox.setOnClickListener(v -> {
            ContentResolver resolver = v.getContext().getContentResolver();
            MockResponseEntity mockResponseEntity = entity.clone();
            boolean flag = mockResponseEntity.isAuto();
            mockResponseEntity.setAuto(!flag);
            boolean recorded = ParseHelper.recorded(mockResponseEntity);
            if (recorded) {
                Toast.makeText(v.getContext(), "已存在同样条件的request", Toast.LENGTH_LONG).show();
            }
            ContentValues values = new ContentValues();
            values.put("inUse", holder.checkBox.isChecked());
            resolver.update(ContentUris.withAppendedId(MockContentProvider.CONTENT_URI, id), values, null, null);
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        MockResponseEntity entity = new MockResponseEntity();
        final int entityId = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_ID));
        String md5 = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_MD5));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_URL));
        String host = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_HOST));
        String path = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_PATH));
        String method = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_METHOD));
        String contentType = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_CONTENT_TYPE));
        String headers = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_REQUEST_HEADERS));
        String parameters = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_REQUEST_BODY));
        String responseHeaders = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_RESPONSE_HEADERS));
        String response = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_RESPONSE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_DESCRIPTION));
        int inUse = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_IN_USE));
        int auto = cursor.getInt(cursor.getColumnIndexOrThrow(MockResponseEntity.COLUMN_AUTO));
        entity.setId(entityId);
        entity.setMd5(md5);
        entity.setUrl(url);
        entity.setHost(host);
        entity.setPath(path);
        entity.setMethod(method);
        entity.setContentType(contentType);
        entity.setRequestHeaders(headers);
        entity.setRequestBody(parameters);
        entity.setResponseHeaders(responseHeaders);
        entity.setResponse(response);
        entity.setDescription(description);
        entity.setInUse(inUse == 1);
        entity.setAuto(auto == 1);
        Intent intent = new Intent(mContext, MockDetailActivity.class);
        intent.putExtra("mockEntity", entity);
        mContext.startActivity(intent);
    }

    public void updateQueryText(String queryText) {
        mQueryText = queryText;
    }

    private static class ViewHolder {

        private TextView hostView;
        private TextView typeView;
        private TextView pathView;
        private View pathLayout;
        private View queryLayout;
        private TextView queryView;
        private TextView requestBodyView;
        private View requestBodyLayout;
        private TextView methodView;
        private TextView contentTypeTitleView;
        private TextView contentTypeView;
        private TextView descView;
        private View descLayout;
        private AppCompatCheckBox checkBox;

        private ViewHolder(View itemView) {
            hostView = itemView.findViewById(R.id.host);
            typeView = itemView.findViewById(R.id.type);
            pathView = itemView.findViewById(R.id.path);
            pathLayout = itemView.findViewById(R.id.path_layout);
            queryView = itemView.findViewById(R.id.query);
            queryLayout = itemView.findViewById(R.id.query_layout);
            requestBodyView = itemView.findViewById(R.id.request_body);
            requestBodyLayout = itemView.findViewById(R.id.request_body_layout);
            methodView = itemView.findViewById(R.id.method);
            contentTypeTitleView = itemView.findViewById(R.id.content_type_title);
            contentTypeView = itemView.findViewById(R.id.content_type);
            descView = itemView.findViewById(R.id.desc);
            descLayout = itemView.findViewById(R.id.desc_layout);
            checkBox = itemView.findViewById(R.id.check_box);
        }
    }
}
