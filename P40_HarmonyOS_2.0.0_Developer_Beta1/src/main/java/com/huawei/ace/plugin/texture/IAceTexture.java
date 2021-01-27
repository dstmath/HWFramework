package com.huawei.ace.plugin.texture;

public interface IAceTexture {
    void markTextureFrameAvailable(long j);

    void registerTexture(long j, Object obj);

    void unregisterTexture(long j);
}
