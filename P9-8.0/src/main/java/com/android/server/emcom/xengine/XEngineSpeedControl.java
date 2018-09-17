package com.android.server.emcom.xengine;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.server.emcom.EmcomThread;
import com.android.server.emcom.daemon.DaemonCommand;
import com.android.server.emcom.networkevaluation.INetworkEvaluationCallback;
import com.android.server.emcom.networkevaluation.NetworkEvaluationEntry;
import com.android.server.emcom.networkevaluation.NetworkEvaluationResult;
import com.android.server.emcom.policy.HicomPolicyManager;
import com.android.server.emcom.policy.HicomPolicyManager.IRttUpdateListener;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class XEngineSpeedControl {
    private static final int MSG_NETWORK_QUALITY_CHANGE = 3;
    private static final int MSG_RTT_CHANGE = 4;
    private static final int MSG_SPEED_CTRL_FEEDBACK = 2;
    private static final int MSG_SPEED_CTRL_REQ = 1;
    private static final int MSG_START_FROM_POLICYMANAGER = 5;
    private static final int MSG_STOP_FROM_POLICYMANAGER = 6;
    private static final int START_PARAM_MAXGRADE = 2;
    private static final int START_PARAM_MINGRADE = 3;
    private static final int START_PARAM_NUMBER = 4;
    private static final int START_PARAM_TARGET = 1;
    private static final int START_PARAM_UID = 0;
    private static final String TAG = "XEngineSpeedControl";
    private static volatile XEngineSpeedControl s_Speeder;
    private Handler mHandler = new Handler(EmcomThread.getInstanceLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(XEngineSpeedControl.TAG, "receive speed control response");
                    return;
                case 2:
                    XEngineSpeedControl.this.updateSpeedCtrlStatus();
                    return;
                case 3:
                    XEngineSpeedControl.this.updateNetworkQuality(msg.arg1, msg.arg2);
                    return;
                case 4:
                    XEngineSpeedControl.this.updateAppExperienceStatus(msg.arg1);
                    return;
                case 5:
                    XEngineSpeedControl.this.startInMessageThread(msg.obj);
                    return;
                case 6:
                    XEngineSpeedControl.this.stopInMessageThread();
                    return;
                default:
                    Log.e(XEngineSpeedControl.TAG, "unknown message type.");
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
                Message msg = XEngineSpeedControl.this.mHandler.obtainMessage(3);
                msg.arg1 = type;
                msg.arg2 = quality;
                XEngineSpeedControl.this.mHandler.sendMessage(msg);
            }
        }
    };
    private IRttUpdateListener mRttListener = new IRttUpdateListener() {
        public void onRttChanged(int rtt) {
            Log.d(XEngineSpeedControl.TAG, "receive rtt change message: rtt " + rtt);
            if (rtt > 0) {
                Message msg = XEngineSpeedControl.this.mHandler.obtainMessage(4);
                msg.arg1 = rtt;
                XEngineSpeedControl.this.mHandler.sendMessage(msg);
            }
        }
    };

    private class Info {
        private static final int BASE_WIN_SIZE_LTE = 10000;
        private static final int BASE_WIN_SIZE_WIFI = 10000;
        private static final int CALLBACK_DELAY = 10000;
        private static final int MAX_GRADE = 50;
        private static final int MIN_GRADE = 1;
        public static final int STATUS_PREPARED = 1;
        public static final int STATUS_WAIT_FEEDBACK = 2;
        private static final int TARGE_COUNT = 10;
        private int mBaseSize;
        private int mCount;
        private int mCtrlGrade;
        private int mLastLatancy;
        private final int mMaxGrade;
        private final int mMinGrade;
        private int mStatus;
        private final int mTarge;
        private final int mUid;

        public Info(int uid, int targe, int maxGrade, int minGrade) {
            this.mUid = uid;
            this.mTarge = targe;
            if (maxGrade >= 50) {
                maxGrade = 50;
            }
            this.mMaxGrade = maxGrade;
            if (minGrade <= 1) {
                minGrade = 1;
            }
            this.mMinGrade = minGrade;
        }

        public void updateStatus(int status) {
            this.mStatus = status;
        }

        public void updateExperienceStatus(int rrt) {
            if (this.mStatus == 2) {
                Log.d(XEngineSpeedControl.TAG, "waitting feedback, return");
            } else if (rrt <= 0) {
                Log.i(XEngineSpeedControl.TAG, "latancy is error, ignore.");
            } else if (this.mLastLatancy == rrt) {
                Log.d(XEngineSpeedControl.TAG, "latancy is not change, ignore");
            } else {
                this.mLastLatancy = rrt;
                if (rrt <= this.mTarge) {
                    Log.d(XEngineSpeedControl.TAG, "latancy is small than targe.");
                    if (this.mCount > 10) {
                        this.mCtrlGrade--;
                        this.mCtrlGrade = this.mCtrlGrade > 0 ? this.mCtrlGrade : 0;
                        this.mCount = 0;
                    } else {
                        this.mCount++;
                    }
                    return;
                }
                this.mCount = 0;
                if (this.mCtrlGrade < this.mMaxGrade) {
                    this.mCtrlGrade++;
                    this.mBaseSize = 10000;
                    Log.d(XEngineSpeedControl.TAG, "update control grade: grade = " + this.mCtrlGrade);
                    sendSpeedCtrlReq(getWindowSize());
                    XEngineSpeedControl.this.mHandler.sendMessageDelayed(XEngineSpeedControl.this.mHandler.obtainMessage(2), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                    this.mStatus = 2;
                } else {
                    Log.d(XEngineSpeedControl.TAG, "control grade is already max, keep max grade control.");
                }
            }
        }

        private void sendSpeedCtrlReq(int winSize) {
            if (sizeIsValid(winSize)) {
                DaemonCommand.getInstance().exeSpeedCtrl(this.mUid, winSize, XEngineSpeedControl.this.mHandler.obtainMessage(1));
                Log.d(XEngineSpeedControl.TAG, "send speed control request, set size = " + winSize);
                return;
            }
            Log.e(XEngineSpeedControl.TAG, "win size is error!winSize = " + winSize);
        }

        private int getWindowSize() {
            if (this.mCtrlGrade == 0) {
                return 0;
            }
            return ((this.mMaxGrade - this.mCtrlGrade) + 1) * this.mBaseSize;
        }

        private boolean sizeIsValid(int size) {
            if (size != 0) {
                return size >= this.mMinGrade * this.mBaseSize && size <= this.mMaxGrade * this.mBaseSize;
            } else {
                return true;
            }
        }

        public void handleNetworkQualityChange(int type, int quality) {
            int lastGrade = this.mCtrlGrade;
            int lastBase = this.mBaseSize;
            switch (type) {
                case 0:
                    Log.d(XEngineSpeedControl.TAG, "network type is lte.");
                    this.mBaseSize = 10000;
                    break;
                case 1:
                    Log.d(XEngineSpeedControl.TAG, "network type is wifi.");
                    this.mBaseSize = 10000;
                    break;
                default:
                    Log.i(XEngineSpeedControl.TAG, " unknown network type.");
                    break;
            }
            switch (quality) {
                case 1:
                    Log.d(XEngineSpeedControl.TAG, "network quality is good.");
                    this.mCtrlGrade = this.mMinGrade;
                    break;
                case 2:
                    Log.d(XEngineSpeedControl.TAG, "network quality is normal.");
                    this.mCtrlGrade = (this.mMaxGrade + this.mMinGrade) / 2;
                    break;
                case 3:
                    Log.d(XEngineSpeedControl.TAG, "network quality is bad.");
                    this.mCtrlGrade = this.mMaxGrade;
                    break;
                default:
                    Log.i(XEngineSpeedControl.TAG, " unknown network quality.");
                    break;
            }
            if (lastGrade != this.mCtrlGrade || lastBase != this.mBaseSize) {
                sendSpeedCtrlReq(getWindowSize());
            }
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("uid=").append(this.mUid).append(", ").append("targe=").append(this.mTarge).append(", ").append("grade range from ").append(this.mMinGrade).append(" to ").append(this.mMaxGrade);
            return buffer.toString();
        }
    }

    private XEngineSpeedControl() {
    }

    public static XEngineSpeedControl getInstance() {
        if (s_Speeder == null) {
            synchronized (XEngineSpeedControl.class) {
                if (s_Speeder == null) {
                    s_Speeder = new XEngineSpeedControl();
                }
            }
        }
        return s_Speeder;
    }

    public void start(int uid, int targe, int maxGrade, int minGrade) {
        Message msg = this.mHandler.obtainMessage();
        msg.obj = new int[]{uid, targe, maxGrade, minGrade};
        msg.what = 5;
        this.mHandler.sendMessage(msg);
    }

    public void startInMessageThread(int[] param) {
        if (param == null || param.length != 4) {
            Log.e(TAG, "startInMessageThread. start param is error");
        } else if (this.mHasStarted) {
            Log.e(TAG, "cannot start: already started, please stop frist!");
        } else if (param[0] < 0) {
            Log.e(TAG, "cannot start: uid is invalid!");
        } else if (param[1] < 0) {
            Log.e(TAG, "cannot start: targe latancy is error!");
        } else if (param[2] <= 0 || param[3] <= 0 || param[2] <= param[3]) {
            Log.e(TAG, "cannot start: grade is error!");
        } else {
            this.mInfo = new Info(param[0], param[1], param[2], param[3]);
            Log.d(TAG, "XEngineSpeedControl start: mInfo " + this.mInfo);
            HicomPolicyManager policy = HicomPolicyManager.getInstance();
            if (policy != null) {
                policy.registerListener(this.mRttListener);
            }
            NetworkEvaluationEntry entry = NetworkEvaluationEntry.getInstance(null);
            if (entry != null) {
                entry.registerNetworkEvaluationCallback(this.mNetworkQualityCallback);
            }
            this.mHasStarted = true;
        }
    }

    public void stop() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 6;
        this.mHandler.sendMessage(msg);
    }

    public void stopInMessageThread() {
        if (this.mHasStarted) {
            Log.d(TAG, "XEngineSpeedControl stop: mInfo " + this.mInfo);
            HicomPolicyManager policy = HicomPolicyManager.getInstance();
            if (policy != null) {
                policy.unRegisterListener(this.mRttListener);
            }
            NetworkEvaluationEntry entry = NetworkEvaluationEntry.getInstance(null);
            if (entry != null) {
                entry.unRegisterNetworkEvaluationCallback(this.mNetworkQualityCallback);
            }
            if (this.mInfo != null) {
                this.mInfo.sendSpeedCtrlReq(0);
                this.mInfo = null;
            }
            this.mHasStarted = false;
            return;
        }
        Log.e(TAG, "XEngineSpeedControl not start yet!");
    }

    private void updateNetworkQuality(int type, int quality) {
        Log.d(TAG, "updateNetworkQuality: quality " + quality + ", type " + type);
        if (this.mInfo != null) {
            this.mInfo.handleNetworkQualityChange(type, quality);
        }
    }

    private void updateAppExperienceStatus(int rtt) {
        Log.d(TAG, "updateAppExperienceStatus:  rtt " + rtt);
        if (this.mInfo != null) {
            this.mInfo.updateExperienceStatus(rtt);
        }
    }

    private void updateSpeedCtrlStatus() {
        Log.d(TAG, "begin to observe the rrt again");
        if (this.mInfo != null) {
            this.mInfo.updateStatus(1);
        }
    }
}
