package com.android.server.power.batterysaver;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;

public class HwCustBatterySaverControllerImpl extends HwCustBatterySaverController {
    private static final int GENIE_SAVER_MODE = 1;
    private static final int GENIE_SMART_MODE = 2;
    private static final boolean IS_USE_GBATTERY = SystemProperties.getBoolean("ro.config.use_gbattery", false);
    private static final int SAVER_MODE = 4;
    private static final int SMART_MODE = 1;
    private Context mContext;

    public HwCustBatterySaverControllerImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void setPowerSaverMode(boolean stateOn) {
        if (this.mContext != null && IS_USE_GBATTERY) {
            if (stateOn) {
                wirteSaverMode(SAVER_MODE, 1);
            } else {
                wirteSaverMode(1, GENIE_SMART_MODE);
            }
        }
    }

    private void wirteSaverMode(int saverModeStatus, int genieValue) {
        if (saverModeStatus != readSaverMode()) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "SmartModeStatus", saverModeStatus, 0);
            Intent intent = new Intent("huawei.intent.action.POWER_MODE_CHANGED_ACTION");
            intent.putExtra("state", genieValue);
            this.mContext.sendBroadcast(intent);
        }
    }

    private int readSaverMode() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), "SmartModeStatus", 1, 0);
    }
}
