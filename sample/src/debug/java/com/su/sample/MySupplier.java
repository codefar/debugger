package com.su.sample;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.su.debugger.DebuggerSupplier;
import com.su.sample.web.JsCommunication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by su on 18-1-3.
 */

public class MySupplier extends DebuggerSupplier {

    @Override
    public boolean isLogin() {
        return Math.random() * 3.0d > 1.0d;
    }

    @Override
    public String downtimeResponse(@NonNull String url) {
        return "{\n" +
                "  \"errorCode\": -100,\n" +
                "  \"errorMessage\": \"假装停机维护～～\"\n" +
                "}";
    }

    @Nullable
    public String toCookies(@NonNull String host) {
        return "uid=abc";
    }

    @NonNull
    @Override
    public Map<String, Object> jsObjectList(Activity activity) {
        Map<String, Object> map = new HashMap<>();
        map.put("JsCommunication", new JsCommunication(activity));
        return map;
    }

    @NonNull
    @Override
    public List<Pair<String, String>> allHosts() {
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("test", "https://www.baidu.com"));
        list.add(new Pair<>("release", "https://www.google.com"));
        return list;
    }

    @NonNull
    @Override
    public List<Pair<String, String>> allWebViewHosts() {
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("test", "https://www.baidu.com"));
        list.add(new Pair<>("release", "https://www.facebook.com"));
        return list;
    }

    @NonNull
    @Override
    public List<List<String>> getRequestBodyExcludeKeys() {
        List<List<String>> keys = new ArrayList<>();
        List<String> key = new ArrayList<>();
        keys.add(key);
        return keys;
    }
}
