package com.huawei.agpengine.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreSwapchainCreateInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSwapchainCreateInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSwapchainCreateInfo obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreSwapchainCreateInfo(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPreferSrgbFormat(boolean isEnabled) {
        CoreJni.setVarpreferSrgbFormatCoreSwapchainCreateInfo(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getPreferSrgbFormat() {
        return CoreJni.getVarpreferSrgbFormatCoreSwapchainCreateInfo(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSwapchainFlags(long value) {
        CoreJni.setVarswapchainFlagsCoreSwapchainCreateInfo(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getSwapchainFlags() {
        return CoreJni.getVarswapchainFlagsCoreSwapchainCreateInfo(this.agpCptr, this);
    }

    CoreSwapchainCreateInfo(BigInteger surfaceHandle, boolean isVsync) {
        this(CoreJni.newCoreSwapchainCreateInfo(surfaceHandle, isVsync), true);
    }
}
