package com.google.android.settings.display

import android.content.Context
import android.os.Build
import android.os.UserHandle
import android.provider.Settings
import com.android.settings.R
import com.android.settings.core.TogglePreferenceController

class HighEmissionFrequencyController(context: Context, prefKey: String) :
    TogglePreferenceController(context, prefKey) {
    override fun getAvailabilityStatus(): Int {
        // SettingsGoogle and PixelDisplayService both seem to assume that the array might not exist
        // and perform a dynamic lookup. However, this array actually exists in AOSP and there's no
        // reason we can't just use the static identifier.
        val options = mContext.resources.getIntArray(com.android.internal.R.array.config_availableEMValueOptions)

        // This matches PixelDisplayService's check. Note that SettingsGoogle has a different
        // conditional, which checks if the array is exactly [0, 1].
        return if ("Google".equals(Build.MANUFACTURER) && options.size >= 2) {
            AVAILABLE
        } else {
            UNSUPPORTED_ON_DEVICE
        }
    }

    override fun isChecked(): Boolean {
        return Settings.Secure.getIntForUser(
            mContext.contentResolver,
            Settings.Secure.EM_VALUE,
            0,
            UserHandle.USER_CURRENT,
        ) == 1
    }

    override fun setChecked(isChecked: Boolean): Boolean {
        return Settings.Secure.putIntForUser(
            mContext.contentResolver,
            Settings.Secure.EM_VALUE,
            (if (isChecked) 1 else 0),
            UserHandle.USER_CURRENT,
        )
    }

    override fun getSliceHighlightMenuRes(): Int = R.string.menu_key_display
}
