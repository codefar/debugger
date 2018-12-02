package com.su.debugger.ui.test;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.su.debugger.AppHelper;
import com.su.debugger.DebuggerSupplier;
import com.su.debugger.R;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.IOUtil;
import com.su.debugger.utils.SearchableHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by su on 17-4-7.
 * 调试功能列表 - android - js 接口调试
 */
public class JsInterfaceTestActivity extends BaseAppCompatActivity implements SearchView.OnQueryTextListener, ExpandableListView.OnChildClickListener {

    private static final String TAG = JsInterfaceTestActivity.class.getSimpleName();
    private static final String INIT_URL = "file:///android_asset/web/html/debugger_js_interface_web.html";
    private JsInterface mAdapter;
    private ExpandableListView mListView;
    private List<Map<Integer, Integer>> mNameFilterColorIndexList = new ArrayList<>();
    private List<Map<Integer, Integer>> mDescFilterColorIndexList = new ArrayList<>();
    private List<Pair<String, List<MethodItem>>> mAllMethods = new ArrayList<>();
    private List<Pair<String, List<MethodItem>>> mFilterMethodItems = new ArrayList<>();
    private SearchableHelper mSearchableHelper = new SearchableHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_activity_js_interface_list);
        mListView = findViewById(R.id.expandable_list);
        mAdapter = new JsInterface(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(this);
        makeData();
        filter("");
        expandAll(mFilterMethodItems.size());
    }

    private String getInjectObjNameByClassname(String classname) {
        DebuggerSupplier supplier = DebuggerSupplier.getInstance();
        Map<String, Object> jsObjectMap = supplier.jsObjectList(this);
        Set<Map.Entry<String, Object>> entrySet = jsObjectMap.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            String cn = entry.getValue().getClass().getSimpleName();
            if (TextUtils.equals(cn, classname)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
        setTitle("android - js接口列表");
    }

    private void makeData() {
        AssetManager manager = getAssets();
        String[] filenames;
        try {
            filenames = manager.list("generated");
            if (filenames == null) {
                return;
            }
        } catch (IOException e) {
            Log.w(TAG, e);
            return;
        }
        for (String filename : filenames) {
            if (!filename.startsWith("JsCallAndroid-") || !filename.endsWith(".json")) {
                continue;
            }

            String classname = filename.replaceFirst("JsCallAndroid-", "").replaceFirst("\\.json", "");
            String injectName = getInjectObjNameByClassname(classname);
            if (TextUtils.isEmpty(injectName)) {
                Toast.makeText(this, "Supplier中未提供对象测试此类中的方法: " + classname, Toast.LENGTH_LONG).show();
                continue;
            }

            readData(manager, filename, injectName);
        }
    }

    private void expandAll(int groupCount) {
        for (int i=0; i< groupCount; i++) {
            mListView.expandGroup(i);
        }
    }

    private void readData(AssetManager manager, String filename, String injectName) {
        BufferedReader reader = null;
        StringBuilder buf = new StringBuilder();
        String str = null;
        try {
            reader = new BufferedReader(new InputStreamReader(manager.open("generated/" + filename), "UTF-8"));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
            str = buf.toString();
        } catch (IOException e) {
            Toast.makeText(GeneralInfoHelper.getContext(), "请检查文件" + " generated/" + filename, Toast.LENGTH_LONG).show();
        } finally {
            IOUtil.close(reader);
        }

        if (!TextUtils.isEmpty(str)) {
            List<MethodItem> list = new ArrayList<>();
            Pair<String, List<MethodItem>> filePair = new Pair<>(injectName, list);
            JSONArray array = JSON.parseArray(str);
            int size = array.size();
            for (int i = 0; i < size; i++) {
                MethodItem methodItem = new MethodItem();
                JSONObject jsonObject = array.getJSONObject(i);
                methodItem.setName(jsonObject.getString("functionName"));
                methodItem.setDesc(jsonObject.getString("description"));
                methodItem.setParameters(jsonObject.getString("parameters"));
                list.add(methodItem);
            }
            if (size > 0) {
                mAllMethods.add(filePair);
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        expandAll(mFilterMethodItems.size());
        return false;
    }

    private void filter(String str) {
        mFilterMethodItems.clear();
        mNameFilterColorIndexList.clear();
        mDescFilterColorIndexList.clear();
        if (TextUtils.isEmpty(str)) {
            mFilterMethodItems.addAll(mAllMethods);
            mAdapter.notifyDataSetChanged();
            return;
        }
        for (Pair<String, List<MethodItem>> pair : mAllMethods) {
            Pair<String, List<MethodItem>> newPair = new Pair<>(pair.first, new ArrayList<>());
            List<MethodItem> methodItems = pair.second;
            List<MethodItem> newMethodItems = newPair.second;
            boolean has = false;
            for (MethodItem search : methodItems) {
                boolean nameFind = false;
                boolean descFind = false;
                if (mSearchableHelper.isConformSplitFilter(str, search.getName(), mNameFilterColorIndexList)) {
                    nameFind = true;
                }

                if (mSearchableHelper.isConformSplitFilter(str, search.getDesc(), mDescFilterColorIndexList)) {
                    descFind = true;
                }

                if (nameFind && !descFind) {
                    mDescFilterColorIndexList.add(new HashMap<>());
                } else if (!nameFind && descFind) {
                    mNameFilterColorIndexList.add(new HashMap<>());
                }

                if (nameFind || descFind) {
                    newMethodItems.add(search);
                    if (!has) {
                        mFilterMethodItems.add(newPair);
                        has = true;
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Pair<String, List<MethodItem>> pair = mAllMethods.get(groupPosition);
        MethodItem methodItem = pair.second.get(childPosition);
        String functionName = methodItem.getName();
        String parameters = methodItem.getParameters();
        String params = "?javascriptInterfaceObjectName=" + pair.first
                + "&functionName=" + Uri.encode(functionName);
        if (!TextUtils.isEmpty(parameters)) {
            params = params + "&functionParameter=" + Uri.encode(parameters);
        }
        AppHelper.startWebView(this, "android - js接口调试", INIT_URL + params, false);
        return false;
    }

    private class JsInterface extends BaseExpandableListAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        private JsInterface(Context context) {
            this.mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return mFilterMethodItems.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mFilterMethodItems.get(groupPosition).second.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mFilterMethodItems.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mFilterMethodItems.get(groupPosition).second.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.debugger_item_function_file, parent, false);
                viewHolder = new GroupViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) convertView.getTag();
            }

            final String className = mFilterMethodItems.get(groupPosition).first;
            viewHolder.filename.setText(className);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.debugger_item_function_info, parent, false);
                viewHolder = new ItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemViewHolder) convertView.getTag();
            }

            MethodItem item = mFilterMethodItems.get(groupPosition).second.get(childPosition);
            viewHolder.name.setText(item.getName());
            if (TextUtils.isEmpty(item.getDesc())) {
                viewHolder.desc.setVisibility(View.GONE);
            } else {
                viewHolder.desc.setText(item.getDesc());
                viewHolder.desc.setVisibility(View.VISIBLE);
            }
            viewHolder.hasParameter.setText(item.isHasParameters() ? "" : "无参数");
            int position = (int) getCombinedChildId(groupPosition, childPosition);
            mSearchableHelper.refreshFilterColor(viewHolder.name, position, mNameFilterColorIndexList);
            mSearchableHelper.refreshFilterColor(viewHolder.desc, position, mDescFilterColorIndexList);
            return convertView;
        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            if (groupId == 0) {
                return childId;
            }

            int count = 0;
            for (int i = 0; i < groupId; i++) {
                Pair<String, List<MethodItem>> pair = mFilterMethodItems.get(i);
                count += pair.second.size();
            }
            count += childId;
            return count;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private static class GroupViewHolder {
        private TextView filename;

        GroupViewHolder(View view) {
            filename = view.findViewById(R.id.group_layout);
        }
    }

    private static class ItemViewHolder {
        private TextView name;
        private TextView desc;
        private TextView hasParameter;

        ItemViewHolder(View view) {
            name = view.findViewById(R.id.name);
            desc = view.findViewById(R.id.desc);
            hasParameter = view.findViewById(R.id.has_parameter);
        }
    }

    private static class MethodItem {
        private String name;
        private String parameters;
        private boolean isStatic;
        private String desc;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        String getParameters() {
            return parameters;
        }

        void setParameters(String parameters) {
            this.parameters = parameters;
        }

        boolean isHasParameters() {
            return !TextUtils.isEmpty(parameters);
        }

        public boolean isStatic() {
            return isStatic;
        }

        public void setStatic(boolean aStatic) {
            isStatic = aStatic;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @NonNull
        @Override
        public String toString() {
            return "MethodItem{" +
                    "name='" + name + '\'' +
                    ", requestBody='" + parameters + '\'' +
                    ", isStatic=" + isStatic +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

    @Override
    public int menuRes() {
        return R.menu.debugger_search_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
