package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.math.Vector3;

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

    public void setColor(Vector3 color) {
        this.mColor = color;
    }

    public float getIntensity() {
        return this.mIntensity;
    }

    public void setIntensity(float intensity) {
        this.mIntensity = intensity;
    }

    public float getRange() {
        return this.mRange;
    }

    public void setRange(float range) {
        this.mRange = range;
    }

    public float getSpotInnerAngle() {
        return this.mSpotInnerAngle;
    }

    public void setSpotInnerAngle(float spotInnerAngle) {
        this.mSpotInnerAngle = spotInnerAngle;
    }

    public float getSpotOuterAngle() {
        return this.mSpotOuterAngle;
    }

    public void setSpotOuterAngle(float spotOuterAngle) {
        this.mSpotOuterAngle = spotOuterAngle;
    }

    public boolean isShadowEnabled() {
        return this.mIsShadowEnabled;
    }

    public void setShadowEnabled(boolean isShadowEnabled) {
        this.mIsShadowEnabled = isShadowEnabled;
    }
}
