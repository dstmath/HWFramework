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
    private boolean intiQualityData = false;
    private boolean isEvaluating = false;
    private boolean isOTADataUpdate = false;
    private boolean isOTARTTUpdate = false;
    private boolean isTCPDataUpdate = false;
    private int lastTxBad = 0;
    private int lastTxGood = 0;
    private List<IHwQoECallback> mCallBackList = new ArrayList();
    private Context mContext;
    private Handler mEvaluateHandler;
    private Object mLock = new Object();
    private HwQoEQualityInfo mResult;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            HwQoEEvaluateAdapter.this.mEvaluateHandler.sendMessage(HwQoEEvaluateAdapter.this.mEvaluateHandler.obtainMessage(HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO, 0, 0));
        }
    };
    private WifiManager mWifiManager;
    private WifiStateMachine mWifiStateMachine;
    private WifiproBqeUtils mWifiproBqeUtils;
    private AsyncChannel mWsmChannel = new AsyncChannel();

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
                switch (msg.what) {
                    case 10:
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
                        HwQoEEvaluateAdapter.this.isTCPDataUpdate = true;
                        removeMessages(HwQoEUtils.QOE_MSG_WIFI_EVALUATE_TIMEOUT);
                        break;
                    case HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO /*106*/:
                        HwQoEEvaluateAdapter.this.isOTARTTUpdate = true;
                        HwQoEEvaluateAdapter.this.mResult.mOtaRtt = (long) msg.arg1;
                        HwQoEEvaluateAdapter.this.reportEvaluateResult(true);
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_EVALUATE_TIMEOUT /*114*/:
                        HwQoEUtils.logD("QOE_MSG_WIFI_EVALUATE_TIMEOUT");
                        HwQoEEvaluateAdapter.this.isTCPDataUpdate = true;
                        HwQoEEvaluateAdapter.this.isOTARTTUpdate = true;
                        HwQoEEvaluateAdapter.this.isOTADataUpdate = true;
                        HwQoEEvaluateAdapter.this.reportEvaluateResult(false);
                        HwQoEEvaluateAdapter.this.mWifiproBqeUtils.stopBqeService();
                        break;
                    case 151573:
                        HwQoEUtils.logE("RSSI_PKTCNT_FETCH_SUCCEEDED intiQualityData = " + HwQoEEvaluateAdapter.this.intiQualityData);
                        RssiPacketCountInfo info = msg.obj;
                        if (!HwQoEEvaluateAdapter.this.intiQualityData) {
                            HwQoEEvaluateAdapter.this.lastTxGood = info.txgood;
                            HwQoEEvaluateAdapter.this.lastTxBad = info.txbad;
                            HwQoEEvaluateAdapter.this.intiQualityData = true;
                            break;
                        }
                        int periodTxBad = info.txbad - HwQoEEvaluateAdapter.this.lastTxBad;
                        int periodTotal = periodTxBad + (info.txgood - HwQoEEvaluateAdapter.this.lastTxGood);
                        HwQoEUtils.logD("RSSI_PKTCNT_FETCH_SUCCEEDED periodTxBad = " + periodTxBad + " periodTotal = " + periodTotal);
                        if (periodTotal > 0) {
                            HwQoEEvaluateAdapter.this.mResult.mOtaLossRate = (periodTxBad * 100) / periodTotal;
                        }
                        HwQoEEvaluateAdapter.this.isOTADataUpdate = true;
                        HwQoEEvaluateAdapter.this.reportEvaluateResult(true);
                        break;
                    case 151574:
                        HwQoEEvaluateAdapter.this.reportEvaluateResult(false);
                        break;
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

    private void reportEvaluateResult(boolean isSuccess) {
        HwQoEUtils.logD("reportEvaluateResult isOTADataUpdate = " + this.isOTADataUpdate + " isOTARTTUpdate = " + this.isOTARTTUpdate + " isTCPDataUpdate = " + this.isTCPDataUpdate);
        synchronized (this.mLock) {
            if (!isSuccess) {
                for (IHwQoECallback mCallback : this.mCallBackList) {
                    try {
                        mCallback.onNetworkEvaluate(false, this.mResult);
                    } catch (RemoteException e) {
                        HwQoEUtils.logE("reportEvaluateResult is error " + e.toString());
                    }
                }
                this.mCallBackList.clear();
                this.isEvaluating = false;
            } else if (this.isOTADataUpdate && this.isOTARTTUpdate && this.isTCPDataUpdate) {
                for (IHwQoECallback mCallback2 : this.mCallBackList) {
                    try {
                        mCallback2.onNetworkEvaluate(true, this.mResult);
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
        sb.append(String.valueOf((int) ((ip >> 24) & 255)));
        return sb.toString();
    }

    public boolean netWorkisReachable(String ipAddress, int timeout) {
        return false;
    }
}
