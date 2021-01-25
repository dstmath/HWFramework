package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreSceneUtil {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSceneUtil(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSceneUtil obj) {
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
    public int createCamera(CoreEcs ecs, CoreVec3 position, CoreQuat rotation, float zNear, float zFar, float fovDegrees) {
        return CoreJni.createCameraInCoreSceneUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, CoreVec3.getCptr(position), position, CoreQuat.getCptr(rotation), rotation, zNear, zFar, fovDegrees);
    }

    /* access modifiers changed from: package-private */
    public void updateCameraViewport(CoreEcs ecs, int entity, CoreUVec2 renderResolution) {
        CoreJni.updateCameraViewportInCoreSceneUtil0(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, entity, CoreUVec2.getCptr(renderResolution), renderResolution);
    }

    /* access modifiers changed from: package-private */
    public void updateCameraViewport(CoreEcs ecs, int entity, CoreUVec2 renderResolution, boolean autoAspect, float fovY, float orthoScale) {
        CoreJni.updateCameraViewportInCoreSceneUtil1(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, entity, CoreUVec2.getCptr(renderResolution), renderResolution, autoAspect, fovY, orthoScale);
    }

    /* access modifiers changed from: package-private */
    public int createLight(CoreEcs ecs, CoreLightComponent lightComponent, CoreVec3 position, CoreQuat rotation) {
        return CoreJni.createLightInCoreSceneUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, CoreLightComponent.getCptr(lightComponent), lightComponent, CoreVec3.getCptr(position), position, CoreQuat.getCptr(rotation), rotation);
    }

    static class CoreReflectionPlane {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreReflectionPlane(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreReflectionPlane obj) {
            long j;
            if (obj == null) {
                return 0;
            }
            synchronized (obj) {
                j = obj.agpCptr;
            }
            return j;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public synchronized void delete() {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSceneUtilCoreReflectionPlane(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void setEntity(int value) {
            CoreJni.setVarentityCoreSceneUtilCoreReflectionPlane(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public int getEntity() {
            return CoreJni.getVarentityCoreSceneUtilCoreReflectionPlane(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setMesh(CoreResourceHandle value) {
            CoreJni.setVarmeshCoreSceneUtilCoreReflectionPlane(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getMesh() {
            long cptr = CoreJni.getVarmeshCoreSceneUtilCoreReflectionPlane(this.agpCptr, this);
            if (cptr == 0) {
                return null;
            }
            return new CoreResourceHandle(cptr, false);
        }

        /* access modifiers changed from: package-private */
        public void setMaterial(CoreResourceHandle value) {
            CoreJni.setVarmaterialCoreSceneUtilCoreReflectionPlane(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getMaterial() {
            long cptr = CoreJni.getVarmaterialCoreSceneUtilCoreReflectionPlane(this.agpCptr, this);
            if (cptr == 0) {
                return null;
            }
            return new CoreResourceHandle(cptr, false);
        }

        /* access modifiers changed from: package-private */
        public void setColorTarget(long value) {
            CoreJni.setVarcolorTargetCoreSceneUtilCoreReflectionPlane(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public long getColorTarget() {
            return CoreJni.getVarcolorTargetCoreSceneUtilCoreReflectionPlane(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setDepthTarget(long value) {
            CoreJni.setVardepthTargetCoreSceneUtilCoreReflectionPlane(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public long getDepthTarget() {
            return CoreJni.getVardepthTargetCoreSceneUtilCoreReflectionPlane(this.agpCptr, this);
        }

        CoreReflectionPlane() {
            this(CoreJni.newCoreReflectionPlane(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreReflectionPlane createReflectionPlane(CoreEcs ecs, String name, CoreVec2 size, CoreVec3 position, CoreQuat rotation) {
        return new CoreReflectionPlane(CoreJni.createReflectionPlaneInCoreSceneUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, name, CoreVec2.getCptr(size), size, CoreVec3.getCptr(position), position, CoreQuat.getCptr(rotation), rotation), true);
    }

    /* access modifiers changed from: package-private */
    public void destroyReflectionPlane(CoreEcs ecs, CoreReflectionPlane plane) {
        CoreJni.destroyReflectionPlaneInCoreSceneUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, CoreReflectionPlane.getCptr(plane), plane);
    }
}
