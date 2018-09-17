package com.huawei.zxing.datamatrix.encoder;

interface Encoder {
    void encode(EncoderContext encoderContext);

    int getEncodingMode();
}
