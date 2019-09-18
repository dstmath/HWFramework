package com.huawei.nb.ai;

import java.nio.ByteBuffer;

public class AiModelByteBuffer {
    private ByteBuffer mModelByteBuffer = null;
    private final AiModelResponse mResponse;

    public AiModelByteBuffer(AiModelResponse response) {
        this.mResponse = response;
    }

    public AiModelResponse getAiModelResponse() {
        return this.mResponse;
    }

    public int getBufferSize() {
        if (this.mModelByteBuffer != null) {
            return this.mModelByteBuffer.capacity();
        }
        return 0;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.mModelByteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return this.mModelByteBuffer;
    }
}
