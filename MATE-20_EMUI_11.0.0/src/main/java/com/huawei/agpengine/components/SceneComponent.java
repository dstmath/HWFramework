package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.Entity;
import com.huawei.agpengine.math.Quaternion;
import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.resources.GpuResourceHandle;

public class SceneComponent implements Component {
    private BackgroundType mBackgroundType;
    private Entity mCamera;
    private GpuResourceHandle mEnvMap;
    private Vector3 mEnvironmentDiffuseColor;
    private float mEnvironmentDiffuseIntensity;
    private Quaternion mEnvironmentRotation;
    private Vector3 mEnvironmentSpecularColor;
    private float mEnvironmentSpecularIntensity;
    private Vector3[] mIrradianceCoefficients;
    private GpuResourceHandle mRadianceCubemap;

    public enum BackgroundType {
        NONE,
        IMAGE,
        CUBEMAP,
        EQUIRECTANGULAR
    }

    public Vector3 getEnvironmentDiffuseColor() {
        return this.mEnvironmentDiffuseColor;
    }

    public void setEnvironmentDiffuseColor(Vector3 environmentDiffuseColor) {
        this.mEnvironmentDiffuseColor = environmentDiffuseColor;
    }

    public Vector3 getEnvironmentSpecularColor() {
        return this.mEnvironmentSpecularColor;
    }

    public void setEnvironmentSpecularColor(Vector3 environmentSpecularColor) {
        this.mEnvironmentSpecularColor = environmentSpecularColor;
    }

    public float getEnvironmentDiffuseIntensity() {
        return this.mEnvironmentDiffuseIntensity;
    }

    public void setEnvironmentDiffuseIntensity(float environmentDiffuseIntensity) {
        this.mEnvironmentDiffuseIntensity = environmentDiffuseIntensity;
    }

    public float getEnvironmentSpecularIntensity() {
        return this.mEnvironmentSpecularIntensity;
    }

    public void setEnvironmentSpecularIntensity(float environmentSpecularIntensity) {
        this.mEnvironmentSpecularIntensity = environmentSpecularIntensity;
    }

    public Entity getCamera() {
        return this.mCamera;
    }

    public void setCamera(Entity camera) {
        this.mCamera = camera;
    }

    public GpuResourceHandle getRadianceCubemap() {
        return this.mRadianceCubemap;
    }

    public void setRadianceCubemap(GpuResourceHandle radianceCubemap) {
        this.mRadianceCubemap = radianceCubemap;
    }

    public GpuResourceHandle getEnvMap() {
        return this.mEnvMap;
    }

    public void setEnvMap(GpuResourceHandle envMap) {
        this.mEnvMap = envMap;
    }

    public Quaternion getEnvironmentRotation() {
        return this.mEnvironmentRotation;
    }

    public void setEnvironmentRotation(Quaternion environmentRotation) {
        this.mEnvironmentRotation = environmentRotation;
    }

    public BackgroundType getBackgroundType() {
        return this.mBackgroundType;
    }

    public void setBackgroundType(BackgroundType backgroundType) {
        this.mBackgroundType = backgroundType;
    }

    public Vector3[] getIrradianceCoefficients() {
        return this.mIrradianceCoefficients;
    }

    public void setIrradianceCoefficients(Vector3[] irradianceCoefficients) {
        this.mIrradianceCoefficients = irradianceCoefficients;
    }
}
