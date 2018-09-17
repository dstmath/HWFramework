package com.huawei.zxing.qrcode.decoder;

import com.huawei.zxing.ResultPoint;

public final class QRCodeDecoderMetaData {
    private final boolean mirrored;

    QRCodeDecoderMetaData(boolean mirrored) {
        this.mirrored = mirrored;
    }

    public boolean isMirrored() {
        return this.mirrored;
    }

    public void applyMirroredCorrection(ResultPoint[] points) {
        if (this.mirrored && points != null && points.length >= 3) {
            ResultPoint bottomLeft = points[0];
            points[0] = points[2];
            points[2] = bottomLeft;
        }
    }
}
