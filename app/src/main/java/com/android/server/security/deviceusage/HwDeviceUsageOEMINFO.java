package com.android.server.security.deviceusage;

import android.util.Slog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HwDeviceUsageOEMINFO {
    private static final boolean HW_DEBUG = false;
    private static final int OEMINFO_ENABLE_RETREAD = 163;
    private static final int OEMINFO_ENABLE_RETREAD_SIZE = 40;
    private static final String TAG = "HwDeviceUsageOEMINFO";
    private static HwDeviceUsageOEMINFO mInstance;
    private ByteBuffer mDataBuffer;
    private OEMINFOData mOEMINFOData;

    static class OEMINFOData {
        public long mChargeTime;
        public long mFristUseTime;
        public int mOpenFlag;
        public long mScreenOnTime;
        public long mTalkTime;

        OEMINFOData() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.deviceusage.HwDeviceUsageOEMINFO.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.deviceusage.HwDeviceUsageOEMINFO.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.deviceusage.HwDeviceUsageOEMINFO.<clinit>():void");
    }

    private HwDeviceUsageOEMINFO() {
        this.mDataBuffer = ByteBuffer.allocate(OEMINFO_ENABLE_RETREAD_SIZE);
        this.mOEMINFOData = new OEMINFOData();
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
        byte[] oEMData = HwOEMInfoAdapter.getByteArrayFromOeminfo(OEMINFO_ENABLE_RETREAD, OEMINFO_ENABLE_RETREAD_SIZE);
        if (oEMData == null || oEMData.length < OEMINFO_ENABLE_RETREAD_SIZE) {
            Slog.e(TAG, "get OEMINFO error");
        } else {
            setByteArrayToDataBuffer(oEMData);
        }
    }

    public boolean getOpenFlag() {
        getOEMINFOData();
        boolean flag = this.mOEMINFOData.mOpenFlag != 0 ? true : HW_DEBUG;
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

    protected void setOpenFlag(int flag) {
        this.mOEMINFOData.mOpenFlag = flag;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, OEMINFO_ENABLE_RETREAD_SIZE, this.mDataBuffer.array());
    }

    protected void setScreenOnTime(long time) {
        this.mOEMINFOData.mScreenOnTime = time;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, OEMINFO_ENABLE_RETREAD_SIZE, this.mDataBuffer.array());
    }

    protected void setChargeTime(long time) {
        this.mOEMINFOData.mChargeTime = time;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, OEMINFO_ENABLE_RETREAD_SIZE, this.mDataBuffer.array());
    }

    protected void setTalkTime(long time) {
        this.mOEMINFOData.mTalkTime = time;
        setDataBuffer();
        HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, OEMINFO_ENABLE_RETREAD_SIZE, this.mDataBuffer.array());
    }

    protected int setFristUseTime(long time) {
        this.mOEMINFOData.mFristUseTime = time;
        setDataBuffer();
        return HwOEMInfoAdapter.writeByteArrayToOeminfo(OEMINFO_ENABLE_RETREAD, OEMINFO_ENABLE_RETREAD_SIZE, this.mDataBuffer.array());
    }

    private void setDataBuffer() {
        this.mDataBuffer.clear();
        if (this.mDataBuffer.remaining() >= OEMINFO_ENABLE_RETREAD_SIZE) {
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
        if (this.mDataBuffer.remaining() >= OEMINFO_ENABLE_RETREAD_SIZE) {
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
