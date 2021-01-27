package ohos.agp.render.render3d.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreSwapchainCreateInfo {
    private transient long agpCptrSwapchainCreateInfo;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreSwapchainCreateInfo(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrSwapchainCreateInfo = j;
    }

    static long getCptr(CoreSwapchainCreateInfo coreSwapchainCreateInfo) {
        if (coreSwapchainCreateInfo == null) {
            return 0;
        }
        return coreSwapchainCreateInfo.agpCptrSwapchainCreateInfo;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrSwapchainCreateInfo != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSwapchainCreateInfo(this.agpCptrSwapchainCreateInfo);
                }
                this.agpCptrSwapchainCreateInfo = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSwapchainCreateInfo coreSwapchainCreateInfo, boolean z) {
        if (coreSwapchainCreateInfo != null) {
            synchronized (coreSwapchainCreateInfo.delLock) {
                coreSwapchainCreateInfo.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreSwapchainCreateInfo);
    }

    /* access modifiers changed from: package-private */
    public void setPreferSrgbFormat(boolean z) {
        CoreJni.setVarpreferSrgbFormatCoreSwapchainCreateInfo(this.agpCptrSwapchainCreateInfo, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean getPreferSrgbFormat() {
        return CoreJni.getVarpreferSrgbFormatCoreSwapchainCreateInfo(this.agpCptrSwapchainCreateInfo, this);
    }

    /* access modifiers changed from: package-private */
    public void setSwapchainFlags(long j) {
        CoreJni.setVarswapchainFlagsCoreSwapchainCreateInfo(this.agpCptrSwapchainCreateInfo, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getSwapchainFlags() {
        return CoreJni.getVarswapchainFlagsCoreSwapchainCreateInfo(this.agpCptrSwapchainCreateInfo, this);
    }

    CoreSwapchainCreateInfo(BigInteger bigInteger, boolean z) {
        this(CoreJni.newCoreSwapchainCreateInfo(bigInteger, z), true);
    }
}
