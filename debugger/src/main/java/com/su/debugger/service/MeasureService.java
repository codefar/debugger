package com.su.debugger.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import com.su.debugger.ui.ui.RulerActivity;

@TargetApi(Build.VERSION_CODES.N)
public class MeasureService extends TileService {
    @Override
    public void onClick() {
        startActivityAndCollapse(new Intent(this, RulerActivity.class));
    }
}
