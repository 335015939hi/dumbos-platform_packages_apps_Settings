package com.google.android.settings.fuelgauge;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.fuelgauge.BatteryInfo;
import com.android.settings.fuelgauge.PowerUsageFeatureProviderImpl;

import vendor.google.google_battery.IGoogleBattery;

// based on code from SettingsGoogle app
public class PowerUsageFeatureProviderGoogleImpl extends PowerUsageFeatureProviderImpl {
    private static final String TAG = "PowerUsageFeatureProviderGoogleImpl";

    public PowerUsageFeatureProviderGoogleImpl(Context context) {
        super(context);
    }

    private static final String DWELL_DEFEND_TRIGGER_KEY = "ACTIVE";
    private static final String TEMP_DEFEND_TRIGGER_KEY = " t=1";

    @Override
    public boolean isBatteryDefend(BatteryInfo info) {
        IGoogleBattery googleBattery = GoogleBattery.getService();
        if (googleBattery == null) {
            return false;
        }

        try {
            String dwellStatus = fetchFeatureStatus(googleBattery, 3);
            String tempStatus = fetchFeatureStatus(googleBattery, 1);
            Log.d(TAG, "dwell status: " + dwellStatus + ", temp status: " + tempStatus);
            boolean isDwellDefend = DWELL_DEFEND_TRIGGER_KEY.equals(dwellStatus);
            boolean isTempDefend = tempStatus != null && tempStatus.contains(TEMP_DEFEND_TRIGGER_KEY);
            return isDwellDefend || isTempDefend;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    private static final int GOOGLE_BATTERY_PROPERTY_STATE = 18;

    private static String fetchFeatureStatus(IGoogleBattery googleBattery, int feature) {
        try {
            return googleBattery.getStringProperty(feature, GOOGLE_BATTERY_PROPERTY_STATE);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }
}
