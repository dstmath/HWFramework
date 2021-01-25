package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreEntity {
    private transient long agpCptrCoreEntity;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreEntity(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreEntity = j;
    }

    static long getCptr(CoreEntity coreEntity) {
        if (coreEntity == null) {
            return 0;
        }
        return coreEntity.agpCptrCoreEntity;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreEntity != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEntity(this.agpCptrCoreEntity);
                }
                this.agpCptrCoreEntity = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEntity coreEntity, boolean z) {
        if (coreEntity != null) {
            synchronized (coreEntity.lock) {
                coreEntity.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEntity);
    }

    /* access modifiers changed from: package-private */
    public void setId(long j) {
        CoreJni.setVaridCoreEntity(this.agpCptrCoreEntity, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getId() {
        return CoreJni.getVaridCoreEntity(this.agpCptrCoreEntity, this);
    }

    CoreEntity() {
        this(CoreJni.newCoreEntity(), true);
    }
}
