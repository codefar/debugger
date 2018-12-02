package com.su.debugger.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by su on 2018/5/26.
 */

public class MockResponseEntity implements Parcelable, Cloneable {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MD5 = "md5";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_HOST = "host"; //只做查询用
    public static final String COLUMN_PATH = "path"; //只做查询用
    public static final String COLUMN_METHOD = "method";
    public static final String COLUMN_CONTENT_TYPE = "contentType";
    public static final String COLUMN_REQUEST_HEADERS = "requestHeaders";
    public static final String COLUMN_REQUEST_BODY = "requestBody";
    public static final String COLUMN_RESPONSE_HEADERS = "responseHeaders";
    public static final String COLUMN_RESPONSE = "response";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_AUTO = "auto";
    public static final String COLUMN_IN_USE = "inUse";
    public static final String[] PROJECTION = new String[] {COLUMN_ID,
            COLUMN_MD5,
            COLUMN_URL,
            COLUMN_HOST,
            COLUMN_PATH,
            COLUMN_METHOD,
            COLUMN_CONTENT_TYPE,
            COLUMN_REQUEST_HEADERS,
            COLUMN_REQUEST_BODY,
            COLUMN_RESPONSE_HEADERS,
            COLUMN_RESPONSE,
            COLUMN_DESCRIPTION,
            COLUMN_AUTO,
            COLUMN_IN_USE};
    public static final String TYPE_REQUEST_HEADERS = "RequestHeaders";
    public static final String TYPE_REQUEST_QUERY = "RequestQuery";
    public static final String TYPE_REQUEST_BODY = "RequestBody";
    public static final String TYPE_RESPONSE_HEADERS = "ResponseHeaders";
    public static final String TYPE_RESPONSE = "Response";

    private long id;
    private String md5; //url + method + requestHeaders(JSONString)  + requestBody(JSONString)  + body(JSONString) + auto
    private String url;
    private String host;
    private String path;
    private String pages;
    private String description;
    private String contentType;
    private String method;
    private String requestHeaders;
    private String requestBody;
    private String responseHeaders;
    private String response;
    private boolean test;
    private boolean auto;
    private boolean inUse;

    public MockResponseEntity() {}

    protected MockResponseEntity(Parcel in) {
        id = in.readLong();
        md5 = in.readString();
        url = in.readString();
        host = in.readString();
        path = in.readString();
        pages = in.readString();
        description = in.readString();
        contentType = in.readString();
        method = in.readString();
        requestHeaders = in.readString();
        requestBody = in.readString();
        responseHeaders = in.readString();
        response = in.readString();
        test = in.readByte() != 0;
        auto = in.readByte() != 0;
        inUse = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(md5);
        dest.writeString(url);
        dest.writeString(host);
        dest.writeString(path);
        dest.writeString(pages);
        dest.writeString(description);
        dest.writeString(contentType);
        dest.writeString(method);
        dest.writeString(requestHeaders);
        dest.writeString(requestBody);
        dest.writeString(responseHeaders);
        dest.writeString(response);
        dest.writeByte((byte) (test ? 1 : 0));
        dest.writeByte((byte) (auto ? 1 : 0));
        dest.writeByte((byte) (inUse ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MockResponseEntity> CREATOR = new Creator<MockResponseEntity>() {
        @Override
        public MockResponseEntity createFromParcel(Parcel in) {
            return new MockResponseEntity(in);
        }

        @Override
        public MockResponseEntity[] newArray(int size) {
            return new MockResponseEntity[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    @Override
    public MockResponseEntity clone() {
        MockResponseEntity o = null;
        try {
            o = (MockResponseEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.w("CLONE", e);
        }
        return o;
    }

    @NonNull
    @Override
    public String toString() {
        return "MockResponseEntity{" +
                "id=" + id +
                ", md5='" + md5 + '\'' +
                ", url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", path='" + path + '\'' +
                ", pages='" + pages + '\'' +
                ", description='" + description + '\'' +
                ", contentType='" + contentType + '\'' +
                ", method='" + method + '\'' +
                ", requestHeaders='" + requestHeaders + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", responseHeaders='" + responseHeaders + '\'' +
                ", response='" + response + '\'' +
                ", test=" + test +
                ", auto=" + auto +
                ", inUse=" + inUse +
                '}';
    }
}
