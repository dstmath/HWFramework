package com.huawei.agpengine.impl;

class CorePicking extends CoreInterface {
    private transient long agpCptr;

    CorePicking(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCorePicking(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CorePicking obj) {
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
    public CoreVec3 screenToWorld(CoreEcs ecs, int cameraEntity, CoreVec3 screenCoordinate) {
        return new CoreVec3(CoreJni.screenToWorldInCorePicking(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, cameraEntity, CoreVec3.getCptr(screenCoordinate), screenCoordinate), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 worldToScreen(CoreEcs ecs, int cameraEntity, CoreVec3 worldCoordinate) {
        return new CoreVec3(CoreJni.worldToScreenInCorePicking(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, cameraEntity, CoreVec3.getCptr(worldCoordinate), worldCoordinate), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMinAndMax getWorldAabb(CoreMat4X4 world, CoreVec3 aabbMin, CoreVec3 aabbMax) {
        return new CoreMinAndMax(CoreJni.getWorldAabbInCorePicking(this.agpCptr, this, CoreMat4X4.getCptr(world), world, CoreVec3.getCptr(aabbMin), aabbMin, CoreVec3.getCptr(aabbMax), aabbMax), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMinAndMax getWorldMatrixComponentAabb(int entity, boolean isRecursive, CoreEcs ecs) {
        return new CoreMinAndMax(CoreJni.getWorldMatrixComponentAabbInCorePicking(this.agpCptr, this, entity, isRecursive, CoreEcs.getCptr(ecs), ecs), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMinAndMax getTransformComponentAabb(int entity, boolean isRecursive, CoreEcs ecs) {
        return new CoreMinAndMax(CoreJni.getTransformComponentAabbInCorePicking(this.agpCptr, this, entity, isRecursive, CoreEcs.getCptr(ecs), ecs), true);
    }

    /* access modifiers changed from: package-private */
    public CoreRayCastResultArray rayCast(CoreEcs ecs, CoreVec3 start, CoreVec3 direction) {
        return new CoreRayCastResultArray(CoreJni.rayCastInCorePicking(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, CoreVec3.getCptr(start), start, CoreVec3.getCptr(direction), direction), true);
    }

    /* access modifiers changed from: package-private */
    public CoreRayCastResultArray rayCastFromCamera(CoreEcs ecs, int camera, CoreVec2 screenPos) {
        return new CoreRayCastResultArray(CoreJni.rayCastFromCameraInCorePicking(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, camera, CoreVec2.getCptr(screenPos), screenPos), true);
    }

    static CorePicking dynamicCast(CoreInterface ptr) {
        long cptr = CoreJni.dynamicCastInCorePicking(CoreInterface.getCptr(ptr), ptr);
        if (cptr == 0) {
            return null;
        }
        return new CorePicking(cptr, false);
    }

    static CorePicking getInterface(CorePluginRegister ptr) {
        long cptr = CoreJni.getInterfaceInCorePicking(CorePluginRegister.getCptr(ptr), ptr);
        if (cptr == 0) {
            return null;
        }
        return new CorePicking(cptr, false);
    }
}
