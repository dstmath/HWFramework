package com.huawei.agpengine.impl;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import com.huawei.agpengine.TargetBuffer;
import java.math.BigInteger;

/* access modifiers changed from: package-private */
public final class SurfaceTargetBufferImpl implements TargetBuffer {
    private static final String TAG = "core: SurfaceTargetBufferImpl";
    private AgpContextImpl mAgpContext;
    private int mHeight;
    private boolean mIsSrgbConversionRequired = false;
    private BigInteger mNativeSurfaceHandle = BigInteger.ZERO;
    private CoreNativeWindow mNativeWindow;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private int mWidth;

    SurfaceTargetBufferImpl() {
    }

    @Override // com.huawei.agpengine.TargetBuffer
    public boolean isBufferAvailable() {
        return this.mSurface != null;
    }

    @Override // com.huawei.agpengine.TargetBuffer
    public boolean isSrgbConversionRequired() {
        return this.mIsSrgbConversionRequired;
    }

    @Override // com.huawei.agpengine.TargetBuffer
    public int getWidth() {
        return this.mWidth;
    }

    @Override // com.huawei.agpengine.TargetBuffer
    public int getHeight() {
        return this.mHeight;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mAgpContext != null) {
            Log.e(TAG, "TargetBuffer.release() was not called explicitly.");
        }
        super.finalize();
    }

    /* access modifiers changed from: package-private */
    public void init(AgpContextImpl agpContext, SurfaceTexture surfaceTexture) {
        if (surfaceTexture != null) {
            this.mAgpContext = agpContext;
            this.mSurfaceTexture = surfaceTexture;
            doInit(new Surface(surfaceTexture));
            return;
        }
        throw new NullPointerException("surfaceTexture must not be null");
    }

    /* access modifiers changed from: package-private */
    public void init(AgpContextImpl agpContext, Surface surface) {
        this.mAgpContext = agpContext;
        this.mSurfaceTexture = null;
        doInit(surface);
    }

    private void doInit(Surface surface) {
        if (this.mAgpContext == null) {
            throw new NullPointerException("Internal graphics engine error");
        } else if (surface == null) {
            throw new NullPointerException("Surface must not be null.");
        } else if (!surface.isValid()) {
            throw new IllegalArgumentException("Surface is not valid.");
        } else if (this.mSurface == null) {
            this.mSurface = surface;
            this.mNativeWindow = Core.createAndroidNativeWindow(this.mAgpContext.getPlatform(), this.mSurface);
            if (this.mNativeWindow.isValid()) {
                this.mIsSrgbConversionRequired = !Core.isSrgbSurfaceSupported(this.mAgpContext.getDevice());
                return;
            }
            throw new IllegalArgumentException("Cannot get ANativeWindow from given Surface.");
        } else {
            throw new IllegalStateException("Already init.");
        }
    }

    /* access modifiers changed from: package-private */
    public void updateSize(int width, int height) {
        if (this.mAgpContext != null) {
            if (!this.mNativeSurfaceHandle.equals(BigInteger.ZERO)) {
                this.mAgpContext.getEngine().destroySwapchain();
                Core.destroyAndroidSurface(this.mAgpContext.getDevice(), this.mNativeSurfaceHandle);
            }
            Surface surface = this.mSurface;
            if (surface == null) {
                throw new IllegalStateException("Not initialized.");
            } else if (surface.isValid()) {
                this.mWidth = width;
                this.mHeight = height;
                this.mNativeSurfaceHandle = Core.createAndroidSurface(this.mAgpContext.getDevice(), this.mNativeWindow);
                if (!BigInteger.ZERO.equals(this.mNativeSurfaceHandle)) {
                    CoreSwapchainCreateInfo createInfo = new CoreSwapchainCreateInfo(this.mNativeSurfaceHandle, true);
                    createInfo.setPreferSrgbFormat(true);
                    createInfo.setSwapchainFlags((long) (CoreSwapchainFlagBits.CORE_SWAPCHAIN_COLOR_BUFFER_BIT.swigValue() | CoreSwapchainFlagBits.CORE_SWAPCHAIN_DEPTH_BUFFER_BIT.swigValue()));
                    this.mAgpContext.getEngine().createSwapchain(createInfo);
                    return;
                }
                throw new IllegalArgumentException("Cannot recreate VkSurfaceKHR/EGLSurface from given Surface.");
            } else {
                throw new IllegalStateException("Surface in bad state.");
            }
        } else {
            throw new IllegalStateException("Uninitialized context.");
        }
    }

    @Override // com.huawei.agpengine.TargetBuffer
    public void release() {
        if (!this.mNativeSurfaceHandle.equals(BigInteger.ZERO)) {
            this.mAgpContext.getEngine().destroySwapchain();
            Core.destroyAndroidSurface(this.mAgpContext.getDevice(), this.mNativeSurfaceHandle);
            this.mNativeSurfaceHandle = BigInteger.ZERO;
        }
        CoreNativeWindow coreNativeWindow = this.mNativeWindow;
        if (coreNativeWindow != null) {
            Core.destroyAndroidNativeWindow(coreNativeWindow);
            this.mNativeWindow = null;
        }
        Surface surface = this.mSurface;
        if (surface != null) {
            surface.release();
            this.mSurface = null;
        }
        SurfaceTexture surfaceTexture = this.mSurfaceTexture;
        if (surfaceTexture != null) {
            surfaceTexture.release();
            this.mSurfaceTexture = null;
        }
        this.mAgpContext = null;
    }
}
