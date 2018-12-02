package com.su.debugger.entity;

import android.support.annotation.NonNull;

import com.su.debugger.component.annotation.Searchable;

/**
 * Created by su on 2018/1/13.
 */

public class OpenSourceInfo {

    @Searchable
    private String name;
    private String author;
    @Searchable
    private String desc;
    private String url;

    public OpenSourceInfo() {
    }

    public OpenSourceInfo(String name, String author, String desc, String url) {
        this.name = name;
        this.author = author;
        this.desc = desc;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @NonNull
    @Override
    public String toString() {
        return "OpenSourceInfo{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", desc='" + desc + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
