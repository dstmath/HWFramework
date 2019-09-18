package com.huawei.opcollect.collector.receivercollection;

import android.content.Context;
import com.huawei.opcollect.strategy.AbsActionParam;
import com.huawei.opcollect.strategy.Action;

public class NfcConnectAction extends Action {
    private static final Object LOCK = new Object();
    private static final String TAG = "NfcConnectAction";
    private static NfcConnectAction sInstance = null;

    public static NfcConnectAction getInstance(Context context) {
        NfcConnectAction nfcConnectAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new NfcConnectAction(SysEventUtil.NFC_CONNECTION_STATUS, context);
            }
            nfcConnectAction = sInstance;
        }
        return nfcConnectAction;
    }

    private NfcConnectAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.NFC_CONNECTION_STATUS));
    }

    public void enable() {
        super.enable();
    }

    /* access modifiers changed from: protected */
    public boolean executeWithArgs(AbsActionParam absActionParam) {
        return true;
    }

    public void disable() {
        super.disable();
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
}
