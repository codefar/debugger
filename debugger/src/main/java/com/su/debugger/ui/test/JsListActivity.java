package com.su.debugger.ui.test;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.su.debugger.AppHelper;
import com.su.debugger.Debugger;
import com.su.debugger.R;
import com.su.debugger.entity.JsFunction;
import com.su.debugger.entity.NoteJsFunction;
import com.su.debugger.utils.IOUtil;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by su on 17-10-24.
 * 调试rhino
 */

public class JsListActivity extends BaseAppCompatActivity implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener, View.OnClickListener {
    private static final String TAG = JsListActivity.class.getSimpleName();

    private File mJsDIr = new File(Debugger.getDebuggerSdcardDir(), "js");
    private BottomSheetBehavior mBehavior;
    private TextView mJsFileNameView;
    private EditText mJsContentView;
    private ExpandableListView mListView;
    private List<String> groupList = new ArrayList<>();
    private List<List<JsFunction>> itemLists = new ArrayList<>();
    private JsAdapter mJsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_activity_js_list);
        if (!mJsDIr.exists()) {
            mJsDIr.mkdirs();
        }
        mListView = findViewById(R.id.expandable_list);
        View bottomSheet = findViewById(R.id.bottomSheet);
        mJsFileNameView = findViewById(R.id.js_file_name);
        mJsContentView = findViewById(R.id.content);
        findViewById(R.id.close).setOnClickListener(this);
        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View view, int state) {
                if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_HIDDEN) {
                    File file = new File((String) mJsFileNameView.getTag());
                    createOrUpdateJs(file.getAbsolutePath(), mJsContentView.getText().toString());
                    loadFiles();
                    mJsAdapter.notifyDataSetChanged();
                    AppHelper.hideSoftInputFromWindow(getWindow());
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {}
        });
        loadFiles();
        mJsAdapter = new JsAdapter(this);
        mListView.setAdapter(mJsAdapter);
        mListView.setOnGroupClickListener(this);
        mListView.setOnChildClickListener(this);
        expandAll(groupList.size());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Js函数列表");
    }

    private void expandAll(int groupCount) {
        for (int i=0; i< groupCount; i++) {
            mListView.expandGroup(i);
        }
    }

    private void loadFiles() {
        groupList.clear();
        itemLists.clear();
        if (mJsDIr.listFiles() == null) {
            return;
        }
        List<File> files = new ArrayList<>(Arrays.asList(mJsDIr.listFiles()));
        String data = IOUtil.readAssetsFile(this, "generated/js.json");
        if (!TextUtils.isEmpty(data)) {
            List<NoteJsFunction> list = JSON.parseArray(data, NoteJsFunction.class);
            for (NoteJsFunction noteJsFunction : list) {
                String url = noteJsFunction.getJsFilepath().getFilepath();
                String sourceString = null;
                String filepath = "";
                if (URLUtil.isAssetUrl(url)) {
                    filepath = IOUtil.getAssetFilePath(url);
                    sourceString = IOUtil.readAssetsFile(this, filepath);
                } else if (URLUtil.isFileUrl(url)) {
                    try {
                        File file = new File(new URI(url));
                        filepath = file.getAbsolutePath();
                        sourceString = IOUtil.readFile(file);
                    } catch (URISyntaxException e) {
                        Log.w(TAG, e);
                    }
                } else if (URLUtil.isNetworkUrl(url)) {
                    sourceString = "";
                }
                if (TextUtils.isEmpty(sourceString)) {
                    Toast.makeText(this, "请检查文件: " + filepath, Toast.LENGTH_LONG).show();
                } else {
                    excludeJsFiles(files, new File(filepath));
                    parseJs(sourceString, url);
                }
            }
        }

        if (files.isEmpty()) {
            return;
        }

        String sourceString;
        for (File file : files) {
            sourceString = IOUtil.readFile(file);
            parseJs(sourceString, Uri.fromFile(file).toString());
        }
    }

    private void excludeJsFiles(List<File> files, File fromAnnotations) {
        if (files == null || files.isEmpty()) {
            return;
        }

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            try {
                if (IOUtil.isSameFile(file, fromAnnotations)) {
                    iterator.remove();
                    return;
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }

    @Override
    public void onClick(View v) {
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        String filepath = groupList.get(groupPosition);
        JsFunction jsFunction = itemLists.get(groupPosition).get(childPosition);
        Intent intent = new Intent(this, ExecJsActivity.class);
        intent.putExtra("filepath", filepath);
        intent.putExtra("function", jsFunction);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        return itemLists.get(groupPosition).isEmpty();
    }

    private class JsAdapter extends BaseExpandableListAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        private JsAdapter(Context context) {
            this.mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return itemLists.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return itemLists.get(groupPosition).get(childPosition);
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
                convertView = mInflater.inflate(R.layout.debugger_item_group_js_file, parent, false);
                viewHolder = new GroupViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) convertView.getTag();
            }

            final String filepath = groupList.get(groupPosition);
            viewHolder.filenameView.setText(new File(filepath).getName());
            viewHolder.openView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "filepath: " + filepath, Toast.LENGTH_SHORT).show();
                    mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    try {
                        String content = "";
                        if (URLUtil.isAssetUrl(filepath)) {
                            String realFilePath = IOUtil.getAssetFilePath(filepath);
                            content = IOUtil.readAssetsFile(mContext, realFilePath);
                            mJsContentView.setEnabled(false);
                        } else if (URLUtil.isFileUrl(filepath)) {
                            try {
                                File file = new File(new URI(filepath));
                                content = IOUtil.readFile(file);
                                mJsContentView.setEnabled(true);
                            } catch (URISyntaxException e) {
                                Log.w(TAG, e);
                            }
                        }
                        File file = new File(new URI(filepath));
                        mJsFileNameView.setText(file.getName());
                        mJsFileNameView.setTag(file.getAbsolutePath());
                        mJsContentView.setText(content);
                    } catch (URISyntaxException e) {
                        Log.w(TAG, e);
                    }
                }
            });
            viewHolder.deleteView.setVisibility(URLUtil.isAssetUrl(filepath) ? View.GONE : View.VISIBLE);
            viewHolder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteFileDialog(groupPosition, filepath);
                }
            });
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.debugger_item_js_function, parent, false);
                viewHolder = new ItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemViewHolder) convertView.getTag();
            }

            JsFunction jsFunction = (JsFunction) getChild(groupPosition, childPosition);
            viewHolder.functionNameView.setText(jsFunction.getName());
            String parameters = jsFunction.getParametersString();
            if (TextUtils.isEmpty(parameters)) {
                viewHolder.parametersView.setText("无参数");
            } else {
                viewHolder.parametersView.setText(parameters);
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private static class GroupViewHolder {
        private TextView filenameView;
        private Button openView;
        private Button deleteView;

        private GroupViewHolder(View view) {
            this.filenameView = view.findViewById(R.id.filename);
            this.openView = view.findViewById(R.id.open);
            this.deleteView = view.findViewById(R.id.delete);
        }
    }

    private static class ItemViewHolder {
        private TextView functionNameView;
        private TextView parametersView;

        private ItemViewHolder(View view) {
            this.functionNameView = view.findViewById(R.id.function_name);
            this.parametersView = view.findViewById(R.id.parameters);
        }
    }

    private void parseJs(String sourceString, String sourceUri) {
        if (TextUtils.isEmpty(sourceString)) {
            groupList.add(sourceUri);
            itemLists.add(new ArrayList<>());
            Log.d(TAG, "functions: remote js file. load later.");
        } else {
            AstNode node = new Parser().parse(sourceString, sourceUri, 0);
            FunctionNodeVisitor visitor = new FunctionNodeVisitor(sourceUri);
            node.visit(visitor);
            groupList.add(sourceUri);
            itemLists.add(visitor.getJsFunctions());
            Log.d(TAG, "functions: " + visitor.getJsFunctions());
        }
    }

    private static class FunctionNodeVisitor implements NodeVisitor {
        private String filepath;
        private List<JsFunction> jsFunctionList = new ArrayList<>();

        private FunctionNodeVisitor(@NonNull String filepath) {
            this.filepath = filepath;
        }

        @Override
        public boolean visit(AstNode node) {
            if (node instanceof FunctionCall) {
                // How do I get the name of the function being called?
                FunctionCall functionCall = (FunctionCall) node;
                Log.d(TAG, "functionCall: " + functionCall);
            } else if (node instanceof FunctionNode) {
                FunctionNode functionNode = (FunctionNode) node;
                List<AstNode> params = functionNode.getParams();
                int size = params.size();
                Log.d(TAG, "functionNode: " + functionNode);
                if (functionNode.getFunctionName() == null) {
                    //匿名函数
                    if (params.isEmpty()) {
                        Log.w(TAG, "no params.");
                    } else {
                        for (int i = 0; i < size; i++) {
                            Name name = (Name) params.get(i);
                            Log.d(TAG, "params[" + i + "]: " + name.getIdentifier());
                        }
                    }
                    return true;
                }

                String functionName = functionNode.getFunctionName().getIdentifier();
                List<String> paramNames = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    paramNames.add(((Name) params.get(i)).getIdentifier());
                }
                JsFunction jsFunction = new JsFunction(functionName, paramNames);
                jsFunctionList.add(jsFunction);
            }
            return true;
        }

        public String getFilepath() {
            return filepath;
        }

        String getFileName() {
            return new File(filepath).getName();
        }

        List<JsFunction> getJsFunctions() {
            return jsFunctionList;
        }

        @NonNull
        @Override
        public String toString() {
            return "FunctionNodeVisitor{" +
                    "filepath='" + filepath + '\'' +
                    ", jsFunctionList=" + jsFunctionList +
                    '}';
        }
    }

    @Override
    public void onBackPressed() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    private void showDeleteFileDialog(final int groupPosition, String filepath) {
        final File file = new File(Uri.parse(filepath).getPath());
        new AlertDialog.Builder(this)
                .setMessage("确定要将" + file.getName() + "删除吗")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.delete()) {
                            groupList.remove(groupPosition);
                            itemLists.remove(groupPosition);
                            mJsAdapter.notifyDataSetChanged();
                            Toast.makeText(JsListActivity.this, file.getName() + "删除成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(JsListActivity.this, file.getName() + "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    public void add(@NonNull MenuItem item) {
        final View v = LayoutInflater.from(this).inflate(R.layout.debugger_dialog_new_js_file, null);
        final EditText titleView = v.findViewById(R.id.title);
        final EditText contentView = v.findViewById(R.id.content);
        new AlertDialog.Builder(this)
                .setTitle("创建js文件")
                .setView(v)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryCreateJsFile(titleView, contentView);
                    }
                })
                .show();
    }

    private void tryCreateJsFile(final EditText titleView, final EditText contentView) {
        String fileName = titleView.getText().toString();
        if (!fileName.endsWith(".js")) {
            fileName += ".js";
        }
        File file = new File(mJsDIr, fileName);
        if (file.exists()) {
            new AlertDialog.Builder(JsListActivity.this)
                    .setMessage("同名js文件已存在，是否要覆盖现有js文件？")
                    .setPositiveButton("覆盖", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createOrUpdateJs(file.getAbsolutePath(), contentView.getText().toString());
                            loadFiles();
                            mJsAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("", null)
                    .show();
            return;
        }
        createOrUpdateJs(file.getAbsolutePath(), contentView.getText().toString());
        loadFiles();
        mJsAdapter.notifyDataSetChanged();
    }

    private void createOrUpdateJs(String filepath, String content) {
        IOUtil.writeFile(filepath, content);
    }

    @Override
    public int menuRes() {
        return R.menu.debugger_add_menu;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
