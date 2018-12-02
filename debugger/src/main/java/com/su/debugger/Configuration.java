package com.su.debugger;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by su on 18-1-4.
 */

public class Configuration {

    private Class<? extends DebuggerSupplier> mRequestSupplierClass;
    private String mRequestSupplierClassname;
    private HttpLibrary mHttpLibrary;

    public Class<? extends DebuggerSupplier> getRequestSupplierClass() {
        return mRequestSupplierClass;
    }

    public void setRequestSupplierClass(Class<? extends DebuggerSupplier> requestSupplierClass) {
        this.mRequestSupplierClass = requestSupplierClass;
        this.mRequestSupplierClassname = requestSupplierClass.getName();
    }

    private void setRequestSupplierClass(@NonNull String supplierClassname) {
        try {
            Class<?> clazz = Class.forName(supplierClassname);
            if (DebuggerSupplier.class.isAssignableFrom(clazz)) {
                mRequestSupplierClass = (Class<? extends DebuggerSupplier>) clazz;
            } else {
                throw new IllegalArgumentException("supplier class must extends DebuggerSupplier");
            }
        } catch (ClassNotFoundException e) {
            Log.e("Configuration", e.getMessage());
        }
    }

    public String getRequestSupplierClassname() {
        return mRequestSupplierClassname;
    }

    public void setRequestSupplierClassname(String requestSupplierClassname) {
        this.mRequestSupplierClassname = requestSupplierClassname;
        setRequestSupplierClass(requestSupplierClassname);
    }

    public HttpLibrary getHttpLibrary() {
        return mHttpLibrary;
    }

    public void setHttpLibrary(HttpLibrary httpLibrary) {
        this.mHttpLibrary = httpLibrary;
    }

    public enum HttpLibrary {
        OK_HTTP,
        VOLLEY,
        OTHER
    }
}
