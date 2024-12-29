package com.google.android.settings.fuelgauge;

import android.content.Context;
import android.ext.power.BatteryChargeLimit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.fuelgauge.BatterySettingsFeatureProviderImpl;
import com.android.settingslib.utils.PowerUtil;

// based on code from SettingsGoogle app
public class BatterySettingsFeatureProviderGoogleImpl extends BatterySettingsFeatureProviderImpl {

    @Override
    public boolean isChargingOptimizationMode(@NonNull Context context) {
        return BatteryChargeLimit.isChargeLimitEnabled(context);
    }

    @Nullable
    @Override
    public CharSequence getChargingOptimizationRemainingLabel(
            @NonNull Context context,
            int batteryLevel,
            int pluggedStatus,
            long chargeRemainingTimeMs,
            long currentTimeMs) {
        if (batteryLevel >= BatteryChargeLimit.CHARGE_LEVEL) {
            return context.getString(R.string.charging_optimization_reach_limit_remaining_time_label);
        }
        if (chargeRemainingTimeMs <= 0) {
            return null;
        }
        String targetTime = PowerUtil.getTargetTimeShortString(context,
                chargeRemainingTimeMs, currentTimeMs);

        return context.getString(R.string.charging_optimization_remaining_time_label, targetTime);
    }

    @Nullable
    @Override
    public CharSequence getChargingOptimizationChargeLabel(
            @NonNull Context context,
            int batteryLevel,
            String batteryPercentageString,
            long chargeRemainingTimeMs,
            long currentTimeMs) {
        if (batteryLevel >= BatteryChargeLimit.CHARGE_LEVEL) {
            return context.getString(R.string.charging_optimization_reach_limit_charge_label,
                    batteryPercentageString);
        }
        if (chargeRemainingTimeMs > 0) {
            String targetTime = PowerUtil.getTargetTimeShortString(context,
                    chargeRemainingTimeMs, currentTimeMs);
            return context.getString(R.string.charging_optimization_charge_label,
                    batteryPercentageString, targetTime);
        }
        return null;
    }
}
