package com.android.settings.spa.app.micspoofing

import android.annotation.SuppressLint
import android.content.Context
import android.ext.micspoofing.MicSpoofingApi
import android.os.UserHandle

@SuppressLint("MissingPermission")
fun launchMicSpoofingConfig(
    context: Context,
    packageName: String,
    userHandle: UserHandle,
) {
    MicSpoofingApi
        .createConfigActivityIntent(packageName)
        .let { intent ->
            context.startActivityAsUser(intent, userHandle)
        }
}
