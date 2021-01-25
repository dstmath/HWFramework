package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.math.Matrix4x4;
import com.huawei.agpengine.resources.GpuResourceHandle;

public class CameraComponent implements Component {
    public static final int CAMERA_FLAG_ACTIVE_RENDER_BIT = 1;
    public static final int CAMERA_FLAG_SHADOW_BIT = 2;
    private static final int VIEWPORT_PARAM_COUNT = 4;
    private int mAdditionalFlags;
    private CameraTargetType mCameraTargetType;
    private CameraType mCameraType;
    private GpuResourceHandle mCustomColorTarget;
    private GpuResourceHandle mCustomDepthTarget;
    private Matrix4x4 mCustomProjection = new Matrix4x4();
    private String mName;
    private float mOrthoHeight;
    private float mOrthoWidth;
    private float mPerspectiveAspectRatio;
    private float mPerspectiveVerticalFov;
    private int mRenderResolutionX;
    private int mRenderResolutionY;
    private float[] mViewportParams = new float[4];
    private float mZfar;
    private float mZnear;

    public enum CameraTargetType {
        DEFAULT,
        CUSTOM
    }

    public enum CameraType {
        ORTHOGRAPHIC,
        PERSPECTIVE,
        CUSTOM
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public CameraType getCameraType() {
        return this.mCameraType;
    }

    public void setCameraType(CameraType cameraType) {
        this.mCameraType = cameraType;
    }

    public int getAdditionalFlags() {
        return this.mAdditionalFlags;
    }

    public void setAdditionalFlags(int additionalFlags) {
        this.mAdditionalFlags = additionalFlags;
    }

    public float getPerspectiveAspectRatio() {
        return this.mPerspectiveAspectRatio;
    }

    public void setPerspectiveAspectRatio(float perspectiveAspectRatio) {
        this.mPerspectiveAspectRatio = perspectiveAspectRatio;
    }

    public float getPerspectiveVerticalFov() {
        return this.mPerspectiveVerticalFov;
    }

    public void setPerspectiveVerticalFov(float perspectiveVerticalFov) {
        this.mPerspectiveVerticalFov = perspectiveVerticalFov;
    }

    public float getOrthoWidth() {
        return this.mOrthoWidth;
    }

    public void setOrthoWidth(float orthoWidth) {
        this.mOrthoWidth = orthoWidth;
    }

    public float getOrthoHeight() {
        return this.mOrthoHeight;
    }

    public void setOrthoHeight(float orthoHeight) {
        this.mOrthoHeight = orthoHeight;
    }

    public Matrix4x4 getCustomProjection() {
        return this.mCustomProjection;
    }

    public void setCustomProjection(Matrix4x4 customProjection) {
        this.mCustomProjection = customProjection;
    }

    public float getZnear() {
        return this.mZnear;
    }

    public void setZnear(float znear) {
        this.mZnear = znear;
    }

    public float getZfar() {
        return this.mZfar;
    }

    public void setZfar(float zfar) {
        this.mZfar = zfar;
    }

    public float[] getViewportParams() {
        return this.mViewportParams;
    }

    public void setViewportParams(float[] viewportParams) {
        this.mViewportParams = viewportParams;
    }

    public int getRenderResolutionX() {
        return this.mRenderResolutionX;
    }

    public int getRenderResolutionY() {
        return this.mRenderResolutionY;
    }

    public void setRenderResolution(int renderResolutionX, int renderResolutionY) {
        this.mRenderResolutionX = renderResolutionX;
        this.mRenderResolutionY = renderResolutionY;
    }

    public CameraTargetType getCameraTargetType() {
        return this.mCameraTargetType;
    }

    public void setCameraTargetType(CameraTargetType cameraTargetType) {
        this.mCameraTargetType = cameraTargetType;
    }

    public GpuResourceHandle getCustomColorTarget() {
        return this.mCustomColorTarget;
    }

    public void setCustomColorTarget(GpuResourceHandle colorTarget) {
        this.mCustomColorTarget = colorTarget;
    }

    public GpuResourceHandle getCustomDepthTarget() {
        return this.mCustomDepthTarget;
    }

    public void setCustomDepthTarget(GpuResourceHandle depthTarget) {
        this.mCustomDepthTarget = depthTarget;
    }
}
