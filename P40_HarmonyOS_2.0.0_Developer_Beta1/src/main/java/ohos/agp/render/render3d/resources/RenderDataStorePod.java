package ohos.agp.render.render3d.resources;

import java.nio.ByteBuffer;

public interface RenderDataStorePod {
    void createPod(String str, String str2, ByteBuffer byteBuffer);

    void destroyPod(String str, String str2);

    byte[] get(String str);

    void release();

    void set(String str, ByteBuffer byteBuffer);
}
