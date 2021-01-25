package com.huawei.nb.ai;

import java.nio.ByteBuffer;

public class AiModelByteBuffer {
    private ByteBuffer mModelByteBuffer = null;
    private final AiModelResponse mResponse;

    public AiModelByteBuffer(AiModelResponse aiModelResponse) {
        this.mResponse = aiModelResponse;
    }

    public AiModelResponse getAiModelResponse() {
        return this.mResponse;
    }

    public int getBufferSize() {
        ByteBuffer byteBuffer = this.mModelByteBuffer;
        if (byteBuffer != null) {
            return byteBuffer.capacity();
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
