package com.huawei.agpengine.resources;

import java.nio.ByteBuffer;

public interface RenderDataStorePod {
    void createPod(String str, String str2, ByteBuffer byteBuffer);

    void createShaderSpecializationRenderPod(String str, String str2, int[] iArr);

    void destroyPod(String str, String str2);

    byte[] get(String str);

    void release();

    void set(String str, ByteBuffer byteBuffer);

    void setShaderSpecializationRenderPod(String str, int[] iArr);
}
