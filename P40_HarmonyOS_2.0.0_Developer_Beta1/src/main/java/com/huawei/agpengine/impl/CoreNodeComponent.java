package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreNodeComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreNodeComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreNodeComponent obj) {
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
                CoreJni.deleteCoreNodeComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setName(String value) {
        CoreJni.setVarnameCoreNodeComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCoreNodeComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setParent(int value) {
        CoreJni.setVarparentCoreNodeComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public int getParent() {
        return CoreJni.getVarparentCoreNodeComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean isEnabled) {
        CoreJni.setVarenabledCoreNodeComponent(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getEnabled() {
        return CoreJni.getVarenabledCoreNodeComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setEffectivelyEnabled(boolean isEnabled) {
        CoreJni.setVareffectivelyEnabledCoreNodeComponent(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getEffectivelyEnabled() {
        return CoreJni.getVareffectivelyEnabledCoreNodeComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setExported(boolean isEnabled) {
        CoreJni.setVarexportedCoreNodeComponent(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getExported() {
        return CoreJni.getVarexportedCoreNodeComponent(this.agpCptr, this);
    }

    CoreNodeComponent() {
        this(CoreJni.newCoreNodeComponent(), true);
    }
}
