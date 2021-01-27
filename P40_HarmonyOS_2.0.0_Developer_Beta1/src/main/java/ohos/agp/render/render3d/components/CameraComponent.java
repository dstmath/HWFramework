package ohos.agp.render.render3d.components;

import java.util.Arrays;
import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.math.Matrix4x4;

public class CameraComponent implements Component {
    public static final int CAMERA_FLAG_SHADOW_BIT = 1;
    private static final int VIEWPORT_PARAM_COUNT = 4;
    private int mAdditionalFlags;
    private CameraType mCameraType;
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

    public enum CameraType {
        ORTHOGRAPHIC,
        PERSPECTIVE,
        CUSTOM
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String str) {
        this.mName = str;
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

    public void setAdditionalFlags(int i) {
        this.mAdditionalFlags = i;
    }

    public float getPerspectiveAspectRatio() {
        return this.mPerspectiveAspectRatio;
    }

    public void setPerspectiveAspectRatio(float f) {
        this.mPerspectiveAspectRatio = f;
    }

    public float getPerspectiveVerticalFov() {
        return this.mPerspectiveVerticalFov;
    }

    public void setPerspectiveVerticalFov(float f) {
        this.mPerspectiveVerticalFov = f;
    }

    public float getOrthoWidth() {
        return this.mOrthoWidth;
    }

    public void setOrthoWidth(float f) {
        this.mOrthoWidth = f;
    }

    public float getOrthoHeight() {
        return this.mOrthoHeight;
    }

    public void setOrthoHeight(float f) {
        this.mOrthoHeight = f;
    }

    public Matrix4x4 getCustomProjection() {
        return this.mCustomProjection;
    }

    public void setCustomProjection(Matrix4x4 matrix4x4) {
        this.mCustomProjection = matrix4x4;
    }

    public float getZnear() {
        return this.mZnear;
    }

    public void setZnear(float f) {
        this.mZnear = f;
    }

    public float getZfar() {
        return this.mZfar;
    }

    public void setZfar(float f) {
        this.mZfar = f;
    }

    public float[] getViewportParams() {
        float[] fArr = this.mViewportParams;
        return Arrays.copyOf(fArr, fArr.length);
    }

    public void setViewportParams(float[] fArr) {
        this.mViewportParams = Arrays.copyOf(fArr, fArr.length);
    }

    public int getRenderResolutionX() {
        return this.mRenderResolutionX;
    }

    public int getRenderResolutionY() {
        return this.mRenderResolutionY;
    }

    public void setRenderResolution(int i, int i2) {
        this.mRenderResolutionX = i;
        this.mRenderResolutionY = i2;
    }
}
