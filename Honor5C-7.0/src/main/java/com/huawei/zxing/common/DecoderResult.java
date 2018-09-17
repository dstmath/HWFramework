package com.huawei.zxing.common;

import java.util.List;

public final class DecoderResult {
    private final List<byte[]> byteSegments;
    private final String ecLevel;
    private Integer erasures;
    private Integer errorsCorrected;
    private Object other;
    private final byte[] rawBytes;
    private final String text;

    public DecoderResult(byte[] rawBytes, String text, List<byte[]> byteSegments, String ecLevel) {
        this.rawBytes = rawBytes;
        this.text = text;
        this.byteSegments = byteSegments;
        this.ecLevel = ecLevel;
    }

    public byte[] getRawBytes() {
        return this.rawBytes;
    }

    public String getText() {
        return this.text;
    }

    public List<byte[]> getByteSegments() {
        return this.byteSegments;
    }

    public String getECLevel() {
        return this.ecLevel;
    }

    public Integer getErrorsCorrected() {
        return this.errorsCorrected;
    }

    public void setErrorsCorrected(Integer errorsCorrected) {
        this.errorsCorrected = errorsCorrected;
    }

    public Integer getErasures() {
        return this.erasures;
    }

    public void setErasures(Integer erasures) {
        this.erasures = erasures;
    }

    public Object getOther() {
        return this.other;
    }

    public void setOther(Object other) {
        this.other = other;
    }
}
