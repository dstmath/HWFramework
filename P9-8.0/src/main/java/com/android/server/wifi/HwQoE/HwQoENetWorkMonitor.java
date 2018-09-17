package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.RssiPacketCountInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.WifiStateMachine;

public class HwQoENetWorkMonitor {
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    private final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private boolean intiQualityData = false;
    private boolean isOTADataUpdate = false;
    private boolean isStartMonitor = false;
    private boolean isTCPDataUpdate = false;
    private boolean isUDPDataUpdate = true;
    private IHwQoEMonitorCallback mCallback;
    private Context mContext;
    private HwQoEJNIAdapter mHwQoEJNIAdapter;
    private int mLastDnsFailCount = 0;
    private long mLastOTATxBadPacket = 0;
    private long mLastOTATxGoodPacket = 0;
    private long mLastTcpRetransPacket = 0;
    private long mLastTcpRxByte = 0;
    private long mLastTcpRxPacket = 0;
    private long mLastTcpTxByte = 0;
    private long mLastTcpTxPacket = 0;
    private Handler mLocalHandler;
    private int mPeriodDnsFailCount = 0;
    private long mPeriodOTATxBadPacket = 0;
    private long mPeriodOTATxGoodPacket = 0;
    private int mPeriodTcpQuality = 0;
    private long mPeriodTcpRTT = 0;
    private long mPeriodTcpRTTPacket = 0;
    private int mPeriodTcpRTTWhen = 0;
    private long mPeriodTcpRetransPacket = 0;
    private long mPeriodTcpRxPacket = 0;
    private long mPeriodTcpRxSpeed = 0;
    private long mPeriodTcpTxPacket = 0;
    private long mPeriodTcpTxSpeed = 0;
    private int mPeriodTime;
    private WifiStateMachine mWifiStateMachine;
    private AsyncChannel mWsmChannel = new AsyncChannel();

    public HwQoENetWorkMonitor(Context context, WifiStateMachine wifiStateMachine, int periodTime, IHwQoEMonitorCallback callback) {
        HwQoEUtils.logE("HwQoENetWorkMonitor");
        this.mContext = context;
        this.mWifiStateMachine = wifiStateMachine;
        this.mPeriodTime = periodTime * 1000;
        this.mCallback = callback;
        initHandlerThread();
        this.mWsmChannel.connectSync(this.mContext, this.mLocalHandler, this.mWifiStateMachine.getMessenger());
        this.mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
    }

    public void startMonitor() {
        HwQoEUtils.logE("startMonitor isStartMonitor = " + this.isStartMonitor);
        if (!this.isStartMonitor) {
            this.isStartMonitor = true;
            this.intiQualityData = true;
            this.mLocalHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
        }
    }

    public void stopMonitor() {
        HwQoEUtils.logE("stopMonitor isStartMonitor = " + this.isStartMonitor);
        if (this.isStartMonitor) {
            this.isStartMonitor = false;
            this.mLocalHandler.removeMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
        }
    }

    public int getDnsFaileCount() {
        int currentDnsFailCount = 0;
        String dnsFailCountStr = SystemProperties.get(HwSelfCureUtils.DNS_MONITOR_FLAG, "0");
        if (dnsFailCountStr == null) {
            return 0;
        }
        try {
            currentDnsFailCount = Integer.parseInt(dnsFailCountStr);
        } catch (NumberFormatException e) {
            HwQoEUtils.logE("currentDnsFailCount  parseInt err:" + e);
        }
        return currentDnsFailCount;
    }

    private void initHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("HwQoENetWorkMonitor Thread");
        handlerThread.start();
        this.mLocalHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO /*105*/:
                        HwQoEUtils.logE("***************************period begin ***************************");
                        HwQoEUtils.logE("QOE_MSG_GET_QUALITY_INFO mPeriodTime = " + HwQoENetWorkMonitor.this.mPeriodTime);
                        HwQoENetWorkInfo result = HwQoENetWorkMonitor.this.mHwQoEJNIAdapter.queryPeriodData();
                        if (HwQoENetWorkMonitor.this.intiQualityData) {
                            HwQoENetWorkMonitor.this.initTcpQualityDate(result);
                        } else {
                            HwQoENetWorkMonitor.this.updateTcpQualityDate(result);
                        }
                        HwQoENetWorkMonitor.this.isTCPDataUpdate = true;
                        HwQoENetWorkMonitor.this.reportNetworkInfo();
                        break;
                    case 151573:
                        HwQoEUtils.logD("RSSI_PKTCNT_FETCH_SUCCEEDED");
                        RssiPacketCountInfo info = msg.obj;
                        if (HwQoENetWorkMonitor.this.intiQualityData) {
                            HwQoENetWorkMonitor.this.initOTAData(info);
                            HwQoENetWorkMonitor.this.initTranfficData();
                        } else {
                            HwQoENetWorkMonitor.this.updateOTAData(info);
                            HwQoENetWorkMonitor.this.updateTranfficData();
                        }
                        HwQoENetWorkMonitor.this.isOTADataUpdate = true;
                        HwQoENetWorkMonitor.this.reportNetworkInfo();
                        break;
                    case 151574:
                        HwQoEUtils.logD("RSSI_PKTCNT_FETCH_FAILED");
                        sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO, (long) HwQoENetWorkMonitor.this.mPeriodTime);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void initOTAData(RssiPacketCountInfo info) {
        this.mLastDnsFailCount = getDnsFaileCount();
        this.mLastOTATxGoodPacket = (long) info.txgood;
        this.mLastOTATxBadPacket = (long) info.txbad;
    }

    private void updateOTAData(RssiPacketCountInfo info) {
        this.mPeriodDnsFailCount = getDnsFaileCount() - this.mLastDnsFailCount;
        this.mLastDnsFailCount = getDnsFaileCount();
        this.mPeriodOTATxGoodPacket = ((long) info.txgood) - this.mLastOTATxGoodPacket;
        this.mLastOTATxGoodPacket = (long) info.txgood;
        this.mPeriodOTATxBadPacket = ((long) info.txbad) - this.mLastOTATxBadPacket;
        this.mLastOTATxBadPacket = (long) info.txbad;
    }

    private void initTranfficData() {
        this.mLastTcpTxByte = TrafficStats.getTxBytes(this.WLAN_IFACE);
        this.mLastTcpRxByte = TrafficStats.getRxBytes(this.WLAN_IFACE);
    }

    private void updateTranfficData() {
        if (this.mPeriodTime != 0) {
            this.mPeriodTcpTxSpeed = (TrafficStats.getTxBytes(this.WLAN_IFACE) - this.mLastTcpTxByte) / ((long) this.mPeriodTime);
            this.mLastTcpTxByte = TrafficStats.getTxBytes(this.WLAN_IFACE);
            this.mPeriodTcpRxSpeed = (TrafficStats.getRxBytes(this.WLAN_IFACE) - this.mLastTcpRxByte) / ((long) this.mPeriodTime);
            this.mLastTcpRxByte = TrafficStats.getRxBytes(this.WLAN_IFACE);
        }
    }

    private void initTcpQualityDate(HwQoENetWorkInfo result) {
        this.mLastTcpTxPacket = result.mTcpTxPacket;
        this.mLastTcpRxPacket = result.mTcpRxPacket;
        this.mLastTcpRetransPacket = result.mTcpRetransPacket;
    }

    private void updateTcpQualityDate(HwQoENetWorkInfo result) {
        HwQoEUtils.logE("updateTcpQualityDate");
        this.mPeriodTcpRTT = result.mTcpRTT;
        this.mPeriodTcpRTTPacket = result.mTcpRTTPacket;
        this.mPeriodTcpRTTWhen = result.mTcpRTTWhen;
        this.mPeriodTcpQuality = result.mTcpQuality;
        this.mPeriodTcpTxPacket = result.mTcpTxPacket - this.mLastTcpTxPacket;
        this.mLastTcpTxPacket = result.mTcpTxPacket;
        this.mPeriodTcpRxPacket = result.mTcpRxPacket - this.mLastTcpRxPacket;
        this.mLastTcpRxPacket = result.mTcpRxPacket;
        this.mPeriodTcpRetransPacket = result.mTcpRetransPacket - this.mLastTcpRetransPacket;
        this.mLastTcpRetransPacket = result.mTcpRetransPacket;
    }

    private void reportNetworkInfo() {
        HwQoEUtils.logE("reportNetworkInfo isOTADataUpdate = " + this.isOTADataUpdate + " isTCPDataUpdate = " + this.isTCPDataUpdate + " isUDPDataUpdate = " + this.isUDPDataUpdate);
        if (this.isTCPDataUpdate && this.isUDPDataUpdate) {
            HwQoENetWorkInfo info = new HwQoENetWorkInfo();
            info.mDnsFailCount = this.mPeriodDnsFailCount;
            info.mOTATxBadPacket = this.mPeriodOTATxBadPacket;
            info.mOTATxGoodPacket = this.mPeriodOTATxGoodPacket;
            info.mTcpTxSpeed = this.mPeriodTcpTxSpeed;
            info.mTcpRxSpeed = this.mPeriodTcpRxSpeed;
            info.mTcpRTT = this.mPeriodTcpRTT;
            info.mTcpRTTPacket = this.mPeriodTcpRTTPacket;
            info.mTcpRTTWhen = this.mPeriodTcpRTTWhen;
            info.mTcpRxPacket = this.mPeriodTcpRxPacket;
            info.mTcpTxPacket = this.mPeriodTcpTxPacket;
            info.mTcpRetransPacket = this.mPeriodTcpRetransPacket;
            info.mTcpQuality = this.mPeriodTcpQuality;
            if (this.intiQualityData) {
                this.intiQualityData = false;
            } else {
                this.mCallback.onNetworkInfoUpdate(info);
            }
            this.mLocalHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO, (long) this.mPeriodTime);
        }
    }

    public void release() {
        if (this.mLocalHandler != null) {
            Looper looper = this.mLocalHandler.getLooper();
            if (looper != null && looper != Looper.getMainLooper()) {
                looper.quitSafely();
                HwQoEUtils.logD("HwQoENetWorkMonitor$HandlerThread::Release");
            }
        }
    }
}
