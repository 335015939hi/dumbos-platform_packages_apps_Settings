package com.android.settings.security.screenlock;

import static com.android.internal.widget.LockDomain.Primary;

import android.content.Context;
import android.ext.settings.ExtSettings;
import android.os.UserHandle;

import com.android.internal.widget.LockDomain;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ext.BoolSettingPrefController;
import com.android.settings.overlay.FeatureFactory;

public class PinScramblingPrefController extends BoolSettingPrefController {

    private final LockPatternUtils lockPatternUtils;
    private final LockDomain lockDomain;
    private final UserHandle user;

    static final String PREF_KEY = "scramble_pin_layout";

    public PinScramblingPrefController(Context ctx, LockDomain lockDomain, UserHandle user) {
        super(ctx, PREF_KEY, lockDomain == Primary ?
                ExtSettings.SCRAMBLE_LOCKSCREEN_PIN_LAYOUT_PRIMARY :
                ExtSettings.SCRAMBLE_LOCKSCREEN_PIN_LAYOUT_SECONDARY,
                user);
        this.lockDomain = lockDomain;
        this.lockPatternUtils = FeatureFactory.getFeatureFactory()
                .getSecurityFeatureProvider()
                .getLockPatternUtils(ctx);
        this.user = user;
    }

    @Override
    public int getAvailabilityStatus() {
        int res = super.getAvailabilityStatus();
        if (res == AVAILABLE) {
            if (lockPatternUtils.getCredentialTypeForUser(user.getIdentifier(), lockDomain)
                    != LockPatternUtils.CREDENTIAL_TYPE_PIN) {
                return CONDITIONALLY_UNAVAILABLE;
            }
        }
        return res;
    }
}
