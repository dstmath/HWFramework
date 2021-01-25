package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreLightComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreLightComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreLightComponent obj) {
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
                CoreJni.deleteCoreLightComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setType(short value) {
        CoreJni.setVartypeCoreLightComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public short getType() {
        return CoreJni.getVartypeCoreLightComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setColor(CoreVec3 value) {
        CoreJni.setVarcolorCoreLightComponent(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getColor() {
        long cptr = CoreJni.getVarcolorCoreLightComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setIntensity(float value) {
        CoreJni.setVarintensityCoreLightComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getIntensity() {
        return CoreJni.getVarintensityCoreLightComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRange(float value) {
        CoreJni.setVarrangeCoreLightComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getRange() {
        return CoreJni.getVarrangeCoreLightComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpotInnerAngle(float value) {
        CoreJni.setVarspotInnerAngleCoreLightComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getSpotInnerAngle() {
        return CoreJni.getVarspotInnerAngleCoreLightComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpotOuterAngle(float value) {
        CoreJni.setVarspotOuterAngleCoreLightComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getSpotOuterAngle() {
        return CoreJni.getVarspotOuterAngleCoreLightComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setShadowEnabled(boolean isEnabled) {
        CoreJni.setVarshadowEnabledCoreLightComponent(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getShadowEnabled() {
        return CoreJni.getVarshadowEnabledCoreLightComponent(this.agpCptr, this);
    }

    CoreLightComponent() {
        this(CoreJni.newCoreLightComponent(), true);
    }
}
