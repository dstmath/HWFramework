package android.widget.sr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PowerModeReceiver extends BroadcastReceiver {
    public static final String CHANGE_MODE_ACTION = "huawei.intent.action.POWER_MODE_CHANGED_ACTION";
    public static final String CHANGE_MODE_STATE = "state";
    public static final int NORMAL_DEFAULT_MODE_FLAG = 2;
    public static final int NORMAL_POWER_SAVE_MODE_FLAG = 1;
    public static final String TAG = "BroadcastReceiver";
    private PowerMode mPowerMode;

    public PowerModeReceiver(PowerMode powerMode) {
        this.mPowerMode = powerMode;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.w(TAG, "PowerModeReceiver.onReceive [intent == null]");
            return;
        }
        if (CHANGE_MODE_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.w(TAG, "PowerModeReceiver.onReceive [bundle == null]");
                return;
            }
            int powerValue = bundle.getInt("state");
            if (this.mPowerMode != null && 1 == powerValue) {
                Log.i(TAG, "Enter PowerSaveMode");
                this.mPowerMode.setNormalPowerSaveMode(true);
            } else if (this.mPowerMode != null && 2 == powerValue) {
                Log.i(TAG, "Leave PowerSaveMode");
                this.mPowerMode.setNormalPowerSaveMode(false);
            }
        }
    }
}
