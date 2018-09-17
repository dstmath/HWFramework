package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.device.connectivitychrlog.CSubCPUInfo;
import com.huawei.device.connectivitychrlog.CSubDNS;
import com.huawei.device.connectivitychrlog.CSubMemInfo;
import com.huawei.device.connectivitychrlog.CSubPacketCount;
import java.util.ArrayList;
import java.util.List;

public class HwCHRWebMonitor {
    private static final int BYTES_100K = 102400;
    private static final int BYTES_150K = 153600;
    private static final int BYTES_300K = 307200;
    private static String CPU_FILE_NAME = "proc/stat";
    private static final int ERR_NETWORK_OK = 2;
    private static final int ERR_NETWORK_SLOWLY = 1;
    private static final boolean HWFLOW;
    private static String MEMINFO_FILE_NAME = "proc/meminfo";
    private static final String TAG = "HwCHRWebMonitor";
    private static String TCP_FILE_NAME = HwSelfCureUtils.WIFI_STAT_FILE;
    private static final int UNMODIFY_COUNT = 20;
    private static final int WIFI_ACCESS_WEB_SLOW_RSSI = -78;
    private static final int WIFI_PACKETS_COUNT = 10;
    private static final double WIFI_PACKETS_RATE = 0.3d;
    private static HwCHRWebMonitor sWebMonitor = null;
    private List<HwCHRWifiUIDWebSpeed> lstUID = new ArrayList();
    private HwCHRWifiCPUUsage mCPUUsage = new HwCHRWifiCPUUsage();
    private Context mContext;
    private HwCHRWifiDNS mDNS = new HwCHRWifiDNS();
    private int mFailReason = 0;
    private HwCHRWifiLinkMonitor mLinkMonitor = new HwCHRWifiLinkMonitor();
    private HwCHRWifiMemUsage mMemUsage = new HwCHRWifiMemUsage();
    private long mNetNormalDuration = 0;
    private long mNetSlowlyDuration = 0;
    private int mNetWorkSuckTimes = 0;
    private int mOnAppSuckTime = 0;
    private HwCHRWifiPacketCnt mPktcnt = null;
    private long mStartNetNormalTime = 0;
    private long mStartNetSlowlyTime = 0;
    private HwCHRWebSpeed mStastics = HwCHRWebSpeed.getDefault();
    private String mUIDIsSuck = "";
    private int mUnModifyCount = 0;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    private HwCHRWebMonitor(Context context, WifiNative wifiNatie) {
        this.mContext = context;
        this.mPktcnt = new HwCHRWifiPacketCnt(wifiNatie);
    }

    public static synchronized HwCHRWebMonitor newInstance(Context context, WifiNative wifiNative) {
        HwCHRWebMonitor hwCHRWebMonitor;
        synchronized (HwCHRWebMonitor.class) {
            if (sWebMonitor == null) {
                sWebMonitor = new HwCHRWebMonitor(context, wifiNative);
            }
            hwCHRWebMonitor = sWebMonitor;
        }
        return hwCHRWebMonitor;
    }

    public HwCHRWebSpeed getWebStatistic() {
        return this.mStastics;
    }

    public static CSubMemInfo getMemCHR() {
        CSubMemInfo mem = new CSubMemInfo();
        if (sWebMonitor != null) {
            return sWebMonitor.mMemUsage.getMemInfoCHR();
        }
        return mem;
    }

    public HwCHRWifiPacketCnt getPktcnt() {
        return this.mPktcnt;
    }

    public static CSubCPUInfo getCpuCHR() {
        CSubCPUInfo cpu = new CSubCPUInfo();
        if (sWebMonitor != null) {
            return sWebMonitor.mCPUUsage.getCPUInfoCHR();
        }
        return cpu;
    }

    public static CSubDNS getDNSCHR() {
        CSubDNS dns = new CSubDNS();
        if (sWebMonitor != null) {
            return sWebMonitor.mDNS.getDNSCHR();
        }
        return dns;
    }

    public static CSubPacketCount getPacketCountCHR() {
        CSubPacketCount pkt = new CSubPacketCount();
        if (sWebMonitor != null) {
            return sWebMonitor.mPktcnt.getPacketCntCHR();
        }
        return pkt;
    }

    private int getUIDBegin(List<String> lines) {
        int listSize = lines.size();
        for (int i = 0; i < listSize; i++) {
            if (((String) lines.get(i)).startsWith("UID\t")) {
                return i;
            }
        }
        return -1;
    }

    private long getRunningProcUID() {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager.getRunningAppProcesses() == null || activityManager.getRunningAppProcesses().size() == 0) {
            loge("running process is null");
            return -1;
        }
        loge("running processName=" + ((RunningAppProcessInfo) activityManager.getRunningAppProcesses().get(0)).processName);
        return (long) ((RunningAppProcessInfo) activityManager.getRunningAppProcesses().get(0)).uid;
    }

    public void checkWebSpeed(long rxBytes, long txBytes, int rssi, HwCHRWifiRSSIGroupSummery grp, ClientHandler ch, AccessWebStatus aws) {
        List<String> result = HwCHRWifiFile.getFileResult(TCP_FILE_NAME);
        if (result.size() != 0) {
            if (this.mStastics == null) {
                this.mStastics = new HwCHRWebSpeed();
            }
            this.mStastics.parse_line(result, 0);
            this.mNetWorkSuckTimes = this.mStastics.getSuckTimes();
            loge(toString() + "  " + this.mStastics.toString());
            long duration = this.mStastics.getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RTT_DURATION);
            long packets = this.mStastics.getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RTT_PACKETS);
            int rtt = 0;
            if (grp != null) {
                grp.updateTcpSummery(packets, duration, rssi);
            }
            if (packets > 0) {
                rtt = (int) (duration / packets);
            }
            int index = getUIDBegin(result);
            if (-1 != index) {
                this.mUIDIsSuck = updateUIDSpeed(result, index, rxBytes, txBytes, rtt, (int) packets, rssi);
            }
            if (this.mOnAppSuckTime >= 3) {
                if (0 == this.mStartNetSlowlyTime) {
                    this.mStartNetSlowlyTime = SystemClock.elapsedRealtime();
                }
                if (0 == this.mNetSlowlyDuration) {
                    this.mNetSlowlyDuration = SystemClock.elapsedRealtime() - this.mStartNetSlowlyTime;
                }
                if (0 != this.mStartNetNormalTime) {
                    this.mNetNormalDuration = SystemClock.elapsedRealtime() - this.mStartNetNormalTime;
                    this.mStartNetNormalTime = 0;
                }
                HwWifiStatStoreImpl.getDefault().incrWebSpeedStatus(0, 1);
                HwWifiCHRStateManagerImpl wchm = HwWifiCHRStateManagerImpl.getDefaultImpl();
                wchm.setNetNormalTime(this.mNetNormalDuration / 1000);
                wchm.setNetSlowlyTime(this.mNetSlowlyDuration / 1000);
                this.mNetSlowlyDuration = 0;
                this.mNetNormalDuration = 0;
                wchm.updateSpeedInfo(this.mStastics.getSpeedInfo(), this.mUIDIsSuck, this.mNetWorkSuckTimes >= 3 ? 1 : 2, this.mFailReason);
                wchm.setCHRCounters(this.mLinkMonitor.getCounterLst());
                ch.readHisiChipsetDebugInfo(aws);
                wchm.updateAccessWebException(102, "", aws);
            } else {
                if (0 == this.mStartNetNormalTime) {
                    this.mStartNetNormalTime = SystemClock.elapsedRealtime();
                }
                if (0 != this.mStartNetSlowlyTime) {
                    this.mNetSlowlyDuration = SystemClock.elapsedRealtime() - this.mStartNetSlowlyTime;
                    this.mStartNetSlowlyTime = 0;
                }
            }
        }
    }

    public int getAppSuckTime() {
        return this.mOnAppSuckTime;
    }

    public void checkAccessWebStatus(AccessWebStatus status) {
        if (this.mStastics != null) {
            long rx = this.mStastics.getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RECVSEGS);
            long tx = this.mStastics.getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_SENDSEGS);
            status.setRxCnt(Integer.parseInt(String.valueOf(rx)));
            status.setTxCnt(Integer.parseInt(String.valueOf(tx)));
        }
    }

    private HwCHRWifiUIDWebSpeed getUIDWebByLine(String line) {
        int listSize = this.lstUID.size();
        for (int j = 0; j < listSize; j++) {
            HwCHRWifiUIDWebSpeed item = (HwCHRWifiUIDWebSpeed) this.lstUID.get(j);
            if (item.isSameUID(line)) {
                return item;
            }
        }
        HwCHRWifiUIDWebSpeed uid = new HwCHRWifiUIDWebSpeed(this.mContext);
        this.lstUID.add(uid);
        return uid;
    }

    void incrNetBadCounter(int deta) {
        loge("incrNetBadCounter mOnAppSuckTime mOnAppSuckTime=" + this.mOnAppSuckTime + " deta=" + deta);
        if (deta > 0 && this.mOnAppSuckTime < 3) {
            this.mOnAppSuckTime += deta;
            this.mUnModifyCount = 0;
        } else if (deta >= 0 || this.mOnAppSuckTime <= 0) {
            this.mUnModifyCount++;
        } else {
            this.mUnModifyCount = 0;
            this.mOnAppSuckTime += deta;
        }
    }

    private boolean isUIDInSuck(HwCHRWifiPacketCnt pkt, int rssi, HwCHRWifiUIDWebSpeed uid) {
        int rtt = 0;
        long rtt_duration = uid.getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RTT_DURATION);
        long rtt_packets = uid.getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RTT_PACKETS);
        if (rtt_packets > 0) {
            rtt = (int) (rtt_duration / rtt_packets);
        }
        if (rssi >= WIFI_ACCESS_WEB_SLOW_RSSI || rtt <= 300 || rtt_packets >= 3) {
            int WifiPackets = pkt.getTxTotal();
            int badWifiPacket = pkt.getTxBad();
            double rate = 0.0d;
            if (WifiPackets > 0) {
                rate = ((double) badWifiPacket) / ((double) WifiPackets);
            }
            if (rate <= WIFI_PACKETS_RATE || WifiPackets < 10 || rtt <= 300 || rtt_packets >= 3) {
                return false;
            }
            this.mFailReason = 5;
            return true;
        }
        this.mFailReason = 4;
        return true;
    }

    private String updateUIDSpeed(List<String> lines, int index, long rxBytes, long txBytes, int rtt, int packets, int rssi) {
        int i;
        String result = "";
        int listSize = this.lstUID.size();
        for (i = 0; i < listSize; i++) {
            ((HwCHRWifiUIDWebSpeed) this.lstUID.get(i)).old();
        }
        this.mPktcnt.fetchPktcntNative();
        loge(this.mPktcnt + " rssi=" + rssi);
        if (rxBytes >= 307200 || txBytes >= 307200 || (packets >= 5 && rtt <= 100)) {
            this.mOnAppSuckTime = 0;
            this.mUnModifyCount = 0;
            loge("Good NetWork Traffic , so mOnAppSuckTime, mUnModifyCount reset 0");
            return "";
        } else if (rxBytes >= 153600 || txBytes >= 153600 || ((packets >= 5 && rtt <= 200) || this.mUnModifyCount >= 20)) {
            incrNetBadCounter(-1);
            loge("RecoveredNetWork Traffic , so mOnAppSuckTime=" + this.mOnAppSuckTime + " modify -1");
            return "";
        } else if (rxBytes >= 102400 || txBytes >= 102400 || (packets >= 5 && rtt <= 300)) {
            loge("Unstable NetWork Traffic, so do nothing");
            return "";
        } else {
            long running_uid = getRunningProcUID();
            String cols = (String) lines.get(index);
            listSize = lines.size();
            i = index + 1;
            while (i < listSize && !((String) lines.get(i)).startsWith("custom ip")) {
                HwCHRWifiUIDWebSpeed item = getUIDWebByLine((String) lines.get(i));
                item.parserValue(cols, (String) lines.get(i));
                item.checkSuckStatus();
                loge(item.toString());
                long item_uid = item.getUID();
                int suckTime = item.getSuckTimes();
                if (item_uid == running_uid) {
                    if (isUIDInSuck(this.mPktcnt, rssi, item)) {
                        incrNetBadCounter(1);
                    } else if (suckTime > 0) {
                        result = item.toString();
                        this.mFailReason = item.getFailReason();
                        incrNetBadCounter(1);
                    }
                    loge("item_uid= " + item_uid + " running_uid=" + running_uid + " suckTime=" + suckTime + " mOnAppSuckTime=" + this.mOnAppSuckTime + " mFailReason=" + this.mFailReason);
                } else {
                    i++;
                }
            }
            if (this.mOnAppSuckTime == 3 || this.mOnAppSuckTime == 1) {
                this.mLinkMonitor.runCounters();
                monitorCpuUsage();
                monitorMemUsage();
                this.mDNS.monitorDNS();
            }
            List<HwCHRWifiUIDWebSpeed> rmvLst = new ArrayList();
            listSize = this.lstUID.size();
            for (i = 0; i < listSize; i++) {
                if (((HwCHRWifiUIDWebSpeed) this.lstUID.get(i)).getOld() <= 0) {
                    rmvLst.add((HwCHRWifiUIDWebSpeed) this.lstUID.get(i));
                }
            }
            listSize = rmvLst.size();
            for (i = 0; i < listSize; i++) {
                this.lstUID.remove(rmvLst.get(i));
            }
            return result;
        }
    }

    private void monitorCpuUsage() {
        List<String> file = HwCHRWifiFile.getFileResult(CPU_FILE_NAME);
        if (file.size() >= 1 && !((String) file.get(0)).equals("")) {
            this.mCPUUsage.parserValue((String) file.get(0));
        }
    }

    private void monitorMemUsage() {
        List<String> file = HwCHRWifiFile.getFileResult(MEMINFO_FILE_NAME);
        if (file.size() >= 1 && !((String) file.get(0)).equals("")) {
            this.mMemUsage.parse_file(file);
        }
    }

    private void loge(String str) {
        Log.e(TAG, str);
    }

    public String toString() {
        return "WebMonitor [mNetWorkIsSuck=" + this.mNetWorkSuckTimes + " mUIDIsSuck=" + this.mUIDIsSuck + "]";
    }
}
