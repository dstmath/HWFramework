package com.huawei.zxing;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

public final class Result implements Serializable {
    private final BarcodeFormat format;
    private final byte[] rawBytes;
    private Map<ResultMetadataType, Object> resultMetadata;
    private ResultPoint[] resultPoints;
    private final String text;
    private final long timestamp;

    public Result(String text2, byte[] rawBytes2, ResultPoint[] resultPoints2, BarcodeFormat format2) {
        this(text2, rawBytes2, resultPoints2, format2, System.currentTimeMillis());
    }

    public Result(String text2, byte[] rawBytes2, ResultPoint[] resultPoints2, BarcodeFormat format2, long timestamp2) {
        this.text = text2;
        this.rawBytes = rawBytes2;
        this.resultPoints = resultPoints2;
        this.format = format2;
        this.resultMetadata = null;
        this.timestamp = timestamp2;
    }

    public String getText() {
        return this.text;
    }

    public byte[] getRawBytes() {
        return this.rawBytes;
    }

    public ResultPoint[] getResultPoints() {
        return this.resultPoints;
    }

    public BarcodeFormat getBarcodeFormat() {
        return this.format;
    }

    public Map<ResultMetadataType, Object> getResultMetadata() {
        return this.resultMetadata;
    }

    public void putMetadata(ResultMetadataType type, Object value) {
        if (this.resultMetadata == null) {
            this.resultMetadata = new EnumMap(ResultMetadataType.class);
        }
        this.resultMetadata.put(type, value);
    }

    public void putAllMetadata(Map<ResultMetadataType, Object> metadata) {
        if (metadata != null) {
            Map<ResultMetadataType, Object> map = this.resultMetadata;
            if (map == null) {
                this.resultMetadata = metadata;
            } else {
                map.putAll(metadata);
            }
        }
    }

    public void addResultPoints(ResultPoint[] newPoints) {
        ResultPoint[] oldPoints = this.resultPoints;
        if (oldPoints == null) {
            this.resultPoints = newPoints;
        } else if (newPoints != null && newPoints.length > 0) {
            ResultPoint[] allPoints = new ResultPoint[(oldPoints.length + newPoints.length)];
            System.arraycopy(oldPoints, 0, allPoints, 0, oldPoints.length);
            System.arraycopy(newPoints, 0, allPoints, oldPoints.length, newPoints.length);
            this.resultPoints = allPoints;
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Override // java.lang.Object
    public String toString() {
        return this.text;
    }
}
