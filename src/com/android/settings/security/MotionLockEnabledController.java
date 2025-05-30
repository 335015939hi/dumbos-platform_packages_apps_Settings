package com.android.settings.security;

import android.content.Context;
import android.provider.Settings;
import com.android.settings.core.TogglePreferenceController;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settings.R;

public class MotionLockEnabledController extends TogglePreferenceController {
    private static final String KEY = "motion_lock_enabled";

    public MotionLockEnabledController(Context context) {
        super(context, KEY);
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(), KEY, 0, UserHandle.USER_SYSTEM) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putIntForUser(mContext.getContentResolver(), KEY, isChecked ? 1 : 0, UserHandle.USER_SYSTEM);
    }

    @Override
    public int getAvailabilityStatus() {
        UserManager userManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        // Only available to system user
        return (UserHandle.myUserId() == UserHandle.USER_SYSTEM) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_security;
    }
} 