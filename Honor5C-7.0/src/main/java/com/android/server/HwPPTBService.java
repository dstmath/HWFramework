package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.HwLog;
import android.util.Log;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;

public class HwPPTBService extends SystemService {
    private static final String TAG = "HwPPTBService";
    private Context mContext;
    private PGPlug mPGPlug;
    private PgEventProcesser mPgEventProcesser;
    private final BroadcastReceiver mReceiver;

    private class PgEventProcesser implements IPGPlugCallbacks {
        private PgEventProcesser() {
        }

        public void onDaemonConnected() {
        }

        public void onConnectedTimeout() {
        }

        public boolean onEvent(int actionID, String msg) {
            if (PGAction.checkActionType(actionID) == 1 && PGAction.checkActionFlag(actionID) == 3) {
                HwPPTBService.this.getForegroundPackage(msg);
            }
            return true;
        }
    }

    public void onStart() {
        Log.i(TAG, "start HwPPTBService");
        initBroadcastReceiver();
    }

    public HwPPTBService(Context context) {
        super(context);
        this.mPgEventProcesser = new PgEventProcesser();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                        HwPPTBService.this.initPgPlugThread();
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        HwPPTBService.this.getRemovedPackage(intent);
                    } else if ("android.intent.action.UID_REMOVED".equals(action)) {
                        HwPPTBService.this.getRemovedUid(intent);
                    } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                        HwPPTBService.this.notifyShutDown();
                    }
                    Log.d(HwPPTBService.TAG, "BroadcastReceiver " + action);
                }
            }
        };
        Log.i(TAG, TAG);
        this.mContext = context;
    }

    private void initBroadcastReceiver() {
        initGenericReceiver();
        initPackageReceiver();
    }

    private void initGenericReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.UID_REMOVED");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPackageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPgPlugThread() {
        this.mPGPlug = new PGPlug(this.mPgEventProcesser, TAG);
        new Thread(this.mPGPlug, TAG).start();
    }

    private void getForegroundPackage(String msg) {
        try {
            String[] splits = msg.split("\t");
            if (splits.length > 0) {
                HwLog.bdate("BDAT_TAG_SCENE_CHANGED", "name=" + splits[0]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        }
    }

    private void getRemovedPackage(Intent intent) {
        String pkgName = intent.getData().getSchemeSpecificPart();
        Bundle intentExtras = intent.getExtras();
        HwLog.bdate("BDAT_TAG_PACKAGE_REMOVED", "uid=" + (intentExtras != null ? intentExtras.getInt("android.intent.extra.UID") : -1) + " name=" + pkgName);
    }

    private void getRemovedUid(Intent intent) {
        Bundle intentExtras = intent.getExtras();
        HwLog.bdate("BDAT_TAG_UID_REMOVED", "uid=" + (intentExtras != null ? intentExtras.getInt("android.intent.extra.UID") : -1));
    }

    private void notifyShutDown() {
        HwLog.bdate("BDAT_TAG_SHUTDOWN", "status=1");
    }
}
