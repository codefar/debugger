package com.su.debugger.ui.test.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.MenuRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.su.debugger.R;
import com.su.debugger.entity.NoteComponentEntity;
import com.su.debugger.entity.Parameter;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.widget.SimpleOnTabSelectedListener;

import java.util.List;

/**
 * Created by su on 17-5-27.
 * 调试功能列表 - 应用信息 - 四大组件详情
 */
public class ComponentActivity extends BaseAppCompatActivity {

    private static final String TAG = ComponentActivity.class.getSimpleName();
    private String mType;
    private ComponentInfo mComponentInfo;
    private NoteComponentEntity mNoteComponent;
    private ComponentName mComponentName;
    private ComponentParametersFragment mComponentParametersFragment;
    private ViewPager mPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_activity_component_info);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mType = intent.getStringExtra("type");
            mComponentInfo = intent.getParcelableExtra("info");
            mNoteComponent = intent.getParcelableExtra("note");
        } else {
            mType = savedInstanceState.getString("type");
            mComponentInfo = savedInstanceState.getParcelable("info");
            mNoteComponent = savedInstanceState.getParcelable("note");
        }
        mComponentName = new ComponentName(mComponentInfo.packageName, mComponentInfo.name);
        if (mNoteComponent == null) {
            mNoteComponent = new NoteComponentEntity();
        }

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new InfoPagerAdapter(getSupportFragmentManager()));
        mTabLayout = findViewById(R.id.tab_layout);
        if (mNoteComponent.getParameters().isEmpty()) {
            mTabLayout.setVisibility(View.GONE);
        } else {
            mTabLayout.setVisibility(View.VISIBLE);
        }
    }

    private TabLayout.Tab makeTab(String title) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(title);
        return tab;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        switch (mType) {
            case "activity":
                setTitle("Activity详情");
                break;
            case "service":
                setTitle("Service详情");
                break;
            case "receiver":
                setTitle("Receiver详情");
                break;
            case "provider":
                setTitle("Provider详情");
                break;
            default:
                break;
        }
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mPager.setCurrentItem(position);
                if (!"activity".equalsIgnoreCase(mType) && !"service".equalsIgnoreCase(mType)) {
                    return;
                }
                Menu menu = mToolbar.getMenu();
                MenuItem format = menu.findItem(R.id.format);
                MenuItem go = menu.findItem(R.id.go);
                if (position == 1) {
                    format.setVisible(!mNoteComponent.getParameters().isEmpty());
                    go.setVisible(true);
                } else if (mNoteComponent.getParameters().isEmpty()) {
                    format.setVisible(false);
                    go.setVisible(true);
                } else {
                    format.setVisible(false);
                    go.setVisible(false);
                }
            }
        });
        mTabLayout.addTab(makeTab("组件信息"));
        if (!mNoteComponent.getParameters().isEmpty()) {
            mTabLayout.addTab(makeTab("参数列表"));
        }
    }

    private class InfoPagerAdapter extends FragmentPagerAdapter {
        InfoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return ComponentInfoFragment.newInstance(mType, mNoteComponent, mComponentInfo);
            } else {
                mComponentParametersFragment = ComponentParametersFragment.newInstance(mNoteComponent);
                return mComponentParametersFragment;
            }
        }

        @Override
        public int getCount() {
            return mNoteComponent.getParameters().isEmpty() ? 1 : 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "组件信息";
            } else {
                return "参数列表";
            }
        }
    }

    @MenuRes
    @Override
    public int menuRes() {
        if ("activity".equalsIgnoreCase(mType) || "service".equalsIgnoreCase(mType)) {
            return R.menu.debugger_activity_parameters_menu;
        }
        return 0;
    }

    //格式化当前EditText中的参数
    public void format(MenuItem item) {
        mComponentParametersFragment.format();
        if (mPager.getCurrentItem() != 1) {
            mPager.setCurrentItem(1);
        }
    }

    public void go(MenuItem item) {
        List<Parameter> parameters = mNoteComponent.getParameters();
        Intent intent;
        if (parameters.isEmpty()) {
            intent = new Intent();
        } else {
            if (mComponentParametersFragment.checkRequired()) {
                intent = mComponentParametersFragment.makeIntent();
            } else {
                if (mPager.getCurrentItem() != 1) {
                    mPager.setCurrentItem(1);
                }
                return;
            }
        }
        intent.setComponent(mComponentName);
        if ("activity".equalsIgnoreCase(mType)) {
            startActivity(intent);
        } else if ("service".equalsIgnoreCase(mType)) {
            startService(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("info", (Parcelable) mComponentInfo);
        outState.putParcelable("note", mNoteComponent);
        outState.putString("type", mType);
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
