package com.android.settings.privatespace.users;

import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.privatespace.PrivateSpaceMaintainer;

public class PsEndSessionPrefController extends BasePreferenceController {

    private static final String TAG = "PsEndSessionPrefController";

    public PsEndSessionPrefController(Context ctx, String key) {
        super(ctx, key);
    }

    @Override
    public int getAvailabilityStatus() {
        PrivateSpaceMaintainer privateSpaceMaintainer = PrivateSpaceMaintainer.getInstance(mContext);
        UserHandle privateSpaceUserHandle = privateSpaceMaintainer.getPrivateProfileHandle();
        if (privateSpaceUserHandle == null) {
            Log.w(getLogTag(), "No private space user fetched, treating as unavailable");
            return CONDITIONALLY_UNAVAILABLE;
        }

        return AVAILABLE;
    }

    private String getLogTag() {
        return TAG;
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

        PrivateSpaceMaintainer privateSpaceMaintainer = PrivateSpaceMaintainer.getInstance(mContext);
        UserHandle privateSpaceUserHandle = privateSpaceMaintainer.getPrivateProfileHandle();
        if (privateSpaceUserHandle == null) {
            Log.w(getLogTag(), "No private space user to stop");
            return false;
        }

        UserManager userManager = mContext.getSystemService(UserManager.class);
        if (userManager == null) {
            Log.w(getLogTag(), "UserManager system service is not available");
            return false;
        }

        try {
            ActivityManager.getService().stopProfile(privateSpaceUserHandle.getIdentifier());
            userManager.requestQuietModeEnabled(true, privateSpaceUserHandle);
        } catch (RemoteException e) {
            Log.e(getLogTag(), "", e);
        }

        return true;
    }
}