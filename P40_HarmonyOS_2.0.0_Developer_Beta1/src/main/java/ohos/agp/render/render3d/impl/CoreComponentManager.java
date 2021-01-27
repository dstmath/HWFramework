package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreComponentManager {
    static final long INVALID_COMPONENT_ID = -1;
    private transient long agpCptrCoreComponentManager;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreComponentManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreComponentManager = j;
    }

    static long getCptr(CoreComponentManager coreComponentManager) {
        if (coreComponentManager == null) {
            return 0;
        }
        return coreComponentManager.agpCptrCoreComponentManager;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreComponentManager != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreComponentManager(this.agpCptrCoreComponentManager);
                }
                this.agpCptrCoreComponentManager = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreComponentManager coreComponentManager, boolean z) {
        if (coreComponentManager != null) {
            coreComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreComponentManager);
    }

    /* access modifiers changed from: package-private */
    public String name() {
        return CoreJni.nameInCoreComponentManager(this.agpCptrCoreComponentManager, this);
    }

    /* access modifiers changed from: package-private */
    public long componentCount() {
        return CoreJni.componentCountInCoreComponentManager(this.agpCptrCoreComponentManager, this);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity entity(long j) {
        return new CoreEntity(CoreJni.entityInCoreComponentManager(this.agpCptrCoreComponentManager, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public long getComponentGeneration(long j) {
        return CoreJni.getComponentGenerationInCoreComponentManager(this.agpCptrCoreComponentManager, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean hasComponent(CoreEntity coreEntity) {
        return CoreJni.hasComponentInCoreComponentManager(this.agpCptrCoreComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public long getComponentId(CoreEntity coreEntity) {
        return CoreJni.getComponentIdInCoreComponentManager(this.agpCptrCoreComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public void create(CoreEntity coreEntity) {
        CoreJni.createInCoreComponentManager(this.agpCptrCoreComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public boolean destroy(CoreEntity coreEntity) {
        return CoreJni.destroyInCoreComponentManager0(this.agpCptrCoreComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public void gc() {
        CoreJni.gcInCoreComponentManager(this.agpCptrCoreComponentManager, this);
    }

    /* access modifiers changed from: package-private */
    public void destroy(CoreEntityArrayView coreEntityArrayView) {
        CoreJni.destroyInCoreComponentManager1(this.agpCptrCoreComponentManager, this, CoreEntityArrayView.getCptr(coreEntityArrayView), coreEntityArrayView);
    }

    /* access modifiers changed from: package-private */
    public long getModifiedFlags() {
        return CoreJni.getModifiedFlagsInCoreComponentManager(this.agpCptrCoreComponentManager, this);
    }

    /* access modifiers changed from: package-private */
    public void clearModifiedFlags() {
        CoreJni.clearModifiedFlagsInCoreComponentManager(this.agpCptrCoreComponentManager, this);
    }

    /* access modifiers changed from: package-private */
    public long getGenerationCounter() {
        return CoreJni.getGenerationCounterInCoreComponentManager(this.agpCptrCoreComponentManager, this);
    }
}
