package com.su.debugger.ui.test.web;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.su.debugger.AppHelper;
import com.su.debugger.BuildConfig;
import com.su.debugger.DebuggerSupplier;
import com.su.debugger.R;
import com.su.debugger.entity.NoteWebViewEntity;
import com.su.debugger.entity.SimpleParameter;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.widget.refresh.SwipeRefreshLayout;
import com.su.debugger.widget.refresh.SwipeRefreshLayoutDirection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by su on 15-4-7.
 */
public class WebViewFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = WebViewFragment.class.getSimpleName();
    private View mLoadingErrorLayout;
    private String mTitle;
    private String mUrl;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WebView mWebView;
    private BaseAppCompatActivity mActivity;
    private NoteWebViewEntity mEntity;

    public void loadJs(final String jsMethod, final String... params) {
        if (mWebView == null) {
            Log.d(TAG, "webView = null");
            return;
        }

        mWebView.post(() -> {
            String url;
            if (params == null || params.length == 0) {
                url = "javascript:" + jsMethod + "()";
            } else {
                StringBuilder sb = new StringBuilder();
                for (String p : params) {
                    sb.append("'" + p + "',");
                }
                sb.deleteCharAt(sb.length() - 1);
                url = "javascript:" + jsMethod + "(" + sb + ")";
            }
            mWebView.loadUrl(url);
            Log.d(TAG, "loadJs: " + url);
        });
    }

    public static WebViewFragment newInstance(String title, String url, boolean sharable) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle b = new Bundle();
        b.putBoolean("sharable", sharable);
        b.putString("title", title);
        b.putString("url", url);
        fragment.setArguments(b);
        return fragment;
    }

    public static WebViewFragment newInstance(NoteWebViewEntity entity, boolean sharable) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle b = new Bundle();
        b.putBoolean("sharable", sharable);
        b.putParcelable("entity", entity);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseAppCompatActivity) getActivity();
        Bundle b = getArguments();
        if (savedInstanceState == null) {
            mTitle = b.getString("title");
            mUrl = b.getString("url");
            mEntity = b.getParcelable("entity");
        } else {
            mUrl = savedInstanceState.getString("url");
            mTitle = savedInstanceState.getString("title");
            mEntity = savedInstanceState.getParcelable("entity");
        }

        if (mEntity != null) {
            mUrl = mEntity.getUrl();
            mTitle = mEntity.getTitle();
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.debugger_fragment_webview, container, false);
        mSwipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mWebView = v.findViewById(R.id.web_view);

        if (!verifyUrl(mUrl)) {
            String toast = "跳转链接错误！";
            toast += mUrl;
            Toast.makeText(mActivity, toast, Toast.LENGTH_LONG).show();
            mActivity.finish();
        }

        updateWebViewActivityUrl();

        mLoadingErrorLayout = v.findViewById(R.id.load_error_layout);
        mLoadingErrorLayout.setOnClickListener(this);

        initWebViewSettings();
        DebuggerSupplier supplier = DebuggerSupplier.getInstance();
        Map<String, Object> jsObjectMap = supplier.jsObjectList(mActivity);
        for (Map.Entry<String, Object> entry : jsObjectMap.entrySet()) {
            mWebView.addJavascriptInterface(entry.getValue(), entry.getKey());
        }
        removeAdditionalJavascriptInterface();
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mSwipeRefreshLayout.finishRefreshing();
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //当前url与原始url一致时，同视为原始加载页面
                //优先使用配置中的title
                String webViewUrl = view.getUrl().replaceAll("/$", "");
                String url = mUrl.replaceAll("/$", "");
                if (TextUtils.equals(webViewUrl, url)) {
                    if (TextUtils.isEmpty(mTitle)) {
                        mActivity.setTitle(title);
                    } else {
                        mActivity.setTitle(mTitle);
                    }
                } else {
                    if (!TextUtils.isEmpty(title)) {
                        mActivity.setTitle(title);
                    }
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.stopLoading();
                view.clearView();
                mSwipeRefreshLayout.finishRefreshing();
                mLoadingErrorLayout.setVisibility(View.VISIBLE);
                Log.w(TAG, "failingUrl: " + failingUrl + " errorCode: " + errorCode + " description: " + description);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!verifyUrl(url)) {
                    Log.w(TAG, "url错误: " + url);
                    Toast.makeText(mActivity, "url错误: " + url, Toast.LENGTH_LONG).show();
                    return true;
                }
                mUrl = url;
                addCookie(url);
                Log.d(TAG, "newUrl: " + mUrl);
                updateWebViewActivityUrl();
                mSwipeRefreshLayout.startRefreshing();
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    toastHttpError(request, errorResponse);
                }
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            private void toastHttpError(WebResourceRequest request, WebResourceResponse errorResponse) {
                Toast.makeText(mActivity, "HTTP error: " + errorResponse.getStatusCode() + "\n url: " + request.getUrl(), Toast.LENGTH_LONG).show();
            }
        });

        mLoadingErrorLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.startRefreshing();
            loadUrl();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        return v;
    }

    private void loadUrl() {
        addCookie(mUrl);
        if (mEntity == null) {
            mWebView.loadUrl(mUrl);
            return;
        }
        String method = mEntity.getMethod();
        if (TextUtils.equals("POST", method)) {
            postUrl();
        } else {
            getUrl();
        }
        toastMockData();
    }

    public void addCookie(String url) {
        CookieSyncManager.createInstance(mActivity);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        DebuggerSupplier debugger = DebuggerSupplier.getInstance();
        String cookie = null;
        if (host != null) {
            cookie = debugger.toCookies(host);
        }
        cookieManager.setCookie(url, cookie);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "url: " + url + " cookie: " + cookieManager.getCookie(url));
        }
        CookieSyncManager.getInstance().sync();
    }

    private void postUrl() {
        DebuggerSupplier debugger = DebuggerSupplier.getInstance();
        mWebView.postUrl(mUrl, debugger.toPostData(mEntity.getPostContent()));
    }

    private void getUrl() {
        List<SimpleParameter> requestHeaders = mEntity.getRequestHeaders();
        Map<String, String> requestHeadersMap = new HashMap<>();
        for (SimpleParameter parameter : requestHeaders) {
            String key = parameter.getKey();
            if (requestHeadersMap.containsKey(key)) {
                Log.e(TAG, "duplicate key found: " + key);
            }
            requestHeadersMap.put(key, parameter.getValue());
        }

        StringBuilder stringBuilder = new StringBuilder();
        List<SimpleParameter> parameters = mEntity.getParameters();
        for (SimpleParameter parameter : parameters) {
            stringBuilder.append(parameter.getKey());
            stringBuilder.append("=");
            stringBuilder.append(AppHelper.encodeUrlString(parameter.getValue()));
            stringBuilder.append("&");
        }
        if (!parameters.isEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            if (mUrl.contains("?")) {
                mUrl += "&" + stringBuilder;
            } else {
                mUrl += "?" + stringBuilder;
            }
        }
        mWebView.loadUrl(mUrl, requestHeadersMap);
    }

    private void toastMockData() {
        if (mEntity == null) {
            return;
        }
        String method = mEntity.getMethod();
        String title = mEntity.getTitle();
        String description = mEntity.getDescription();
        String content;
        if (TextUtils.equals("POST", method)) {
            content = mEntity.getPostContent();
        } else {
            StringBuilder headerBuilder = new StringBuilder();
            List<SimpleParameter> requestHeaders = mEntity.getRequestHeaders();
            for (SimpleParameter parameter : requestHeaders) {
                String key = parameter.getKey();
                headerBuilder.append(key);
                headerBuilder.append(": ");
                headerBuilder.append(parameter.getValue());
                headerBuilder.append("\n");
            }
            if (requestHeaders.size() > 0) {
                headerBuilder.deleteCharAt(headerBuilder.length() - 1);
                headerBuilder.insert(0, "headers: \n");
            }

            if (TextUtils.isEmpty(headerBuilder)) {
                content = mUrl;
            } else {
                content = mUrl + "\n" + headerBuilder;
            }
        }
        String toast = title + "\n" +
                "description: " + description + "\n" +
                "method: " + method + "\n" +
                content;
        Uri uri = Uri.parse(mUrl);
        String host = uri.getHost();
        DebuggerSupplier debugger = DebuggerSupplier.getInstance();
        String cookie = null;
        if (host != null) {
            cookie = debugger.toCookies(host);
        }
        if (!TextUtils.isEmpty(cookie)) {
            toast += "\ncookie: " + cookie;
        }
        Toast.makeText(mActivity, toast, Toast.LENGTH_LONG).show();
    }

    private boolean verifyUrl(String url) {
        return URLUtil.isNetworkUrl(url) || URLUtil.isAssetUrl(url);
    }

    private void removeAdditionalJavascriptInterface() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
            mWebView.removeJavascriptInterface("accessibility");
            mWebView.removeJavascriptInterface("accessibilityTraversal");
        }
    }

    private void initWebViewSettings() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
    }

    @Override
    public void onRefresh(SwipeRefreshLayoutDirection direction) {
        if (direction == SwipeRefreshLayoutDirection.TOP) {
            mWebView.reload();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((ViewGroup) mWebView.getParent()).removeAllViews();
        mWebView.destroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", mUrl);
        outState.putString("title", mTitle);
        outState.putParcelable("entity", mEntity);
    }

    public void clearWebViewCache() {
        mWebView.clearCache(true);
        mActivity.deleteDatabase("webview.db");
        mActivity.deleteDatabase("webviewCache.db");
        Toast.makeText(mActivity, "webview缓存已清除", Toast.LENGTH_LONG).show();
    }

    public void onBackPressed() {
        if (mWebView.isFocused() && mWebView.canGoBack()) {
            mLoadingErrorLayout.setVisibility(View.GONE);
            updateWebViewActivityUrl();
            mSwipeRefreshLayout.startRefreshing();
            mWebView.goBack();
            mUrl = mWebView.getUrl();
        } else {
            mActivity.finish();
        }
    }

    private void updateWebViewActivityUrl() {
        if (mActivity instanceof WebViewActivity) {
            ((WebViewActivity) mActivity).setUrl(mUrl);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.load_error_layout) {
            refresh();
        }
    }

    public void refresh() {
        mLoadingErrorLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.startRefreshing();
        mWebView.reload();
    }

    public WebView getWebView() {
        return mWebView;
    }
}
