package com.android.settings.privacy;

import android.content.Context;
import android.ext.settings.ClipboardReadSetting;
import android.ext.settings.ExtSettings;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.ext.IntSettingPrefController;
import com.android.settings.ext.RadioButtonPickerFragment2;

public class ClipboardReadPreferenceController extends IntSettingPrefController {

    public ClipboardReadPreferenceController(Context ctx, String key) {
        super(ctx, key, ExtSettings.CLIPBOARD_READ_ACCESS);
    }

    @Override
    protected void getEntries(Entries entries) {
        entries.add(R.string.clipboard_read_allowed, ClipboardReadSetting.ALLOWED);
        entries.add(R.string.clipboard_read_ask_every_time, ClipboardReadSetting.ASK_EVERY_TIME);
        entries.add(R.string.clipboard_read_blocked, ClipboardReadSetting.BLOCKED);
    }

    @Override
    public void addPrefsAfterList(RadioButtonPickerFragment2 fragment, PreferenceScreen screen) {
        addFooterPreference(screen, R.string.clipboard_read_pref_footer);
    }
}
