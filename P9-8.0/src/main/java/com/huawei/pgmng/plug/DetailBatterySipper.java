package com.huawei.pgmng.plug;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import android.util.LogException;

public class DetailBatterySipper implements Comparable<DetailBatterySipper>, Parcelable {
    public static final int BG = 0;
    public static final int BG_RX = 1;
    public static final int BG_TX = 0;
    public static final Creator<DetailBatterySipper> CREATOR = new Creator<DetailBatterySipper>() {
        public DetailBatterySipper createFromParcel(Parcel in) {
            return new DetailBatterySipper(in);
        }

        public DetailBatterySipper[] newArray(int size) {
            return new DetailBatterySipper[size];
        }
    };
    public static final int FG = 1;
    public static final int FG_RX = 3;
    public static final int FG_TX = 2;
    public static final int NET_ENTRY_RX_BYTES = 0;
    public static final int NET_ENTRY_RX_PACKETS = 2;
    public static final int NET_ENTRY_SIZE = 4;
    public static final int NET_ENTRY_TX_BYTES = 1;
    public static final int NET_ENTRY_TX_PACKETS = 3;
    public static final int NET_TIME_SIZE = 4;
    private static final String TAG = "DetailBatterySipper";
    public float mBgCpuPower;
    public long mBgCpuTime;
    public float mBgGpsPower;
    public long mBgGpsTime;
    public float mBgMobileRxPower;
    public long mBgMobileRxTime;
    public float mBgMobileTxPower;
    public long mBgMobileTxTime;
    public float mBgSensorPower;
    public long mBgSensorsTime;
    public float mBgTotalPower;
    public float mBgWifiRxPower;
    public float mBgWifiScanPower;
    public long mBgWifiScanTime;
    public float mBgWifiTxPower;
    public float mBgWlPower;
    public long mBgWlTime;
    public float mDistributedPower;
    public float mFgCpuPower;
    public long mFgCpuTime;
    public float mFgGpsPower;
    public long mFgGpsTime;
    public float mFgGpuPower;
    public long mFgGpuTime;
    public float mFgMobileRxPower;
    public long mFgMobileRxTime;
    public float mFgMobileTxPower;
    public long mFgMobileTxTime;
    public float mFgScreenPower;
    public long mFgScreenTime;
    public float mFgSensorPower;
    public long mFgSensorsTime;
    public float mFgTotalPower;
    public float mFgWifiRxPower;
    public float mFgWifiScanPower;
    public long mFgWifiScanTime;
    public float mFgWifiTxPower;
    public boolean mIsPkg;
    public NetEntry[] mMobileEntry;
    public String mName;
    public int mUid;
    public NetEntry[] mWifiEntry;

    public static class NetEntry {
        public long[] mItem = new long[4];

        public void add(NetEntry delta) {
            for (int i = 0; i < 4; i++) {
                long[] jArr = this.mItem;
                jArr[i] = jArr[i] + delta.mItem[i];
            }
        }

        public void set(long rxBytes, long txBytes, long rxPackets, long txPackets) {
            this.mItem[0] = rxBytes;
            this.mItem[1] = txBytes;
            this.mItem[2] = rxPackets;
            this.mItem[3] = txPackets;
        }

        public void set(int idx, long val) {
            if (4 > idx && idx >= 0) {
                this.mItem[idx] = val;
            }
        }

        public void set(NetEntry newEntry) {
            for (int i = 0; i < 4; i++) {
                this.mItem[i] = newEntry.mItem[i];
            }
        }

        public NetEntry minus(NetEntry other) {
            NetEntry entry = new NetEntry();
            for (int i = 0; i < 4; i++) {
                entry.mItem[i] = this.mItem[i] - other.mItem[i];
                if (entry.mItem[i] < 0) {
                    Log.d(DetailBatterySipper.TAG, "minus negative, entry.mItem[i]: " + entry.mItem[i] + ", i: " + entry.mItem[i]);
                }
            }
            return entry;
        }

        public long getTotalPackets() {
            return this.mItem[2] + this.mItem[3];
        }

        public long getTxPackets() {
            return this.mItem[3];
        }

        public long getRxPackets() {
            return this.mItem[2];
        }

        public long get(int idx) {
            if (4 <= idx || idx < 0) {
                return 0;
            }
            return this.mItem[idx];
        }

        public void reset() {
            for (int i = 0; i < 4; i++) {
                this.mItem[i] = 0;
            }
        }

        private void processBytesUnit(StringBuilder sb, long bytes) {
            if (bytes > 1048576) {
                sb.append(String.format("%.2fMB", new Object[]{Double.valueOf(((((double) bytes) * 1.0d) / 1024.0d) / 1024.0d)}));
            } else if (bytes > 1024) {
                sb.append(String.format("%.2fKB", new Object[]{Double.valueOf((((double) bytes) * 1.0d) / 1024.0d)}));
            } else {
                sb.append(bytes);
                sb.append("B");
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            processBytesUnit(sb, this.mItem[0]);
            sb.append(" ");
            processBytesUnit(sb, this.mItem[1]);
            sb.append(" ");
            sb.append(this.mItem[2]);
            sb.append(" ");
            sb.append(this.mItem[3]);
            return sb.toString();
        }

        public void writeToParcel(Parcel out) {
            for (int i = 0; i < 4; i++) {
                out.writeLong(this.mItem[i]);
            }
        }

        public void readFromParcel(Parcel in) {
            for (int i = 0; i < 4; i++) {
                this.mItem[i] = in.readLong();
            }
        }
    }

    public DetailBatterySipper(String name, int uid, boolean isPkg) {
        this.mIsPkg = false;
        this.mFgScreenTime = 0;
        this.mBgGpsTime = 0;
        this.mFgGpsTime = 0;
        this.mBgSensorsTime = 0;
        this.mFgSensorsTime = 0;
        this.mBgWlTime = 0;
        this.mBgCpuTime = 0;
        this.mFgCpuTime = 0;
        this.mFgGpuTime = 0;
        this.mBgMobileTxTime = 0;
        this.mBgMobileRxTime = 0;
        this.mFgMobileTxTime = 0;
        this.mFgMobileRxTime = 0;
        this.mMobileEntry = new NetEntry[2];
        this.mWifiEntry = new NetEntry[2];
        this.mBgWifiScanTime = 0;
        this.mFgWifiScanTime = 0;
        this.mBgTotalPower = 0.0f;
        this.mFgTotalPower = 0.0f;
        this.mDistributedPower = 0.0f;
        this.mFgScreenPower = 0.0f;
        this.mBgGpsPower = 0.0f;
        this.mFgGpsPower = 0.0f;
        this.mBgSensorPower = 0.0f;
        this.mFgSensorPower = 0.0f;
        this.mBgWlPower = 0.0f;
        this.mBgCpuPower = 0.0f;
        this.mFgCpuPower = 0.0f;
        this.mFgGpuPower = 0.0f;
        this.mBgMobileTxPower = 0.0f;
        this.mBgMobileRxPower = 0.0f;
        this.mFgMobileTxPower = 0.0f;
        this.mFgMobileRxPower = 0.0f;
        this.mBgWifiTxPower = 0.0f;
        this.mBgWifiRxPower = 0.0f;
        this.mFgWifiTxPower = 0.0f;
        this.mFgWifiRxPower = 0.0f;
        this.mBgWifiScanPower = 0.0f;
        this.mFgWifiScanPower = 0.0f;
        this.mName = name;
        this.mUid = uid;
        this.mIsPkg = isPkg;
        this.mMobileEntry[0] = new NetEntry();
        this.mMobileEntry[1] = new NetEntry();
        this.mWifiEntry[0] = new NetEntry();
        this.mWifiEntry[1] = new NetEntry();
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setIsPkg(boolean isPkg) {
        this.mIsPkg = isPkg;
    }

    public void setDistributePower(float power) {
        this.mDistributedPower = power;
    }

    public boolean getIsPkg() {
        return this.mIsPkg;
    }

    public DetailBatterySipper(Parcel in) {
        this.mIsPkg = false;
        this.mFgScreenTime = 0;
        this.mBgGpsTime = 0;
        this.mFgGpsTime = 0;
        this.mBgSensorsTime = 0;
        this.mFgSensorsTime = 0;
        this.mBgWlTime = 0;
        this.mBgCpuTime = 0;
        this.mFgCpuTime = 0;
        this.mFgGpuTime = 0;
        this.mBgMobileTxTime = 0;
        this.mBgMobileRxTime = 0;
        this.mFgMobileTxTime = 0;
        this.mFgMobileRxTime = 0;
        this.mMobileEntry = new NetEntry[2];
        this.mWifiEntry = new NetEntry[2];
        this.mBgWifiScanTime = 0;
        this.mFgWifiScanTime = 0;
        this.mBgTotalPower = 0.0f;
        this.mFgTotalPower = 0.0f;
        this.mDistributedPower = 0.0f;
        this.mFgScreenPower = 0.0f;
        this.mBgGpsPower = 0.0f;
        this.mFgGpsPower = 0.0f;
        this.mBgSensorPower = 0.0f;
        this.mFgSensorPower = 0.0f;
        this.mBgWlPower = 0.0f;
        this.mBgCpuPower = 0.0f;
        this.mFgCpuPower = 0.0f;
        this.mFgGpuPower = 0.0f;
        this.mBgMobileTxPower = 0.0f;
        this.mBgMobileRxPower = 0.0f;
        this.mFgMobileTxPower = 0.0f;
        this.mFgMobileRxPower = 0.0f;
        this.mBgWifiTxPower = 0.0f;
        this.mBgWifiRxPower = 0.0f;
        this.mFgWifiTxPower = 0.0f;
        this.mFgWifiRxPower = 0.0f;
        this.mBgWifiScanPower = 0.0f;
        this.mFgWifiScanPower = 0.0f;
        this.mName = in.readString();
        this.mUid = in.readInt();
        this.mIsPkg = in.readInt() == 1;
        this.mMobileEntry[0] = new NetEntry();
        this.mMobileEntry[1] = new NetEntry();
        this.mWifiEntry[0] = new NetEntry();
        this.mWifiEntry[1] = new NetEntry();
        this.mFgScreenTime = in.readLong();
        this.mBgGpsTime = in.readLong();
        this.mFgGpsTime = in.readLong();
        this.mBgSensorsTime = in.readLong();
        this.mFgSensorsTime = in.readLong();
        this.mBgWlTime = in.readLong();
        this.mBgCpuTime = in.readLong();
        this.mFgCpuTime = in.readLong();
        this.mFgGpuTime = in.readLong();
        this.mBgMobileTxTime = in.readLong();
        this.mBgMobileRxTime = in.readLong();
        this.mFgMobileTxTime = in.readLong();
        this.mFgMobileRxTime = in.readLong();
        this.mMobileEntry[0].readFromParcel(in);
        this.mMobileEntry[1].readFromParcel(in);
        this.mWifiEntry[0].readFromParcel(in);
        this.mWifiEntry[1].readFromParcel(in);
        this.mBgWifiScanTime = in.readLong();
        this.mFgWifiScanTime = in.readLong();
        this.mBgTotalPower = in.readFloat();
        this.mFgTotalPower = in.readFloat();
        this.mDistributedPower = in.readFloat();
        this.mFgScreenPower = in.readFloat();
        this.mBgGpsPower = in.readFloat();
        this.mFgGpsPower = in.readFloat();
        this.mBgSensorPower = in.readFloat();
        this.mFgSensorPower = in.readFloat();
        this.mBgWlPower = in.readFloat();
        this.mBgCpuPower = in.readFloat();
        this.mFgCpuPower = in.readFloat();
        this.mFgGpuPower = in.readFloat();
        this.mBgMobileTxPower = in.readFloat();
        this.mBgMobileRxPower = in.readFloat();
        this.mFgMobileTxPower = in.readFloat();
        this.mFgMobileRxPower = in.readFloat();
        this.mBgWifiTxPower = in.readFloat();
        this.mBgWifiRxPower = in.readFloat();
        this.mFgWifiTxPower = in.readFloat();
        this.mFgWifiRxPower = in.readFloat();
        this.mBgWifiScanPower = in.readFloat();
        this.mFgWifiScanPower = in.readFloat();
    }

    public void addScreenTime(long time) {
        this.mFgScreenTime += time;
    }

    public void addScreenPower(float power) {
        this.mFgScreenPower += power;
    }

    public void addGpsTime(long[] time) {
        this.mBgGpsTime += time[0];
        this.mFgGpsTime += time[1];
    }

    public void addGpsPower(float[] power) {
        this.mBgGpsPower += power[0];
        this.mFgGpsPower += power[1];
    }

    public void addSensorTime(long[] time) {
        this.mBgSensorsTime += time[0];
        this.mFgSensorsTime += time[1];
    }

    public void addSensorPower(float[] power) {
        this.mBgSensorPower += power[0];
        this.mFgSensorPower += power[1];
    }

    public void addWlTime(long time) {
        this.mBgWlTime += time;
    }

    public void addWlPower(float power) {
        this.mBgWlPower += power;
    }

    public void addCpuTime(long[] time) {
        this.mBgCpuTime += time[0];
        this.mFgCpuTime += time[1];
    }

    public void addCpuPower(float[] power) {
        this.mBgCpuPower += power[0];
        this.mFgCpuPower += power[1];
    }

    public void addGpuTime(long time) {
        this.mFgGpuTime += time;
    }

    public void addGpuPower(float power) {
        this.mFgGpuPower += power;
    }

    public void addMobileTime(long[] time) {
        this.mBgMobileTxTime += time[0];
        this.mBgMobileRxTime += time[1];
        this.mFgMobileTxTime += time[2];
        this.mFgMobileRxTime += time[3];
    }

    public void addMobileByteAndPacket(NetEntry[] entry) {
        this.mMobileEntry[0].add(entry[0]);
        this.mMobileEntry[1].add(entry[1]);
    }

    public void addMobilePower(float[] power) {
        this.mBgMobileTxPower += power[0];
        this.mBgMobileRxPower += power[1];
        this.mFgMobileTxPower += power[2];
        this.mFgMobileRxPower += power[3];
    }

    public void addWifiByteAndPacket(NetEntry[] entry) {
        this.mWifiEntry[0].add(entry[0]);
        this.mWifiEntry[1].add(entry[1]);
    }

    public void addWifiScanTime(long[] time) {
        this.mBgWifiScanTime += time[0];
        this.mFgWifiScanTime += time[1];
    }

    public void addWifiActivePower(float[] power) {
        this.mBgWifiTxPower += power[0];
        this.mBgWifiRxPower += power[1];
        this.mFgWifiTxPower += power[2];
        this.mFgWifiRxPower += power[3];
    }

    public void addWifiScanPower(float[] power) {
        this.mBgWifiScanPower += power[0];
        this.mFgWifiScanPower += power[1];
    }

    public void add(DetailBatterySipper other) {
        this.mFgScreenTime += other.mFgScreenTime;
        this.mBgGpsTime += other.mBgGpsTime;
        this.mFgGpsTime += other.mFgGpsTime;
        this.mBgSensorsTime += other.mBgSensorsTime;
        this.mFgSensorsTime += other.mFgSensorsTime;
        this.mBgWlTime += other.mBgWlTime;
        this.mBgCpuTime += other.mBgCpuTime;
        this.mFgCpuTime += other.mFgCpuTime;
        this.mFgGpuTime += other.mFgGpuTime;
        this.mBgMobileTxTime += other.mBgMobileTxTime;
        this.mBgMobileRxTime += other.mBgMobileRxTime;
        this.mFgMobileTxTime += other.mFgMobileTxTime;
        this.mFgMobileRxTime += other.mFgMobileRxTime;
        this.mMobileEntry[0].add(other.mMobileEntry[0]);
        this.mMobileEntry[1].add(other.mMobileEntry[1]);
        this.mWifiEntry[0].add(other.mWifiEntry[0]);
        this.mWifiEntry[1].add(other.mWifiEntry[1]);
        this.mBgWifiScanTime += other.mBgWifiScanTime;
        this.mFgWifiScanTime += other.mFgWifiScanTime;
        this.mBgTotalPower += other.mBgTotalPower;
        this.mFgTotalPower += other.mFgTotalPower;
        this.mDistributedPower += other.mDistributedPower;
        this.mFgScreenPower += other.mFgScreenPower;
        this.mBgGpsPower += other.mBgGpsPower;
        this.mFgGpsPower += other.mFgGpsPower;
        this.mBgSensorPower += other.mBgSensorPower;
        this.mFgSensorPower += other.mFgSensorPower;
        this.mBgWlPower += other.mBgWlPower;
        this.mBgCpuPower += other.mBgCpuPower;
        this.mFgCpuPower += other.mFgCpuPower;
        this.mFgGpuPower += other.mFgGpuPower;
        this.mBgMobileTxPower += other.mBgMobileTxPower;
        this.mBgMobileRxPower += other.mBgMobileRxPower;
        this.mFgMobileTxPower += other.mFgMobileTxPower;
        this.mFgMobileRxPower += other.mFgMobileRxPower;
        this.mBgWifiTxPower += other.mBgWifiTxPower;
        this.mBgWifiRxPower += other.mBgWifiRxPower;
        this.mFgWifiTxPower += other.mFgWifiTxPower;
        this.mFgWifiRxPower += other.mFgWifiRxPower;
        this.mBgWifiScanPower += other.mBgWifiScanPower;
        this.mFgWifiScanPower += other.mFgWifiScanPower;
    }

    public float getWifiPower() {
        return ((((this.mBgWifiTxPower + this.mBgWifiRxPower) + this.mFgWifiTxPower) + this.mFgWifiRxPower) + this.mBgWifiScanPower) + this.mFgWifiScanPower;
    }

    public long getTotalCpuTime() {
        return this.mBgCpuTime + this.mFgCpuTime;
    }

    public float getFgTotalPower() {
        return this.mFgTotalPower;
    }

    public float getBgTotalPower() {
        return this.mBgTotalPower;
    }

    public float getDistributedPower() {
        return this.mDistributedPower;
    }

    public float getTotalPower() {
        return (this.mBgTotalPower + this.mFgTotalPower) + this.mDistributedPower;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeInt(this.mUid);
        if (this.mIsPkg) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeLong(this.mFgScreenTime);
        dest.writeLong(this.mBgGpsTime);
        dest.writeLong(this.mFgGpsTime);
        dest.writeLong(this.mBgSensorsTime);
        dest.writeLong(this.mFgSensorsTime);
        dest.writeLong(this.mBgWlTime);
        dest.writeLong(this.mBgCpuTime);
        dest.writeLong(this.mFgCpuTime);
        dest.writeLong(this.mFgGpuTime);
        dest.writeLong(this.mBgMobileTxTime);
        dest.writeLong(this.mBgMobileRxTime);
        dest.writeLong(this.mFgMobileTxTime);
        dest.writeLong(this.mFgMobileRxTime);
        this.mMobileEntry[0].writeToParcel(dest);
        this.mMobileEntry[1].writeToParcel(dest);
        this.mWifiEntry[0].writeToParcel(dest);
        this.mWifiEntry[1].writeToParcel(dest);
        dest.writeLong(this.mBgWifiScanTime);
        dest.writeLong(this.mFgWifiScanTime);
        dest.writeFloat(this.mBgTotalPower);
        dest.writeFloat(this.mFgTotalPower);
        dest.writeFloat(this.mDistributedPower);
        dest.writeFloat(this.mFgScreenPower);
        dest.writeFloat(this.mBgGpsPower);
        dest.writeFloat(this.mFgGpsPower);
        dest.writeFloat(this.mBgSensorPower);
        dest.writeFloat(this.mFgSensorPower);
        dest.writeFloat(this.mBgWlPower);
        dest.writeFloat(this.mBgCpuPower);
        dest.writeFloat(this.mFgCpuPower);
        dest.writeFloat(this.mFgGpuPower);
        dest.writeFloat(this.mBgMobileTxPower);
        dest.writeFloat(this.mBgMobileRxPower);
        dest.writeFloat(this.mFgMobileTxPower);
        dest.writeFloat(this.mFgMobileRxPower);
        dest.writeFloat(this.mBgWifiTxPower);
        dest.writeFloat(this.mBgWifiRxPower);
        dest.writeFloat(this.mFgWifiTxPower);
        dest.writeFloat(this.mFgWifiRxPower);
        dest.writeFloat(this.mBgWifiScanPower);
        dest.writeFloat(this.mFgWifiScanPower);
    }

    public int compareTo(DetailBatterySipper other) {
        return Float.compare(other.getTotalPower(), getTotalPower());
    }

    public void sumPower() {
        this.mBgTotalPower = (((((((this.mBgSensorPower + this.mBgGpsPower) + this.mBgWlPower) + this.mBgCpuPower) + this.mBgMobileTxPower) + this.mBgMobileRxPower) + this.mBgWifiTxPower) + this.mBgWifiRxPower) + this.mBgWifiScanPower;
        this.mFgTotalPower = ((((((((this.mFgScreenPower + this.mFgSensorPower) + this.mFgGpsPower) + this.mFgCpuPower) + this.mFgGpuPower) + this.mFgMobileTxPower) + this.mFgMobileRxPower) + this.mFgWifiTxPower) + this.mFgWifiRxPower) + this.mFgWifiScanPower;
        float totalPower = (this.mBgTotalPower + this.mFgTotalPower) + this.mDistributedPower;
    }

    public int describeContents() {
        return 0;
    }

    private void appendDetailInfo(StringBuilder sb) {
        if ((this.mBgCpuTime + this.mFgCpuTime) / 1000 > 0) {
            sb.append("    |---Cpu:");
            sb.append(String.format("\tpower: (total %.1f", new Object[]{Float.valueOf(this.mBgCpuPower + this.mFgCpuPower)}));
            sb.append(String.format(", bg %.1f", new Object[]{Float.valueOf(this.mBgCpuPower)}));
            sb.append(String.format(", fg %.1f)", new Object[]{Float.valueOf(this.mFgCpuPower)}));
            sb.append("\ttime: (total ");
            sb.append((this.mBgCpuTime + this.mFgCpuTime) / 1000);
            sb.append(", bg ");
            sb.append(this.mBgCpuTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgCpuTime / 1000);
            sb.append(")\n");
        }
        if (this.mFgScreenPower > 0.0f) {
            sb.append("    |---Screen:");
            sb.append(String.format("\t%.1f\t", new Object[]{Float.valueOf(this.mFgScreenPower)}));
            sb.append(this.mFgScreenTime / 1000);
            sb.append(10);
        }
        if (this.mBgGpsPower + this.mFgGpsPower > 0.0f) {
            sb.append("    |---Gps:");
            sb.append(String.format("\tpower: (total %.2f", new Object[]{Float.valueOf(this.mBgGpsPower + this.mFgGpsPower)}));
            sb.append(String.format(", bg %.2f", new Object[]{Float.valueOf(this.mBgGpsPower)}));
            sb.append(String.format(", fg %.2f)", new Object[]{Float.valueOf(this.mFgGpsPower)}));
            sb.append("\ttime: (total ");
            sb.append((this.mBgGpsTime + this.mFgGpsTime) / 1000);
            sb.append(", bg ");
            sb.append(this.mBgGpsTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgGpsTime / 1000);
            sb.append(")\n");
        }
        if (this.mBgSensorPower + this.mFgSensorPower > 0.0f) {
            sb.append("    |---Sensor:");
            sb.append(String.format("\tpower: (total %.3f", new Object[]{Float.valueOf(this.mBgSensorPower + this.mFgSensorPower)}));
            sb.append(String.format(", bg %.3f", new Object[]{Float.valueOf(this.mBgSensorPower)}));
            sb.append(String.format(", fg %.3f)", new Object[]{Float.valueOf(this.mFgSensorPower)}));
            sb.append("\ttime: (total ");
            sb.append((this.mBgSensorsTime + this.mFgSensorsTime) / 1000);
            sb.append(", bg ");
            sb.append(this.mBgSensorsTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgSensorsTime / 1000);
            sb.append(")\n");
        }
        if (this.mBgWlTime / 1000 > 0) {
            sb.append("    |---WakeLock:");
            sb.append(String.format("\t%.1f", new Object[]{Float.valueOf(this.mBgWlPower)}));
            sb.append(9);
            sb.append(this.mBgWlTime / 1000);
            sb.append(10);
        }
        if (this.mFgGpuTime / 1000 > 0) {
            sb.append("    |---Gpu:");
            sb.append(String.format("\t%.1f", new Object[]{Float.valueOf(this.mFgGpuPower)}));
            sb.append(9);
            sb.append(this.mFgGpuTime / 1000);
            sb.append(10);
        }
        long mobileTime = ((this.mBgMobileTxTime + this.mBgMobileRxTime) + this.mFgMobileTxTime) + this.mFgMobileRxTime;
        if (mobileTime / 1000 > 0) {
            sb.append("    |---Mobile:");
            sb.append(String.format("\tpower: (total %.1f", new Object[]{Float.valueOf(((this.mBgMobileTxPower + this.mBgMobileRxPower) + this.mFgMobileTxPower) + this.mFgMobileRxPower)}));
            sb.append(String.format(", bgTx %.1f", new Object[]{Float.valueOf(this.mBgMobileTxPower)}));
            sb.append(String.format(", bgRx %.1f", new Object[]{Float.valueOf(this.mBgMobileRxPower)}));
            sb.append(String.format(", fgTx %.1f", new Object[]{Float.valueOf(this.mFgMobileTxPower)}));
            sb.append(String.format(", fgRx %.1f)", new Object[]{Float.valueOf(this.mFgMobileRxPower)}));
            sb.append("\ttime: (total ");
            sb.append(mobileTime / 1000);
            sb.append(", bgTx ");
            sb.append(this.mBgMobileTxTime / 1000);
            sb.append(", bgRx ");
            sb.append(this.mBgMobileRxTime / 1000);
            sb.append(", fgTx ");
            sb.append(this.mFgMobileTxTime / 1000);
            sb.append(", fgRx");
            sb.append(this.mFgMobileRxTime / 1000);
            sb.append(")\tmobile bg: (");
            sb.append(this.mMobileEntry[0]);
            sb.append("\tmobile fg: ");
            sb.append(this.mMobileEntry[1]);
            sb.append(")\n");
        }
        if (getWifiPower() > 0.0f) {
            sb.append("    |---Wifi:");
            sb.append(String.format("\tactivePower: (total %.1f", new Object[]{Float.valueOf(wifiPower)}));
            sb.append(String.format(", bgTx %.1f", new Object[]{Float.valueOf(this.mBgWifiTxPower)}));
            sb.append(String.format(", bgRx %.1f", new Object[]{Float.valueOf(this.mBgWifiRxPower)}));
            sb.append(String.format(", fgTx %.1f", new Object[]{Float.valueOf(this.mFgWifiTxPower)}));
            sb.append(String.format(", fgRx %.1f)", new Object[]{Float.valueOf(this.mFgWifiRxPower)}));
            sb.append("\twifi bg: (");
            sb.append(this.mWifiEntry[0]);
            sb.append("\twifi fg: ");
            sb.append(this.mWifiEntry[1]);
            sb.append(String.format(")\tScanPower: (total %.1f", new Object[]{Float.valueOf(this.mBgWifiScanPower + this.mFgWifiScanPower)}));
            sb.append(String.format(", bg %.1f", new Object[]{Float.valueOf(this.mBgWifiScanPower)}));
            sb.append(String.format(", fg %.1f)", new Object[]{Float.valueOf(this.mFgWifiScanPower)}));
            sb.append("\tscanTime: (total ");
            sb.append((this.mBgWifiScanTime + this.mFgWifiScanTime) / 1000);
            sb.append(", bg ");
            sb.append(this.mBgWifiScanTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgWifiScanTime / 1000);
            sb.append(')');
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getTotalPower() <= 0.0f) {
            return LogException.NO_VALUE;
        }
        sb.append(this.mName);
        sb.append(9);
        sb.append(this.mUid);
        sb.append(String.format("\ttotal: %.1f", new Object[]{Float.valueOf(allPower)}));
        sb.append(String.format("\tbg: %.1f", new Object[]{Float.valueOf(this.mBgTotalPower)}));
        sb.append(String.format("\tfg: %.1f", new Object[]{Float.valueOf(this.mFgTotalPower)}));
        sb.append(String.format("\tdis: %.1f\n", new Object[]{Float.valueOf(this.mDistributedPower)}));
        appendDetailInfo(sb);
        return sb.toString();
    }

    public String toString(float totalPower, boolean verbose) {
        float f = 0.0f;
        StringBuilder sb = new StringBuilder();
        float allPower = getTotalPower();
        if (allPower <= 0.0f) {
            return LogException.NO_VALUE;
        }
        sb.append(this.mName);
        sb.append(9);
        sb.append(this.mUid);
        sb.append(String.format("\ttotal: %.1f", new Object[]{Float.valueOf(allPower)}));
        sb.append(String.format("\tbg: %.1f", new Object[]{Float.valueOf(this.mBgTotalPower)}));
        sb.append(String.format("\tfg: %.1f", new Object[]{Float.valueOf(this.mFgTotalPower)}));
        sb.append(String.format("\tdis: %.1f", new Object[]{Float.valueOf(this.mDistributedPower)}));
        String str = "\tratio: %.1f%%\n";
        Object[] objArr = new Object[1];
        if (totalPower > 0.0f) {
            f = (allPower / totalPower) * 100.0f;
        }
        objArr[0] = Float.valueOf(f);
        sb.append(String.format(str, objArr));
        if (!verbose) {
            return sb.toString();
        }
        appendDetailInfo(sb);
        return sb.toString();
    }
}
