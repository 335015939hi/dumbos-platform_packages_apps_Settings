package com.android.settings.security;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.R;
import com.android.settings.security.MotionLockConstants;
import com.android.settings.security.MotionLockSensitivity;
import android.os.UserHandle;
import android.os.UserManager;

public class MotionLockSensitivityController extends BasePreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    private static final String KEY = "motion_lock_sensitivity";
    private final MotionLockSensitivityFragment mFragment;

    public MotionLockSensitivityController(Context context, MotionLockSensitivityFragment fragment) {
        super(context, KEY);
        mFragment = fragment;
    }

    @Override
    public int getAvailabilityStatus() {
        UserManager userManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        // Only available to system user
        return (UserHandle.myUserId() == UserHandle.USER_SYSTEM) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ListPreference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            preference.setEntries(R.array.motion_lock_sensitivity_entries);
            preference.setEntryValues(R.array.motion_lock_sensitivity_values);
            updateState(preference);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof ListPreference) {
            int value = Settings.Secure.getInt(mContext.getContentResolver(),
                    MotionLockConstants.KEY_MOTION_LOCK_SENSITIVITY,
                    MotionLockConstants.DEFAULT_SENSITIVITY);
            
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setValue(String.valueOf(value));
            listPreference.setSummary(getSummaryForSensitivity(value));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            String value = (String) newValue;
            Settings.Secure.putInt(mContext.getContentResolver(),
                    MotionLockConstants.KEY_MOTION_LOCK_SENSITIVITY,
                    Integer.parseInt(value));
            updateState(preference);
            return true;
        }
        return false;
    }

    private String getSummaryForSensitivity(int sensitivity) {
        switch (sensitivity) {
            case MotionLockSensitivity.LOW:
                return mContext.getString(R.string.motion_lock_sensitivity_low);
            case MotionLockSensitivity.MEDIUM:
                return mContext.getString(R.string.motion_lock_sensitivity_medium);
            case MotionLockSensitivity.HIGH:
                return mContext.getString(R.string.motion_lock_sensitivity_high);
            default:
                return mContext.getString(R.string.motion_lock_sensitivity_medium);
        }
    }
} 