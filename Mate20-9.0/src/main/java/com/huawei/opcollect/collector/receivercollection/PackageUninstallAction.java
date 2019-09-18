package com.huawei.opcollect.collector.receivercollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;

public class PackageUninstallAction extends Action {
    private static final String TAG = "PackageUninstallAction";
    private static PackageUninstallAction sInstance = null;
    /* access modifiers changed from: private */
    public String mPackageName = null;
    private AppChangeReceiver mReceiver = null;

    class AppChangeReceiver extends BroadcastReceiver {
        AppChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    OPCollectLog.r("PackageUninstallAction", "onReceive action: " + action);
                    if ("android.intent.action.PACKAGE_REMOVED".equals(action) && Boolean.FALSE.booleanValue() == intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                        String unused = PackageUninstallAction.this.mPackageName = intent.getData().getSchemeSpecificPart();
                        PackageUninstallAction.this.perform();
                    }
                }
            }
        }
    }

    public static synchronized PackageUninstallAction getInstance(Context context) {
        PackageUninstallAction packageUninstallAction;
        synchronized (PackageUninstallAction.class) {
            if (sInstance == null) {
                sInstance = new PackageUninstallAction(context, "PackageUninstallAction");
            }
            packageUninstallAction = sInstance;
        }
        return packageUninstallAction;
    }

    private PackageUninstallAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(SysEventUtil.querySysEventDailyCount(SysEventUtil.EVENT_APP_UNINSTALL));
        OPCollectLog.r("PackageUninstallAction", "PackageUninstallAction");
    }

    public void enable() {
        super.enable();
        if (this.mReceiver == null && this.mContext != null) {
            this.mReceiver = new AppChangeReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addDataScheme("package");
            this.mContext.registerReceiver(this.mReceiver, intentFilter, null, OdmfCollectScheduler.getInstance().getCtrlHandler());
            OPCollectLog.r("PackageUninstallAction", "enabled");
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        SysEventUtil.collectSysEventData(SysEventUtil.EVENT_APP_UNINSTALL, this.mPackageName);
        this.mPackageName = null;
        return true;
    }

    public boolean perform() {
        return super.perform();
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

    private static synchronized void destroyInstance() {
        synchronized (PackageUninstallAction.class) {
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
