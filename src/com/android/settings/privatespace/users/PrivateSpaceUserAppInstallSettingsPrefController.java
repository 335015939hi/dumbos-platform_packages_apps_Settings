package com.android.settings.privatespace.users;

import android.content.Context;
import android.os.UserHandle;

import com.android.settings.privatespace.PrivateSpaceMaintainer;
import com.android.settings.users.UserAppsInstallSettingsPrefController;

public class PrivateSpaceUserAppInstallSettingsPrefController extends UserAppsInstallSettingsPrefController {

    public PrivateSpaceUserAppInstallSettingsPrefController(Context ctx, String key) {
        super(ctx, key);
        UserHandle userHandle =
                PrivateSpaceMaintainer.getInstance(mContext).getPrivateProfileHandle();
        if (userHandle != null) {
            initUserRestrictions(userHandle.getIdentifier());
        }
    }
}
