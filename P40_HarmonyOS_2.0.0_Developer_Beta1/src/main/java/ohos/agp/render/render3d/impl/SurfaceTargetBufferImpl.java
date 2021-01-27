package ohos.agp.render.render3d.impl;

import java.lang.reflect.Field;
import java.math.BigInteger;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.graphics.Surface;
import ohos.agp.render.render3d.BuildConfig;
import ohos.agp.render.render3d.TargetBuffer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public final class SurfaceTargetBufferImpl implements TargetBuffer {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: SurfaceTargetBufferImpl");
    private AgpContextImpl mAgpContext;
    private int mHeight;
    private BigInteger mNativeSurfaceHandle = BigInteger.ZERO;
    private CoreNativeWindow mNativeWindow;
    private Surface mSurface;
    private int mWidth;

    SurfaceTargetBufferImpl() {
    }

    @Override // ohos.agp.render.render3d.TargetBuffer
    public boolean isBufferAvailable() {
        return this.mSurface != null;
    }

    @Override // ohos.agp.render.render3d.TargetBuffer
    public int getWidth() {
        return this.mWidth;
    }

    @Override // ohos.agp.render.render3d.TargetBuffer
    public int getHeight() {
        return this.mHeight;
    }

    /* access modifiers changed from: package-private */
    public void init(AgpContextImpl agpContextImpl, Surface surface) {
        this.mAgpContext = agpContextImpl;
        try {
            doInit(surface);
        } catch (IllegalAccessException | IllegalArgumentException | IllegalStateException | NoSuchFieldException | NullPointerException e) {
            HiLog.error(LABEL, "Init has exception: %{public}s.", new Object[]{e.getMessage()});
        }
    }

    private void doInit(Surface surface) throws NoSuchFieldException, IllegalAccessException {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "init()", new Object[0]);
        }
        if (this.mAgpContext == null || surface == null) {
            throw new NullPointerException();
        } else if (this.mSurface == null) {
            this.mSurface = surface;
            Field declaredField = this.mSurface.getClass().getDeclaredField("mNativePtr");
            declaredField.setAccessible(true);
            this.mNativeWindow = Core.createAndroidNativeWindow(this.mAgpContext.getPlatform(), ((Long) declaredField.get(this.mSurface)).longValue());
            if (!this.mNativeWindow.isValid()) {
                throw new IllegalArgumentException("Cannot get ANativeWindow from given Surface.");
            }
        } else {
            throw new IllegalStateException("Already init.");
        }
    }

    /* access modifiers changed from: package-private */
    public void updateSize(int i, int i2) {
        if (this.mAgpContext != null) {
            if (!this.mNativeSurfaceHandle.equals(BigInteger.ZERO)) {
                this.mAgpContext.getEngine().destroySwapchain();
                Core.destroyAndroidSurface(this.mAgpContext.getDevice(), this.mNativeSurfaceHandle);
            }
            if (this.mSurface != null) {
                this.mWidth = i;
                this.mHeight = i2;
                this.mNativeSurfaceHandle = Core.createAndroidSurface(this.mAgpContext.getDevice(), this.mNativeWindow);
                if (!BigInteger.ZERO.equals(this.mNativeSurfaceHandle)) {
                    CoreSwapchainCreateInfo coreSwapchainCreateInfo = new CoreSwapchainCreateInfo(this.mNativeSurfaceHandle, true);
                    coreSwapchainCreateInfo.setPreferSrgbFormat(true);
                    coreSwapchainCreateInfo.setSwapchainFlags((long) (CoreSwapchainFlagBits.CORE_SWAPCHAIN_COLOR_BUFFER_BIT.swigValue() | CoreSwapchainFlagBits.CORE_SWAPCHAIN_DEPTH_BUFFER_BIT.swigValue()));
                    this.mAgpContext.getEngine().createSwapchain(coreSwapchainCreateInfo);
                    return;
                }
                throw new IllegalArgumentException("Cannot recreate VkSurfaceKHR/EGLSurface from given Surface.");
            }
            throw new IllegalStateException("Not initialized.");
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void release() {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "release()", new Object[0]);
        }
        if (!this.mNativeSurfaceHandle.equals(BigInteger.ZERO)) {
            this.mAgpContext.getEngine().destroySwapchain();
            Core.destroyAndroidSurface(this.mAgpContext.getDevice(), this.mNativeSurfaceHandle);
            this.mNativeSurfaceHandle = BigInteger.ZERO;
        }
        this.mAgpContext = null;
    }
}
