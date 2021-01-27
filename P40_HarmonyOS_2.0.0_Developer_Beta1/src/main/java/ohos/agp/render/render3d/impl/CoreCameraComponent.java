package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreCameraComponent {
    private transient long agpCptrCoreCameraComponent;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreCameraComponent(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreCameraComponent = j;
    }

    static long getCptr(CoreCameraComponent coreCameraComponent) {
        if (coreCameraComponent == null) {
            return 0;
        }
        return coreCameraComponent.agpCptrCoreCameraComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreCameraComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreCameraComponent(this.agpCptrCoreCameraComponent);
                }
                this.agpCptrCoreCameraComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreCameraComponent coreCameraComponent, boolean z) {
        if (coreCameraComponent != null) {
            synchronized (coreCameraComponent.lock) {
                coreCameraComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreCameraComponent);
    }

    /* access modifiers changed from: package-private */
    public void setName(String str) {
        CoreJni.setVarnameCoreCameraComponent(this.agpCptrCoreCameraComponent, this, str);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setType(short s) {
        CoreJni.setVartypeCoreCameraComponent(this.agpCptrCoreCameraComponent, this, s);
    }

    /* access modifiers changed from: package-private */
    public short getType() {
        return CoreJni.getVartypeCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setAdditionalFlags(short s) {
        CoreJni.setVaradditionalFlagsCoreCameraComponent(this.agpCptrCoreCameraComponent, this, s);
    }

    /* access modifiers changed from: package-private */
    public short getAdditionalFlags() {
        return CoreJni.getVaradditionalFlagsCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setAspect(float f) {
        CoreJni.setVaraspectCoreCameraComponent(this.agpCptrCoreCameraComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getAspect() {
        return CoreJni.getVaraspectCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setVerticalFov(float f) {
        CoreJni.setVarverticalFovCoreCameraComponent(this.agpCptrCoreCameraComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getVerticalFov() {
        return CoreJni.getVarverticalFovCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setOrthoWidth(float f) {
        CoreJni.setVarorthoWidthCoreCameraComponent(this.agpCptrCoreCameraComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getOrthoWidth() {
        return CoreJni.getVarorthoWidthCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setOrthoHeight(float f) {
        CoreJni.setVarorthoHeightCoreCameraComponent(this.agpCptrCoreCameraComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getOrthoHeight() {
        return CoreJni.getVarorthoHeightCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setZnear(float f) {
        CoreJni.setVarznearCoreCameraComponent(this.agpCptrCoreCameraComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getZnear() {
        return CoreJni.getVarznearCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setZfar(float f) {
        CoreJni.setVarzfarCoreCameraComponent(this.agpCptrCoreCameraComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getZfar() {
        return CoreJni.getVarzfarCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setViewport(float[] fArr) {
        CoreJni.setVarviewportCoreCameraComponent(this.agpCptrCoreCameraComponent, this, fArr);
    }

    /* access modifiers changed from: package-private */
    public float[] getViewport() {
        return CoreJni.getVarviewportCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setRenderResolution(long[] jArr) {
        CoreJni.setVarrenderResolutionCoreCameraComponent(this.agpCptrCoreCameraComponent, this, jArr);
    }

    /* access modifiers changed from: package-private */
    public long[] getRenderResolution() {
        return CoreJni.getVarrenderResolutionCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setProjection(CoreMat4X4 coreMat4X4) {
        CoreJni.setVarprojectionCoreCameraComponent(this.agpCptrCoreCameraComponent, this, CoreMat4X4.getCptr(coreMat4X4), coreMat4X4);
    }

    /* access modifiers changed from: package-private */
    public CoreMat4X4 getProjection() {
        long varprojectionCoreCameraComponent = CoreJni.getVarprojectionCoreCameraComponent(this.agpCptrCoreCameraComponent, this);
        if (varprojectionCoreCameraComponent == 0) {
            return null;
        }
        return new CoreMat4X4(varprojectionCoreCameraComponent, false);
    }

    CoreCameraComponent() {
        this(CoreJni.newCoreCameraComponent(), true);
    }
}
