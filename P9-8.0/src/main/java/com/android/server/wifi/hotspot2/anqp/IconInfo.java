package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.ByteBufferReader;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IconInfo {
    private final String mFileName;
    private final int mHeight;
    private final String mIconType;
    private final String mLanguage;
    private final int mWidth;

    public IconInfo(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() < 9) {
            throw new ProtocolException("Truncated icon meta data");
        }
        this.mWidth = payload.getShort() & Constants.SHORT_MASK;
        this.mHeight = payload.getShort() & Constants.SHORT_MASK;
        this.mLanguage = ByteBufferReader.readString(payload, 3, StandardCharsets.US_ASCII).trim();
        this.mIconType = ByteBufferReader.readStringWithByteLength(payload, StandardCharsets.US_ASCII);
        this.mFileName = ByteBufferReader.readStringWithByteLength(payload, StandardCharsets.UTF_8);
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public String getLanguage() {
        return this.mLanguage;
    }

    public String getIconType() {
        return this.mIconType;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        IconInfo that = (IconInfo) thatObject;
        if (this.mHeight == that.mHeight && this.mWidth == that.mWidth && this.mFileName.equals(that.mFileName) && this.mIconType.equals(that.mIconType)) {
            z = this.mLanguage.equals(that.mLanguage);
        }
        return z;
    }

    public int hashCode() {
        return (((((((this.mWidth * 31) + this.mHeight) * 31) + this.mLanguage.hashCode()) * 31) + this.mIconType.hashCode()) * 31) + this.mFileName.hashCode();
    }

    public String toString() {
        return "IconInfo{Width=" + this.mWidth + ", Height=" + this.mHeight + ", Language=" + this.mLanguage + ", IconType='" + this.mIconType + '\'' + ", FileName='" + this.mFileName + '\'' + '}';
    }
}
