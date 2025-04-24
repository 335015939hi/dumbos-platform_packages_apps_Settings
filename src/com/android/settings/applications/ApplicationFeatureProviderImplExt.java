package com.android.settings.applications;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.ArraySet;

import androidx.annotation.NonNull;

import com.android.settings.backup.BackupSettingsHelper;

import java.util.Set;

public final class ApplicationFeatureProviderImplExt {

    private static final String TAG = "AppFeatureProviderImplExt";

    private ApplicationFeatureProviderImplExt() {
    }

    @NonNull
    public static Set<String> getAlwaysAllowToDisablePackages(@NonNull Context ctx) {
        Set<String> alwaysAllowToDisablePackages = new ArraySet<>();
        // Current third-party backup app implementation will NOT brick phone even when disabled.
        // Its usage of platform key is required by invasive permission(s)
        // requested for non-core functionalities, allow disabling it.
        BackupSettingsHelper backupHelper = new BackupSettingsHelper(ctx);
        Intent backupIntent = backupHelper.getIntentForBackupSettings();
        ComponentName componentName = backupIntent.resolveActivity(ctx.getPackageManager());
        String componentPkgName = componentName != null ? componentName.getPackageName() : null;
        if (!ctx.getPackageName().equals(componentPkgName)) {
            alwaysAllowToDisablePackages.add(componentPkgName);
        }

        return alwaysAllowToDisablePackages;
    }
}
