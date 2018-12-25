package com.su.debugger;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebuggerSupplier {

    public boolean isLogin() {
        return false;
    }

    @Nullable
    public String downtimeResponse(@NonNull String url) {
        return "";
    }

    @NonNull
    public Map<String, Object> jsObjectList(Activity activity) {
        return new HashMap<>();
    }

    @NonNull
    public List<Pair<String, String>> allHosts() {
        return new ArrayList<>();
    }

    @NonNull
    public List<List<String>> getRequestBodyExcludeKeys() {
        return new ArrayList<>();
    }

    @NonNull
    public String urlMapping(@NonNull String url, @NonNull String newHost) {
        return url;
    }

    @Nullable
    public byte[] toPostData(@Nullable String content) {
        return null;
    }

    @Nullable
    public String toCookies(@NonNull String host) {
        return null;
    }

    public static DebuggerSupplier getInstance() {
        return null;
    }
}
