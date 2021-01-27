package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreCameraComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreCameraComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreCameraComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreCameraComponentManager obj) {
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
    @Override // com.huawei.agpengine.impl.CoreComponentManager
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
    public void set(long index, CoreCameraComponent data) {
        CoreJni.setInCoreCameraComponentManager0(this.agpCptr, this, index, CoreCameraComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreCameraComponent data) {
        CoreJni.setInCoreCameraComponentManager1(this.agpCptr, this, entity, CoreCameraComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreCameraComponent get(long index) {
        return new CoreCameraComponent(CoreJni.getInCoreCameraComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreCameraComponent get(int entity) {
        return new CoreCameraComponent(CoreJni.getInCoreCameraComponentManager1(this.agpCptr, this, entity), true);
    }
}
