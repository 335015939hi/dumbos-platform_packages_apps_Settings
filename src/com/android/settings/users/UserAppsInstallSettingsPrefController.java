package com.android.settings.users;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.ext.AbstractListPreferenceController;
import com.android.settings.ext.RadioButtonPickerFragment2;
import com.android.settingslib.widget.CandidateInfo;

import java.util.ArrayList;

public class UserAppsInstallSettingsPrefController extends AbstractListPreferenceController {

    private static final int UNKNOWN = -1;
    private static final int INSTALL_ENABLED = 0;
    private static final int INSTALL_FIRST_PARTY_ENABLED = 1;
    private static final int INSTALL_DISABLED = 2;

    static void launchSettings(Context context, Preference preference, int userId) {
        preference.getExtras().putInt(AppCopyFragment.EXTRA_USER_ID, userId);
        new UserAppsInstallSettingsPrefController(context, preference.getKey())
                .handlePreferenceTreeClick(preference);
    }

    private UserRestrictions userRestrictions;

    public UserAppsInstallSettingsPrefController(Context ctx, String key) {
        super(ctx, key);
    }

    protected void initUserRestrictions(int userId) {
        if (userRestrictions != null) {
            return;
        }

        userRestrictions = UserRestrictions.createInstance(mContext, userId);
    }

    @Override
    public int getAvailabilityStatus() {
        if (userRestrictions == null) {
            return CONDITIONALLY_UNAVAILABLE;
        }

        return AVAILABLE;
    }

    @Override
    protected void getEntries(Entries entries) {
        if (userRestrictions == null) {
            if (fragment != null) {
                Bundle args = fragment.getArguments();
                if (args == null) {
                    throw new IllegalStateException();
                }

                int userId = args.getInt(AppCopyFragment.EXTRA_USER_ID, UserHandle.USER_NULL);
                if (userId != UserHandle.USER_NULL) {
                    initUserRestrictions(userId);
                }
            }
        }

        if (userRestrictions == null) {
            throw new IllegalStateException();
        }

        UserInfo userInfo = userRestrictions.userInfo;

        if (!userInfo.isGuest()) {
            entries.add(R.string.user_app_install_enabled,
                    R.string.user_app_install_enabled_desc,
                    INSTALL_ENABLED);
        }

        entries.add(R.string.user_app_install_enabled_first_party_sources,
                R.string.user_app_install_enabled_first_party_sources_desc,
                INSTALL_FIRST_PARTY_ENABLED);
        entries.add(R.string.user_app_install_disabled,
                R.string.user_app_install_disabled_desc,
                INSTALL_DISABLED);
    }

    @Override
    protected final int getCurrentValue() {
        if (userRestrictions == null) {
            return UNKNOWN;
        }

        final boolean isInstallDisallowed =
                userRestrictions.isSet(UserManager.DISALLOW_INSTALL_APPS);
        final boolean isInstallDisallowedForUnknownSources =
                userRestrictions.isSet(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
        if (isInstallDisallowed) {
            if (isInstallDisallowedForUnknownSources) {
                return INSTALL_DISABLED;
            }

            return UNKNOWN;
        }

        if (isInstallDisallowedForUnknownSources) {
            return INSTALL_FIRST_PARTY_ENABLED;
        }

        return INSTALL_ENABLED;
    }

    @Override
    protected final boolean setValue(int installEnabledValue) {
        if (userRestrictions == null) {
            return false;
        }

        switch (installEnabledValue) {
            case INSTALL_ENABLED -> {
                userRestrictions.set(UserManager.DISALLOW_INSTALL_APPS, false);
                userRestrictions.set(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, false);
                return true;
            }
            case INSTALL_FIRST_PARTY_ENABLED ->  {
                userRestrictions.set(UserManager.DISALLOW_INSTALL_APPS, false);
                userRestrictions.set(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, true);
                return true;
            }
            case INSTALL_DISABLED -> {
                userRestrictions.set(UserManager.DISALLOW_INSTALL_APPS, true);
                userRestrictions.set(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, true);
                return true;
            }
        }

        return false;
    }
}
