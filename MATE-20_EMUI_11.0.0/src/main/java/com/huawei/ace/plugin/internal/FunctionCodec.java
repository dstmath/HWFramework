package com.huawei.ace.plugin.internal;

import com.huawei.ace.plugin.Function;
import java.nio.ByteBuffer;

public interface FunctionCodec {
    Function decodeFunction(ByteBuffer byteBuffer);

    ByteBuffer encodeErrorReply(int i, Object obj);

    ByteBuffer encodeSuccessReply(int i, Object obj);

    ByteBuffer encodeSuccessReply(String str);
}
