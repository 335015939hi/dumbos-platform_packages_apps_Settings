package com.google.android.settings.overlay

import com.google.android.settings.fuelgauge.BatterySettingsFeatureProviderGoogleImpl
import com.android.settings.overlay.FeatureFactoryImpl
import com.google.android.settings.fuelgauge.PowerUsageFeatureProviderGoogleImpl

class FeatureFactoryGoogleImpl : FeatureFactoryImpl() {

    override val powerUsageFeatureProvider by lazy {
        PowerUsageFeatureProviderGoogleImpl(appContext)
    }

    override val batterySettingsFeatureProvider by lazy {
        BatterySettingsFeatureProviderGoogleImpl()
    }
}
