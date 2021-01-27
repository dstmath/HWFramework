package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreLightComponent {
    private transient long agpCptrCoreLightComponent;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreLightComponent(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreLightComponent = j;
    }

    static long getCptr(CoreLightComponent coreLightComponent) {
        if (coreLightComponent == null) {
            return 0;
        }
        return coreLightComponent.agpCptrCoreLightComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreLightComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreLightComponent(this.agpCptrCoreLightComponent);
                }
                this.agpCptrCoreLightComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreLightComponent coreLightComponent, boolean z) {
        if (coreLightComponent != null) {
            synchronized (coreLightComponent.lock) {
                coreLightComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreLightComponent);
    }

    /* access modifiers changed from: package-private */
    public void setType(short s) {
        CoreJni.setVartypeCoreLightComponent(this.agpCptrCoreLightComponent, this, s);
    }

    /* access modifiers changed from: package-private */
    public short getType() {
        return CoreJni.getVartypeCoreLightComponent(this.agpCptrCoreLightComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setColor(CoreVec3 coreVec3) {
        CoreJni.setVarcolorCoreLightComponent(this.agpCptrCoreLightComponent, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getColor() {
        long varcolorCoreLightComponent = CoreJni.getVarcolorCoreLightComponent(this.agpCptrCoreLightComponent, this);
        if (varcolorCoreLightComponent == 0) {
            return null;
        }
        return new CoreVec3(varcolorCoreLightComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setIntensity(float f) {
        CoreJni.setVarintensityCoreLightComponent(this.agpCptrCoreLightComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getIntensity() {
        return CoreJni.getVarintensityCoreLightComponent(this.agpCptrCoreLightComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setRange(float f) {
        CoreJni.setVarrangeCoreLightComponent(this.agpCptrCoreLightComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getRange() {
        return CoreJni.getVarrangeCoreLightComponent(this.agpCptrCoreLightComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpotInnerAngle(float f) {
        CoreJni.setVarspotInnerAngleCoreLightComponent(this.agpCptrCoreLightComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getSpotInnerAngle() {
        return CoreJni.getVarspotInnerAngleCoreLightComponent(this.agpCptrCoreLightComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpotOuterAngle(float f) {
        CoreJni.setVarspotOuterAngleCoreLightComponent(this.agpCptrCoreLightComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getSpotOuterAngle() {
        return CoreJni.getVarspotOuterAngleCoreLightComponent(this.agpCptrCoreLightComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setShadowEnabled(boolean z) {
        CoreJni.setVarshadowEnabledCoreLightComponent(this.agpCptrCoreLightComponent, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean getShadowEnabled() {
        return CoreJni.getVarshadowEnabledCoreLightComponent(this.agpCptrCoreLightComponent, this);
    }

    CoreLightComponent() {
        this(CoreJni.newCoreLightComponent(), true);
    }
}
