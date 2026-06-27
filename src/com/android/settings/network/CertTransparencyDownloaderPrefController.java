package com.android.settings.network;

import android.content.Context;
import android.ext.settings.CertTransparencyDownloaderSetting;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.ext.IntSettingPrefController;
import com.android.settings.ext.RadioButtonPickerFragment2;

public class CertTransparencyDownloaderPrefController extends IntSettingPrefController {

    public CertTransparencyDownloaderPrefController(Context ctx, String key) {
        super(ctx, key, CertTransparencyDownloaderSetting.SETTING);
    }

    @Override
    protected void getEntries(Entries entries) {
        entries.add(R.string.cert_transparency_dl_grapheneos_server,
                CertTransparencyDownloaderSetting.VAL_GRAPHENEOS);
        entries.add(R.string.cert_transparency_dl_standard_server,
                CertTransparencyDownloaderSetting.VAL_STANDARD);
        entries.add(R.string.cert_transparency_dl_off, CertTransparencyDownloaderSetting.VAL_OFF);
    }

    @Override
    public void addPrefsAfterList(RadioButtonPickerFragment2 fragment, PreferenceScreen screen) {
        addFooterPreference(screen, R.string.cert_transparency_dl_footer);
    }
}
