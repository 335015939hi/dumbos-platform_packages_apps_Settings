package com.android.settings.development;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.provider.Settings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

@RunWith(RobolectricTestRunner.class)
public class TetheringHardwareAccelFixupTest {
    private Context mContext;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        clearState();
    }

    @After
    public void tearDown() {
        clearState();
    }

    @Test
    public void run_devOptionsDisabledAndTetherOffloadDisabled_erasesSetting() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.TETHER_OFFLOAD_DISABLED,
                TetheringHardwareAccelPreferenceController.SETTING_VALUE_OFF);

        TetheringHardwareAccelFixup.run(mContext);

        assertThat(getTetherOffloadDisabledValue()).isNull();
        assertThat(TetheringHardwareAccelFixup.getMarkerFile(mContext).isFile()).isTrue();
    }

    @Test
    public void run_devOptionsDisabledAndTetherOffloadEnabled_erasesSetting() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.TETHER_OFFLOAD_DISABLED,
                TetheringHardwareAccelPreferenceController.SETTING_VALUE_ON);

        TetheringHardwareAccelFixup.run(mContext);

        assertThat(getTetherOffloadDisabledValue()).isNull();
        assertThat(TetheringHardwareAccelFixup.getMarkerFile(mContext).isFile()).isTrue();
    }

    @Test
    public void run_devOptionsEnabledAndTetherOffloadDisabled_keepsSetting() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.TETHER_OFFLOAD_DISABLED,
                TetheringHardwareAccelPreferenceController.SETTING_VALUE_OFF);

        TetheringHardwareAccelFixup.run(mContext);

        assertThat(getTetherOffloadDisabled()).isEqualTo(
                TetheringHardwareAccelPreferenceController.SETTING_VALUE_OFF);
        assertThat(TetheringHardwareAccelFixup.getMarkerFile(mContext).isFile()).isFalse();
    }

    @Test
    public void run_markerExists_doesNotChangeSetting() throws Exception {
        File markerFile = TetheringHardwareAccelFixup.getMarkerFile(mContext);
        assertThat(markerFile.createNewFile()).isTrue();
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.TETHER_OFFLOAD_DISABLED,
                TetheringHardwareAccelPreferenceController.SETTING_VALUE_OFF);

        TetheringHardwareAccelFixup.run(mContext);

        assertThat(getTetherOffloadDisabled()).isEqualTo(
                TetheringHardwareAccelPreferenceController.SETTING_VALUE_OFF);
    }

    private int getTetherOffloadDisabled() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.TETHER_OFFLOAD_DISABLED,
                TetheringHardwareAccelPreferenceController.SETTING_VALUE_ON);
    }

    private String getTetherOffloadDisabledValue() {
        return Settings.Global.getString(mContext.getContentResolver(),
                Settings.Global.TETHER_OFFLOAD_DISABLED);
    }

    private void clearState() {
        Settings.Global.putString(mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, null);
        Settings.Global.putString(mContext.getContentResolver(),
                Settings.Global.TETHER_OFFLOAD_DISABLED, null);
        TetheringHardwareAccelFixup.getMarkerFile(mContext).delete();
    }
}
