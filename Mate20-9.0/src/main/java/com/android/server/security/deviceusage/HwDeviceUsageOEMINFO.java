package com.android.server.security.deviceusage;

import android.util.Log;
import android.util.Slog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HwDeviceUsageOEMINFO {
    private static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int OEMINFO_ENABLE_RETREAD = 163;
    private static final int OEMINFO_ENABLE_RETREAD_SIZE = 40;
    private static final String TAG = "HwDeviceUsageOEMINFO";
    private static HwDeviceUsageOEMINFO mInstance;
    private ByteBuffer mDataBuffer = ByteBuffer.allocate(40);
    private OEMINFOData mOEMINFOData = new OEMINFOData();

    static class OEMINFOData {
        public long mChargeTime;
        public long mFristUseTime;
        public int mOpenFlag;
        public long mScreenOnTime;
        public long mTalkTime;

        OEMINFOData() {
        }
    }

    private HwDeviceUsageOEMINFO() {
        getOEMINFOData();
    }

    public static synchronized HwDeviceUsageOEMINFO getInstance() {
        HwDeviceUsageOEMINFO hwDeviceUsageOEMINFO;
        synchronized (HwDeviceUsageOEMINFO.class) {
            if (mInstance == null) {
                mInstance = new HwDeviceUsageOEMINFO();
            }
            hwDeviceUsageOEMINFO = mInstance;
        }
        return hwDeviceUsageOEMINFO;
    }

    private void getOEMINFOByteArray() {
        byte[] oEMData = HwOEMInfoAdapter.getByteArrayFromOeminfo(OEMINFO_ENABLE_RETREAD, 40);
        if (oEMData == null || oEMData.length < 40) {
            Slog.e(TAG, "get OEMINFO error");
        } else {
            setByteArrayToDataBuffer(oEMData);
        }
    }

    public boolean getOpenFlag() {
        getOEMINFOData();
        boolean flag = this.mOEMINFOData.mOpenFlag != 0;
        Slog.d(TAG, "flag = " + flag);
        return flag;
    }

    public long getScreenOnTime() {
        if (HW_DEBUG) {
            Slog.i(TAG, "getScreenOnTime");
        }
        return this.mOEMINFOData.mScreenOnTime;
    }

    public long getChargeTime() {
        if (HW_DEBUG) {
            Slog.i(TAG, "getChargeTime");
        }
        return this.mOEMINFOData.mChargeTime;
    }

    public long getTalkTime() {
        return this.mOEMINFOData.mTalkTime;
    }

    public long getFristUseTime() {
        return this.mOEMINFOData.mFristUseTime;
    }

    /* access modifiers changed from: protected */
    public void setOpenFlag(int flag) {
        this.mOEMINFOData.mOpenFlag = flag;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, 40, this.mDataBuffer.array());
    }

    /* access modifiers changed from: protected */
    public void setScreenOnTime(long time) {
        this.mOEMINFOData.mScreenOnTime = time;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, 40, this.mDataBuffer.array());
    }

    /* access modifiers changed from: protected */
    public void setChargeTime(long time) {
        this.mOEMINFOData.mChargeTime = time;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, 40, this.mDataBuffer.array());
    }

    /* access modifiers changed from: protected */
    public void setTalkTime(long time) {
        this.mOEMINFOData.mTalkTime = time;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, 40, this.mDataBuffer.array());
    }

    /* access modifiers changed from: protected */
    public int setFristUseTime(long time) {
        this.mOEMINFOData.mFristUseTime = time;
        setDataBuffer();
        return HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, 40, this.mDataBuffer.array());
    }

    private void setDataBuffer() {
        this.mDataBuffer.clear();
        if (this.mDataBuffer.remaining() >= 40) {
            this.mDataBuffer.putInt(this.mOEMINFOData.mOpenFlag);
            this.mDataBuffer.putInt(0);
            this.mDataBuffer.putLong(this.mOEMINFOData.mScreenOnTime);
            this.mDataBuffer.putLong(this.mOEMINFOData.mChargeTime);
            this.mDataBuffer.putLong(this.mOEMINFOData.mTalkTime);
            this.mDataBuffer.putLong(this.mOEMINFOData.mFristUseTime);
            if (HW_DEBUG) {
                Slog.d(TAG, "setDataBuffer mOpenFlag = " + this.mOEMINFOData.mOpenFlag + " OEMINFOData.mScreenOnTime = " + this.mOEMINFOData.mScreenOnTime + " mChargeTime = " + this.mOEMINFOData.mChargeTime + " mTalkTime = " + this.mOEMINFOData.mTalkTime + " mFristUseTime = " + this.mOEMINFOData.mFristUseTime);
            }
        }
    }

    private void setByteArrayToDataBuffer(byte[] mByte) {
        this.mDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.mDataBuffer.clear();
        this.mDataBuffer.put(mByte);
    }

    private void getOEMINFOData() {
        getOEMINFOByteArray();
        if (this.mDataBuffer == null) {
            if (HW_DEBUG) {
                Slog.d(TAG, "mDataBuffer is null");
            }
            return;
        }
        this.mDataBuffer.flip();
        if (this.mDataBuffer.remaining() >= 40) {
            this.mOEMINFOData.mOpenFlag = this.mDataBuffer.getInt();
            this.mDataBuffer.getInt();
            this.mOEMINFOData.mScreenOnTime = this.mDataBuffer.getLong();
            this.mOEMINFOData.mChargeTime = this.mDataBuffer.getLong();
            this.mOEMINFOData.mTalkTime = this.mDataBuffer.getLong();
            this.mOEMINFOData.mFristUseTime = this.mDataBuffer.getLong();
            if (HW_DEBUG) {
                Slog.d(TAG, "getOEMINFOData mOpenFlag " + this.mOEMINFOData.mOpenFlag + " OEMINFOData.mScreenOnTime " + this.mOEMINFOData.mScreenOnTime + " mChargeTime " + this.mOEMINFOData.mChargeTime + " mTalkTime " + this.mOEMINFOData.mTalkTime + " mFristUseTime " + this.mOEMINFOData.mFristUseTime);
            }
        }
    }
}
