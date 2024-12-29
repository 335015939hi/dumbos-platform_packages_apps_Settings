package com.google.android.settings.fuelgauge;

import android.annotation.Nullable;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

import vendor.google.google_battery.IGoogleBattery;

public class GoogleBattery {
    static final String TAG = "GoogleBattery";

    @Nullable
    public static IGoogleBattery getService() {
        String svc = IGoogleBattery.DESCRIPTOR + "/default";
        IBinder binder = ServiceManager.getService(svc);
        if (binder == null) {
            Log.w(TAG, svc + " is null");
            return null;
        }
        return IGoogleBattery.Stub.asInterface(binder);
    }
}
