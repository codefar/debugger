package com.su.sample;

import com.su.debugger.Debugger;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by su on 17-4-12.
 */

public class RequestHelper {

    private static final HttpLoggingInterceptor.Level LOG_LEVEL = HttpLoggingInterceptor.Level.BODY;

    private static OkHttpClient sClient;

    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(LOG_LEVEL);
        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder builder = client.newBuilder()
                .protocols(Util.immutableList(Protocol.HTTP_1_1, Protocol.HTTP_2))//just for http1.1
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS);
        Object hostInterceptor = Debugger.getHostInterceptor();
        if (hostInterceptor != null) {
            builder.addInterceptor((Interceptor) hostInterceptor);
        }
        builder.addNetworkInterceptor(logging);
        Object mockInterceptor = Debugger.getMockInterceptor();
        if (mockInterceptor != null) {
            builder.addInterceptor((Interceptor) mockInterceptor);
        }
        Object dataCollectorInterceptor = Debugger.getDataCollectorInterceptor();
        if (dataCollectorInterceptor != null) {
            builder.addInterceptor((Interceptor) dataCollectorInterceptor);
        }
        sClient = builder.build();
    }

    public static OkHttpClient getClient() {
        return sClient;
    }
}
