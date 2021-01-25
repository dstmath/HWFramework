package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreShaderSpecializationRenderPod {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreShaderSpecializationRenderPod(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreShaderSpecializationRenderPod obj) {
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
                CoreJni.deleteCoreShaderSpecializationRenderPod(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    static long getMaxSpecializationConstantCount() {
        return CoreJni.getVarmaxSpecializationConstantCountCoreShaderSpecializationRenderPod();
    }

    static class CoreConstantFlags {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreConstantFlags(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreConstantFlags obj) {
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
                    CoreJni.deleteCoreShaderSpecializationRenderPodCoreConstantFlags(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void setValue(long value) {
            CoreJni.setVarvalueCoreShaderSpecializationRenderPodCoreConstantFlags(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public long getValue() {
            return CoreJni.getVarvalueCoreShaderSpecializationRenderPodCoreConstantFlags(this.agpCptr, this);
        }

        CoreConstantFlags() {
            this(CoreJni.newCoreConstantFlags(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSpecializationConstantCount(long value) {
        CoreJni.setVarspecializationConstantCountCoreShaderSpecializationRenderPod(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getSpecializationConstantCount() {
        return CoreJni.getVarspecializationConstantCountCoreShaderSpecializationRenderPod(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpecializationFlags(CoreConstantFlags value) {
        CoreJni.setVarspecializationFlagsCoreShaderSpecializationRenderPod(this.agpCptr, this, CoreConstantFlags.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreConstantFlags getSpecializationFlags() {
        long cptr = CoreJni.getVarspecializationFlagsCoreShaderSpecializationRenderPod(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreConstantFlags(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setConstant(int id, long value) {
        CoreJni.setConstantInCoreShaderSpecializationRenderPod(this.agpCptr, this, id, value);
    }

    CoreShaderSpecializationRenderPod() {
        this(CoreJni.newCoreShaderSpecializationRenderPod(), true);
    }
}
