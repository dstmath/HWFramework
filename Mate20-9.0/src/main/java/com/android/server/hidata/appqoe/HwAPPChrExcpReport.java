package com.android.server.hidata.appqoe;

import android.util.IMonitor;
import com.android.server.hidata.channelqoe.HwChannelQoEManager;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwAPPChrExcpReport {
    public static final int EVENT_APP_CHR_STATICS = 909002046;
    public static final int EVENT_APP_INFO = 909009042;
    public static final int EVENT_APP_QOE_EXCEPTION = 909002047;
    public static final int EVENT_APP_QOE_INFO_STATIS = 909009044;
    public static final int EVENT_APP_SCENCE_INFO = 909009043;
    public static final int EVENT_CHAN_QOE_INFO_STATIS = 909009045;
    public static final int MAX_LIMITED_TIME_SPN = 86400000;
    public static final int MAX_REPORT_CNT = 5;
    public static final int MIN_REPORT_TIME_SPAN = 600000;
    private static final String TAG = "HiData_HwAPPChrExcpReport";
    private static HwAPPChrExcpReport mChrExcpReport = null;
    private long lastReportTime = 0;
    private HwAPPChrManager mHwAPPChrManager = null;
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger = null;
    private Object mLock = new Object();
    private HwAPKQoEQualityMonitor mQualityMonitor = null;
    private HwAPKQoEQualityMonitorCell mQualityMonitorCell = null;
    private int reportCnt = 0;

    private HwAPPChrExcpReport() {
    }

    protected static HwAPPChrExcpReport createHwAPPChrExcpReport() {
        if (mChrExcpReport == null) {
            mChrExcpReport = new HwAPPChrExcpReport();
        }
        return mChrExcpReport;
    }

    public static synchronized HwAPPChrExcpReport getInstance() {
        HwAPPChrExcpReport hwAPPChrExcpReport;
        synchronized (HwAPPChrExcpReport.class) {
            if (mChrExcpReport == null) {
                mChrExcpReport = new HwAPPChrExcpReport();
            }
            hwAPPChrExcpReport = mChrExcpReport;
        }
        return hwAPPChrExcpReport;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00ab, code lost:
        r3 = android.util.IMonitor.openEventStream(EVENT_APP_QOE_INFO_STATIS);
        r3.setParam("NETTYPE", r0.netType).setParam("RSSI", r0.rssi).setParam("RTT", r0.rtt).setParam("TXPACKET", r0.txPacket).setParam("TXBYTE", r0.txByte).setParam("RXPACKET", r0.rxPacket).setParam("RXBYTE", r0.rxByte).setParam("RSPACKET", r0.rsPacket).setParam("PARA1", r0.para1).setParam("PARA2", r0.para2).setParam("PARA3", r0.para3).setParam("PARA4", r0.para4);
        r4 = android.util.IMonitor.openEventStream(EVENT_CHAN_QOE_INFO_STATIS);
        r4.setParam("NETTYPE", r1).setParam("RTT", r2.getRttBef()).setParam("DLTPT", r2.getTupBef()).setParam("SIGPWR", r2.getPwr()).setParam("SIGSNR", r2.getSnr()).setParam("SIGQUAL", r2.getQual()).setParam("SIGLOAD", r2.getLoad());
        com.android.server.hidata.appqoe.HwAPPQoEUtils.logD(TAG, "mChlInfoStream:" + r2.getRttBef() + "," + r2.getPwr() + "," + r2.getSnr() + "," + r2.getQual() + "," + r2.getLoad());
        r5 = android.util.IMonitor.openEventStream(EVENT_APP_QOE_EXCEPTION);
        r5.setParam("EVENTTYPE", r10).setParam("SCENCE", r11).setParam("APPINFO", r3).setParam("CHINFO", r4);
        android.util.IMonitor.sendEvent(r5);
        android.util.IMonitor.closeEventStream(r5);
        android.util.IMonitor.closeEventStream(r4);
        android.util.IMonitor.closeEventStream(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x01cd, code lost:
        return;
     */
    public void reportAPPQoExcpInfo(int excpType, int appScenceId) {
        HwAPPChrExcpInfo tempExcpInfo = null;
        int netType = -1;
        HwChannelQoEManager.HistoryMseasureInfo tempChlInfo = null;
        synchronized (this.mLock) {
            HwChannelQoEManager mChannelQoEManager = HwChannelQoEManager.getInstance();
            if (!(this.mQualityMonitor == null || this.mQualityMonitorCell == null)) {
                if (mChannelQoEManager != null) {
                    if (appScenceId >= 0) {
                        this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
                        this.mHwAPPChrManager = HwAPPChrManager.getInstance();
                        HwAPPQoEAPKConfig config = this.mHwAPPQoEResourceManger.getAPKScenceConfig(appScenceId);
                        if (isReportPermmitted()) {
                            HwAPPQoEUtils.logD(TAG, "reportAPPQoExcpInfo:exception type is:" + excpType);
                            switch (excpType) {
                                case 1:
                                    netType = 800;
                                    tempChlInfo = new HwChannelQoEManager.HistoryMseasureInfo();
                                    tempExcpInfo = this.mQualityMonitor.getAPPQoEInfo();
                                    if (config != null) {
                                        this.mHwAPPChrManager.updateStatisInfo(config.mAppId, appScenceId, 19);
                                        break;
                                    }
                                    break;
                                case 2:
                                    netType = 801;
                                    tempChlInfo = mChannelQoEManager.getHistoryMseasureInfo(801);
                                    tempExcpInfo = new HwAPPChrExcpInfo();
                                    if (config != null) {
                                        this.mHwAPPChrManager.updateStatisInfo(config.mAppId, appScenceId, 18);
                                        break;
                                    }
                                    break;
                                case 3:
                                    netType = 801;
                                    tempChlInfo = mChannelQoEManager.getHistoryMseasureInfo(801);
                                    tempExcpInfo = new HwAPPChrExcpInfo();
                                    break;
                            }
                            if (tempExcpInfo != null) {
                                if (tempChlInfo != null) {
                                    this.lastReportTime = System.currentTimeMillis();
                                    if (this.reportCnt >= 5) {
                                        this.reportCnt = 1;
                                    } else {
                                        this.reportCnt++;
                                    }
                                }
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
            HwAPPQoEUtils.logD(TAG, "reportAPPQoExcpInfo , invalid input");
        }
    }

    public boolean isReportPermmitted() {
        long curTime = System.currentTimeMillis();
        boolean result = false;
        synchronized (this.mLock) {
            if (this.reportCnt >= 5) {
                if (curTime - this.lastReportTime > 86400000) {
                    result = true;
                }
            } else if (curTime - this.lastReportTime > AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME) {
                result = true;
            }
        }
        HwAPPQoEUtils.logD(TAG, "report permmition:" + result);
        return result;
    }

    public void setMonitorInstance(HwAPKQoEQualityMonitor tempQualityMonitor, HwAPKQoEQualityMonitorCell tempQualityMonitorCell) {
        synchronized (this.mLock) {
            this.mQualityMonitor = tempQualityMonitor;
            this.mQualityMonitorCell = tempQualityMonitorCell;
            HwAPPQoEUtils.logD(TAG, "Init MonitorInstance.");
        }
    }

    public boolean reportStaticsInfo(List<HwAPPChrStatisInfo> infoList) {
        boolean uploadResult;
        boolean isSaved;
        boolean uploadResult2 = false;
        int apkNum = 0;
        HwAPPQoEManager mHwAPPQoEManager = HwAPPQoEManager.getInstance();
        boolean state = false;
        List<Integer> appIdList = new ArrayList<>();
        if (mHwAPPQoEManager != null) {
            state = mHwAPPQoEManager.getHidataState();
        }
        IMonitor.EventStream staticsInfo = IMonitor.openEventStream(909002046);
        Iterator<HwAPPChrStatisInfo> it = infoList.iterator();
        while (true) {
            if (!it.hasNext()) {
                uploadResult = uploadResult2;
                break;
            }
            HwAPPChrStatisInfo result = it.next();
            int appid = result.appId;
            boolean isSaved2 = false;
            for (Integer intValue : appIdList) {
                if (appid == intValue.intValue()) {
                    isSaved2 = true;
                }
            }
            if (!isSaved2) {
                appIdList.add(Integer.valueOf(appid));
                IMonitor.EventStream appInfo = IMonitor.openEventStream(EVENT_APP_INFO);
                appInfo.setParam("APKID", result.appId);
                appInfo.setParam("STATE", Boolean.valueOf(state));
                int scenceNum = 0;
                Iterator<HwAPPChrStatisInfo> it2 = infoList.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        isSaved = isSaved2;
                        uploadResult = uploadResult2;
                        break;
                    }
                    HwAPPChrStatisInfo info = it2.next();
                    if (info.appId == appid) {
                        IMonitor.EventStream scenceInfo = IMonitor.openEventStream(EVENT_APP_SCENCE_INFO);
                        isSaved = isSaved2;
                        uploadResult = uploadResult2;
                        scenceInfo.setParam("SCENCEID", info.scenceId).setParam("WIFISTARTNUM", info.wifiStartNum).setParam("CELLSTARTNUM", info.cellStartNum).setParam("WIFISTALLNUM", info.wifiStallNum).setParam("CELLSTALLNUM", info.cellStallNum).setParam("WIFISPNUM", info.wifispNum).setParam("CELLSPNUM", info.cellspNum).setParam("RN1NUM", info.rn1Num).setParam("RN2NUM", info.rn2Num).setParam("RN3NUM", info.rn3Num).setParam("RN4NUM", info.rn4Num).setParam("CHFNUM", info.chfNum).setParam("MPFNUM", info.mpfNum).setParam("MPSNUM", info.mpsNum).setParam("AFGNUM", info.afgNum).setParam("AFBNUM", info.afbNum).setParam("TRFFIC", info.trffic).setParam("INKQINUM", info.inKQINum).setParam("OVERKQINUM", info.overKQINum).setParam("CLOSECELLNUM", info.closeCellNum).setParam("CLOSEWIFINUM", info.closeWiFiNum).setParam("STARTHICNUM", info.startHicNum).setParam("HICSNUM", info.hicsNum);
                        appInfo.fillArrayParam("SCENCEINFO", scenceInfo);
                        IMonitor.closeEventStream(scenceInfo);
                        scenceNum++;
                        if (scenceNum >= 5) {
                            break;
                        }
                    } else {
                        isSaved = isSaved2;
                        uploadResult = uploadResult2;
                    }
                    isSaved2 = isSaved;
                    uploadResult2 = uploadResult;
                }
                HwAPPQoEUtils.logD(TAG, "scenceNum = " + scenceNum);
                staticsInfo.fillArrayParam("APKQOEINFO", appInfo);
                IMonitor.closeEventStream(appInfo);
                apkNum++;
                if (apkNum >= 10) {
                    boolean z = isSaved;
                    break;
                }
                boolean z2 = isSaved;
                uploadResult2 = uploadResult;
            }
        }
        HwAPPQoEUtils.logD(TAG, "apkNum = " + apkNum);
        if (apkNum > 0) {
            IMonitor.sendEvent(staticsInfo);
            uploadResult = true;
        }
        IMonitor.closeEventStream(staticsInfo);
        return uploadResult;
    }
}
