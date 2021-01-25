package com.huawei.agpengine.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreEcs {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreEcs(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreEcs obj) {
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
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    static class CoreListener {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreListener(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreListener obj) {
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
        public synchronized void delete() {
            if (this.agpCptr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onEntitiesAdded(CoreEntityArrayView entities) {
            CoreJni.onEntitiesAddedInCoreEcsCoreListener(this.agpCptr, this, CoreEntityArrayView.getCptr(entities), entities);
        }

        /* access modifiers changed from: package-private */
        public void onEntitiesRemoved(CoreEntityArrayView entities) {
            CoreJni.onEntitiesRemovedInCoreEcsCoreListener(this.agpCptr, this, CoreEntityArrayView.getCptr(entities), entities);
        }

        /* access modifiers changed from: package-private */
        public void onComponentsAdded(CoreComponentManager componentManager, CoreEntityArrayView entities) {
            CoreJni.onComponentsAddedInCoreEcsCoreListener(this.agpCptr, this, CoreComponentManager.getCptr(componentManager), componentManager, CoreEntityArrayView.getCptr(entities), entities);
        }

        /* access modifiers changed from: package-private */
        public void onComponentsRemoved(CoreComponentManager componentManager, CoreEntityArrayView entities) {
            CoreJni.onComponentsRemovedInCoreEcsCoreListener(this.agpCptr, this, CoreComponentManager.getCptr(componentManager), componentManager, CoreEntityArrayView.getCptr(entities), entities);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreEntityManager getEntityManager() {
        return new CoreEntityManager(CoreJni.getEntityManagerInCoreEcs(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreSystem getSystem(String name) {
        long cptr = CoreJni.getSystemInCoreEcs(this.agpCptr, this, name);
        if (cptr == 0) {
            return null;
        }
        CoreSystem object = new CoreSystem(cptr, false);
        String systemName = object.name();
        if ("NodeSystem".equals(systemName)) {
            return new CoreNodeSystem(CoreSystem.getCptrAndSetMemOwn(object, false), false);
        }
        if ("AnimationSystem".equals(systemName)) {
            return new CoreAnimationSystem(CoreSystem.getCptrAndSetMemOwn(object, false), false);
        }
        if ("MorphingSystem".equals(systemName)) {
            return new CoreMorphingSystem(CoreSystem.getCptrAndSetMemOwn(object, false), false);
        }
        return object;
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManagerArray componentManagers() {
        return new CoreComponentManagerArray(CoreJni.componentManagersInCoreEcs(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManager getComponentManager(String name) {
        long cptr = CoreJni.getComponentManagerInCoreEcs(this.agpCptr, this, name);
        if (cptr == 0) {
            return null;
        }
        return new CoreComponentManager(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public int cloneEntity(int entity) {
        return CoreJni.cloneEntityInCoreEcs(this.agpCptr, this, entity);
    }

    /* access modifiers changed from: package-private */
    public void processEvents() {
        CoreJni.processEventsInCoreEcs(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void initialize() {
        CoreJni.initializeInCoreEcs(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean update(BigInteger time, BigInteger delta) {
        return CoreJni.updateInCoreEcs(this.agpCptr, this, time, delta);
    }

    /* access modifiers changed from: package-private */
    public void uninitialize() {
        CoreJni.uninitializeInCoreEcs(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void addListener(CoreListener listener) {
        CoreJni.addListenerInCoreEcs(this.agpCptr, this, CoreListener.getCptr(listener), listener);
    }

    /* access modifiers changed from: package-private */
    public void removeListener(CoreListener listener) {
        CoreJni.removeListenerInCoreEcs(this.agpCptr, this, CoreListener.getCptr(listener), listener);
    }

    /* access modifiers changed from: package-private */
    public void requestRender() {
        CoreJni.requestRenderInCoreEcs(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRenderMode(CoreRenderMode renderMode) {
        CoreJni.setRenderModeInCoreEcs(this.agpCptr, this, renderMode.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreRenderMode getRenderMode() {
        return CoreRenderMode.swigToEnum(CoreJni.getRenderModeInCoreEcs(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public boolean needRender() {
        return CoreJni.needRenderInCoreEcs(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CorePluginRegister getPluginRegister() {
        return new CorePluginRegister(CoreJni.getPluginRegisterInCoreEcs(this.agpCptr, this), false);
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

        static CoreRenderMode swigToEnum(int swigValue2) {
            CoreRenderMode[] swigValues = (CoreRenderMode[]) CoreRenderMode.class.getEnumConstants();
            if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
                return swigValues[swigValue2];
            }
            for (CoreRenderMode swigEnum : swigValues) {
                if (swigEnum.swigValue == swigValue2) {
                    return swigEnum;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreRenderMode.class + " with value " + swigValue2);
        }

        private CoreRenderMode() {
            this.swigValue = SwigNext.next;
            SwigNext.access$008();
        }

        private CoreRenderMode(int swigValue2) {
            this.swigValue = swigValue2;
            int unused = SwigNext.next = swigValue2 + 1;
        }

        private CoreRenderMode(CoreRenderMode swigEnum) {
            this.swigValue = swigEnum.swigValue;
            int unused = SwigNext.next = this.swigValue + 1;
        }

        private static class SwigNext {
            private static int next = 0;

            private SwigNext() {
            }

            static /* synthetic */ int access$008() {
                int i = next;
                next = i + 1;
                return i;
            }
        }
    }
}
