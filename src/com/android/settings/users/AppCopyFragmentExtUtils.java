package com.android.settings.users;

import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.preference.PreferenceGroup;

import com.android.settings.R;
import com.android.settingslib.users.AppCopyHelper;
import com.android.settingslib.widget.AppSwitchPreference;

final class AppCopyFragmentExtUtils {

    static void maybeModifyAppListGroupPref(
            AppCopyFragment appCopyFragment, PreferenceGroup appListGroupPref) {
        appListGroupPref.setOrderingAsAdded(true);
    }

    static void maybeModifySelectableAppInfoPref(
            AppCopyFragment appCopyFragment,
            AppSwitchPreference appSwitchPref,
            AppCopyHelper.SelectableAppInfo selectableAppInfo) {
        final int installerOfRecordUid = selectableAppInfo.ext.installerOfRecordUid;
        final UserManager userManager = appCopyFragment.mUserManager;
        String summary = "";
        if (!selectableAppInfo.ext.installed) {
            summary += appCopyFragment.getString(R.string.app_copy_package_not_installed_in_user,
                    userManager.getUserName());
            summary += "\n";
        }

        if (installerOfRecordUid < 0) {
            summary += appCopyFragment.getString(R.string.app_copy_package_installed_or_updated_by_user_unknown);
            appSwitchPref.setSummary(summary);
            return;
        }

        UserInfo userInfo = userManager.getUserInfo(
                UserHandle.getUserId(installerOfRecordUid));
        if (userInfo != null) {
            String userName = userInfo.name;
            if (userInfo.id == UserHandle.USER_SYSTEM) {
                summary += appCopyFragment.getString(
                        R.string.app_copy_package_installed_or_updated_by_primary_user,
                        userName
                );
            } else {
                summary += appCopyFragment.getString(
                        R.string.app_copy_package_installed_or_updated_by_secondary_user,
                        userName
                );
            }
        }

        appSwitchPref.setSummary(summary);
    }
}
