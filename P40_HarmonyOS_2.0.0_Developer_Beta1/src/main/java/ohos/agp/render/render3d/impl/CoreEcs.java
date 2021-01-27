package ohos.agp.render.render3d.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreEcs {
    private transient long agpCptrCoreEcs;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreEcs(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreEcs = j;
    }

    static long getCptr(CoreEcs coreEcs) {
        if (coreEcs == null) {
            return 0;
        }
        return coreEcs.agpCptrCoreEcs;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreEcs != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEcs(this.agpCptrCoreEcs);
                }
                this.agpCptrCoreEcs = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEcs coreEcs, boolean z) {
        if (coreEcs != null) {
            synchronized (coreEcs.lock) {
                coreEcs.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEcs);
    }

    static class CoreListener {
        private transient long agpCptrCoreListener;
        transient boolean isAgpCmemOwn;
        private final Object lock2 = new Object();

        CoreListener(long j, boolean z) {
            this.isAgpCmemOwn = z;
            this.agpCptrCoreListener = j;
        }

        static long getCptr(CoreListener coreListener) {
            if (coreListener == null) {
                return 0;
            }
            return coreListener.agpCptrCoreListener;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.lock2) {
                if (this.agpCptrCoreListener != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreEcsCoreListener(this.agpCptrCoreListener);
                    }
                    this.agpCptrCoreListener = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreListener coreListener, boolean z) {
            if (coreListener != null) {
                synchronized (coreListener.lock2) {
                    coreListener.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreListener);
        }

        /* access modifiers changed from: package-private */
        public void onEntitiesAdded(CoreEntityArrayView coreEntityArrayView) {
            CoreJni.onEntitiesAddedInCoreEcsCoreListener(this.agpCptrCoreListener, this, CoreEntityArrayView.getCptr(coreEntityArrayView), coreEntityArrayView);
        }

        /* access modifiers changed from: package-private */
        public void onEntitiesRemoved(CoreEntityArrayView coreEntityArrayView) {
            CoreJni.onEntitiesRemovedInCoreEcsCoreListener(this.agpCptrCoreListener, this, CoreEntityArrayView.getCptr(coreEntityArrayView), coreEntityArrayView);
        }

        /* access modifiers changed from: package-private */
        public void onComponentsAdded(CoreComponentManager coreComponentManager, CoreEntityArrayView coreEntityArrayView) {
            CoreJni.onComponentsAddedInCoreEcsCoreListener(this.agpCptrCoreListener, this, CoreComponentManager.getCptr(coreComponentManager), coreComponentManager, CoreEntityArrayView.getCptr(coreEntityArrayView), coreEntityArrayView);
        }

        /* access modifiers changed from: package-private */
        public void onComponentsRemoved(CoreComponentManager coreComponentManager, CoreEntityArrayView coreEntityArrayView) {
            CoreJni.onComponentsRemovedInCoreEcsCoreListener(this.agpCptrCoreListener, this, CoreComponentManager.getCptr(coreComponentManager), coreComponentManager, CoreEntityArrayView.getCptr(coreEntityArrayView), coreEntityArrayView);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreEntityManager getEntityManager() {
        return new CoreEntityManager(CoreJni.getEntityManagerInCoreEcs(this.agpCptrCoreEcs, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreSystem getSystem(String str) {
        long systemInCoreEcs = CoreJni.getSystemInCoreEcs(this.agpCptrCoreEcs, this, str);
        if (systemInCoreEcs == 0) {
            return null;
        }
        CoreSystem coreSystem = new CoreSystem(systemInCoreEcs, false);
        String name = coreSystem.name();
        if ("NodeSystem".equals(name)) {
            return new CoreNodeSystem(CoreSystem.getCptrAndSetMemOwn(coreSystem, false), false);
        }
        if ("AnimationSystem".equals(name)) {
            return new CoreAnimationSystem(CoreSystem.getCptrAndSetMemOwn(coreSystem, false), false);
        }
        return "MorphingSystem".equals(name) ? new CoreMorphingSystem(CoreSystem.getCptrAndSetMemOwn(coreSystem, false), false) : coreSystem;
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManager getComponentManager(String str) {
        long componentManagerInCoreEcs = CoreJni.getComponentManagerInCoreEcs(this.agpCptrCoreEcs, this, str);
        if (componentManagerInCoreEcs == 0) {
            return null;
        }
        return new CoreComponentManager(componentManagerInCoreEcs, false);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity cloneEntity(CoreEntity coreEntity) {
        return new CoreEntity(CoreJni.cloneEntityInCoreEcs(this.agpCptrCoreEcs, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }

    /* access modifiers changed from: package-private */
    public void processEvents() {
        CoreJni.processEventsInCoreEcs(this.agpCptrCoreEcs, this);
    }

    /* access modifiers changed from: package-private */
    public void initialize() {
        CoreJni.initializeInCoreEcs(this.agpCptrCoreEcs, this);
    }

    /* access modifiers changed from: package-private */
    public boolean update(BigInteger bigInteger, BigInteger bigInteger2) {
        return CoreJni.updateInCoreEcs(this.agpCptrCoreEcs, this, bigInteger, bigInteger2);
    }

    /* access modifiers changed from: package-private */
    public void uninitialize() {
        CoreJni.uninitializeInCoreEcs(this.agpCptrCoreEcs, this);
    }

    /* access modifiers changed from: package-private */
    public CoreDevGui getDevGui() {
        return new CoreDevGui(CoreJni.getDevGuiInCoreEcs(this.agpCptrCoreEcs, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceManager getResourceManager() {
        return new CoreResourceManager(CoreJni.getResourceManagerInCoreEcs(this.agpCptrCoreEcs, this), false);
    }

    /* access modifiers changed from: package-private */
    public void addListener(CoreListener coreListener) {
        CoreJni.addListenerInCoreEcs(this.agpCptrCoreEcs, this, CoreListener.getCptr(coreListener), coreListener);
    }

    /* access modifiers changed from: package-private */
    public void removeListener(CoreListener coreListener) {
        CoreJni.removeListenerInCoreEcs(this.agpCptrCoreEcs, this, CoreListener.getCptr(coreListener), coreListener);
    }

    /* access modifiers changed from: package-private */
    public void requestRender() {
        CoreJni.requestRenderInCoreEcs(this.agpCptrCoreEcs, this);
    }

    /* access modifiers changed from: package-private */
    public void setRenderMode(CoreRenderMode coreRenderMode) {
        CoreJni.setRenderModeInCoreEcs(this.agpCptrCoreEcs, this, coreRenderMode.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreRenderMode getRenderMode() {
        return CoreRenderMode.swigToEnum(CoreJni.getRenderModeInCoreEcs(this.agpCptrCoreEcs, this));
    }

    /* access modifiers changed from: package-private */
    public boolean needRender() {
        return CoreJni.needRenderInCoreEcs(this.agpCptrCoreEcs, this);
    }

    /* access modifiers changed from: package-private */
    public enum CoreRenderMode {
        RENDER_IF_DIRTY,
        RENDER_ALWAYS;
        
        private final int swigValue;

        /* access modifiers changed from: package-private */
        public final int swigValue() {
            return this.swigValue;
        }

        static CoreRenderMode swigToEnum(int i) {
            CoreRenderMode[] coreRenderModeArr = (CoreRenderMode[]) CoreRenderMode.class.getEnumConstants();
            if (i < coreRenderModeArr.length && i >= 0 && coreRenderModeArr[i].swigValue == i) {
                return coreRenderModeArr[i];
            }
            for (CoreRenderMode coreRenderMode : coreRenderModeArr) {
                if (coreRenderMode.swigValue == i) {
                    return coreRenderMode;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreRenderMode.class + " with value " + i);
        }

        private CoreRenderMode() {
            this(SwigNext.next);
        }

        private CoreRenderMode(int i) {
            this.swigValue = i;
            int unused = SwigNext.next = i + 1;
        }

        private CoreRenderMode(CoreRenderMode coreRenderMode) {
            this(coreRenderMode.swigValue);
        }

        private static class SwigNext {
            private static int next;

            private SwigNext() {
            }
        }
    }
}
