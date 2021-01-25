package ohos.batterysipper;

import java.util.Locale;
import ohos.annotation.SystemApi;

public class DetailBatteryStats implements Comparable<DetailBatteryStats> {
    public static final int BG = 0;
    public static final int BG_FG_SIZE = 2;
    public static final int BG_RX = 1;
    public static final int BG_TX = 0;
    public static final int FG = 1;
    public static final int FG_RX = 3;
    public static final int FG_TX = 2;
    public static final int NET_ENTRY_RX_BYTES = 0;
    public static final int NET_ENTRY_RX_PACKETS = 2;
    public static final int NET_ENTRY_SIZE = 4;
    public static final int NET_ENTRY_TX_BYTES = 1;
    public static final int NET_ENTRY_TX_PACKETS = 3;
    public static final int NET_TIME_SIZE = 4;
    public static final long SECONDS = 1000;
    private static final String TAG = "DetailBatteryStats";
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

    public DetailBatteryStats(String str, int i, boolean z) {
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
        this.mName = str;
        this.mUid = i;
        this.mIsPkg = z;
        this.mMobileEntry[0] = new NetEntry();
        this.mMobileEntry[1] = new NetEntry();
        this.mWifiEntry[0] = new NetEntry();
        this.mWifiEntry[1] = new NetEntry();
    }

    public DetailBatteryStats() {
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
        this.mMobileEntry[0] = new NetEntry();
        this.mMobileEntry[1] = new NetEntry();
        this.mWifiEntry[0] = new NetEntry();
        this.mWifiEntry[1] = new NetEntry();
    }

    @SystemApi
    public void setName(String str) {
        this.mName = str;
    }

    @SystemApi
    public String getName() {
        String str = this.mName;
        return str == null ? "" : str;
    }

    @SystemApi
    public void setIsPkg(boolean z) {
        this.mIsPkg = z;
    }

    @SystemApi
    public boolean getIsPkg() {
        return this.mIsPkg;
    }

    @SystemApi
    public void setDistributePower(float f) {
        this.mDistributedPower = f;
    }

    @SystemApi
    public void addScreenTime(long j) {
        this.mFgScreenTime += j;
    }

    @SystemApi
    public void addScreenPower(float f) {
        this.mFgScreenPower += f;
    }

    @SystemApi
    public void addGpsTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.mBgGpsTime += jArr[0];
            this.mFgGpsTime += jArr[1];
        }
    }

    @SystemApi
    public void addGpsPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.mBgGpsPower += fArr[0];
            this.mFgGpsPower += fArr[1];
        }
    }

    @SystemApi
    public void addSensorTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.mBgSensorsTime += jArr[0];
            this.mFgSensorsTime += jArr[1];
        }
    }

    @SystemApi
    public void addSensorPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.mBgSensorPower += fArr[0];
            this.mFgSensorPower += fArr[1];
        }
    }

    @SystemApi
    public void addWlTime(long j) {
        this.mBgWlTime += j;
    }

    @SystemApi
    public void addWlPower(float f) {
        this.mBgWlPower += f;
    }

    @SystemApi
    public void addCpuTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.mBgCpuTime += jArr[0];
            this.mFgCpuTime += jArr[1];
        }
    }

    @SystemApi
    public void addCpuPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.mBgCpuPower += fArr[0];
            this.mFgCpuPower += fArr[1];
        }
    }

    @SystemApi
    public void addGpuTime(long j) {
        this.mFgGpuTime += j;
    }

    @SystemApi
    public void addGpuPower(float f) {
        this.mFgGpuPower += f;
    }

    @SystemApi
    public void addMobileTime(long[] jArr) {
        if (jArr != null && jArr.length >= 4) {
            this.mBgMobileTxTime += jArr[0];
            this.mBgMobileRxTime += jArr[1];
            this.mFgMobileTxTime += jArr[2];
            this.mFgMobileRxTime += jArr[3];
        }
    }

    @SystemApi
    public void addMobileByteAndPacket(NetEntry[] netEntryArr) {
        if (netEntryArr != null && netEntryArr.length >= 2) {
            this.mMobileEntry[0].add(netEntryArr[0]);
            this.mMobileEntry[1].add(netEntryArr[1]);
        }
    }

    @SystemApi
    public void addMobilePower(float[] fArr) {
        if (fArr != null && fArr.length >= 4) {
            this.mBgMobileTxPower += fArr[0];
            this.mBgMobileRxPower += fArr[1];
            this.mFgMobileTxPower += fArr[2];
            this.mFgMobileRxPower += fArr[3];
        }
    }

    @SystemApi
    public void addWifiByteAndPacket(NetEntry[] netEntryArr) {
        if (netEntryArr != null && netEntryArr.length >= 2) {
            this.mWifiEntry[0].add(netEntryArr[0]);
            this.mWifiEntry[1].add(netEntryArr[1]);
        }
    }

    @SystemApi
    public void addWifiScanTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.mBgWifiScanTime += jArr[0];
            this.mFgWifiScanTime += jArr[1];
        }
    }

    @SystemApi
    public void addWifiActivePower(float[] fArr) {
        if (fArr != null && fArr.length >= 4) {
            this.mBgWifiTxPower += fArr[0];
            this.mBgWifiRxPower += fArr[1];
            this.mFgWifiTxPower += fArr[2];
            this.mFgWifiRxPower += fArr[3];
        }
    }

    @SystemApi
    public void addWifiScanPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.mBgWifiScanPower += fArr[0];
            this.mFgWifiScanPower += fArr[1];
        }
    }

    @SystemApi
    public void add(DetailBatteryStats detailBatteryStats) {
        if (detailBatteryStats != null) {
            NetEntry[] netEntryArr = detailBatteryStats.mMobileEntry;
            if (netEntryArr.length >= 2 && detailBatteryStats.mWifiEntry.length >= 2) {
                this.mFgScreenTime += detailBatteryStats.mFgScreenTime;
                this.mBgGpsTime += detailBatteryStats.mBgGpsTime;
                this.mFgGpsTime += detailBatteryStats.mFgGpsTime;
                this.mBgSensorsTime += detailBatteryStats.mBgSensorsTime;
                this.mFgSensorsTime += detailBatteryStats.mFgSensorsTime;
                this.mBgWlTime += detailBatteryStats.mBgWlTime;
                this.mBgCpuTime += detailBatteryStats.mBgCpuTime;
                this.mFgCpuTime += detailBatteryStats.mFgCpuTime;
                this.mFgGpuTime += detailBatteryStats.mFgGpuTime;
                this.mBgMobileTxTime += detailBatteryStats.mBgMobileTxTime;
                this.mBgMobileRxTime += detailBatteryStats.mBgMobileRxTime;
                this.mFgMobileTxTime += detailBatteryStats.mFgMobileTxTime;
                this.mFgMobileRxTime += detailBatteryStats.mFgMobileRxTime;
                this.mMobileEntry[0].add(netEntryArr[0]);
                this.mMobileEntry[1].add(detailBatteryStats.mMobileEntry[1]);
                this.mWifiEntry[0].add(detailBatteryStats.mWifiEntry[0]);
                this.mWifiEntry[1].add(detailBatteryStats.mWifiEntry[1]);
                this.mBgWifiScanTime += detailBatteryStats.mBgWifiScanTime;
                this.mFgWifiScanTime += detailBatteryStats.mFgWifiScanTime;
                this.mBgTotalPower += detailBatteryStats.mBgTotalPower;
                this.mFgTotalPower += detailBatteryStats.mFgTotalPower;
                this.mDistributedPower += detailBatteryStats.mDistributedPower;
                this.mFgScreenPower += detailBatteryStats.mFgScreenPower;
                this.mBgGpsPower += detailBatteryStats.mBgGpsPower;
                this.mFgGpsPower += detailBatteryStats.mFgGpsPower;
                this.mBgSensorPower += detailBatteryStats.mBgSensorPower;
                this.mFgSensorPower += detailBatteryStats.mFgSensorPower;
                this.mBgWlPower += detailBatteryStats.mBgWlPower;
                this.mBgCpuPower += detailBatteryStats.mBgCpuPower;
                this.mFgCpuPower += detailBatteryStats.mFgCpuPower;
                this.mFgGpuPower += detailBatteryStats.mFgGpuPower;
                this.mBgMobileTxPower += detailBatteryStats.mBgMobileTxPower;
                this.mBgMobileRxPower += detailBatteryStats.mBgMobileRxPower;
                this.mFgMobileTxPower += detailBatteryStats.mFgMobileTxPower;
                this.mFgMobileRxPower += detailBatteryStats.mFgMobileRxPower;
                this.mBgWifiTxPower += detailBatteryStats.mBgWifiTxPower;
                this.mBgWifiRxPower += detailBatteryStats.mBgWifiRxPower;
                this.mFgWifiTxPower += detailBatteryStats.mFgWifiTxPower;
                this.mFgWifiRxPower += detailBatteryStats.mFgWifiRxPower;
                this.mBgWifiScanPower += detailBatteryStats.mBgWifiScanPower;
                this.mFgWifiScanPower += detailBatteryStats.mFgWifiScanPower;
            }
        }
    }

    @SystemApi
    public long getTotalCpuTime() {
        return this.mBgCpuTime + this.mFgCpuTime;
    }

    @SystemApi
    public float getWifiPower() {
        return this.mBgWifiTxPower + this.mBgWifiRxPower + this.mFgWifiTxPower + this.mFgWifiRxPower + this.mBgWifiScanPower + this.mFgWifiScanPower;
    }

    @SystemApi
    public float getFgTotalPower() {
        return this.mFgTotalPower;
    }

    @SystemApi
    public float getBgTotalPower() {
        return this.mBgTotalPower;
    }

    @SystemApi
    public float getDistributedPower() {
        return this.mDistributedPower;
    }

    @SystemApi
    public float getTotalPower() {
        return this.mBgTotalPower + this.mFgTotalPower + this.mDistributedPower;
    }

    public int compareTo(DetailBatteryStats detailBatteryStats) {
        if (detailBatteryStats == null) {
            return 0;
        }
        return Float.compare(detailBatteryStats.getTotalPower(), getTotalPower());
    }

    @SystemApi
    public void sumPower() {
        this.mBgTotalPower = this.mBgSensorPower + this.mBgGpsPower + this.mBgWlPower + this.mBgCpuPower + this.mBgMobileTxPower + this.mBgMobileRxPower + this.mBgWifiTxPower + this.mBgWifiRxPower + this.mBgWifiScanPower;
        this.mFgTotalPower = this.mFgScreenPower + this.mFgSensorPower + this.mFgGpsPower + this.mFgCpuPower + this.mFgGpuPower + this.mFgMobileTxPower + this.mFgMobileRxPower + this.mFgWifiTxPower + this.mFgWifiRxPower + this.mFgWifiScanPower;
        float f = this.mBgTotalPower;
        float f2 = this.mFgTotalPower;
        float f3 = this.mDistributedPower;
    }

    private void appendDetailInfo(StringBuilder sb) {
        long j = (this.mBgCpuTime + this.mFgCpuTime) / 1000;
        if (j > 0) {
            sb.append("    |---Cpu:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.1f", Float.valueOf(this.mBgCpuPower + this.mFgCpuPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.1f", Float.valueOf(this.mBgCpuPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.1f)", Float.valueOf(this.mFgCpuPower)));
            sb.append("\ttime: (total ");
            sb.append(j);
            sb.append(", bg ");
            sb.append(this.mBgCpuTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgCpuTime / 1000);
            sb.append(')');
            sb.append('\n');
        }
        if (this.mFgScreenPower > 0.0f) {
            sb.append("    |---Screen:");
            sb.append(String.format(Locale.ROOT, "\t%.1f\t", Float.valueOf(this.mFgScreenPower)));
            sb.append(this.mFgScreenTime / 1000);
            sb.append('\n');
        }
        if (this.mBgGpsPower + this.mFgGpsPower > 0.0f) {
            sb.append("    |---Gps:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.2f", Float.valueOf(this.mBgGpsPower + this.mFgGpsPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.2f", Float.valueOf(this.mBgGpsPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.2f)", Float.valueOf(this.mFgGpsPower)));
            sb.append("\ttime: (total ");
            sb.append((this.mBgGpsTime + this.mFgGpsTime) / 1000);
            sb.append(", bg ");
            sb.append(this.mBgGpsTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgGpsTime / 1000);
            sb.append(')');
            sb.append('\n');
        }
        if (this.mBgSensorPower + this.mFgSensorPower > 0.0f) {
            sb.append("    |---Sensor:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.3f", Float.valueOf(this.mBgSensorPower + this.mFgSensorPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.3f", Float.valueOf(this.mBgSensorPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.3f)", Float.valueOf(this.mFgSensorPower)));
            sb.append("\ttime: (total ");
            sb.append((this.mBgSensorsTime + this.mFgSensorsTime) / 1000);
            sb.append(", bg ");
            sb.append(this.mBgSensorsTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgSensorsTime / 1000);
            sb.append(')');
            sb.append('\n');
        }
        appendDetailInfoEx(sb);
    }

    private void appendDetailInfoEx(StringBuilder sb) {
        if (this.mBgWlTime / 1000 > 0) {
            sb.append("    |---WakeLock:");
            sb.append(String.format(Locale.ROOT, "\t%.1f", Float.valueOf(this.mBgWlPower)));
            sb.append('\t');
            sb.append(this.mBgWlTime / 1000);
            sb.append('\n');
        }
        if (this.mFgGpuTime / 1000 > 0) {
            sb.append("    |---Gpu:");
            sb.append(String.format(Locale.ROOT, "\t%.1f", Float.valueOf(this.mFgGpuPower)));
            sb.append('\t');
            sb.append(this.mFgGpuTime / 1000);
            sb.append('\n');
        }
        appendMobileAndWifiDetailInfo(sb);
    }

    private void appendMobileAndWifiDetailInfo(StringBuilder sb) {
        long j = (((this.mBgMobileTxTime + this.mBgMobileRxTime) + this.mFgMobileTxTime) + this.mFgMobileRxTime) / 1000;
        if (j > 0) {
            sb.append("    |---Mobile:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.1f", Float.valueOf(this.mBgMobileTxPower + this.mBgMobileRxPower + this.mFgMobileTxPower + this.mFgMobileRxPower)));
            sb.append(String.format(Locale.ROOT, ", bgTx %.1f", Float.valueOf(this.mBgMobileTxPower)));
            sb.append(String.format(Locale.ROOT, ", bgRx %.1f", Float.valueOf(this.mBgMobileRxPower)));
            sb.append(String.format(Locale.ROOT, ", fgTx %.1f", Float.valueOf(this.mFgMobileTxPower)));
            sb.append(String.format(Locale.ROOT, ", fgRx %.1f)", Float.valueOf(this.mFgMobileRxPower)));
            sb.append("\ttime: (total ");
            sb.append(j);
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
            sb.append(')');
            sb.append('\n');
        }
        float wifiPower = getWifiPower();
        if (wifiPower > 0.0f) {
            sb.append("    |---Wifi:");
            sb.append(String.format(Locale.ROOT, "\tactivePower: (total %.1f", Float.valueOf(wifiPower)));
            sb.append(String.format(Locale.ROOT, ", bgTx %.1f", Float.valueOf(this.mBgWifiTxPower)));
            sb.append(String.format(Locale.ROOT, ", bgRx %.1f", Float.valueOf(this.mBgWifiRxPower)));
            sb.append(String.format(Locale.ROOT, ", fgTx %.1f", Float.valueOf(this.mFgWifiTxPower)));
            sb.append(String.format(Locale.ROOT, ", fgRx %.1f)", Float.valueOf(this.mFgWifiRxPower)));
            sb.append("\twifi bg: (");
            sb.append(this.mWifiEntry[0]);
            sb.append("\twifi fg: ");
            sb.append(this.mWifiEntry[1]);
            sb.append(String.format(Locale.ROOT, ")\tScanPower: (total %.1f", Float.valueOf(this.mBgWifiScanPower + this.mFgWifiScanPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.1f", Float.valueOf(this.mBgWifiScanPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.1f)", Float.valueOf(this.mFgWifiScanPower)));
            sb.append("\tscanTime: (total ");
            sb.append((this.mBgWifiScanTime + this.mFgWifiScanTime) / 1000);
            sb.append(", bg ");
            sb.append(this.mBgWifiScanTime / 1000);
            sb.append(", fg ");
            sb.append(this.mFgWifiScanTime / 1000);
            sb.append(')');
        }
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        float totalPower = getTotalPower();
        if (totalPower <= 0.0f) {
            return "";
        }
        sb.append(this.mName);
        sb.append('\t');
        sb.append(this.mUid);
        sb.append(String.format(Locale.ROOT, "\ttotal: %.1f", Float.valueOf(totalPower)));
        sb.append(String.format(Locale.ROOT, "\tbg: %.1f", Float.valueOf(this.mBgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tfg: %.1f", Float.valueOf(this.mFgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tdis: %.1f%n", Float.valueOf(this.mDistributedPower)));
        appendDetailInfo(sb);
        return sb.toString();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0082: APUT  
      (r4v1 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Float : 0x007e: INVOKE  (r9v1 java.lang.Float) = (r2v1 float) type: STATIC call: java.lang.Float.valueOf(float):java.lang.Float)
     */
    @SystemApi
    public String toString(float f, boolean z) {
        StringBuilder sb = new StringBuilder();
        float totalPower = getTotalPower();
        float f2 = 0.0f;
        if (totalPower <= 0.0f) {
            return "";
        }
        sb.append(this.mName);
        sb.append('\t');
        sb.append(this.mUid);
        sb.append(String.format(Locale.ROOT, "\ttotal: %.1f", Float.valueOf(totalPower)));
        sb.append(String.format(Locale.ROOT, "\tbg: %.1f", Float.valueOf(this.mBgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tfg: %.1f", Float.valueOf(this.mFgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tdis: %.1f", Float.valueOf(this.mDistributedPower)));
        Locale locale = Locale.ROOT;
        Object[] objArr = new Object[1];
        if (f > 0.0f) {
            f2 = (totalPower / f) * 100.0f;
        }
        objArr[0] = Float.valueOf(f2);
        sb.append(String.format(locale, "\tratio: %.1f%%%n", objArr));
        if (!z) {
            return sb.toString();
        }
        appendDetailInfo(sb);
        return sb.toString();
    }

    public static class NetEntry {
        public long[] mItem = new long[4];

        @SystemApi
        public void add(NetEntry netEntry) {
            if (netEntry != null && netEntry.mItem.length >= 4) {
                for (int i = 0; i < 4; i++) {
                    long[] jArr = this.mItem;
                    jArr[i] = jArr[i] + netEntry.mItem[i];
                }
            }
        }

        @SystemApi
        public void set(long j, long j2, long j3, long j4) {
            long[] jArr = this.mItem;
            jArr[0] = j;
            jArr[1] = j2;
            jArr[2] = j3;
            jArr[3] = j4;
        }

        @SystemApi
        public void set(int i, long j) {
            if (i < 4 && i >= 0) {
                this.mItem[i] = j;
            }
        }

        @SystemApi
        public void set(NetEntry netEntry) {
            if (netEntry != null && netEntry.mItem.length >= 4) {
                for (int i = 0; i < 4; i++) {
                    this.mItem[i] = netEntry.mItem[i];
                }
            }
        }

        @SystemApi
        public NetEntry minus(NetEntry netEntry) {
            NetEntry netEntry2 = new NetEntry();
            if (netEntry != null && netEntry.mItem.length >= 4) {
                for (int i = 0; i < 4; i++) {
                    netEntry2.mItem[i] = this.mItem[i] - netEntry.mItem[i];
                }
            }
            return netEntry2;
        }

        @SystemApi
        public long getTotalPackets() {
            long[] jArr = this.mItem;
            return jArr[2] + jArr[3];
        }

        @SystemApi
        public long getTxPackets() {
            return this.mItem[3];
        }

        @SystemApi
        public long getRxPackets() {
            return this.mItem[2];
        }

        @SystemApi
        public long get(int i) {
            if (i >= 4 || i < 0) {
                return 0;
            }
            return this.mItem[i];
        }

        @SystemApi
        public void reset() {
            for (int i = 0; i < 4; i++) {
                this.mItem[i] = 0;
            }
        }

        private void processBytesUnit(StringBuilder sb, long j) {
            if (j > ((long) 1048576)) {
                float f = (float) 1024;
                sb.append(String.format(Locale.ROOT, "%.2fMB", Float.valueOf(((((float) j) * 1.0f) / f) / f)));
            } else if (j > ((long) 1024)) {
                sb.append(String.format(Locale.ROOT, "%.2fKB", Float.valueOf((((float) j) * 1.0f) / ((float) 1024))));
            } else {
                sb.append(j);
                sb.append("B");
            }
        }

        @SystemApi
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
    }
}
