package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreCameraComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreCameraComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreCameraComponent obj) {
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
                CoreJni.deleteCoreCameraComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setName(String value) {
        CoreJni.setVarnameCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setType(short value) {
        CoreJni.setVartypeCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public short getType() {
        return CoreJni.getVartypeCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setTargetType(short value) {
        CoreJni.setVartargetTypeCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public short getTargetType() {
        return CoreJni.getVartargetTypeCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setAdditionalFlags(short value) {
        CoreJni.setVaradditionalFlagsCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public short getAdditionalFlags() {
        return CoreJni.getVaradditionalFlagsCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setAspect(float value) {
        CoreJni.setVaraspectCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getAspect() {
        return CoreJni.getVaraspectCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVerticalFov(float value) {
        CoreJni.setVarverticalFovCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getVerticalFov() {
        return CoreJni.getVarverticalFovCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setOrthoWidth(float value) {
        CoreJni.setVarorthoWidthCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getOrthoWidth() {
        return CoreJni.getVarorthoWidthCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setOrthoHeight(float value) {
        CoreJni.setVarorthoHeightCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getOrthoHeight() {
        return CoreJni.getVarorthoHeightCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setZnear(float value) {
        CoreJni.setVarznearCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getZnear() {
        return CoreJni.getVarznearCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setZfar(float value) {
        CoreJni.setVarzfarCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getZfar() {
        return CoreJni.getVarzfarCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setViewport(float[] value) {
        CoreJni.setVarviewportCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getViewport() {
        return CoreJni.getVarviewportCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setScissor(float[] value) {
        CoreJni.setVarscissorCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getScissor() {
        return CoreJni.getVarscissorCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRenderResolution(long[] value) {
        CoreJni.setVarrenderResolutionCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long[] getRenderResolution() {
        return CoreJni.getVarrenderResolutionCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setProjection(CoreMat4X4 value) {
        CoreJni.setVarprojectionCoreCameraComponent(this.agpCptr, this, CoreMat4X4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreMat4X4 getProjection() {
        long cptr = CoreJni.getVarprojectionCoreCameraComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreMat4X4(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setCustomDepthTarget(long value) {
        CoreJni.setVarcustomDepthTargetCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getCustomDepthTarget() {
        return CoreJni.getVarcustomDepthTargetCoreCameraComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setCustomColorTarget(long value) {
        CoreJni.setVarcustomColorTargetCoreCameraComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getCustomColorTarget() {
        return CoreJni.getVarcustomColorTargetCoreCameraComponent(this.agpCptr, this);
    }

    CoreCameraComponent() {
        this(CoreJni.newCoreCameraComponent(), true);
    }
}
