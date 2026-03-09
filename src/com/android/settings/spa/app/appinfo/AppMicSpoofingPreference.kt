package com.android.settings.spa.app.appinfo

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.GosPackageState
import android.content.pm.GosPackageStateFlag
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.settings.R
import com.android.settings.spa.app.micspoofing.launchMicSpoofingConfig
import com.android.settingslib.spa.widget.preference.Preference
import com.android.settingslib.spa.widget.preference.PreferenceModel
import com.android.settingslib.spaprivileged.model.app.userHandle
import com.android.settingslib.spaprivileged.model.app.userId

@Composable
fun AppMicSpoofingPreference(app: ApplicationInfo) {
    val isEnabled = GosPackageState
        .get(app.packageName, app.userId)
        .hasFlag(GosPackageStateFlag.MIC_SPOOFING_ENABLED)

    val context = LocalContext.current
    val summaryText = stringResource(
        if (isEnabled) R.string.switch_on_text else R.string.switch_off_text,
    )

    Preference(object : PreferenceModel {
        override val title = stringResource(R.string.microphone_spoofing)

        override val summary = { summaryText }

        @SuppressLint("MissingPermission")
        override val onClick = {
            launchMicSpoofingConfig(context, app.packageName, app.userHandle)
        }
    })
}
