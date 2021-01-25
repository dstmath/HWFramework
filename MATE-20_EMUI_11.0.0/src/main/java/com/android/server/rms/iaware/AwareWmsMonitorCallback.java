package com.android.server.rms.iaware;

import android.os.Binder;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import com.huawei.android.view.IHwWMDAMonitorCallback;

/* access modifiers changed from: package-private */
public class AwareWmsMonitorCallback extends IHwWMDAMonitorCallback.Stub {
    private static final String TAG = "AwareWmsMonitorCallback";

    AwareWmsMonitorCallback() {
    }

    public boolean isResourceNeeded(String resourceId) {
        return HwSysResManager.getInstance().isResourceNeeded(getResourceId(resourceId));
    }

    private int getResourceId(String resourceId) {
        if (resourceId == null) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_INVALIDE_TYPE);
        }
        if ("RESOURCE_APPASSOC".equals(resourceId)) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC);
        }
        if ("RESOURCE_SYSLOAD".equals(resourceId)) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SYSLOAD);
        }
        if ("RESOURCE_WINSTATE".equals(resourceId)) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_WINSTATE);
        }
        return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_INVALIDE_TYPE);
    }

    public void reportData(String resourceId, long timeStamp, Bundle args) {
        if (args != null) {
            CollectData data = new CollectData(getResourceId(resourceId), timeStamp, args);
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }
}
