package com.su.debugger.component.annotation;

import com.su.debugger.Configuration;
import com.su.debugger.DebuggerSupplier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by su on 18-1-5.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DebugConfiguration {

    Class<? extends DebuggerSupplier> requestSupplier();

    Configuration.HttpLibrary httpLibrary() default Configuration.HttpLibrary.OTHER;
}
