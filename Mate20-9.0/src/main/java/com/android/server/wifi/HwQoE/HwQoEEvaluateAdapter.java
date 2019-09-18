package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.HwQoE.HwQoEQualityInfo;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.wifipro.WifiproBqeUtils;
import java.util.ArrayList;
import java.util.List;

public class HwQoEEvaluateAdapter {
    private static final int BQE_REQUEST_EXPIRE = 7000;
    private static int OTA_RTT_CHECK_TIME = 3;
    private static HwQoEEvaluateAdapter mHwQoEEvaluateAdapter = null;
    /* access modifiers changed from: private */
    public boolean intiQualityData = false;
    private boolean isEvaluating = false;
    /* access modifiers changed from: private */
    public boolean isOTADataUpdate = false;
    /* access modifiers changed from: private */
    public boolean isOTARTTUpdate = false;
    /* access modifiers changed from: private */
    public boolean isTCPDataUpdate = false;
    /* access modifiers changed from: private */
    public int lastTxBad = 0;
    /* access modifiers changed from: private */
    public int lastTxGood = 0;
    private List<IHwQoECallback> mCallBackList = new ArrayList();
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mEvaluateHandler;
    private Object mLock = new Object();
    /* access modifiers changed from: private */
    public HwQoEQualityInfo mResult;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            HwQoEEvaluateAdapter.this.mEvaluateHandler.sendMessage(HwQoEEvaluateAdapter.this.mEvaluateHandler.obtainMessage(HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO, 0, 0));
        }
    };
    private WifiManager mWifiManager;
    private WifiStateMachine mWifiStateMachine;
    /* access modifiers changed from: private */
    public WifiproBqeUtils mWifiproBqeUtils;
    /* access modifiers changed from: private */
    public AsyncChannel mWsmChannel = new AsyncChannel();

    private HwQoEEvaluateAdapter(Context context, WifiStateMachine wifiStateMachine) {
        this.mContext = context;
        this.mWifiStateMachine = wifiStateMachine;
        initQoEEvaluateAdapter();
        this.mWifiproBqeUtils = WifiproBqeUtils.getInstance(this.mContext);
        this.mResult = new HwQoEQualityInfo();
        this.mWsmChannel.connectSync(this.mContext, this.mEvaluateHandler, this.mWifiStateMachine.getMessenger());
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public static synchronized HwQoEEvaluateAdapter getInstance(Context context, WifiStateMachine wifiStateMachine) {
        HwQoEEvaluateAdapter hwQoEEvaluateAdapter;
        synchronized (HwQoEEvaluateAdapter.class) {
            if (mHwQoEEvaluateAdapter == null) {
                mHwQoEEvaluateAdapter = new HwQoEEvaluateAdapter(context, wifiStateMachine);
            }
            hwQoEEvaluateAdapter = mHwQoEEvaluateAdapter;
        }
        return hwQoEEvaluateAdapter;
    }

    private void initQoEEvaluateAdapter() {
        HandlerThread evaluateThread = new HandlerThread("HwQoEAdapter evaluate Thread");
        evaluateThread.start();
        this.mEvaluateHandler = new Handler(evaluateThread.getLooper()) {
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 10) {
                    HwQoEUtils.logE("MSG_BQE_DETECTION_RESULT");
                    Bundle mData = msg.getData();
                    int rtt = mData.getInt("RTT");
                    int pkts = mData.getInt("RTT_PKTS");
                    long speed = mData.getLong("SPEED");
                    HwQoEUtils.logE("MSG_BQE_DETECTION_RESULT rtt = " + rtt + " pkts = " + pkts + " speed = " + speed);
                    HwQoEEvaluateAdapter.this.mWifiproBqeUtils.stopBqeService();
                    HwQoEEvaluateAdapter.this.mResult.mRtt = (long) rtt;
                    HwQoEEvaluateAdapter.this.mResult.mSpeed = speed;
                    HwQoEEvaluateAdapter.this.mWsmChannel.sendMessage(151572);
                    boolean unused = HwQoEEvaluateAdapter.this.isTCPDataUpdate = true;
                    removeMessages(HwQoEUtils.QOE_MSG_WIFI_EVALUATE_TIMEOUT);
                } else if (i == 106) {
                    boolean unused2 = HwQoEEvaluateAdapter.this.isOTARTTUpdate = true;
                    HwQoEEvaluateAdapter.this.mResult.mOtaRtt = (long) msg.arg1;
                    HwQoEEvaluateAdapter.this.reportEvaluateResult(true);
                } else if (i != 114) {
                    switch (i) {
                        case 151573:
                            HwQoEUtils.logE("RSSI_PKTCNT_FETCH_SUCCEEDED intiQualityData = " + HwQoEEvaluateAdapter.this.intiQualityData);
                            RssiPacketCountInfo info = (RssiPacketCountInfo) msg.obj;
                            if (HwQoEEvaluateAdapter.this.intiQualityData) {
                                int periodTxGood = info.txgood - HwQoEEvaluateAdapter.this.lastTxGood;
                                int periodTxBad = info.txbad - HwQoEEvaluateAdapter.this.lastTxBad;
                                int periodTotal = periodTxBad + periodTxGood;
                                HwQoEUtils.logD("RSSI_PKTCNT_FETCH_SUCCEEDED periodTxBad = " + periodTxBad + " periodTotal = " + periodTotal);
                                if (periodTotal > 0) {
                                    HwQoEEvaluateAdapter.this.mResult.mOtaLossRate = (periodTxBad * 100) / periodTotal;
                                }
                                boolean unused3 = HwQoEEvaluateAdapter.this.isOTADataUpdate = true;
                                HwQoEEvaluateAdapter.this.reportEvaluateResult(true);
                                break;
                            } else {
                                int unused4 = HwQoEEvaluateAdapter.this.lastTxGood = info.txgood;
                                int unused5 = HwQoEEvaluateAdapter.this.lastTxBad = info.txbad;
                                boolean unused6 = HwQoEEvaluateAdapter.this.intiQualityData = true;
                                break;
                            }
                        case 151574:
                            HwQoEEvaluateAdapter.this.reportEvaluateResult(false);
                            break;
                    }
                } else {
                    HwQoEUtils.logD("QOE_MSG_WIFI_EVALUATE_TIMEOUT");
                    boolean unused7 = HwQoEEvaluateAdapter.this.isTCPDataUpdate = true;
                    boolean unused8 = HwQoEEvaluateAdapter.this.isOTARTTUpdate = true;
                    boolean unused9 = HwQoEEvaluateAdapter.this.isOTADataUpdate = true;
                    HwQoEEvaluateAdapter.this.reportEvaluateResult(false);
                    HwQoEEvaluateAdapter.this.mWifiproBqeUtils.stopBqeService();
                }
                super.handleMessage(msg);
            }
        };
    }

    public boolean evaluateNetworkQuality(IHwQoECallback callback) {
        HwQoEUtils.logD("evaluateNetworkQuality");
        synchronized (this.mLock) {
            this.mCallBackList.add(callback);
        }
        if (!this.isEvaluating) {
            this.isEvaluating = true;
            this.intiQualityData = false;
            this.isOTARTTUpdate = false;
            this.isOTADataUpdate = false;
            this.isTCPDataUpdate = false;
            hwQoECheckLinked();
            this.mWsmChannel.sendMessage(151572);
            this.mWifiproBqeUtils.requestBqeOnce(HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT, this.mEvaluateHandler);
            this.mEvaluateHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_EVALUATE_TIMEOUT, 7000);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void reportEvaluateResult(boolean isSuccess) {
        HwQoEUtils.logD("reportEvaluateResult isOTADataUpdate = " + this.isOTADataUpdate + " isOTARTTUpdate = " + this.isOTARTTUpdate + " isTCPDataUpdate = " + this.isTCPDataUpdate);
        synchronized (this.mLock) {
            if (isSuccess) {
                try {
                    if (this.isOTADataUpdate && this.isOTARTTUpdate && this.isTCPDataUpdate) {
                        for (IHwQoECallback mCallback : this.mCallBackList) {
                            mCallback.onNetworkEvaluate(true, this.mResult);
                        }
                        this.mCallBackList.clear();
                        this.isEvaluating = false;
                    }
                } catch (RemoteException e) {
                    HwQoEUtils.logE("reportEvaluateResult is error " + e.toString());
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                for (IHwQoECallback mCallback2 : this.mCallBackList) {
                    try {
                        mCallback2.onNetworkEvaluate(false, this.mResult);
                    } catch (RemoteException e2) {
                        HwQoEUtils.logE("reportEvaluateResult is error " + e2.toString());
                    }
                }
                this.mCallBackList.clear();
                this.isEvaluating = false;
            }
        }
    }

    private void hwQoECheckLinked() {
        new Thread(this.mRunnable).start();
    }

    public String getWayIpAddress() {
        DhcpInfo di = this.mWifiManager.getDhcpInfo();
        if (di == null) {
            return null;
        }
        return long2ip((long) di.gateway);
    }

    private String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 255)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 255)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 255)));
        sb.append('.');
        sb.append(String.valueOf((int) (255 & (ip >> 24))));
        return sb.toString();
    }

    public boolean netWorkisReachable(String ipAddress, int timeout) {
        return false;
    }
}
