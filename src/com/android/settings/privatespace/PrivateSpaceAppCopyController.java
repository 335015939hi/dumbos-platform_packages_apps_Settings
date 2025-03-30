package com.android.settings.privatespace;


import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;
import com.android.settings.users.AppCopyFragment;
import com.android.settings.R;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.privatespace.PrivateSpaceMaintainer;
import com.android.settings.users.AppRestrictionsFragment;

public class PrivateSpaceAppCopyController extends BasePreferenceController {

    private final PrivateSpaceMaintainer privateSpaceMaintainer;

    public PrivateSpaceAppCopyController(Context ctx, String key) {
        super(ctx, key);
        privateSpaceMaintainer = PrivateSpaceMaintainer.getInstance(ctx);
    }

    @Override
    public int getAvailabilityStatus() {
        UserHandle privateSpaceUserHandle = privateSpaceMaintainer.getPrivateProfileHandle();
        if (privateSpaceUserHandle == null) {
            return CONDITIONALLY_UNAVAILABLE;
        }
        return AVAILABLE_UNSEARCHABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setVisible(isAvailable());
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }

        UserHandle privateSpaceUserHandle = privateSpaceMaintainer.getPrivateProfileHandle();
        if (privateSpaceUserHandle == null) {
            return false;
        }

        if (privateSpaceMaintainer.isPrivateSpaceLocked()) {
            return false;
        }

        final Bundle extras = new Bundle();
        extras.putInt(AppRestrictionsFragment.EXTRA_USER_ID, privateSpaceUserHandle.getIdentifier());
        new SubSettingLauncher(preference.getContext())
                .setDestination(AppCopyFragment.class.getName())
                .setArguments(extras)
                .setTitleRes(R.string.user_copy_apps_menu_title)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();

        return true;
    }
}
