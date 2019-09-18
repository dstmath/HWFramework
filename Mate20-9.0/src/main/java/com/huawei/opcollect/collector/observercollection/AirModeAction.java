package com.huawei.opcollect.collector.observercollection;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class AirModeAction extends Action {
    private static final String TAG = "AirModeAction";
    private static AirModeAction sInstance = null;
    private Handler mHandler = new Handler();
    private ContentObserver mObserver = null;

    private final class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            AirModeAction.this.perform();
        }
    }

    public static synchronized AirModeAction getInstance(Context context) {
        AirModeAction airModeAction;
        synchronized (AirModeAction.class) {
            if (sInstance == null) {
                sInstance = new AirModeAction(context, "AirModeAction");
            }
            airModeAction = sInstance;
        }
        return airModeAction;
    }

    private AirModeAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_AIRPLANE_ON) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_AIRPLANE_OFF));
    }

    public void enable() {
        super.enable();
        if (this.mObserver == null) {
            if (this.mContext == null) {
                OPCollectLog.e("AirModeAction", "context is null");
                return;
            }
            this.mObserver = new MyContentObserver(this.mHandler);
            try {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), true, this.mObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e("AirModeAction", "registerContentObserver failed: " + e.getMessage());
            }
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (AirModeAction.class) {
            sInstance = null;
        }
    }

    public void disable() {
        super.disable();
        if (this.mObserver != null && this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (this.mContext == null) {
            OPCollectLog.e("AirModeAction", "context is null");
            return false;
        }
        try {
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on") == 0) {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_AIRPLANE_OFF);
            } else {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_AIRPLANE_ON);
            }
            return true;
        } catch (Settings.SettingNotFoundException e) {
            OPCollectLog.e("AirModeAction", "SettingNotFoundException:" + e.getMessage());
            return false;
        }
    }

    public boolean perform() {
        return super.perform();
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
