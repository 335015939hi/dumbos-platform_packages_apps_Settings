package com.android.settings.security;

import android.content.Context;
import android.ext.settings.BoolSetting;
import android.ext.settings.ExtSettings;
import android.net.Uri;
import android.os.PowerManager;

import androidx.appcompat.app.AlertDialog;

import com.android.settings.R;
import com.android.settings.ext.BoolSettingFragment;
import com.android.settingslib.widget.FooterPreference;

import static java.util.Objects.requireNonNull;

public class ExecSpawningFragment extends BoolSettingFragment {

    @Override
    protected BoolSetting getSetting() {
        return ExtSettings.EXEC_SPAWNING;
    }

    @Override
    protected CharSequence getTitle() {
        return getText(R.string.exec_spawning_title);
    }

    @Override
    protected CharSequence getMainSwitchTitle() {
        return getText(R.string.exec_spawning_title_inner);
    }

    @Override
    protected boolean interceptMainSwitchChange(boolean newValue) {
        Context ctx = requireContext();
        var b = new AlertDialog.Builder(ctx);
        b.setMessage(R.string.exec_spawning_reboot_dialog);
        b.setPositiveButton(R.string.exec_spawning_reboot_dialog_btn_restart, (dialog, which) -> {
            var powerManager = requireNonNull(ctx.getSystemService(PowerManager.class));
            if (ExtSettings.EXEC_SPAWNING.put(newValue)) {
                powerManager.reboot(null);
            }
        });
        b.show();
        return true;
    }

    @Override
    protected FooterPreference makeFooterPref(FooterPreference.Builder builder) {
        FooterPreference p = builder.setTitle(R.string.exec_spawning_footer).build();
        setFooterPrefLearnMoreUri(p, Uri.parse("https://grapheneos.org/usage#exec-spawning"));
        return p;
    }
}
