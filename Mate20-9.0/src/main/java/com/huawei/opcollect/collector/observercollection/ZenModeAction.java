package com.huawei.opcollect.collector.observercollection;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class ZenModeAction extends Action {
    private static final Object LOCK = new Object();
    private static final String TAG = "ZenModeAction";
    public static final String ZEN_MODE = "zen_mode";
    private static ZenModeAction sInstance = null;
    private Handler mHandler = new Handler();
    private ContentObserver mObserver = null;

    private final class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            OPCollectLog.i(ZenModeAction.TAG, "onChange.");
            ZenModeAction.this.perform();
        }
    }

    public static ZenModeAction getInstance(Context context) {
        ZenModeAction zenModeAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new ZenModeAction(context, SysEventUtil.DISTURB_STATUS);
            }
            zenModeAction = sInstance;
        }
        return zenModeAction;
    }

    private ZenModeAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.DISTURB_STATUS));
    }

    public void enable() {
        super.enable();
        if (this.mObserver == null && this.mContext != null) {
            this.mObserver = new MyContentObserver(this.mHandler);
            try {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(ZEN_MODE), true, this.mObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e(TAG, "registerContentObserver failed: " + e.getMessage());
            }
            SysEventUtil.collectKVSysEventData("sound/no_disturb_status", SysEventUtil.DISTURB_STATUS, getZenModeState());
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (this.mContext == null) {
            OPCollectLog.e(TAG, "context is null.");
            return false;
        }
        SysEventUtil.collectSysEventData(SysEventUtil.DISTURB_STATUS, getZenModeState());
        SysEventUtil.collectKVSysEventData("sound/no_disturb_status", SysEventUtil.DISTURB_STATUS, getZenModeState());
        return true;
    }

    private String getZenModeState() {
        int state = 0;
        try {
            state = Settings.Global.getInt(this.mContext.getContentResolver(), ZEN_MODE);
        } catch (Settings.SettingNotFoundException e) {
            OPCollectLog.e(TAG, "SettingNotFoundException:" + e.getMessage());
        }
        if (state != 0) {
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
