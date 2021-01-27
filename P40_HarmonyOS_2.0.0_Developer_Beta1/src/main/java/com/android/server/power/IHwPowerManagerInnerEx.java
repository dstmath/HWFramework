package com.android.server.power;

public class IHwPowerManagerInnerEx {
    private IHwPowerManagerInner mIhwPowerInner = null;

    public void setIHwPowerManagerInner(IHwPowerManagerInner hwPowerInner) {
        this.mIhwPowerInner = hwPowerInner;
    }

    public boolean isAwarePreventScreenOn(String pkgName, String tag) {
        IHwPowerManagerInner iHwPowerManagerInner = this.mIhwPowerInner;
        if (iHwPowerManagerInner == null || iHwPowerManagerInner.getPowerMonitor() == null) {
            return false;
        }
        return this.mIhwPowerInner.getPowerMonitor().isAwarePreventScreenOn(pkgName, tag);
    }

    public void sendNoUserActivityNotification(int customActivityTimeout) {
        IHwPowerManagerInner iHwPowerManagerInner = this.mIhwPowerInner;
        if (iHwPowerManagerInner != null) {
            iHwPowerManagerInner.sendNoUserActivityNotification(customActivityTimeout);
        }
    }
}
