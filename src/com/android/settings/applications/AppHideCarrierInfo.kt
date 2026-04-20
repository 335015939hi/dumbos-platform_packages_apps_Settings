package com.android.settings.applications

import android.content.Context
import android.content.pm.ApplicationInfo
import android.ext.settings.ExtSettings
import android.ext.settings.app.AppSwitch
import android.ext.settings.app.AswHideCarrierInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.ext.BoolSettingFragment
import com.android.settings.ext.BoolSettingFragmentPrefController
import com.android.settings.ext.ExtSettingControllerHelper
import com.android.settings.spa.app.appinfo.AswPreference
import com.android.settingslib.widget.FooterPreference

object AswAdapterHideCarrierInfo : AswAdapter<AswHideCarrierInfo>() {

    override fun getAppSwitch() = AswHideCarrierInfo.I

    override fun getAswTitle(ctx: Context) = ctx.getText(R.string.app_hide_carrier_info_title)
    override fun getShortAswTitle(ctx: Context) = ctx.getText(R.string.app_hide_carrier_info_short)

    override fun getOnTitle(ctx: Context) = ctx.getText(R.string.app_hide_carrier_info_hidden)
    override fun getOffTitle(ctx: Context) = ctx.getText(R.string.app_hide_carrier_info_visible)

    override fun getCategory() = Category.MoreSecurityAndPrivacy

    override fun getDetailFragmentClass() = AppHideCarrierInfoFragment::class
}

@Composable
fun AppHideCarrierInfoPreference(app: ApplicationInfo) {
    val context = LocalContext.current
    AswPreference(context, app, AswAdapterHideCarrierInfo)
}

class AppHideCarrierInfoFragment : AswAppInfoFragment<AswHideCarrierInfo>() {

    override fun getAswAdapter() = AswAdapterHideCarrierInfo

    override fun getTitle(): CharSequence = getText(R.string.app_hide_carrier_info_short)

    override fun getSummaryForImmutabilityReason(ir: Int): CharSequence? {
        val id = when (ir) {
            AppSwitch.IR_IS_SYSTEM_APP -> R.string.app_hide_carrier_info_dvr_is_system_app
            else -> return null
        }
        return getText(id)
    }

    override fun updateFooter(fp: FooterPreference) {
        fp.setTitle(R.string.app_hide_carrier_info_footer)
    }
}

class AppDefaultHideCarrierInfoPrefController(ctx: Context, key: String) :
    BoolSettingFragmentPrefController(ctx, key, ExtSettings.HIDE_CARRIER_INFO_BY_DEFAULT) {

    override fun getSummaryOn() = resText(R.string.app_hide_carrier_info_default_summary_hidden)
    override fun getSummaryOff() = resText(R.string.app_hide_carrier_info_default_summary_visible)
}

class AppDefaultHideCarrierInfoFragment : BoolSettingFragment() {

    override fun getSetting() = ExtSettings.HIDE_CARRIER_INFO_BY_DEFAULT

    override fun getTitle() = resText(R.string.app_hide_carrier_info_short)

    override fun getMainSwitchTitle() =
        resText(R.string.app_hide_carrier_info_default_main_switch)

    override fun addExtraPrefs(screen: PreferenceScreen) {
        AswAdapterHideCarrierInfo.addAppListPageLink(screen)
    }

    override fun makeFooterPref(builder: FooterPreference.Builder): FooterPreference {
        return builder.setTitle(R.string.app_hide_carrier_info_footer).build()
    }
}

class HideCarrierInfoAppListPrefController(context: Context, preferenceKey: String) :
    AswAppListPrefController(context, preferenceKey, AswAdapterHideCarrierInfo) {

    override fun getAvailabilityStatus() = ExtSettingControllerHelper
        .getSecondaryUserOnlySettingAvailability(mContext)
}
