package com.android.server.power.batterysaver;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import com.android.server.power.batterysaver.BatterySaverController;

public class BatterySaverLocationPlugin implements BatterySaverController.Plugin {
    private static final boolean DEBUG = false;
    private static final String TAG = "BatterySaverLocationPlugin";
    private final Context mContext;

    public BatterySaverLocationPlugin(Context context) {
        this.mContext = context;
    }

    @Override // com.android.server.power.batterysaver.BatterySaverController.Plugin
    public void onBatterySaverChanged(BatterySaverController caller) {
        updateLocationState(caller);
    }

    @Override // com.android.server.power.batterysaver.BatterySaverController.Plugin
    public void onSystemReady(BatterySaverController caller) {
        updateLocationState(caller);
    }

    private void updateLocationState(BatterySaverController caller) {
        int i = 1;
        boolean kill = caller.getBatterySaverPolicy().getGpsMode() == 2 && !caller.isInteractive();
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (!kill) {
            i = 0;
        }
        Settings.Global.putInt(contentResolver, "location_global_kill_switch", i);
    }
}
