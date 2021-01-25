package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.math.Vector3;

public class LightComponent implements Component {
    private Vector3 mColor;
    private float mIntensity;
    private boolean mIsShadowEnabled;
    private LightType mLightType;
    private float mRange;
    private float mSpotInnerAngle;
    private float mSpotOuterAngle;

    public enum LightType {
        INVALID,
        DIRECTIONAL,
        POINT,
        SPOT
    }

    public LightType getLightType() {
        return this.mLightType;
    }

    public void setLightType(LightType lightType) {
        this.mLightType = lightType;
    }

    public Vector3 getColor() {
        return this.mColor;
    }

    public void setColor(Vector3 vector3) {
        this.mColor = vector3;
    }

    public float getIntensity() {
        return this.mIntensity;
    }

    public void setIntensity(float f) {
        this.mIntensity = f;
    }

    public float getRange() {
        return this.mRange;
    }

    public void setRange(float f) {
        this.mRange = f;
    }

    public float getSpotInnerAngle() {
        return this.mSpotInnerAngle;
    }

    public void setSpotInnerAngle(float f) {
        this.mSpotInnerAngle = f;
    }

    public float getSpotOuterAngle() {
        return this.mSpotOuterAngle;
    }

    public void setSpotOuterAngle(float f) {
        this.mSpotOuterAngle = f;
    }

    public boolean isShadowEnabled() {
        return this.mIsShadowEnabled;
    }

    public void setShadowEnabled(boolean z) {
        this.mIsShadowEnabled = z;
    }
}
