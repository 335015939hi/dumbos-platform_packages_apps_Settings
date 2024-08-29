package com.android.settings.applications.appinfo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.GosPackageState
import android.ext.settings.app.AppSwitch
import android.ext.settings.app.AswClipboardRead
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.android.settings.R
import com.android.settings.spa.app.appinfo.AswPreference
import com.android.settingslib.widget.FooterPreference

class AswAdapterClipboardRead(ctx: Context) : AswAdapter<AswClipboardRead>(ctx) {

    override fun getAppSwitch() = AswClipboardRead.I

    override fun isOneTimeSupported() = true

    override fun getAswTitle() = getText(R.string.app_clipboard_pref_title)

    override fun getOnTitle() = getText(R.string.clipboard_read_allowed)
    override fun getOneTimeTitle() = getText(R.string.clipboard_read_ask_every_time)
    override fun getOffTitle() = getText(R.string.clipboard_read_blocked)

    override fun getDetailFragmentClass() = AppClipboardFragment::class
}

@Composable
fun AppClipboardPreference(app: ApplicationInfo) {
    val context = LocalContext.current
    AswPreference(context, app, AswAdapterClipboardRead(context))
}

class AppClipboardFragment : AswAppInfoFragment<AswClipboardRead>() {

    override fun createAswAdapter(ctx: Context) = AswAdapterClipboardRead(ctx)

    override fun isOneTimeChecked(ps: GosPackageState?): Boolean {
        return !adapter.getAppSwitch().isNotificationSuppressed(ps)
    }

    override fun onEntrySelectedInner(id: Int, asw: AppSwitch, ed: GosPackageState.Editor) {
        when (id) {
            ID_ONE_TIME -> {
                asw.set(ed, false)
                asw.removeSuppressNotificationFlag(ed)
            }
            ID_OFF -> {
                asw.set(ed, false)
                asw.addSuppressNotificationFlag(ed)
            }
            else -> super.onEntrySelectedInner(id, asw, ed)
        }
    }

    override fun getSummaryForImmutabilityReason(ir: Int): CharSequence? {
        val id = when (ir) {
            AppSwitch.IR_IS_SYSTEM_APP -> R.string.app_clipboard_ir_preinstalled_app
            else -> return null
        }
        return getText(id)
    }

    override fun getSummaryForDefaultValueReason(dvr: Int): CharSequence? {
        val id = when (dvr) {
            AppSwitch.DVR_DEFAULT_SETTING -> R.string.dvr_default_privacy_setting
            else -> return null
        }
        return getText(id)
    }

    override fun updateFooter(fp: FooterPreference) {
        fp.setTitle(R.string.app_clipboard_pref_footer)
    }

    override fun shouldKillUidAfterChange() = false
}
