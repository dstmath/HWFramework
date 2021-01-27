package com.android.server.rms.iaware;

import android.appwidget.IHwAWSIDAMonitorCallback;
import android.os.Binder;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;

/* access modifiers changed from: package-private */
public class AwareAwsiMonitorCallback extends IHwAWSIDAMonitorCallback.Stub {
    private static final String TAG = "AwareAWSIMonitorCallback";

    AwareAwsiMonitorCallback() {
    }

    public void updateWidgetFlushReport(int userId, String packageName) {
        if (HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle args = new Bundle();
            args.putInt("userid", userId);
            args.putString("widget", packageName);
            args.putInt("relationType", 32);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
            long origId = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }
}
