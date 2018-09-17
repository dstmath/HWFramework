package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HSWanMetricsElement extends ANQPElement {
    public static final int AT_CAPACITY_MASK = 8;
    public static final int EXPECTED_BUFFER_SIZE = 13;
    public static final int LINK_STATUS_DOWN = 2;
    public static final int LINK_STATUS_MASK = 3;
    public static final int LINK_STATUS_RESERVED = 0;
    public static final int LINK_STATUS_TEST = 3;
    public static final int LINK_STATUS_UP = 1;
    private static final int MAX_LOAD = 256;
    public static final int SYMMETRIC_LINK_MASK = 4;
    private final boolean mCapped;
    private final int mDownlinkLoad;
    private final long mDownlinkSpeed;
    private final int mLMD;
    private final int mStatus;
    private final boolean mSymmetric;
    private final int mUplinkLoad;
    private final long mUplinkSpeed;

    public HSWanMetricsElement(int status, boolean symmetric, boolean capped, long downlinkSpeed, long uplinkSpeed, int downlinkLoad, int uplinkLoad, int lmd) {
        super(ANQPElementType.HSWANMetrics);
        this.mStatus = status;
        this.mSymmetric = symmetric;
        this.mCapped = capped;
        this.mDownlinkSpeed = downlinkSpeed;
        this.mUplinkSpeed = uplinkSpeed;
        this.mDownlinkLoad = downlinkLoad;
        this.mUplinkLoad = uplinkLoad;
        this.mLMD = lmd;
    }

    public static HSWanMetricsElement parse(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() != 13) {
            throw new ProtocolException("Unexpected buffer size: " + payload.remaining());
        }
        int wanInfo = payload.get() & Constants.BYTE_MASK;
        return new HSWanMetricsElement(wanInfo & 3, (wanInfo & 4) != 0, (wanInfo & 8) != 0, ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 4) & Constants.INT_MASK, ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 4) & Constants.INT_MASK, payload.get() & Constants.BYTE_MASK, payload.get() & Constants.BYTE_MASK, ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK);
    }

    public int getStatus() {
        return this.mStatus;
    }

    public boolean isSymmetric() {
        return this.mSymmetric;
    }

    public boolean isCapped() {
        return this.mCapped;
    }

    public long getDownlinkSpeed() {
        return this.mDownlinkSpeed;
    }

    public long getUplinkSpeed() {
        return this.mUplinkSpeed;
    }

    public int getDownlinkLoad() {
        return this.mDownlinkLoad;
    }

    public int getUplinkLoad() {
        return this.mUplinkLoad;
    }

    public int getLMD() {
        return this.mLMD;
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof HSWanMetricsElement)) {
            return false;
        }
        HSWanMetricsElement that = (HSWanMetricsElement) thatObject;
        if (this.mStatus != that.mStatus || this.mSymmetric != that.mSymmetric || this.mCapped != that.mCapped || this.mDownlinkSpeed != that.mDownlinkSpeed || this.mUplinkSpeed != that.mUplinkSpeed || this.mDownlinkLoad != that.mDownlinkLoad || this.mUplinkLoad != that.mUplinkLoad) {
            z = false;
        } else if (this.mLMD != that.mLMD) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (int) (((((((long) this.mStatus) + this.mDownlinkSpeed) + this.mUplinkSpeed) + ((long) this.mDownlinkLoad)) + ((long) this.mUplinkLoad)) + ((long) this.mLMD));
    }

    public String toString() {
        return String.format("HSWanMetrics{mStatus=%s, mSymmetric=%s, mCapped=%s, mDlSpeed=%d, mUlSpeed=%d, mDlLoad=%f, mUlLoad=%f, mLMD=%d}", new Object[]{Integer.valueOf(this.mStatus), Boolean.valueOf(this.mSymmetric), Boolean.valueOf(this.mCapped), Long.valueOf(this.mDownlinkSpeed), Long.valueOf(this.mUplinkSpeed), Double.valueOf((((double) this.mDownlinkLoad) * 100.0d) / 256.0d), Double.valueOf((((double) this.mUplinkLoad) * 100.0d) / 256.0d), Integer.valueOf(this.mLMD)});
    }
}
