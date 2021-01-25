package com.huawei.ace.plugin.internal;

import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.internal.DefaultMessageCodec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.serializer.SerializerFeature;

public final class DefaultFunctionCodec implements FunctionCodec {
    private static final byte ERROR_REPLY = 1;
    public static final DefaultFunctionCodec INSTANCE = new DefaultFunctionCodec(DefaultMessageCodec.INSTANCE);
    private static final String LOG_TAG = "DefaultFunctionCodec";
    private static final byte SUCCESS_REPLY = 0;
    private final DefaultMessageCodec messageCodec;

    private DefaultFunctionCodec(DefaultMessageCodec defaultMessageCodec) {
        this.messageCodec = defaultMessageCodec;
    }

    @Override // com.huawei.ace.plugin.internal.FunctionCodec
    public Function decodeFunction(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.nativeOrder());
        Object readValue = this.messageCodec.readValue(byteBuffer);
        byte readByteSize = DefaultMessageCodec.readByteSize(byteBuffer);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < readByteSize; i++) {
            arrayList.add(this.messageCodec.readValue(byteBuffer));
        }
        if ((readValue instanceof String) && !byteBuffer.hasRemaining()) {
            return new Function((String) readValue, arrayList);
        }
        throw new IllegalArgumentException("Decode function failed");
    }

    @Override // com.huawei.ace.plugin.internal.FunctionCodec
    public ByteBuffer encodeSuccessReply(int i, Object obj) {
        HashMap hashMap = new HashMap(2, 1.0f);
        hashMap.put("code", Integer.valueOf(i));
        hashMap.put("data", obj);
        DefaultMessageCodec.ExposedByteArrayOutputStream exposedByteArrayOutputStream = new DefaultMessageCodec.ExposedByteArrayOutputStream();
        this.messageCodec.writeValue(exposedByteArrayOutputStream, JSONObject.toJSONString(hashMap, new SerializerFeature[]{SerializerFeature.WriteMapNullValue}));
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(exposedByteArrayOutputStream.size());
        allocateDirect.put(exposedByteArrayOutputStream.buffer(), 0, exposedByteArrayOutputStream.size());
        return allocateDirect;
    }

    @Override // com.huawei.ace.plugin.internal.FunctionCodec
    public ByteBuffer encodeSuccessReply(String str) {
        DefaultMessageCodec.ExposedByteArrayOutputStream exposedByteArrayOutputStream = new DefaultMessageCodec.ExposedByteArrayOutputStream();
        this.messageCodec.writeValue(exposedByteArrayOutputStream, str);
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(exposedByteArrayOutputStream.size());
        allocateDirect.put(exposedByteArrayOutputStream.buffer(), 0, exposedByteArrayOutputStream.size());
        return allocateDirect;
    }

    @Override // com.huawei.ace.plugin.internal.FunctionCodec
    public ByteBuffer encodeErrorReply(int i, Object obj) {
        HashMap hashMap = new HashMap(2, 1.0f);
        hashMap.put("code", Integer.valueOf(i));
        hashMap.put("data", obj);
        String jSONString = JSONObject.toJSONString(hashMap, new SerializerFeature[]{SerializerFeature.WriteMapNullValue});
        DefaultMessageCodec.ExposedByteArrayOutputStream exposedByteArrayOutputStream = new DefaultMessageCodec.ExposedByteArrayOutputStream();
        this.messageCodec.writeValue(exposedByteArrayOutputStream, jSONString);
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(exposedByteArrayOutputStream.size());
        allocateDirect.put(exposedByteArrayOutputStream.buffer(), 0, exposedByteArrayOutputStream.size());
        return allocateDirect;
    }
}
