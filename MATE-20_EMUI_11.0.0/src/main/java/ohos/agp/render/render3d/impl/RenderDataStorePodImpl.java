package ohos.agp.render.render3d.impl;

import java.nio.ByteBuffer;
import ohos.agp.render.render3d.resources.RenderDataStorePod;

class RenderDataStorePodImpl implements RenderDataStorePod {
    private final CoreRenderDataStorePod mDataStore;
    private final EngineImpl mEngine;

    RenderDataStorePodImpl(EngineImpl engineImpl, CoreRenderDataStorePod coreRenderDataStorePod) {
        this.mEngine = engineImpl;
        this.mDataStore = coreRenderDataStorePod;
    }

    @Override // ohos.agp.render.render3d.resources.RenderDataStorePod
    public void createPod(String str, String str2, ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect()) {
            Core.createPod(this.mDataStore, str, str2, new CoreByteArrayView(byteBuffer));
            return;
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.agp.render.render3d.resources.RenderDataStorePod
    public void destroyPod(String str, String str2) {
        Core.destroyPod(this.mDataStore, str, str2);
    }

    @Override // ohos.agp.render.render3d.resources.RenderDataStorePod
    public void set(String str, ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect()) {
            Core.setPod(this.mDataStore, str, new CoreByteArrayView(byteBuffer));
            return;
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.agp.render.render3d.resources.RenderDataStorePod
    public byte[] get(String str) {
        CoreByteArrayView pod = Core.getPod(this.mDataStore, str);
        int size = (int) pod.size();
        byte[] bArr = new byte[size];
        for (int i = 0; i < size; i++) {
            bArr[i] = (byte) pod.get((long) i);
        }
        return bArr;
    }

    @Override // ohos.agp.render.render3d.resources.RenderDataStorePod
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
