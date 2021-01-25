package com.huawei.ace.plugin.internal;

import java.nio.ByteBuffer;

public interface FrontendMessageReply {
    void reply(ByteBuffer byteBuffer);

    void replyPluginGetError(int i, String str);
}
