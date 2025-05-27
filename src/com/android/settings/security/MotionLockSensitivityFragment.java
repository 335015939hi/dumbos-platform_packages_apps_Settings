package com.android.settings.security;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.widget.FooterPreference;
import java.util.ArrayList;
import java.util.List;
import com.android.settings.security.MotionLockConstants;
import com.android.settings.security.MotionLockSensitivity;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

public class MotionLockSensitivityFragment extends DashboardFragment {
    private static final String TAG = "MotionLockSensitivityFragment";
    private static final String KEY_MOTION_LOCK_SENSITIVITY = "motion_lock_sensitivity";
    private static final String KEY_MOTION_LOCK_FOOTER = "motion_lock_footer";

    private ListPreference mSensitivityPreference;
    private FooterPreference mFooterPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No manual preference addition; rely on XML
    }

    private void loadSettings() {
        final Context context = getContext();
        if (context == null) return;

        // Load sensitivity
        final int value = Settings.Secure.getInt(context.getContentResolver(),
                MotionLockConstants.KEY_MOTION_LOCK_SENSITIVITY,
                MotionLockSensitivity.MEDIUM);
        updateSensitivitySummary(value);
    }

    private void updateSensitivitySummary(int sensitivity) {
        if (mSensitivityPreference == null) return;

        String[] entries = getResources().getStringArray(R.array.motion_lock_sensitivity_entries);
        String[] values = getResources().getStringArray(R.array.motion_lock_sensitivity_values);
        for (int i = 0; i < values.length; i++) {
            if (Integer.parseInt(values[i]) == sensitivity) {
                mSensitivityPreference.setValue(values[i]);
                mSensitivityPreference.setSummary(entries[i]);
                break;
            }
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.motion_lock_sensitivity_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, MotionLockSensitivityFragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new MotionLockEnabledController(context));
        controllers.add(new MotionLockSensitivityController(context, fragment));
        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SECURITY;
    }
} 