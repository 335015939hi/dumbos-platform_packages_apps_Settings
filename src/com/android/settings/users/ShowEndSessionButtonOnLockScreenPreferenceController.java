package com.android.settings.users;

import static android.os.UserManager.LOGOUTABILITY_STATUS_OK;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.ext.settings.ExtSettings;
import android.os.UserHandle;
import android.os.UserManager;

import com.android.settings.R;
import com.android.settings.ext.BoolSettingPrefController;

public class ShowEndSessionButtonOnLockScreenPreferenceController
        extends BoolSettingPrefController {

    private final UserManager mUserManager;
    private final DevicePolicyManager mDPM;

    static final String PREF_KEY = "user_settings_show_end_session_on_lock_screen";

    public ShowEndSessionButtonOnLockScreenPreferenceController(Context context) {
        super(context, PREF_KEY, ExtSettings.SHOW_END_SESSION_BUTTON_LOCK_SCREEN);
        mUserManager = context.getSystemService(UserManager.class);
        mDPM = context.getSystemService(DevicePolicyManager.class);
    }

    @Override
    public int getAvailabilityStatus() {
        // In SystemUI, the lockscreen logout button visibility from SystemUI follows this logic
        // (note the boolean OR):
        //
        // val isLogoutEnabled: StateFlow<Boolean> =
        //        combine(
        //                userRepository.isPolicyManagerLogoutEnabled,
        //                userRepository.isUserManagerLogoutEnabled,
        //                Boolean::or,
        //            )
        //
        // Both of these conditions are replicated here. For isUserManagerLogoutEnabled, skipped a
        // check for com.android.internal.R.bool.config_userSwitchingMustGoThroughLoginScreen for
        // simplicity.

        // For the system user, getLogoutUser should be null due to a GrapheneOS change in fw/base
        // ("fix DevicePolicyManager#logoutUser to succeed without device admin").
        final boolean isPolicyManagerLogoutEnabled =
                mDPM.isLogoutEnabled() && mDPM.getLogoutUser() != null;
        // For the system user, this will be LOGOUTABILITY_STATUS_CANNOT_LOGOUT_SYSTEM_USER.
        final boolean isUserManagerLogoutEnabled =
                mUserManager.getUserLogoutability(UserHandle.myUserId()) == LOGOUTABILITY_STATUS_OK;

        return isPolicyManagerLogoutEnabled || isUserManagerLogoutEnabled
                ? AVAILABLE : DISABLED_FOR_USER;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_system;
    }
}
