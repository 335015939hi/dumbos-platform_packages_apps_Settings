package com.android.settings.fuelgauge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.ext.power.BatteryChargeLimit;
import android.icu.text.MessageFormat;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.ext.BoolSettingFragmentPrefController;
import com.android.settings.ext.ExtSettingControllerHelper;

import java.util.Map;

import vendor.google.google_battery.IGoogleBattery;

public class BatteryChargingOptimizationPrefController extends BoolSettingFragmentPrefController {
    private static final String TAG = "BatteryChargeLimitPrefController";

    public BatteryChargingOptimizationPrefController(Context ctx, String key) {
        super(ctx, key, BatteryChargeLimit.getSetting());
    }

    @Override
    protected CharSequence getSummaryOn() {
        return MessageFormat.format(
                mContext.getString(R.string.charging_optimization_summary_charge_limit),
                Map.of("batteryLimitLevel", Integer.valueOf(BatteryChargeLimit.CHARGE_LEVEL)));
    }

    @Override
    protected CharSequence getSummaryOff() {
        return mContext.getString(R.string.charging_optimization_summary_off);
    }

    static int getAvailabilityStatus(Context ctx) {
        if (!BatteryChargeLimit.isGoogleDevice()) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return ExtSettingControllerHelper.getGlobalSettingAvailability(ctx);
    }

    @Override
    public int getAvailabilityStatus() {
        return getAvailabilityStatus(mContext);
    }
}
