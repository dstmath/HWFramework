package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LinkQualityInfo implements Parcelable {
    public static final Creator<LinkQualityInfo> CREATOR = null;
    public static final int NORMALIZED_MAX_SIGNAL_STRENGTH = 99;
    public static final int NORMALIZED_MIN_SIGNAL_STRENGTH = 0;
    public static final int NORMALIZED_SIGNAL_STRENGTH_RANGE = 100;
    protected static final int OBJECT_TYPE_LINK_QUALITY_INFO = 1;
    protected static final int OBJECT_TYPE_MOBILE_LINK_QUALITY_INFO = 3;
    protected static final int OBJECT_TYPE_WIFI_LINK_QUALITY_INFO = 2;
    public static final int UNKNOWN_INT = Integer.MAX_VALUE;
    public static final long UNKNOWN_LONG = Long.MAX_VALUE;
    private int mDataSampleDuration;
    private long mLastDataSampleTime;
    private int mNetworkType;
    private int mNormalizedSignalStrength;
    private long mPacketCount;
    private long mPacketErrorCount;
    private int mTheoreticalLatency;
    private int mTheoreticalRxBandwidth;
    private int mTheoreticalTxBandwidth;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.LinkQualityInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.LinkQualityInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.LinkQualityInfo.<clinit>():void");
    }

    public LinkQualityInfo() {
        this.mNetworkType = -1;
        this.mNormalizedSignalStrength = UNKNOWN_INT;
        this.mPacketCount = UNKNOWN_LONG;
        this.mPacketErrorCount = UNKNOWN_LONG;
        this.mTheoreticalTxBandwidth = UNKNOWN_INT;
        this.mTheoreticalRxBandwidth = UNKNOWN_INT;
        this.mTheoreticalLatency = UNKNOWN_INT;
        this.mLastDataSampleTime = UNKNOWN_LONG;
        this.mDataSampleDuration = UNKNOWN_INT;
    }

    public int describeContents() {
        return NORMALIZED_MIN_SIGNAL_STRENGTH;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcel(dest, flags, OBJECT_TYPE_LINK_QUALITY_INFO);
    }

    public void writeToParcel(Parcel dest, int flags, int objectType) {
        dest.writeInt(objectType);
        dest.writeInt(this.mNetworkType);
        dest.writeInt(this.mNormalizedSignalStrength);
        dest.writeLong(this.mPacketCount);
        dest.writeLong(this.mPacketErrorCount);
        dest.writeInt(this.mTheoreticalTxBandwidth);
        dest.writeInt(this.mTheoreticalRxBandwidth);
        dest.writeInt(this.mTheoreticalLatency);
        dest.writeLong(this.mLastDataSampleTime);
        dest.writeInt(this.mDataSampleDuration);
    }

    protected void initializeFromParcel(Parcel in) {
        this.mNetworkType = in.readInt();
        this.mNormalizedSignalStrength = in.readInt();
        this.mPacketCount = in.readLong();
        this.mPacketErrorCount = in.readLong();
        this.mTheoreticalTxBandwidth = in.readInt();
        this.mTheoreticalRxBandwidth = in.readInt();
        this.mTheoreticalLatency = in.readInt();
        this.mLastDataSampleTime = in.readLong();
        this.mDataSampleDuration = in.readInt();
    }

    public int getNetworkType() {
        return this.mNetworkType;
    }

    public void setNetworkType(int networkType) {
        this.mNetworkType = networkType;
    }

    public int getNormalizedSignalStrength() {
        return this.mNormalizedSignalStrength;
    }

    public void setNormalizedSignalStrength(int normalizedSignalStrength) {
        this.mNormalizedSignalStrength = normalizedSignalStrength;
    }

    public long getPacketCount() {
        return this.mPacketCount;
    }

    public void setPacketCount(long packetCount) {
        this.mPacketCount = packetCount;
    }

    public long getPacketErrorCount() {
        return this.mPacketErrorCount;
    }

    public void setPacketErrorCount(long packetErrorCount) {
        this.mPacketErrorCount = packetErrorCount;
    }

    public int getTheoreticalTxBandwidth() {
        return this.mTheoreticalTxBandwidth;
    }

    public void setTheoreticalTxBandwidth(int theoreticalTxBandwidth) {
        this.mTheoreticalTxBandwidth = theoreticalTxBandwidth;
    }

    public int getTheoreticalRxBandwidth() {
        return this.mTheoreticalRxBandwidth;
    }

    public void setTheoreticalRxBandwidth(int theoreticalRxBandwidth) {
        this.mTheoreticalRxBandwidth = theoreticalRxBandwidth;
    }

    public int getTheoreticalLatency() {
        return this.mTheoreticalLatency;
    }

    public void setTheoreticalLatency(int theoreticalLatency) {
        this.mTheoreticalLatency = theoreticalLatency;
    }

    public long getLastDataSampleTime() {
        return this.mLastDataSampleTime;
    }

    public void setLastDataSampleTime(long lastDataSampleTime) {
        this.mLastDataSampleTime = lastDataSampleTime;
    }

    public int getDataSampleDuration() {
        return this.mDataSampleDuration;
    }

    public void setDataSampleDuration(int dataSampleDuration) {
        this.mDataSampleDuration = dataSampleDuration;
    }
}
