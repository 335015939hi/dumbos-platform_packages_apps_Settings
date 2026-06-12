package com.android.settings.development;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class DisableSecureWindowsPreferenceController
        extends DeveloperOptionsPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {

    private static final String KEY = "force_screenshot_secure_windows";

    public DisableSecureWindowsPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.FORCE_SCREENSHOT_SECURE_WINDOWS,
                (Boolean) newValue ? 1 : 0);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        final int value = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.FORCE_SCREENSHOT_SECURE_WINDOWS, 0);
        ((SwitchPreferenceCompat) preference).setChecked(value != 0);
    }

    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.FORCE_SCREENSHOT_SECURE_WINDOWS, 0);
        ((SwitchPreferenceCompat) mPreference).setChecked(false);
    }
}