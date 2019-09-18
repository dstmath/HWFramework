package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.AbsActionParam;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class NfcStateAction extends Action {
    private static final Object LOCK = new Object();
    private static final String TAG = "NfcStateAction";
    private static NfcStateAction sInstance = null;
    private NfcStateReceiver mReceiver = null;

    private class NfcStateActionParam extends AbsActionParam {
        private String state;

        NfcStateActionParam(String state2) {
            this.state = state2;
        }

        /* access modifiers changed from: package-private */
        public String getState() {
            return this.state;
        }
    }

    class NfcStateReceiver extends BroadcastReceiver {
        NfcStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                OPCollectLog.r(NfcStateAction.TAG, "onReceive action: " + action);
                if ("android.nfc.action.ADAPTER_STATE_CHANGED".equalsIgnoreCase(action)) {
                    int state = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1);
                    if (state == 1) {
                        boolean unused = NfcStateAction.this.performWithArgs(new NfcStateActionParam(SysEventUtil.OFF));
                    } else if (state == 3) {
                        boolean unused2 = NfcStateAction.this.performWithArgs(new NfcStateActionParam(SysEventUtil.ON));
                    }
                }
            }
        }
    }

    public static NfcStateAction getInstance(Context context) {
        NfcStateAction nfcStateAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new NfcStateAction(SysEventUtil.NFC_STATUS, context);
            }
            nfcStateAction = sInstance;
        }
        return nfcStateAction;
    }

    private NfcStateAction(String name, Context context) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.NFC_STATUS));
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new NfcStateReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED"), null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
            SysEventUtil.collectKVSysEventData("device_connection/nfc_status", SysEventUtil.NFC_STATUS, (nfcAdapter == null || !nfcAdapter.isEnabled()) ? SysEventUtil.OFF : SysEventUtil.ON);
            OPCollectLog.r(TAG, "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean executeWithArgs(AbsActionParam absActionParam) {
        if (absActionParam != null) {
            SysEventUtil.collectSysEventData(SysEventUtil.NFC_STATUS, ((NfcStateActionParam) absActionParam).getState());
            SysEventUtil.collectKVSysEventData("device_connection/nfc_status", SysEventUtil.NFC_STATUS, ((NfcStateActionParam) absActionParam).getState());
        }
        return true;
    }

    public void disable() {
        super.disable();
        if (this.mReceiver != null && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
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
            if (this.mReceiver == null) {
                pw.println(indent + "receiver is null");
            } else {
                pw.println(indent + "receiver not null");
            }
        }
    }
}
