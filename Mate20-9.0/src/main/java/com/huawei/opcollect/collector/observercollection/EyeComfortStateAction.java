package com.huawei.opcollect.collector.observercollection;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class EyeComfortStateAction extends Action {
    private static final String EYES_PROTECTION_SWITCH = "eyes_protection_mode";
    private static final String TAG = "EyeComfortStateAction";
    private static EyeComfortStateAction sInstance = null;
    private Handler mHandler = new Handler();
    private ContentObserver mObserver = null;

    private final class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            EyeComfortStateAction.this.perform();
        }
    }

    public static synchronized EyeComfortStateAction getInstance(Context context) {
        EyeComfortStateAction eyeComfortStateAction;
        synchronized (EyeComfortStateAction.class) {
            if (sInstance == null) {
                sInstance = new EyeComfortStateAction(context, "EyeComfortStateAction");
            }
            eyeComfortStateAction = sInstance;
        }
        return eyeComfortStateAction;
    }

    private EyeComfortStateAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_EYECOMFORT_ON) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_EYECOMFORT_OFF));
    }

    public void enable() {
        super.enable();
        if (this.mObserver == null) {
            if (this.mContext == null) {
                OPCollectLog.e("EyeComfortStateAction", "context is null");
                return;
            }
            this.mObserver = new MyContentObserver(this.mHandler);
            try {
                this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(EYES_PROTECTION_SWITCH), true, this.mObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e("EyeComfortStateAction", "registerContentObserver failed: " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (this.mContext == null) {
            OPCollectLog.e("EyeComfortStateAction", "context is null");
            return false;
        }
        try {
            if (Settings.System.getInt(this.mContext.getContentResolver(), EYES_PROTECTION_SWITCH) == 0) {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_EYECOMFORT_OFF);
            } else {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_EYECOMFORT_ON);
            }
            return true;
        } catch (Settings.SettingNotFoundException e) {
            OPCollectLog.e("EyeComfortStateAction", "SettingNotFoundException:" + e.getMessage());
            return false;
        }
    }

    public boolean perform() {
        return super.perform();
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

    private static synchronized void destroyInstance() {
        synchronized (EyeComfortStateAction.class) {
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
