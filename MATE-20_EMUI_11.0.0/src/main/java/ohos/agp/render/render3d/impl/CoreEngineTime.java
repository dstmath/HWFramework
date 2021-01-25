package ohos.agp.render.render3d.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreEngineTime {
    private transient long agpCptrCoreEngineTime;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreEngineTime(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreEngineTime = j;
    }

    static long getCptr(CoreEngineTime coreEngineTime) {
        if (coreEngineTime == null) {
            return 0;
        }
        return coreEngineTime.agpCptrCoreEngineTime;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreEngineTime != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEngineTime(this.agpCptrCoreEngineTime);
                }
                this.agpCptrCoreEngineTime = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEngineTime coreEngineTime, boolean z) {
        if (coreEngineTime != null) {
            synchronized (coreEngineTime.lock) {
                coreEngineTime.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEngineTime);
    }

    /* access modifiers changed from: package-private */
    public void setTotalTime(BigInteger bigInteger) {
        CoreJni.setVartotalTimeCoreEngineTime(this.agpCptrCoreEngineTime, this, bigInteger);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getTotalTime() {
        return CoreJni.getVartotalTimeCoreEngineTime(this.agpCptrCoreEngineTime, this);
    }

    /* access modifiers changed from: package-private */
    public void setDeltaTime(BigInteger bigInteger) {
        CoreJni.setVardeltaTimeCoreEngineTime(this.agpCptrCoreEngineTime, this, bigInteger);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getDeltaTime() {
        return CoreJni.getVardeltaTimeCoreEngineTime(this.agpCptrCoreEngineTime, this);
    }

    CoreEngineTime() {
        this(CoreJni.newCoreEngineTime(), true);
    }
}
