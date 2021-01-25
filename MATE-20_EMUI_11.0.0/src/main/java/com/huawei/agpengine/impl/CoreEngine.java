package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreEngine extends CoreInterface {
    private transient long agpCptr;

    CoreEngine(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreEngine(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreEngine obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.agpengine.impl.CoreInterface
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    /* access modifiers changed from: package-private */
    public CoreDevice createDevice(CoreDeviceCreateInfo createInfo) {
        long cptr = CoreJni.createDeviceInCoreEngine(this.agpCptr, this, CoreDeviceCreateInfo.getCptr(createInfo), createInfo);
        if (cptr == 0) {
            return null;
        }
        return new CoreDevice(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void createSwapchain(CoreSwapchainCreateInfo swapchainCreateInfo) {
        CoreJni.createSwapchainInCoreEngine(this.agpCptr, this, CoreSwapchainCreateInfo.getCptr(swapchainCreateInfo), swapchainCreateInfo);
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
    public void renderFrame(CoreRenderNodeGraphInput renderNodeGraphInput) {
        CoreJni.renderFrameInCoreEngine0(this.agpCptr, this, CoreRenderNodeGraphInput.getCptr(renderNodeGraphInput), renderNodeGraphInput);
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphType renderNodeGraph) {
        CoreJni.renderFrameInCoreEngine1(this.agpCptr, this, renderNodeGraph.swigValue());
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphInputArrayView renderNodeGraphInputs) {
        CoreJni.renderFrameInCoreEngine2(this.agpCptr, this, CoreRenderNodeGraphInputArrayView.getCptr(renderNodeGraphInputs), renderNodeGraphInputs);
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
    @Override // com.huawei.agpengine.impl.CoreInterface
    public CorePluginRegister getPluginRegister() {
        return new CorePluginRegister(CoreJni.getPluginRegisterInCoreEngine(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CorePlatform getPlatform() {
        return new CorePlatform(CoreJni.getPlatformInCoreEngine(this.agpCptr, this), false);
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

    /* access modifiers changed from: package-private */
    public String getRootPath() {
        return CoreJni.getRootPathInCoreEngine(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreEcsPtr createECS() {
        return new CoreEcsPtr(CoreJni.createECSInCoreEngine(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public String getVersion() {
        return CoreJni.getVersionInCoreEngine(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean isDebugBuild() {
        return CoreJni.isDebugBuildInCoreEngine(this.agpCptr, this);
    }
}
