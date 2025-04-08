package com.android.settings.security;

import android.content.Context;
import android.ext.settings.ExtSettings;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.ext.ExtSettingControllerHelper;

public class ExpiryWarningDisablePreferenceController extends TogglePreferenceController {
    public ExpiryWarningDisablePreferenceController(Context context,
            String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public boolean isChecked() {
        return ExtSettings.USER_DISABLE_PATCH_LEVEL_EXPIRY_WARNING.get(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return ExtSettings.USER_DISABLE_PATCH_LEVEL_EXPIRY_WARNING.put(mContext, isChecked);
    }

    @Override
    public boolean isSliceable() {
        return false;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        // not needed since it's not sliceable
        return NO_RES;
    }

    @Override
    public int getAvailabilityStatus() {
        if (ExtSettings.DEVICE_DISABLED_PATCH_LEVEL_EXPIRY_WARNING.get()) {
            return UNSUPPORTED_ON_DEVICE;
        }

        return ExtSettingControllerHelper.getGlobalSettingAvailability(mContext);
    }
}
