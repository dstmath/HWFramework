package com.huawei.opcollect.collector.observercollection;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class RotationStateAction extends Action {
    private static final String TAG = "RotationStateAction";
    private static RotationStateAction sInstance = null;
    private Handler mHandler = new Handler();
    private ContentObserver mObserver = null;

    private final class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            RotationStateAction.this.perform();
        }
    }

    public static synchronized RotationStateAction getInstance(Context context) {
        RotationStateAction rotationStateAction;
        synchronized (RotationStateAction.class) {
            if (sInstance == null) {
                sInstance = new RotationStateAction(context, "RotationStateAction");
            }
            rotationStateAction = sInstance;
        }
        return rotationStateAction;
    }

    private RotationStateAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_ROTATE_ON) + SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_ROTATE_OFF));
    }

    public void enable() {
        super.enable();
        if (this.mObserver == null && this.mContext != null) {
            this.mObserver = new MyContentObserver(this.mHandler);
            try {
                this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("accelerometer_rotation"), true, this.mObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e("RotationStateAction", "registerContentObserver failed: " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (this.mContext == null) {
            OPCollectLog.e("RotationStateAction", "context is null.");
            return false;
        }
        try {
            if (Settings.System.getInt(this.mContext.getContentResolver(), "accelerometer_rotation") == 0) {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_ROTATE_OFF);
            } else {
                SysEventUtil.collectSysEventData(SysEventUtil.EVENT_ROTATE_ON);
            }
            return true;
        } catch (Settings.SettingNotFoundException e) {
            OPCollectLog.e("RotationStateAction", "SettingNotFoundException:" + e.getMessage());
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
        synchronized (RotationStateAction.class) {
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
