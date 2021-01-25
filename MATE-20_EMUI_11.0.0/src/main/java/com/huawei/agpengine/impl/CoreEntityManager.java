package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreEntityManager {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreEntityManager(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreEntityManager obj) {
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
    public int create() {
        return CoreJni.createInCoreEntityManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void destroy(int entity) {
        CoreJni.destroyInCoreEntityManager(this.agpCptr, this, entity);
    }

    /* access modifiers changed from: package-private */
    public void destroyAllEntities() {
        CoreJni.destroyAllEntitiesInCoreEntityManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean alive(int entity) {
        return CoreJni.aliveInCoreEntityManager(this.agpCptr, this, entity);
    }

    /* access modifiers changed from: package-private */
    public long maxEntities() {
        return CoreJni.maxEntitiesInCoreEntityManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public long getGenerationCounter() {
        return CoreJni.getGenerationCounterInCoreEntityManager(this.agpCptr, this);
    }
}
