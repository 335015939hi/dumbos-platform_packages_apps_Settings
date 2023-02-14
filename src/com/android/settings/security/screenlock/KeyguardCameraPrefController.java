package com.android.settings.security.screenlock;

import android.content.Context;
import android.ext.settings.ExtSettings;
import android.os.UserHandle;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ext.BoolSettingPrefController;

public class KeyguardCameraPrefController extends BoolSettingPrefController {

    private final LockPatternUtils lockPatternUtils;

    public KeyguardCameraPrefController(Context ctx, String key, UserHandle user,
                                        LockPatternUtils lockPatternUtils) {
        super(ctx, key, ExtSettings.ALLOW_KEYGUARD_CAMERA, user);
        this.lockPatternUtils = lockPatternUtils;
    }

    @Override
    public int getAvailabilityStatus() {
        if (!lockPatternUtils.isSecure(user.getIdentifier())) {
            return CONDITIONALLY_UNAVAILABLE;
        }
        return AVAILABLE;
    }
}
