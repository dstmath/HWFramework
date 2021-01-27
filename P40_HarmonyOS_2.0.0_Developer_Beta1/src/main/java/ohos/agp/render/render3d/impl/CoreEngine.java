package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreEngine {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreEngine(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreEngine coreEngine) {
        if (coreEngine == null) {
            return 0;
        }
        return coreEngine.agpCptr;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEngine(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEngine coreEngine, boolean z) {
        if (coreEngine != null) {
            synchronized (coreEngine.lock) {
                coreEngine.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEngine);
    }

    /* access modifiers changed from: package-private */
    public CoreDevice createDevice(CoreDeviceCreateInfo coreDeviceCreateInfo) {
        long createDeviceInCoreEngine = CoreJni.createDeviceInCoreEngine(this.agpCptr, this, CoreDeviceCreateInfo.getCptr(coreDeviceCreateInfo), coreDeviceCreateInfo);
        if (createDeviceInCoreEngine == 0) {
            return null;
        }
        return new CoreDevice(createDeviceInCoreEngine, false);
    }

    /* access modifiers changed from: package-private */
    public void createSwapchain(CoreSwapchainCreateInfo coreSwapchainCreateInfo) {
        CoreJni.createSwapchainInCoreEngine(this.agpCptr, this, CoreSwapchainCreateInfo.getCptr(coreSwapchainCreateInfo), coreSwapchainCreateInfo);
    }

    /* access modifiers changed from: package-private */
    public void destroySwapchain() {
        CoreJni.destroySwapchainInCoreEngine(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void init() {
        CoreJni.initInCoreEngine(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphInput coreRenderNodeGraphInput) {
        CoreJni.renderFrameInCoreEngine0(this.agpCptr, this, CoreRenderNodeGraphInput.getCptr(coreRenderNodeGraphInput), coreRenderNodeGraphInput);
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphType coreRenderNodeGraphType) {
        CoreJni.renderFrameInCoreEngine1(this.agpCptr, this, coreRenderNodeGraphType.swigValue());
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView) {
        CoreJni.renderFrameInCoreEngine2(this.agpCptr, this, CoreRenderNodeGraphInputArrayView.getCptr(coreRenderNodeGraphInputArrayView), coreRenderNodeGraphInputArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreFileManager getFileManager() {
        return new CoreFileManager(CoreJni.getFileManagerInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceManager getGpuResourceManager() {
        return new CoreGpuResourceManager(CoreJni.getGpuResourceManagerInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceManager getResourceManager() {
        return new CoreResourceManager(CoreJni.getResourceManagerInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderer getRenderer() {
        return new CoreRenderer(CoreJni.getRendererInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderDataStoreManager getRenderDataStoreManager() {
        return new CoreRenderDataStoreManager(CoreJni.getRenderDataStoreManagerInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeGraphManager getRenderNodeGraphManager() {
        return new CoreRenderNodeGraphManager(CoreJni.getRenderNodeGraphManagerInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeManager getRenderNodeManager() {
        return new CoreRenderNodeManager(CoreJni.getRenderNodeManagerInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreShaderManager getShaderManager() {
        return new CoreShaderManager(CoreJni.getShaderManagerInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CorePluginRegister getPluginRegister() {
        return new CorePluginRegister(CoreJni.getPluginRegisterInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreDevGui getDevGui() {
        return new CoreDevGui(CoreJni.getDevGuiInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreEngineTime getEngineTime() {
        return new CoreEngineTime(CoreJni.getEngineTimeInCoreEngine(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreDevice getDevice() {
        return new CoreDevice(CoreJni.getDeviceInCoreEngine(this.agpCptr, this), false);
    }

    static String getVersion() {
        return CoreJni.getVersionInCoreEngine();
    }

    static boolean isDebugBuild() {
        return CoreJni.isDebugBuildInCoreEngine();
    }
}
