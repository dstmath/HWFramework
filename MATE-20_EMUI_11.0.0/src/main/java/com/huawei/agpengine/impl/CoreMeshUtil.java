package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMeshUtil {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMeshUtil(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMeshUtil obj) {
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
    public CoreResourceHandle generateCubeMesh(String name, CoreResourceHandle material, float width, float height, float depth) {
        return new CoreResourceHandle(CoreJni.generateCubeMeshInCoreMeshUtil(this.agpCptr, this, name, CoreResourceHandle.getCptr(material), material, width, height, depth), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle generatePlaneMesh(String name, CoreResourceHandle material, float width, float depth) {
        return new CoreResourceHandle(CoreJni.generatePlaneMeshInCoreMeshUtil(this.agpCptr, this, name, CoreResourceHandle.getCptr(material), material, width, depth), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle generateSphereMesh(String name, CoreResourceHandle material, float radius, long rings, long sectors) {
        return new CoreResourceHandle(CoreJni.generateSphereMeshInCoreMeshUtil(this.agpCptr, this, name, CoreResourceHandle.getCptr(material), material, radius, rings, sectors), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle generateConeMesh(String name, CoreResourceHandle material, float radius, float length, long sectors) {
        return new CoreResourceHandle(CoreJni.generateConeMeshInCoreMeshUtil(this.agpCptr, this, name, CoreResourceHandle.getCptr(material), material, radius, length, sectors), true);
    }

    /* access modifiers changed from: package-private */
    public int generateEntity(CoreEcs ecs, String name, CoreResourceHandle meshHandle) {
        return CoreJni.generateEntityInCoreMeshUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, name, CoreResourceHandle.getCptr(meshHandle), meshHandle);
    }

    /* access modifiers changed from: package-private */
    public int generateCube(CoreEcs ecs, String name, CoreResourceHandle material, float width, float height, float depth) {
        return CoreJni.generateCubeInCoreMeshUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, name, CoreResourceHandle.getCptr(material), material, width, height, depth);
    }

    /* access modifiers changed from: package-private */
    public int generatePlane(CoreEcs ecs, String name, CoreResourceHandle material, float width, float depth) {
        return CoreJni.generatePlaneInCoreMeshUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, name, CoreResourceHandle.getCptr(material), material, width, depth);
    }

    /* access modifiers changed from: package-private */
    public int generateSphere(CoreEcs ecs, String name, CoreResourceHandle material, float radius, long rings, long sectors) {
        return CoreJni.generateSphereInCoreMeshUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, name, CoreResourceHandle.getCptr(material), material, radius, rings, sectors);
    }

    /* access modifiers changed from: package-private */
    public int generateCone(CoreEcs ecs, String name, CoreResourceHandle material, float radius, float length, long sectors) {
        return CoreJni.generateConeInCoreMeshUtil(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, name, CoreResourceHandle.getCptr(material), material, radius, length, sectors);
    }
}
