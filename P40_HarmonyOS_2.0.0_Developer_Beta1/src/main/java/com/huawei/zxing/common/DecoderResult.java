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

    public DecoderResult(byte[] rawBytes2, String text2, List<byte[]> byteSegments2, String ecLevel2) {
        this.rawBytes = rawBytes2;
        this.text = text2;
        this.byteSegments = byteSegments2;
        this.ecLevel = ecLevel2;
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

    public void setErrorsCorrected(Integer errorsCorrected2) {
        this.errorsCorrected = errorsCorrected2;
    }

    public Integer getErasures() {
        return this.erasures;
    }

    public void setErasures(Integer erasures2) {
        this.erasures = erasures2;
    }

    public Object getOther() {
        return this.other;
    }

    public void setOther(Object other2) {
        this.other = other2;
    }
}
