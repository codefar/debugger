package com.su.debugger;

/**
 * Created by su on 18-1-4.
 */

public class Configuration {

    private void setRequestSupplierClass(String supplierClassname) {

    }

    public void setRequestSupplierClassname(String requestSupplierClassname) {
    }

    public void setUseRhino(boolean useRhino) {
    }

    public void setHttpLibrary(HttpLibrary httpLibrary) {
    }

    public enum HttpLibrary {
        OK_HTTP,
        VOLLEY,
        OTHER
    }
}
