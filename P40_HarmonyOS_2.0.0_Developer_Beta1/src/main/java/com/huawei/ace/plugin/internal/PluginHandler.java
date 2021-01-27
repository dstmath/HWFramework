package com.huawei.ace.plugin.internal;

import java.nio.ByteBuffer;

public interface PluginHandler {
    void onReceiveMessage(ByteBuffer byteBuffer, FrontendMessageReply frontendMessageReply, String str, int i);
}
