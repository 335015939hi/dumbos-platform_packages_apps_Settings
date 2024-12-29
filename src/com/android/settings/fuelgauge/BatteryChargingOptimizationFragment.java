package com.android.settings.fuelgauge;

import android.ext.power.BatteryChargeLimit;
import android.ext.settings.BoolSetting;
import android.icu.text.MessageFormat;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.ext.BoolSettingFragment;
import com.android.settingslib.widget.FooterPreference;
import com.google.android.settings.fuelgauge.GoogleBattery;

import java.util.Map;

import vendor.google.google_battery.IGoogleBattery;

public class BatteryChargingOptimizationFragment extends BoolSettingFragment {
    private static final String TAG = "BatteryChargingOptimizationFragment";

    @Override
    protected boolean interceptMainSwitchChange(boolean newValue) {
        int policy = newValue ?
                IGoogleBattery.BatteryChargingPolicy.LONGLIFE :
                IGoogleBattery.BatteryChargingPolicy.DEFAULT;

        // block setting update if policy update fails
        return !setChargingPolicy(policy);
    }

    static boolean setChargingPolicy(int policy) {
        IGoogleBattery service = GoogleBattery.getService();
        if (service == null) {
            return false;
        }
        try {
            service.setChargingPolicy(policy);
            Log.d(TAG, "setChargingPolicy to " + policy);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @Override
    protected BoolSetting getSetting() {
        return BatteryChargeLimit.getSetting();
    }

    @Override
    protected CharSequence getTitle() {
        return getText(R.string.charging_optimization_title);
    }

    @Override
    protected CharSequence getMainSwitchTitle() {
        return MessageFormat.format(
                requireContext().getString(R.string.charging_optimization_summary_charge_limit),
                Map.of("batteryLimitLevel", Integer.valueOf(BatteryChargeLimit.CHARGE_LEVEL)));
    }

    @Override
    protected void onMainSwitchChanged(boolean state) {
        footer.setVisible(getSetting().get(requireContext()));
    }

    private FooterPreference footer;

    @Override
    protected FooterPreference makeFooterPref(FooterPreference.Builder builder) {
        String text = MessageFormat.format(
                requireContext().getString(R.string.charging_optimization_footer_charge_limit),
                Map.of("batteryLimitLevel", Integer.valueOf(BatteryChargeLimit.CHARGE_LEVEL)));

        builder.setTitle(text);

        FooterPreference pref = builder.build();
        pref.setVisible(getSetting().get(requireContext()));
        this.footer = pref;
        return pref;
    }
}
