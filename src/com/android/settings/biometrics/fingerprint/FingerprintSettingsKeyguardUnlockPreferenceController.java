/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.biometrics.fingerprint;

import static android.provider.Settings.Secure.FINGERPRINT_KEYGUARD_ENABLED;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.UserManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.android.settings.Utils;
import com.android.settings.biometrics.activeunlock.ActiveUnlockStatusUtils;
import com.android.settingslib.RestrictedLockUtils;

public class FingerprintSettingsKeyguardUnlockPreferenceController
        extends FingerprintSettingsPreferenceController {

    private static final int ON = 1;
    private static final int OFF = 0;
    private static final int DEFAULT = ON;

    private FingerprintManager mFingerprintManager;
    private UserManager mUserManager;

    public FingerprintSettingsKeyguardUnlockPreferenceController(
            @NonNull Context context, @NonNull String key) {
        super(context, key);
        mFingerprintManager = Utils.getFingerprintManagerOrNull(context);
        mUserManager = context.getSystemService(UserManager.class);
    }

    @Override
    public boolean isChecked() {
        return isChecked(mContext, getUserId());
    }

    public static boolean isChecked(Context context, int userId) {
        return Settings.Secure.getIntForUser(context.getContentResolver(),
                FINGERPRINT_KEYGUARD_ENABLED, DEFAULT, userId) == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        mMetricsFeatureProvider.action(mContext,
                SettingsEnums.ACTION_FINGERPRINTS_ENABLED_ON_KEYGUARD_SETTINGS, isChecked);
        return Settings.Secure.putIntForUser(mContext.getContentResolver(),
                FINGERPRINT_KEYGUARD_ENABLED, isChecked ? ON : OFF, getUserId());
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!FingerprintSettings.isFingerprintHardwareDetected(mContext)) {
            preference.setEnabled(false);
        } else if (!mFingerprintManager.hasEnrolledTemplates(getUserId())) {
            preference.setEnabled(false);
        } else if (getRestrictingAdmin() != null) {
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return getAvailabilityStatus(mContext, mUserManager, getRestrictingAdmin(), getUserId());
    }

    public static int getAvailabilityStatus(Context context, UserManager userManager,
            @Nullable RestrictedLockUtils.EnforcedAdmin restrictingAdmin, int userId) {
        if (userManager.isManagedProfile(userId) || !Utils.hasFingerprintHardware(context)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        final ActiveUnlockStatusUtils activeUnlockStatusUtils =
                new ActiveUnlockStatusUtils(context);
        if (activeUnlockStatusUtils.isAvailable()) {
            return getAvailabilityFromRestrictingAdmin(restrictingAdmin);
        }
        return getAvailabilityFromRestrictingAdmin(restrictingAdmin);
    }

    private static int getAvailabilityFromRestrictingAdmin(@Nullable RestrictedLockUtils.EnforcedAdmin restrictingAdmin) {
        return restrictingAdmin != null ? DISABLED_FOR_USER : AVAILABLE;
    }
}
