package com.su.debugger.ui.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.su.debugger.R;
import com.su.debugger.entity.NoteComponentEntity;
import com.su.debugger.widget.recycler.BaseRecyclerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 17-12-25.
 */

public class ComponentFlagsFragment extends Fragment {

    private static final String TAG = ComponentFlagsFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private FlagAdapter mFlagAdapter;
    private FragmentActivity mActivity;
    private NoteComponentEntity mNoteComponent;
    private List<Flag> mOriginFlagList = new ArrayList<>();
    private int mFlags;

    static ComponentFlagsFragment newInstance(NoteComponentEntity noteComponent) {
        ComponentFlagsFragment fragment = new ComponentFlagsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("note", noteComponent);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            mNoteComponent = bundle.getParcelable("note");
        } else {
            mNoteComponent = savedInstanceState.getParcelable("note");
        }

        if (mNoteComponent == null) {
            mNoteComponent = new NoteComponentEntity();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("note", mNoteComponent);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.debugger_template_recycler_view, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        List<Flag> data = getFlagList();
        initFlagStates(data, mNoteComponent.getFlags());
        initOriginFlags(data);
        mFlagAdapter = new FlagAdapter(this, data);
        mRecyclerView.setAdapter(mFlagAdapter);
        return mRecyclerView;
    }

    private static List<Flag> getFlagList() {
        List<Flag> list = new ArrayList<>();
        Class<Intent> clazz = Intent.class;
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (!field.getName().startsWith("FLAG_ACTIVITY_")) {
                    continue;
                }
                Integer value = (Integer) field.get(null);
                if (value == null) {
                    continue;
                }
                list.add(new Flag(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            Log.w(TAG, e);
        }
        return list;
    }

    private void initFlagStates(@NonNull List<Flag> data, int flags) {
        for (Flag flag : data) {
            if ((flag.value & flags) == flag.value) {
                flag.checked = true;
            }
        }
    }

    private void initOriginFlags(@NonNull List<Flag> data) {
        for (Flag flag : data) {
            if (flag.checked) {
                mOriginFlagList.add(new Flag(flag));
                mFlags |= flag.value;
            }
        }
    }

    int getFlags() {
        return mFlags;
    }

    private static class FlagAdapter extends BaseRecyclerAdapter<Flag> {

        private ComponentFlagsFragment mFragment;

        private FlagAdapter(@NonNull ComponentFlagsFragment fragment, @NonNull List<Flag> data) {
            super(data);
            mFragment = fragment;
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.debugger_item_intent_activity_flag;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            final Flag flag = getData().get(position);
            TextView flagNameView = holder.getView(R.id.flag_name);
            final CheckBox checkBox = holder.getView(R.id.check_box);
            flagNameView.setText(flag.name);
            checkBox.setChecked(flag.checked);
            checkBox.setOnClickListener(v -> {
                flag.checked = checkBox.isChecked();
                if (flag.checked) {
                    mFragment.mFlags |= flag.value;
                } else {
                    mFragment.mFlags = mFragment.mFlags & ~flag.value;
                }
                Log.d(TAG, "flags: " + mFragment.mFlags);
            });
        }
    }

    private static class Flag {
        private String name;
        private int value;
        private boolean checked;

        private Flag(@NonNull String name, int value) {
            this.name = name;
            this.value = value;
        }

        private Flag(@NonNull Flag src) {
            name = src.name;
            value = src.value;
            checked = src.checked;
        }
    }
}
