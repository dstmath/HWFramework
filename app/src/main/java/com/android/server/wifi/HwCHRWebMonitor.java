package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
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
    private static String CPU_FILE_NAME = null;
    private static final int ERR_NETWORK_OK = 2;
    private static final int ERR_NETWORK_SLOWLY = 1;
    private static final boolean HWFLOW = false;
    private static String MEMINFO_FILE_NAME = null;
    private static final String TAG = "HwCHRWebMonitor";
    private static String TCP_FILE_NAME = null;
    private static final int UNMODIFY_COUNT = 20;
    private static final int WIFI_ACCESS_WEB_SLOW_RSSI = -78;
    private static final int WIFI_PACKETS_COUNT = 10;
    private static final double WIFI_PACKETS_RATE = 0.3d;
    private static HwCHRWebMonitor sWebMonitor;
    private List<HwCHRWifiUIDWebSpeed> lstUID;
    private HwCHRWifiCPUUsage mCPUUsage;
    private Context mContext;
    private HwCHRWifiDNS mDNS;
    private int mFailReason;
    private HwWiFiLogUtils mHwLogUtils;
    private HwCHRWifiLinkMonitor mLinkMonitor;
    private HwCHRWifiMemUsage mMemUsage;
    private long mNetNormalDuration;
    private long mNetSlowlyDuration;
    private int mNetWorkSuckTimes;
    private int mOnAppSuckTime;
    private HwCHRWifiPacketCnt mPktcnt;
    private long mStartNetNormalTime;
    private long mStartNetSlowlyTime;
    private HwCHRWebSpeed mStastics;
    private String mUIDIsSuck;
    private int mUnModifyCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwCHRWebMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwCHRWebMonitor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwCHRWebMonitor.<clinit>():void");
    }

    private HwCHRWebMonitor(Context context, WifiNative wifiNatie) {
        this.mStastics = HwCHRWebSpeed.getDefault();
        this.mNetWorkSuckTimes = 0;
        this.mUIDIsSuck = "";
        this.lstUID = new ArrayList();
        this.mFailReason = 0;
        this.mOnAppSuckTime = 0;
        this.mUnModifyCount = 0;
        this.mLinkMonitor = new HwCHRWifiLinkMonitor();
        this.mMemUsage = new HwCHRWifiMemUsage();
        this.mCPUUsage = new HwCHRWifiCPUUsage();
        this.mDNS = new HwCHRWifiDNS();
        this.mPktcnt = null;
        this.mHwLogUtils = null;
        this.mStartNetSlowlyTime = 0;
        this.mStartNetNormalTime = 0;
        this.mNetSlowlyDuration = 0;
        this.mNetNormalDuration = 0;
        this.mContext = context;
        this.mPktcnt = new HwCHRWifiPacketCnt(wifiNatie);
        this.mHwLogUtils = HwWiFiLogUtils.getDefault();
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
        for (int i = 0; i < lines.size(); i += ERR_NETWORK_SLOWLY) {
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
                HwWifiStatStoreImpl.getDefault().incrWebSpeedStatus(0, ERR_NETWORK_SLOWLY);
                HwWifiCHRStateManagerImpl wchm = HwWifiCHRStateManagerImpl.getDefaultImpl();
                wchm.setNetNormalTime(this.mNetNormalDuration / 1000);
                wchm.setNetSlowlyTime(this.mNetSlowlyDuration / 1000);
                this.mNetSlowlyDuration = 0;
                this.mNetNormalDuration = 0;
                wchm.updateSpeedInfo(this.mStastics.getSpeedInfo(), this.mUIDIsSuck, this.mNetWorkSuckTimes >= 3 ? ERR_NETWORK_SLOWLY : ERR_NETWORK_OK, this.mFailReason);
                wchm.setCHRCounters(this.mLinkMonitor.getCounterLst());
                ch.readHisiChipsetDebugInfo(aws);
                wchm.updateAccessWebException(MessageUtil.CMD_START_SCAN, "", aws);
                this.mHwLogUtils.firmwareLog(HWFLOW);
            } else {
                if (this.mOnAppSuckTime == ERR_NETWORK_SLOWLY) {
                    this.mHwLogUtils.firmwareLog(true);
                } else if (this.mOnAppSuckTime == 0) {
                    this.mHwLogUtils.firmwareLog(HWFLOW);
                }
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
            HwSelfCureEngine.getInstance().notifyTcpStatResults(rx, tx, this.mStastics.getCounterDetaByTab(HwCHRWifiSpeedBaseChecker.WEB_RESENDSEGS));
        }
    }

    private HwCHRWifiUIDWebSpeed getUIDWebByLine(String line) {
        for (int j = 0; j < this.lstUID.size(); j += ERR_NETWORK_SLOWLY) {
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
            this.mUnModifyCount += ERR_NETWORK_SLOWLY;
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
        if (rssi >= WIFI_ACCESS_WEB_SLOW_RSSI || rtt <= TCPIpqRtt.RTT_FINE_5 || rtt_packets >= 3) {
            int WifiPackets = pkt.getTxTotal();
            int badWifiPacket = pkt.getTxBad();
            double rate = 0.0d;
            if (WifiPackets > 0) {
                rate = ((double) badWifiPacket) / ((double) WifiPackets);
            }
            if (rate <= WIFI_PACKETS_RATE || WifiPackets < WIFI_PACKETS_COUNT || rtt <= TCPIpqRtt.RTT_FINE_5 || rtt_packets >= 3) {
                return HWFLOW;
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
        for (i = 0; i < this.lstUID.size(); i += ERR_NETWORK_SLOWLY) {
            ((HwCHRWifiUIDWebSpeed) this.lstUID.get(i)).old();
        }
        this.mPktcnt.fetchPktcntNative();
        loge(this.mPktcnt + " rssi=" + rssi);
        if (rxBytes >= 307200 || txBytes >= 307200 || (packets >= 5 && rtt <= 100)) {
            this.mOnAppSuckTime = 0;
            this.mUnModifyCount = 0;
            loge("Good NetWork Traffic , so mOnAppSuckTime, mUnModifyCount reset 0");
            return "";
        } else if (rxBytes >= 153600 || txBytes >= 153600 || ((packets >= 5 && rtt <= 200) || this.mUnModifyCount >= UNMODIFY_COUNT)) {
            incrNetBadCounter(-1);
            loge("RecoveredNetWork Traffic , so mOnAppSuckTime=" + this.mOnAppSuckTime + " modify -1");
            return "";
        } else if (rxBytes >= 102400 || txBytes >= 102400 || (packets >= 5 && rtt <= 300)) {
            loge("Unstable NetWork Traffic, so do nothing");
            return "";
        } else {
            long running_uid = getRunningProcUID();
            String cols = (String) lines.get(index);
            i = index + ERR_NETWORK_SLOWLY;
            while (i < lines.size() && !((String) lines.get(i)).startsWith("custom ip")) {
                HwCHRWifiUIDWebSpeed item = getUIDWebByLine((String) lines.get(i));
                item.parserValue(cols, (String) lines.get(i));
                item.checkSuckStatus();
                loge(item.toString());
                long item_uid = item.getUID();
                int suckTime = item.getSuckTimes();
                if (item_uid == running_uid) {
                    if (isUIDInSuck(this.mPktcnt, rssi, item)) {
                        incrNetBadCounter(ERR_NETWORK_SLOWLY);
                    } else if (suckTime > 0) {
                        result = item.toString();
                        this.mFailReason = item.getFailReason();
                        incrNetBadCounter(ERR_NETWORK_SLOWLY);
                    }
                    loge("item_uid= " + item_uid + " running_uid=" + running_uid + " suckTime=" + suckTime + " mOnAppSuckTime=" + this.mOnAppSuckTime + " mFailReason=" + this.mFailReason);
                } else {
                    i += ERR_NETWORK_SLOWLY;
                }
            }
            if (this.mOnAppSuckTime == 3 || this.mOnAppSuckTime == ERR_NETWORK_SLOWLY) {
                this.mLinkMonitor.runCounters();
                monitorCpuUsage();
                monitorMemUsage();
                this.mDNS.monitorDNS();
            }
            List<HwCHRWifiUIDWebSpeed> rmvLst = new ArrayList();
            for (i = 0; i < this.lstUID.size(); i += ERR_NETWORK_SLOWLY) {
                if (((HwCHRWifiUIDWebSpeed) this.lstUID.get(i)).getOld() <= 0) {
                    rmvLst.add((HwCHRWifiUIDWebSpeed) this.lstUID.get(i));
                }
            }
            for (i = 0; i < rmvLst.size(); i += ERR_NETWORK_SLOWLY) {
                this.lstUID.remove(rmvLst.get(i));
            }
            return result;
        }
    }

    private void monitorCpuUsage() {
        List<String> file = HwCHRWifiFile.getFileResult(CPU_FILE_NAME);
        if (file.size() >= ERR_NETWORK_SLOWLY && !((String) file.get(0)).equals("")) {
            this.mCPUUsage.parserValue((String) file.get(0));
        }
    }

    private void monitorMemUsage() {
        List<String> file = HwCHRWifiFile.getFileResult(MEMINFO_FILE_NAME);
        if (file.size() >= ERR_NETWORK_SLOWLY && !((String) file.get(0)).equals("")) {
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
