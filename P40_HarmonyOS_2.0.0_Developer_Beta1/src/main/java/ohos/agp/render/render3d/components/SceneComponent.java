package ohos.agp.render.render3d.components;

import java.util.Arrays;
import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.math.Quaternion;
import ohos.agp.render.render3d.math.Vector3;
import ohos.agp.render.render3d.resources.GpuResourceHandle;

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

    public void setEnvironmentDiffuseColor(Vector3 vector3) {
        this.mEnvironmentDiffuseColor = vector3;
    }

    public Vector3 getEnvironmentSpecularColor() {
        return this.mEnvironmentSpecularColor;
    }

    public void setEnvironmentSpecularColor(Vector3 vector3) {
        this.mEnvironmentSpecularColor = vector3;
    }

    public float getEnvironmentDiffuseIntensity() {
        return this.mEnvironmentDiffuseIntensity;
    }

    public void setEnvironmentDiffuseIntensity(float f) {
        this.mEnvironmentDiffuseIntensity = f;
    }

    public float getEnvironmentSpecularIntensity() {
        return this.mEnvironmentSpecularIntensity;
    }

    public void setEnvironmentSpecularIntensity(float f) {
        this.mEnvironmentSpecularIntensity = f;
    }

    public Entity getCamera() {
        return this.mCamera;
    }

    public void setCamera(Entity entity) {
        this.mCamera = entity;
    }

    public GpuResourceHandle getRadianceCubemap() {
        return this.mRadianceCubemap;
    }

    public void setRadianceCubemap(GpuResourceHandle gpuResourceHandle) {
        this.mRadianceCubemap = gpuResourceHandle;
    }

    public GpuResourceHandle getEnvMap() {
        return this.mEnvMap;
    }

    public void setEnvMap(GpuResourceHandle gpuResourceHandle) {
        this.mEnvMap = gpuResourceHandle;
    }

    public Quaternion getEnvironmentRotation() {
        return this.mEnvironmentRotation;
    }

    public void setEnvironmentRotation(Quaternion quaternion) {
        this.mEnvironmentRotation = quaternion;
    }

    public BackgroundType getBackgroundType() {
        return this.mBackgroundType;
    }

    public void setBackgroundType(BackgroundType backgroundType) {
        this.mBackgroundType = backgroundType;
    }

    public Vector3[] getIrradianceCoefficients() {
        Vector3[] vector3Arr = this.mIrradianceCoefficients;
        return (Vector3[]) Arrays.copyOf(vector3Arr, vector3Arr.length);
    }

    public void setIrradianceCoefficients(Vector3[] vector3Arr) {
        this.mIrradianceCoefficients = (Vector3[]) Arrays.copyOf(vector3Arr, vector3Arr.length);
    }
}
