package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.ext.settings.ExtSettings;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.settings.ext.BoolSettingPrefController;

public class BluetoothBleOnlyPreferenceController extends BoolSettingPrefController {

    private static final String TAG = "BluetoothBleOnly";

    private static final long RESTART_TIMEOUT_MS = 10_000;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private BroadcastReceiver mRestartReceiver;
    private Runnable mRestartTimeout;

    public BluetoothBleOnlyPreferenceController(Context ctx, String key) {
        super(ctx, key, ExtSettings.BLUETOOTH_BLE_ONLY);
    }

    @Override
    public int getAvailabilityStatus() {
        int r = super.getAvailabilityStatus();
        if (r == AVAILABLE) {
            return mContext.getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                    ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        }
        return r;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        if (!super.setChecked(isChecked)) {
            return false;
        }
        restartAdapter();
        return true;
    }

    private void cancelPendingRestart() {
        if (mRestartTimeout != null) {
            mHandler.removeCallbacks(mRestartTimeout);
            mRestartTimeout = null;
        }
        if (mRestartReceiver != null) {
            try {
                mContext.getApplicationContext().unregisterReceiver(mRestartReceiver);
            } catch (IllegalArgumentException e) {
                // Already unregistered; nothing to do.
            }
            mRestartReceiver = null;
        }
    }

    private void restartAdapter() {
        // Drop any previous in-flight restart before starting a new one.
        cancelPendingRestart();

        BluetoothManager mgr = mContext.getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = mgr != null ? mgr.getAdapter() : null;
        if (adapter == null) {
            return;
        }
        if (!adapter.isEnabled() && !adapter.isLeEnabled()) {
            // Adapter is off: the next bring-up re-reads the property, nothing to restart.
            return;
        }

        final Context appContext = mContext.getApplicationContext();

        mRestartReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                int state = i.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state != BluetoothAdapter.STATE_OFF) {
                    return;
                }
                // Restart actually happened: re-enable, then clean up exactly once.
                cancelPendingRestart();
                Log.i(TAG, "re-enabling adapter to apply BLE-only policy change");
                adapter.enable();
            }
        };
        appContext.registerReceiver(mRestartReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        mRestartTimeout = () -> {
            // disable() never reached STATE_OFF. Unregister and, importantly, do NOT call
            // enable(): the adapter was never actually turned off.
            Log.w(TAG, "adapter did not reach STATE_OFF; abandoning restart");
            cancelPendingRestart();
        };
        mHandler.postDelayed(mRestartTimeout, RESTART_TIMEOUT_MS);

        Log.i(TAG, "restarting adapter to apply BLE-only policy change");
        // persist=false: this is a policy-driven restart, not a user disabling Bluetooth.
        if (!adapter.disable(false)) {
            Log.w(TAG, "adapter.disable() request failed; abandoning restart");
            cancelPendingRestart();
        }
    }
}
