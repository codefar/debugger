package com.su.debugger;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by su on 18-1-4.
 */

public class Configuration {

    private Class<? extends DebuggerSupplier> mRequestSupplierClass;

    public Class<? extends DebuggerSupplier> getRequestSupplierClass() {
        return mRequestSupplierClass;
    }

    public void setRequestSupplierClass(Class<? extends DebuggerSupplier> requestSupplierClass) {
        this.mRequestSupplierClass = requestSupplierClass;
    }

    @SuppressWarnings("unchecked")
    public void setRequestSupplierClassname(@NonNull String supplierClassname) {
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
}
