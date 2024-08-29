package com.android.settings.applications.appinfo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.GosPackageState
import android.ext.settings.app.AppSwitch
import androidx.annotation.StringRes
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import kotlin.reflect.KClass

abstract class AswAdapter<T : AppSwitch>(val context: Context, val userId: Int = context.userId) {
    abstract fun getAppSwitch(): T

    open fun isOneTimeSupported(): Boolean = false

    fun getPreferenceSummary(appInfo: ApplicationInfo): CharSequence {
        val asw = getAppSwitch()
        val si = AppSwitch.StateInfo()
        val isOn = asw.get(context, userId, appInfo, GosPackageState.get(appInfo.packageName, userId), si)
        return if (si.isUsingDefaultValue) {
            getDefaultTitle(isOn)
        } else {
            if (isOn) getOnTitle() else getOffTitle()
        }
    }

    abstract fun getAswTitle(): CharSequence

    fun getDefaultTitle(isOn: Boolean, isOneTime: Boolean = false): CharSequence {
        val subtext = if (isOneTime && isOneTimeSupported()) {
            getOneTimeTitle()
        } else if (isOn) {
            getOnTitle()
        } else {
            getOffTitle()
        }
        return context.getString(R.string.aep_default, subtext)
    }

    open fun getOnTitle(): CharSequence = getText(R.string.aep_enabled)

    open fun getOneTimeTitle(): CharSequence? = null

    open fun getOffTitle(): CharSequence = getText(R.string.aep_disabled)

    protected fun getText(@StringRes id: Int): CharSequence = context.getText(id)

    protected fun getString(@StringRes id: Int): String = context.getString(id)

    abstract fun getDetailFragmentClass(): KClass<out SettingsPreferenceFragment>
}
