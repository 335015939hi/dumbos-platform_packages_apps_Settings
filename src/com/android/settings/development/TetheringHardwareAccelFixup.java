package com.android.settings.development;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public final class TetheringHardwareAccelFixup {
    private static final String TAG = "TetheringHardwareAccelFixup";

    private static final String MARKER_FILE_NAME = "tethering_hardware_accel_fixed_up";

    private TetheringHardwareAccelFixup() {
    }

    public static void run(Context context) {
        try {
            runInner(context);
        } catch (Throwable e) {
            Log.e(TAG, "unable to fix up tethering hardware acceleration setting", e);
        }
    }

    private static void runInner(Context context) throws IOException {
        final File markerFile = getMarkerFile(context);
        if (markerFile.isFile()) {
            return;
        }

        final boolean developmentSettingsEnabled = Settings.Global.getInt(
                context.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                Build.TYPE.equals("eng") ? 1 : 0) != 0;
        if (developmentSettingsEnabled) {
            return;
        }

        // The setting stores the disabled state, so 0/null means tether offload is enabled.
        final int rowsDeleted = context.getContentResolver().delete(
                Settings.Global.getUriFor(Settings.Global.TETHER_OFFLOAD_DISABLED),
                null,
                null);
        if (rowsDeleted == 1) {
            Log.i(TAG, "erased tether offload setting while developer options are disabled");
        } else {
            final String currentValue = Settings.Global.getString(context.getContentResolver(),
                    Settings.Global.TETHER_OFFLOAD_DISABLED);
            if (currentValue != null) {
                Log.w(TAG, "unable to erase tether offload setting, rows deleted: "
                        + rowsDeleted + ", current value: " + currentValue);
                return;
            }
            Log.i(TAG, "tether offload setting is already absent, rows deleted: "
                    + rowsDeleted + ", current value: " + currentValue);
        }

        if (!markerFile.createNewFile()) {
            Log.w(TAG, "markerFile.createNewFile() returned false");
        }
    }

    static File getMarkerFile(Context context) {
        return new File(context.getFilesDir(), MARKER_FILE_NAME);
    }
}
