package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreEntityManager {
    private transient long agpCptrCoreEntityManager;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreEntityManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreEntityManager = j;
    }

    static long getCptr(CoreEntityManager coreEntityManager) {
        if (coreEntityManager == null) {
            return 0;
        }
        return coreEntityManager.agpCptrCoreEntityManager;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreEntityManager != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEntityManager(this.agpCptrCoreEntityManager);
                }
                this.agpCptrCoreEntityManager = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEntityManager coreEntityManager, boolean z) {
        if (coreEntityManager != null) {
            synchronized (coreEntityManager.lock) {
                coreEntityManager.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEntityManager);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity create() {
        return new CoreEntity(CoreJni.createInCoreEntityManager(this.agpCptrCoreEntityManager, this), true);
    }

    /* access modifiers changed from: package-private */
    public void destroy(CoreEntity coreEntity) {
        CoreJni.destroyInCoreEntityManager(this.agpCptrCoreEntityManager, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public void destroyAllEntities() {
        CoreJni.destroyAllEntitiesInCoreEntityManager(this.agpCptrCoreEntityManager, this);
    }

    /* access modifiers changed from: package-private */
    public boolean alive(CoreEntity coreEntity) {
        return CoreJni.aliveInCoreEntityManager(this.agpCptrCoreEntityManager, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public long maxEntities() {
        return CoreJni.maxEntitiesInCoreEntityManager(this.agpCptrCoreEntityManager, this);
    }

    /* access modifiers changed from: package-private */
    public long getGenerationCounter() {
        return CoreJni.getGenerationCounterInCoreEntityManager(this.agpCptrCoreEntityManager, this);
    }
}
