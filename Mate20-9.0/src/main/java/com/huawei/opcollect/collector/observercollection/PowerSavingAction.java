package com.huawei.opcollect.collector.observercollection;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class PowerSavingAction extends Action {
    private static final Object LOCK = new Object();
    public static final String SMART_MODE_STATUS = "SmartModeStatus";
    private static final String TAG = "PowerSavingAction";
    private static PowerSavingAction sInstance = null;
    private Handler mHandler = new Handler();
    private ContentObserver mObserver = null;

    private final class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            OPCollectLog.i(PowerSavingAction.TAG, "onChange");
            PowerSavingAction.this.perform();
        }
    }

    public static PowerSavingAction getInstance(Context context) {
        PowerSavingAction powerSavingAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new PowerSavingAction(context, SysEventUtil.POWER_SAVING_STATUS);
            }
            powerSavingAction = sInstance;
        }
        return powerSavingAction;
    }

    private PowerSavingAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.POWER_SAVING_STATUS));
    }

    public void enable() {
        super.enable();
        if (this.mObserver == null && this.mContext != null) {
            this.mObserver = new MyContentObserver(this.mHandler);
            try {
                this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SMART_MODE_STATUS), true, this.mObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e(TAG, "registerContentObserver failed: " + e.getMessage());
            }
            SysEventUtil.collectKVSysEventData("battery/power_saving_status", SysEventUtil.POWER_SAVING_STATUS, getPowerSavingState());
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (this.mContext == null) {
            OPCollectLog.e(TAG, "context is null.");
            return false;
        }
        SysEventUtil.collectSysEventData(SysEventUtil.POWER_SAVING_STATUS, getPowerSavingState());
        SysEventUtil.collectKVSysEventData("battery/power_saving_status", SysEventUtil.POWER_SAVING_STATUS, getPowerSavingState());
        return true;
    }

    private String getPowerSavingState() {
        int state = 0;
        try {
            state = Settings.System.getInt(this.mContext.getContentResolver(), SMART_MODE_STATUS);
        } catch (Settings.SettingNotFoundException e) {
            OPCollectLog.e(TAG, "SettingNotFoundException:" + e.getMessage());
        }
        if (state == 4) {
            return SysEventUtil.ON;
        }
        return SysEventUtil.OFF;
    }

    public void disable() {
        super.disable();
        if (this.mObserver != null && this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static void destroyInstance() {
        synchronized (LOCK) {
            sInstance = null;
        }
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mObserver == null) {
                pw.println(indent + "observer is null");
            } else {
                pw.println(indent + "observer not null");
            }
        }
    }
}
