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
import android.content.Intent
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment

class AirplaneModeSettings : DashboardFragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)
        use(AirplaneModePreferenceController::class.java).setFragment(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        use(AirplaneModePreferenceController::class.java)
            .onActivityResult(requestCode, resultCode, data)
    }

    override fun getMetricsCategory(): Int = SettingsEnums.SETTINGS_NETWORK_CATEGORY

    override fun getLogTag(): String = TAG

    override fun getPreferenceScreenResId(): Int = R.xml.airplane_mode_settings

    private companion object {
        private const val TAG = "AirplaneModeSettings"
    }
}
