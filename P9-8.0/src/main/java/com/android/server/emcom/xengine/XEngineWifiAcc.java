package com.android.server.emcom.xengine;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.server.emcom.EmcomThread;
import com.android.server.emcom.daemon.DaemonCommand;
import com.android.server.emcom.networkevaluation.INetworkEvaluationCallback;
import com.android.server.emcom.networkevaluation.NetworkEvaluationEntry;
import com.android.server.emcom.networkevaluation.NetworkEvaluationResult;

public class XEngineWifiAcc {
    private static final int MSG_NETWORK_QUALITY_CHANGE = 3;
    private static final int MSG_START_FROM_POLICYMANAGER = 4;
    private static final int MSG_START_UDP_RETRAN = 1;
    private static final int MSG_STOP_FROM_POLICYMANAGER = 5;
    private static final int MSG_STOP_UDP_RETRAN = 2;
    private static final String TAG = "XEngineWifiAcc";
    private static volatile XEngineWifiAcc s_WifiAccController;
    private Handler mHandler = new Handler(EmcomThread.getInstanceLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(XEngineWifiAcc.TAG, "receive udp retran start rsp");
                    return;
                case 2:
                    Log.d(XEngineWifiAcc.TAG, "receive udp retran stop rsp");
                    return;
                case 3:
                    XEngineWifiAcc.this.updateNetworkQuality(msg.arg1, msg.arg2);
                    return;
                case 4:
                    XEngineWifiAcc.this.startInMessageThread(msg.arg1, msg.arg2);
                    return;
                case 5:
                    XEngineWifiAcc.this.stopInMessageThread(msg.arg1);
                    return;
                default:
                    Log.d(XEngineWifiAcc.TAG, "unkown message type");
                    return;
            }
        }
    };
    private volatile boolean mHasStarted;
    private Info mInfo;
    private INetworkEvaluationCallback mNetworkQualityCallback = new INetworkEvaluationCallback() {
        public void onEvaluationResultChanged(NetworkEvaluationResult result) {
            if (result != null) {
                int type = result.getNetworkType();
                int quality = result.getQuality();
                Message msg = XEngineWifiAcc.this.mHandler.obtainMessage(3);
                msg.arg1 = type;
                msg.arg2 = quality;
                XEngineWifiAcc.this.mHandler.sendMessage(msg);
            }
        }
    };

    private class Info {
        private final int mMode;
        private final int mUid;
        private boolean mWifiAccOn = false;

        public Info(int uid, int mode) {
            this.mUid = uid;
            this.mMode = mode;
        }

        private void sendStartUdpRetranReq(int uid) {
            DaemonCommand.getInstance().exeUdpAcc(uid, XEngineWifiAcc.this.mHandler.obtainMessage(1));
            Log.d(XEngineWifiAcc.TAG, "send start udp retran, uid = " + uid);
        }

        private void sendStopUdpRetranReq(int uid) {
            DaemonCommand.getInstance().exeUdpStop(uid, XEngineWifiAcc.this.mHandler.obtainMessage(2));
            Log.d(XEngineWifiAcc.TAG, "send stop udp retran, uid = " + uid);
        }

        public void handleNetworkQualityChange(int type, int quality) {
            switch (type) {
                case 1:
                    Log.d(XEngineWifiAcc.TAG, "network type is wifi.");
                    switch (quality) {
                        case 1:
                            Log.d(XEngineWifiAcc.TAG, "network quality is good.");
                            if (this.mWifiAccOn) {
                                sendStopUdpRetranReq(this.mUid);
                                this.mWifiAccOn = false;
                                break;
                            }
                            break;
                        case 2:
                            Log.d(XEngineWifiAcc.TAG, "network quality is normal.");
                            if (this.mWifiAccOn) {
                                sendStopUdpRetranReq(this.mUid);
                                this.mWifiAccOn = false;
                                break;
                            }
                            break;
                        case 3:
                            Log.d(XEngineWifiAcc.TAG, "network quality is bad.");
                            if (!this.mWifiAccOn) {
                                sendStartUdpRetranReq(this.mUid);
                                this.mWifiAccOn = true;
                                break;
                            }
                            break;
                        default:
                            Log.i(XEngineWifiAcc.TAG, " unknown network quality.");
                            break;
                    }
                    return;
                default:
                    Log.d(XEngineWifiAcc.TAG, "network type is not wifi.");
                    if (this.mWifiAccOn) {
                        sendStopUdpRetranReq(this.mUid);
                        this.mWifiAccOn = false;
                    }
                    return;
            }
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("uid=").append(this.mUid).append(", ").append("mode=").append(this.mMode);
            return buffer.toString();
        }
    }

    private XEngineWifiAcc() {
    }

    public static XEngineWifiAcc getInstance() {
        if (s_WifiAccController == null) {
            synchronized (XEngineWifiAcc.class) {
                if (s_WifiAccController == null) {
                    s_WifiAccController = new XEngineWifiAcc();
                }
            }
        }
        return s_WifiAccController;
    }

    public void start(int uid, int wifiMode) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 4;
        msg.arg1 = uid;
        msg.arg2 = wifiMode;
        this.mHandler.sendMessage(msg);
    }

    public void startInMessageThread(int uid, int wifiMode) {
        if (uid < 0) {
            Log.e(TAG, "start uid is error");
        } else if (this.mHasStarted) {
            Log.e(TAG, "already started, stop frist!");
        } else {
            this.mInfo = new Info(uid, wifiMode);
            Log.d(TAG, "XEngineWifiAcc start: mInfo " + this.mInfo);
            NetworkEvaluationEntry entry = NetworkEvaluationEntry.getInstance(null);
            if (entry != null) {
                entry.registerNetworkEvaluationCallback(this.mNetworkQualityCallback);
            }
            this.mHasStarted = true;
        }
    }

    public void stop(int uid) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 5;
        msg.arg1 = uid;
        this.mHandler.sendMessage(msg);
    }

    public void stopInMessageThread(int uid) {
        if (this.mHasStarted) {
            Log.d(TAG, "XEngineWifiAcc stop: mInfo " + this.mInfo);
            NetworkEvaluationEntry entry = NetworkEvaluationEntry.getInstance(null);
            if (entry != null) {
                entry.unRegisterNetworkEvaluationCallback(this.mNetworkQualityCallback);
            }
            if (this.mInfo != null) {
                this.mInfo.sendStopUdpRetranReq(this.mInfo.mUid);
                this.mInfo = null;
            }
            this.mHasStarted = false;
            return;
        }
        Log.e(TAG, "XEngineWifiAcc not start yet!");
    }

    public void updateNetworkQuality(int type, int quality) {
        Log.d(TAG, "handleNetworkQualityChange: quality " + quality + ", type " + type);
        if (this.mInfo != null) {
            this.mInfo.handleNetworkQualityChange(type, quality);
        }
    }
}
