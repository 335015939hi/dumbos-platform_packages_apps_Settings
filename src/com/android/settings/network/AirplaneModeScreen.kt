/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.settings.network

import android.app.settings.SettingsEnums
import android.content.Context
import android.content.pm.PackageManager
import android.os.UserManager
import androidx.fragment.app.Fragment
import com.android.settings.R
import com.android.settings.core.PreferenceScreenMixin
import com.android.settings.restriction.PreferenceRestrictionMixin
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.ProvidePreferenceScreen
import com.android.settingslib.metadata.preferenceHierarchy
import kotlinx.coroutines.CoroutineScope

@ProvidePreferenceScreen(AirplaneModeScreen.KEY)
open class AirplaneModeScreen :
    PreferenceScreenMixin, PreferenceAvailabilityProvider, PreferenceRestrictionMixin {

    override val key: String
        get() = KEY

    override val title: Int
        get() = R.string.airplane_mode

    override val icon: Int
        get() = R.drawable.ic_airplanemode_active

    override val highlightMenuKey: Int
        get() = R.string.menu_key_network

    override fun getMetricsCategory() = SettingsEnums.SETTINGS_NETWORK_CATEGORY

    override fun isAvailable(context: Context) =
        context.resources.getBoolean(R.bool.config_show_toggle_airplane) &&
            !context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    override fun isEnabled(context: Context) = super<PreferenceRestrictionMixin>.isEnabled(context)

    override val restrictionKeys
        get() = arrayOf(UserManager.DISALLOW_AIRPLANE_MODE)

    override fun hasCompleteHierarchy() = false

    override fun getPreferenceHierarchy(context: Context, coroutineScope: CoroutineScope) =
        preferenceHierarchy(context) {
            +AirplaneModePreference()
        }

    override fun fragmentClass(): Class<out Fragment>? = AirplaneModeSettings::class.java

    companion object {
        const val KEY = "airplane_mode_screen"
    }
}
