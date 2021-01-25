package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreComponentManager {
    static final long INVALID_COMPONENT_ID = -1;
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreComponentManager(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreComponentManager obj) {
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
    public String name() {
        return CoreJni.nameInCoreComponentManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public long componentCount() {
        return CoreJni.componentCountInCoreComponentManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public int entity(long index) {
        return CoreJni.entityInCoreComponentManager(this.agpCptr, this, index);
    }

    /* access modifiers changed from: package-private */
    public long getComponentGeneration(long index) {
        return CoreJni.getComponentGenerationInCoreComponentManager(this.agpCptr, this, index);
    }

    /* access modifiers changed from: package-private */
    public boolean hasComponent(int entity) {
        return CoreJni.hasComponentInCoreComponentManager(this.agpCptr, this, entity);
    }

    /* access modifiers changed from: package-private */
    public long getComponentId(int entity) {
        return CoreJni.getComponentIdInCoreComponentManager(this.agpCptr, this, entity);
    }

    /* access modifiers changed from: package-private */
    public void create(int entity) {
        CoreJni.createInCoreComponentManager(this.agpCptr, this, entity);
    }

    /* access modifiers changed from: package-private */
    public boolean destroy(int entity) {
        return CoreJni.destroyInCoreComponentManager0(this.agpCptr, this, entity);
    }

    /* access modifiers changed from: package-private */
    public void gc() {
        CoreJni.gcInCoreComponentManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void destroy(CoreEntityArrayView gcList) {
        CoreJni.destroyInCoreComponentManager1(this.agpCptr, this, CoreEntityArrayView.getCptr(gcList), gcList);
    }

    /* access modifiers changed from: package-private */
    public long getModifiedFlags() {
        return CoreJni.getModifiedFlagsInCoreComponentManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void clearModifiedFlags() {
        CoreJni.clearModifiedFlagsInCoreComponentManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public long getGenerationCounter() {
        return CoreJni.getGenerationCounterInCoreComponentManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(int entity, CorePropertyHandle data) {
        CoreJni.setDataInCoreComponentManager0(this.agpCptr, this, entity, CorePropertyHandle.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void setData(long index, CorePropertyHandle data) {
        CoreJni.setDataInCoreComponentManager1(this.agpCptr, this, index, CorePropertyHandle.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CorePropertyHandle getPropertyData(long index) {
        long cptr = CoreJni.getPropertyDataInCoreComponentManager(this.agpCptr, this, index);
        if (cptr == 0) {
            return null;
        }
        return new CorePropertyHandle(cptr, false);
    }
}
