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
    public float bgCpuPower;
    public long bgCpuTime;
    public float bgGpsPower;
    public long bgGpsTime;
    public float bgMobileRxPower;
    public long bgMobileRxTime;
    public float bgMobileTxPower;
    public long bgMobileTxTime;
    public float bgSensorPower;
    public long bgSensorsTime;
    public float bgTotalPower;
    public float bgWifiRxPower;
    public float bgWifiScanPower;
    public long bgWifiScanTime;
    public float bgWifiTxPower;
    public float bgWlPower;
    public long bgWlTime;
    public int callerUid;
    public float distributedPower;
    public float fgCpuPower;
    public long fgCpuTime;
    public float fgGpsPower;
    public long fgGpsTime;
    public float fgGpuPower;
    public long fgGpuTime;
    public float fgMobileRxPower;
    public long fgMobileRxTime;
    public float fgMobileTxPower;
    public long fgMobileTxTime;
    public float fgScreenPower;
    public long fgScreenTime;
    public float fgSensorPower;
    public long fgSensorsTime;
    public float fgTotalPower;
    public float fgWifiRxPower;
    public float fgWifiScanPower;
    public long fgWifiScanTime;
    public float fgWifiTxPower;
    public boolean isPkg;
    public NetEntry[] mobileEntry;
    public String packageName;
    public NetEntry[] wifiEntry;

    public DetailBatteryStats(String str, int i, boolean z) {
        this.isPkg = false;
        this.fgScreenTime = 0;
        this.bgGpsTime = 0;
        this.fgGpsTime = 0;
        this.bgSensorsTime = 0;
        this.fgSensorsTime = 0;
        this.bgWlTime = 0;
        this.bgCpuTime = 0;
        this.fgCpuTime = 0;
        this.fgGpuTime = 0;
        this.bgMobileTxTime = 0;
        this.bgMobileRxTime = 0;
        this.fgMobileTxTime = 0;
        this.fgMobileRxTime = 0;
        this.mobileEntry = new NetEntry[2];
        this.wifiEntry = new NetEntry[2];
        this.bgWifiScanTime = 0;
        this.fgWifiScanTime = 0;
        this.bgTotalPower = 0.0f;
        this.fgTotalPower = 0.0f;
        this.distributedPower = 0.0f;
        this.fgScreenPower = 0.0f;
        this.bgGpsPower = 0.0f;
        this.fgGpsPower = 0.0f;
        this.bgSensorPower = 0.0f;
        this.fgSensorPower = 0.0f;
        this.bgWlPower = 0.0f;
        this.bgCpuPower = 0.0f;
        this.fgCpuPower = 0.0f;
        this.fgGpuPower = 0.0f;
        this.bgMobileTxPower = 0.0f;
        this.bgMobileRxPower = 0.0f;
        this.fgMobileTxPower = 0.0f;
        this.fgMobileRxPower = 0.0f;
        this.bgWifiTxPower = 0.0f;
        this.bgWifiRxPower = 0.0f;
        this.fgWifiTxPower = 0.0f;
        this.fgWifiRxPower = 0.0f;
        this.bgWifiScanPower = 0.0f;
        this.fgWifiScanPower = 0.0f;
        this.packageName = str;
        this.callerUid = i;
        this.isPkg = z;
        this.mobileEntry[0] = new NetEntry();
        this.mobileEntry[1] = new NetEntry();
        this.wifiEntry[0] = new NetEntry();
        this.wifiEntry[1] = new NetEntry();
    }

    public DetailBatteryStats() {
        this.isPkg = false;
        this.fgScreenTime = 0;
        this.bgGpsTime = 0;
        this.fgGpsTime = 0;
        this.bgSensorsTime = 0;
        this.fgSensorsTime = 0;
        this.bgWlTime = 0;
        this.bgCpuTime = 0;
        this.fgCpuTime = 0;
        this.fgGpuTime = 0;
        this.bgMobileTxTime = 0;
        this.bgMobileRxTime = 0;
        this.fgMobileTxTime = 0;
        this.fgMobileRxTime = 0;
        this.mobileEntry = new NetEntry[2];
        this.wifiEntry = new NetEntry[2];
        this.bgWifiScanTime = 0;
        this.fgWifiScanTime = 0;
        this.bgTotalPower = 0.0f;
        this.fgTotalPower = 0.0f;
        this.distributedPower = 0.0f;
        this.fgScreenPower = 0.0f;
        this.bgGpsPower = 0.0f;
        this.fgGpsPower = 0.0f;
        this.bgSensorPower = 0.0f;
        this.fgSensorPower = 0.0f;
        this.bgWlPower = 0.0f;
        this.bgCpuPower = 0.0f;
        this.fgCpuPower = 0.0f;
        this.fgGpuPower = 0.0f;
        this.bgMobileTxPower = 0.0f;
        this.bgMobileRxPower = 0.0f;
        this.fgMobileTxPower = 0.0f;
        this.fgMobileRxPower = 0.0f;
        this.bgWifiTxPower = 0.0f;
        this.bgWifiRxPower = 0.0f;
        this.fgWifiTxPower = 0.0f;
        this.fgWifiRxPower = 0.0f;
        this.bgWifiScanPower = 0.0f;
        this.fgWifiScanPower = 0.0f;
        this.mobileEntry[0] = new NetEntry();
        this.mobileEntry[1] = new NetEntry();
        this.wifiEntry[0] = new NetEntry();
        this.wifiEntry[1] = new NetEntry();
    }

    @SystemApi
    public void setName(String str) {
        this.packageName = str;
    }

    @SystemApi
    public String getName() {
        String str = this.packageName;
        return str == null ? "" : str;
    }

    @SystemApi
    public void setIsPkg(boolean z) {
        this.isPkg = z;
    }

    @SystemApi
    public boolean getIsPkg() {
        return this.isPkg;
    }

    @SystemApi
    public void setDistributePower(float f) {
        this.distributedPower = f;
    }

    @SystemApi
    public void addScreenTime(long j) {
        this.fgScreenTime += j;
    }

    @SystemApi
    public void addScreenPower(float f) {
        this.fgScreenPower += f;
    }

    @SystemApi
    public void addGpsTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.bgGpsTime += jArr[0];
            this.fgGpsTime += jArr[1];
        }
    }

    @SystemApi
    public void addGpsPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.bgGpsPower += fArr[0];
            this.fgGpsPower += fArr[1];
        }
    }

    @SystemApi
    public void addSensorTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.bgSensorsTime += jArr[0];
            this.fgSensorsTime += jArr[1];
        }
    }

    @SystemApi
    public void addSensorPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.bgSensorPower += fArr[0];
            this.fgSensorPower += fArr[1];
        }
    }

    @SystemApi
    public void addWlTime(long j) {
        this.bgWlTime += j;
    }

    @SystemApi
    public void addWlPower(float f) {
        this.bgWlPower += f;
    }

    @SystemApi
    public void addCpuTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.bgCpuTime += jArr[0];
            this.fgCpuTime += jArr[1];
        }
    }

    @SystemApi
    public void addCpuPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.bgCpuPower += fArr[0];
            this.fgCpuPower += fArr[1];
        }
    }

    @SystemApi
    public void addGpuTime(long j) {
        this.fgGpuTime += j;
    }

    @SystemApi
    public void addGpuPower(float f) {
        this.fgGpuPower += f;
    }

    @SystemApi
    public void addMobileTime(long[] jArr) {
        if (jArr != null && jArr.length >= 4) {
            this.bgMobileTxTime += jArr[0];
            this.bgMobileRxTime += jArr[1];
            this.fgMobileTxTime += jArr[2];
            this.fgMobileRxTime += jArr[3];
        }
    }

    @SystemApi
    public void addMobileByteAndPacket(NetEntry[] netEntryArr) {
        if (netEntryArr != null && netEntryArr.length >= 2) {
            this.mobileEntry[0].add(netEntryArr[0]);
            this.mobileEntry[1].add(netEntryArr[1]);
        }
    }

    @SystemApi
    public void addMobilePower(float[] fArr) {
        if (fArr != null && fArr.length >= 4) {
            this.bgMobileTxPower += fArr[0];
            this.bgMobileRxPower += fArr[1];
            this.fgMobileTxPower += fArr[2];
            this.fgMobileRxPower += fArr[3];
        }
    }

    @SystemApi
    public void addWifiByteAndPacket(NetEntry[] netEntryArr) {
        if (netEntryArr != null && netEntryArr.length >= 2) {
            this.wifiEntry[0].add(netEntryArr[0]);
            this.wifiEntry[1].add(netEntryArr[1]);
        }
    }

    @SystemApi
    public void addWifiScanTime(long[] jArr) {
        if (jArr != null && jArr.length >= 2) {
            this.bgWifiScanTime += jArr[0];
            this.fgWifiScanTime += jArr[1];
        }
    }

    @SystemApi
    public void addWifiActivePower(float[] fArr) {
        if (fArr != null && fArr.length >= 4) {
            this.bgWifiTxPower += fArr[0];
            this.bgWifiRxPower += fArr[1];
            this.fgWifiTxPower += fArr[2];
            this.fgWifiRxPower += fArr[3];
        }
    }

    @SystemApi
    public void addWifiScanPower(float[] fArr) {
        if (fArr != null && fArr.length >= 2) {
            this.bgWifiScanPower += fArr[0];
            this.fgWifiScanPower += fArr[1];
        }
    }

    @SystemApi
    public void add(DetailBatteryStats detailBatteryStats) {
        if (detailBatteryStats != null) {
            NetEntry[] netEntryArr = detailBatteryStats.mobileEntry;
            if (netEntryArr.length >= 2 && detailBatteryStats.wifiEntry.length >= 2) {
                this.fgScreenTime += detailBatteryStats.fgScreenTime;
                this.bgGpsTime += detailBatteryStats.bgGpsTime;
                this.fgGpsTime += detailBatteryStats.fgGpsTime;
                this.bgSensorsTime += detailBatteryStats.bgSensorsTime;
                this.fgSensorsTime += detailBatteryStats.fgSensorsTime;
                this.bgWlTime += detailBatteryStats.bgWlTime;
                this.bgCpuTime += detailBatteryStats.bgCpuTime;
                this.fgCpuTime += detailBatteryStats.fgCpuTime;
                this.fgGpuTime += detailBatteryStats.fgGpuTime;
                this.bgMobileTxTime += detailBatteryStats.bgMobileTxTime;
                this.bgMobileRxTime += detailBatteryStats.bgMobileRxTime;
                this.fgMobileTxTime += detailBatteryStats.fgMobileTxTime;
                this.fgMobileRxTime += detailBatteryStats.fgMobileRxTime;
                this.mobileEntry[0].add(netEntryArr[0]);
                this.mobileEntry[1].add(detailBatteryStats.mobileEntry[1]);
                this.wifiEntry[0].add(detailBatteryStats.wifiEntry[0]);
                this.wifiEntry[1].add(detailBatteryStats.wifiEntry[1]);
                this.bgWifiScanTime += detailBatteryStats.bgWifiScanTime;
                this.fgWifiScanTime += detailBatteryStats.fgWifiScanTime;
                this.bgTotalPower += detailBatteryStats.bgTotalPower;
                this.fgTotalPower += detailBatteryStats.fgTotalPower;
                this.distributedPower += detailBatteryStats.distributedPower;
                this.fgScreenPower += detailBatteryStats.fgScreenPower;
                this.bgGpsPower += detailBatteryStats.bgGpsPower;
                this.fgGpsPower += detailBatteryStats.fgGpsPower;
                this.bgSensorPower += detailBatteryStats.bgSensorPower;
                this.fgSensorPower += detailBatteryStats.fgSensorPower;
                this.bgWlPower += detailBatteryStats.bgWlPower;
                this.bgCpuPower += detailBatteryStats.bgCpuPower;
                this.fgCpuPower += detailBatteryStats.fgCpuPower;
                this.fgGpuPower += detailBatteryStats.fgGpuPower;
                this.bgMobileTxPower += detailBatteryStats.bgMobileTxPower;
                this.bgMobileRxPower += detailBatteryStats.bgMobileRxPower;
                this.fgMobileTxPower += detailBatteryStats.fgMobileTxPower;
                this.fgMobileRxPower += detailBatteryStats.fgMobileRxPower;
                this.bgWifiTxPower += detailBatteryStats.bgWifiTxPower;
                float f = this.bgWifiRxPower;
                float f2 = detailBatteryStats.fgWifiRxPower;
                this.bgWifiRxPower = f + f2;
                this.fgWifiTxPower += detailBatteryStats.fgWifiTxPower;
                this.fgWifiRxPower += f2;
                this.bgWifiScanPower += detailBatteryStats.bgWifiScanPower;
                this.fgWifiScanPower += detailBatteryStats.fgWifiScanPower;
            }
        }
    }

    @SystemApi
    public long getTotalCpuTime() {
        return this.bgCpuTime + this.fgCpuTime;
    }

    @SystemApi
    public float getWifiPower() {
        return this.bgWifiTxPower + this.bgWifiRxPower + this.fgWifiTxPower + this.fgWifiRxPower + this.bgWifiScanPower + this.fgWifiScanPower;
    }

    @SystemApi
    public float getFgTotalPower() {
        return this.fgTotalPower;
    }

    @SystemApi
    public float getBgTotalPower() {
        return this.bgTotalPower;
    }

    @SystemApi
    public float getDistributedPower() {
        return this.distributedPower;
    }

    @SystemApi
    public float getTotalPower() {
        return this.bgTotalPower + this.fgTotalPower + this.distributedPower;
    }

    public int compareTo(DetailBatteryStats detailBatteryStats) {
        if (detailBatteryStats == null) {
            return 0;
        }
        return Float.compare(detailBatteryStats.getTotalPower(), getTotalPower());
    }

    @SystemApi
    public void sumPower() {
        this.bgTotalPower = this.bgSensorPower + this.bgGpsPower + this.bgWlPower + this.bgCpuPower + this.bgMobileTxPower + this.bgMobileRxPower + this.bgWifiTxPower + this.bgWifiRxPower + this.bgWifiScanPower;
        this.fgTotalPower = this.fgScreenPower + this.fgSensorPower + this.fgGpsPower + this.fgCpuPower + this.fgGpuPower + this.fgMobileTxPower + this.fgMobileRxPower + this.fgWifiTxPower + this.fgWifiRxPower + this.fgWifiScanPower;
        float f = this.bgTotalPower;
        float f2 = this.fgTotalPower;
        float f3 = this.distributedPower;
    }

    private void appendDetailInfo(StringBuilder sb) {
        long j = (this.bgCpuTime + this.fgCpuTime) / 1000;
        if (j > 0) {
            sb.append("    |---Cpu:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.1f", Float.valueOf(this.bgCpuPower + this.fgCpuPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.1f", Float.valueOf(this.bgCpuPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.1f)", Float.valueOf(this.fgCpuPower)));
            sb.append("\ttime: (total ");
            sb.append(j);
            sb.append(", bg ");
            sb.append(this.bgCpuTime / 1000);
            sb.append(", fg ");
            sb.append(this.fgCpuTime / 1000);
            sb.append(')');
            sb.append('\n');
        }
        if (this.fgScreenPower > 0.0f) {
            sb.append("    |---Screen:");
            sb.append(String.format(Locale.ROOT, "\t%.1f\t", Float.valueOf(this.fgScreenPower)));
            sb.append(this.fgScreenTime / 1000);
            sb.append('\n');
        }
        if (this.bgGpsPower + this.fgGpsPower > 0.0f) {
            sb.append("    |---Gps:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.2f", Float.valueOf(this.bgGpsPower + this.fgGpsPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.2f", Float.valueOf(this.bgGpsPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.2f)", Float.valueOf(this.fgGpsPower)));
            sb.append("\ttime: (total ");
            sb.append((this.bgGpsTime + this.fgGpsTime) / 1000);
            sb.append(", bg ");
            sb.append(this.bgGpsTime / 1000);
            sb.append(", fg ");
            sb.append(this.fgGpsTime / 1000);
            sb.append(')');
            sb.append('\n');
        }
        if (this.bgSensorPower + this.fgSensorPower > 0.0f) {
            sb.append("    |---Sensor:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.3f", Float.valueOf(this.bgSensorPower + this.fgSensorPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.3f", Float.valueOf(this.bgSensorPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.3f)", Float.valueOf(this.fgSensorPower)));
            sb.append("\ttime: (total ");
            sb.append((this.bgSensorsTime + this.fgSensorsTime) / 1000);
            sb.append(", bg ");
            sb.append(this.bgSensorsTime / 1000);
            sb.append(", fg ");
            sb.append(this.fgSensorsTime / 1000);
            sb.append(')');
            sb.append('\n');
        }
        appendDetailInfoEx(sb);
    }

    private void appendDetailInfoEx(StringBuilder sb) {
        if (this.bgWlTime / 1000 > 0) {
            sb.append("    |---WakeLock:");
            sb.append(String.format(Locale.ROOT, "\t%.1f", Float.valueOf(this.bgWlPower)));
            sb.append('\t');
            sb.append(this.bgWlTime / 1000);
            sb.append('\n');
        }
        if (this.fgGpuTime / 1000 > 0) {
            sb.append("    |---Gpu:");
            sb.append(String.format(Locale.ROOT, "\t%.1f", Float.valueOf(this.fgGpuPower)));
            sb.append('\t');
            sb.append(this.fgGpuTime / 1000);
            sb.append('\n');
        }
        appendMobileAndWifiDetailInfo(sb);
    }

    private void appendMobileAndWifiDetailInfo(StringBuilder sb) {
        long j = (((this.bgMobileTxTime + this.bgMobileRxTime) + this.fgMobileTxTime) + this.fgMobileRxTime) / 1000;
        if (j > 0) {
            sb.append("    |---Mobile:");
            sb.append(String.format(Locale.ROOT, "\tpower: (total %.1f", Float.valueOf(this.bgMobileTxPower + this.bgMobileRxPower + this.fgMobileTxPower + this.fgMobileRxPower)));
            sb.append(String.format(Locale.ROOT, ", bgTx %.1f", Float.valueOf(this.bgMobileTxPower)));
            sb.append(String.format(Locale.ROOT, ", bgRx %.1f", Float.valueOf(this.bgMobileRxPower)));
            sb.append(String.format(Locale.ROOT, ", fgTx %.1f", Float.valueOf(this.fgMobileTxPower)));
            sb.append(String.format(Locale.ROOT, ", fgRx %.1f)", Float.valueOf(this.fgMobileRxPower)));
            sb.append("\ttime: (total ");
            sb.append(j);
            sb.append(", bgTx ");
            sb.append(this.bgMobileTxTime / 1000);
            sb.append(", bgRx ");
            sb.append(this.bgMobileRxTime / 1000);
            sb.append(", fgTx ");
            sb.append(this.fgMobileTxTime / 1000);
            sb.append(", fgRx");
            sb.append(this.fgMobileRxTime / 1000);
            sb.append(")\tmobile bg: (");
            sb.append(this.mobileEntry[0]);
            sb.append("\tmobile fg: ");
            sb.append(this.mobileEntry[1]);
            sb.append(')');
            sb.append('\n');
        }
        float wifiPower = getWifiPower();
        if (wifiPower > 0.0f) {
            sb.append("    |---Wifi:");
            sb.append(String.format(Locale.ROOT, "\tactivePower: (total %.1f", Float.valueOf(wifiPower)));
            sb.append(String.format(Locale.ROOT, ", bgTx %.1f", Float.valueOf(this.bgWifiTxPower)));
            sb.append(String.format(Locale.ROOT, ", bgRx %.1f", Float.valueOf(this.bgWifiRxPower)));
            sb.append(String.format(Locale.ROOT, ", fgTx %.1f", Float.valueOf(this.fgWifiTxPower)));
            sb.append(String.format(Locale.ROOT, ", fgRx %.1f)", Float.valueOf(this.fgWifiRxPower)));
            sb.append("\twifi bg: (");
            sb.append(this.wifiEntry[0]);
            sb.append("\twifi fg: ");
            sb.append(this.wifiEntry[1]);
            sb.append(String.format(Locale.ROOT, ")\tScanPower: (total %.1f", Float.valueOf(this.bgWifiScanPower + this.fgWifiScanPower)));
            sb.append(String.format(Locale.ROOT, ", bg %.1f", Float.valueOf(this.bgWifiScanPower)));
            sb.append(String.format(Locale.ROOT, ", fg %.1f)", Float.valueOf(this.fgWifiScanPower)));
            sb.append("\tscanTime: (total ");
            sb.append((this.bgWifiScanTime + this.fgWifiScanTime) / 1000);
            sb.append(", bg ");
            sb.append(this.bgWifiScanTime / 1000);
            sb.append(", fg ");
            sb.append(this.fgWifiScanTime / 1000);
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
        sb.append(this.packageName);
        sb.append('\t');
        sb.append(this.callerUid);
        sb.append(String.format(Locale.ROOT, "\ttotal: %.1f", Float.valueOf(totalPower)));
        sb.append(String.format(Locale.ROOT, "\tbg: %.1f", Float.valueOf(this.bgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tfg: %.1f", Float.valueOf(this.fgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tdis: %.1f%n", Float.valueOf(this.distributedPower)));
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
        sb.append(this.packageName);
        sb.append('\t');
        sb.append(this.callerUid);
        sb.append(String.format(Locale.ROOT, "\ttotal: %.1f", Float.valueOf(totalPower)));
        sb.append(String.format(Locale.ROOT, "\tbg: %.1f", Float.valueOf(this.bgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tfg: %.1f", Float.valueOf(this.fgTotalPower)));
        sb.append(String.format(Locale.ROOT, "\tdis: %.1f", Float.valueOf(this.distributedPower)));
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
