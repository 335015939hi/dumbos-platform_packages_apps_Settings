package com.android.settings.security;

import android.content.Context;
import com.android.settings.core.BasePreferenceController;
import android.os.UserHandle;

public class MotionLockPrefController extends BasePreferenceController {
    public MotionLockPrefController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        // Only available to system user
        return (UserHandle.myUserId() == UserHandle.USER_SYSTEM) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }
} 