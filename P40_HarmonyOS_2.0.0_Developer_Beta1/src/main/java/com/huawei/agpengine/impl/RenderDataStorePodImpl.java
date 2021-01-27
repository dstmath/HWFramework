package com.huawei.agpengine.impl;

import com.huawei.agpengine.resources.RenderDataStorePod;
import java.nio.ByteBuffer;

class RenderDataStorePodImpl implements RenderDataStorePod {
    private final CoreRenderDataStorePod mDataStore;
    private final EngineImpl mEngine;

    RenderDataStorePodImpl(EngineImpl engine, CoreRenderDataStorePod dataStore) {
        this.mEngine = engine;
        this.mDataStore = dataStore;
    }

    private static CoreShaderSpecializationRenderPod setSpecialization(int[] constants) {
        CoreShaderSpecializationRenderPod shaderSpecialization = new CoreShaderSpecializationRenderPod();
        int constantCount = Math.min(constants.length, (int) CoreShaderSpecializationRenderPod.getMaxSpecializationConstantCount());
        shaderSpecialization.setSpecializationConstantCount((long) constantCount);
        for (int i = 0; i < constantCount; i++) {
            shaderSpecialization.setConstant(i, (long) constants[i]);
        }
        return shaderSpecialization;
    }

    private ByteBuffer checkBuffer(ByteBuffer data) {
        if (data.isDirect()) {
            return data;
        }
        throw new IllegalArgumentException("data must be a direct buffer.");
    }

    @Override // com.huawei.agpengine.resources.RenderDataStorePod
    public void createPod(String typeName, String name, ByteBuffer data) {
        Core.createPod(this.mDataStore, typeName, name, new CoreByteArrayView(checkBuffer(data)));
    }

    @Override // com.huawei.agpengine.resources.RenderDataStorePod
    public void createShaderSpecializationRenderPod(String typeName, String name, int[] constants) {
        Core.createShaderSpecializationRenderPod(this.mDataStore, typeName, name, setSpecialization(constants));
    }

    @Override // com.huawei.agpengine.resources.RenderDataStorePod
    public void destroyPod(String typeName, String name) {
        Core.destroyPod(this.mDataStore, typeName, name);
    }

    @Override // com.huawei.agpengine.resources.RenderDataStorePod
    public void set(String name, ByteBuffer data) {
        Core.setPod(this.mDataStore, name, new CoreByteArrayView(checkBuffer(data)));
    }

    @Override // com.huawei.agpengine.resources.RenderDataStorePod
    public void setShaderSpecializationRenderPod(String name, int[] constants) {
        Core.setShaderSpecializationRenderPod(this.mDataStore, name, setSpecialization(constants));
    }

    @Override // com.huawei.agpengine.resources.RenderDataStorePod
    public byte[] get(String name) {
        CoreByteArrayView bav = Core.getPod(this.mDataStore, name);
        int dataSize = (int) bav.size();
        byte[] dataArray = new byte[dataSize];
        for (int i = 0; i < dataSize; i++) {
            dataArray[i] = (byte) bav.get((long) i);
        }
        return dataArray;
    }

    @Override // com.huawei.agpengine.resources.RenderDataStorePod
    public void release() {
        Core.destroyRenderDataStorePod(this.mEngine.getAgpContext().getEngine(), this.mDataStore);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof RenderDataStorePod) || CoreRenderDataStorePod.getCptr(this.mDataStore) != CoreRenderDataStorePod.getCptr(((RenderDataStorePodImpl) obj).mDataStore)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (int) CoreRenderDataStorePod.getCptr(this.mDataStore);
    }
}
