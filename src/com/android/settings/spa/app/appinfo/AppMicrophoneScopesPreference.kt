package com.android.settings.spa.app.appinfo

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.GosPackageState
import android.content.pm.GosPackageStateFlag
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.settings.R
import com.android.settingslib.spa.widget.preference.Preference
import com.android.settingslib.spa.widget.preference.PreferenceModel
import com.android.settingslib.spaprivileged.model.app.userHandle
import com.android.settingslib.spaprivileged.model.app.userId

@Composable
fun AppMicrophoneScopesPreference(app: ApplicationInfo) {
    val pkgName = app.packageName
    if (!GosPackageState.get(pkgName, app.userId).hasFlag(GosPackageStateFlag.MICROPHONE_SCOPES_ENABLED)) {
        return
    }

    val context = LocalContext.current

    Preference(object : PreferenceModel {
        override val title = stringResource(R.string.microphone_scopes)
        override val onClick = {
            val intent = Intent()
            intent.component = ComponentName.createRelative(
                "com.android.permissioncontroller", ".mscopes.MicrophoneScopesActivity"
            )
            intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkgName)
            context.startActivityAsUser(intent, app.userHandle)
        }
    })
}
