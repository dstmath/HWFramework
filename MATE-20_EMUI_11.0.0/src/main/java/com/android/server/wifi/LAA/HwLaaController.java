package com.android.server.wifi.LAA;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwLaaController {
    private static String TAG = "LAA_HwLaaController";
    private static HwLaaController mHwLaaController;
    Object hwTelephonyManagerProxy;
    Class<?> hwTelephonyManagerProxyClass;
    private Context mContext;
    private HwLaaCellStatusObserver mHwLaaCellStatusObserver;
    private HwLaaContentAware mHwLaaContentAware;
    private Handler mHwLaaControllerHandler;
    private boolean mIsMobileDataEnabled;
    private SparseArray<Integer> mLaaContorlVoteRecords;
    private int mLastRequestCmd = -1;

    public static HwLaaController createHwLaaController(Context context) {
        if (mHwLaaController == null) {
            mHwLaaController = new HwLaaController(context);
            HwLaaUtils.logD(TAG, false, "createHwLaaController", new Object[0]);
        }
        return mHwLaaController;
    }

    public static HwLaaController getInstrance() {
        if (HwLaaUtils.isLaaPlusEnable()) {
            return mHwLaaController;
        }
        HwLaaUtils.logD(TAG, false, "HwLaaController getInstrance is null", new Object[0]);
        return null;
    }

    private HwLaaController(Context context) {
        this.mContext = context;
        try {
            this.hwTelephonyManagerProxyClass = Class.forName("android.telephony.HwTelephonyManager");
            this.hwTelephonyManagerProxy = this.hwTelephonyManagerProxyClass.newInstance();
        } catch (Exception e) {
            HwLaaUtils.logD(TAG, false, "init fail", new Object[0]);
        }
        initHwLaaContentAwareHandler();
        initialLaaContorlVoteRecords();
        this.mHwLaaContentAware = new HwLaaContentAware(this.mContext, this.mHwLaaControllerHandler);
        new HwLaaWifiStatusObserver(this.mContext, this.mHwLaaControllerHandler);
        this.mHwLaaCellStatusObserver = new HwLaaCellStatusObserver(this.mContext, this.mHwLaaControllerHandler);
    }

    public synchronized boolean setLAAEnabled(boolean enable, int type) {
        return requestSendLaaCmd(enable ? 1 : 0, type);
    }

    public synchronized boolean requestSendLaaCmd(int cmd, int type) {
        if (!HwLaaUtils.isLaaPlusEnable()) {
            return false;
        }
        HwLaaUtils.logD(TAG, false, "requestSendLaaCmd type: %{public}d, cmd:%{public}d", Integer.valueOf(type), Integer.valueOf(cmd));
        if (!requestIsLegal(cmd, type)) {
            HwLaaUtils.logD(TAG, false, "request is illegal !!", new Object[0]);
            return false;
        }
        this.mLastRequestCmd = cmd;
        this.mLaaContorlVoteRecords.put(type, Integer.valueOf(cmd));
        if (cmd == 0) {
            this.mLastRequestCmd = 0;
            sendLaaCmdToRil(this.mLastRequestCmd);
        } else if (isVoteRequestEnableLaa()) {
            this.mLastRequestCmd = 1;
            sendLaaCmdToRil(this.mLastRequestCmd);
        } else {
            HwLaaUtils.logD(TAG, false, "Can Not sendLaaCmdToRil ", new Object[0]);
            return false;
        }
        return true;
    }

    public synchronized int getLaaDetailedState() {
        if (!HwLaaUtils.isLaaPlusEnable()) {
            return -1;
        }
        return this.mHwLaaCellStatusObserver.getLaaDetailedState();
    }

    private void initialLaaContorlVoteRecords() {
        this.mLaaContorlVoteRecords = new SparseArray<>();
        this.mLaaContorlVoteRecords.append(1, 1);
        this.mLaaContorlVoteRecords.append(2, 1);
        this.mLaaContorlVoteRecords.append(3, 1);
        this.mLaaContorlVoteRecords.append(4, 1);
        this.mLaaContorlVoteRecords.append(5, 1);
    }

    private boolean requestIsLegal(int cmd, int type) {
        if (type > 5 || type < 1) {
            return false;
        }
        return cmd == 1 || cmd == 0;
    }

    private boolean isVoteRequestEnableLaa() {
        SparseArray<Integer> sparseArray = this.mLaaContorlVoteRecords;
        if (sparseArray == null || sparseArray.size() == 0) {
            HwLaaUtils.logD(TAG, false, " mLaaContorlVoteRecords is null,Can not EnableLaa", new Object[0]);
            return false;
        }
        int size = this.mLaaContorlVoteRecords.size();
        for (int i = 0; i < size; i++) {
            int key = this.mLaaContorlVoteRecords.keyAt(i);
            if (this.mLaaContorlVoteRecords.get(key).intValue() == 0) {
                HwLaaUtils.logD(TAG, false, "%{public}d opposes enabling LAA!", Integer.valueOf(key));
                return false;
            }
        }
        return true;
    }

    private void initHwLaaContentAwareHandler() {
        this.mHwLaaControllerHandler = new Handler() {
            /* class com.android.server.wifi.LAA.HwLaaController.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                boolean z = true;
                if (i == 1) {
                    HwLaaController.this.requestSendLaaCmd(msg.arg1, msg.arg2);
                } else if (i != 2) {
                    if (i != 3) {
                        if (i == 4) {
                            HwLaaController hwLaaController = HwLaaController.this;
                            hwLaaController.mIsMobileDataEnabled = hwLaaController.mHwLaaCellStatusObserver.getMobileDataEnabled();
                            if (!HwLaaController.this.mIsMobileDataEnabled) {
                                HwLaaController.this.mHwLaaContentAware.setLaaContentAwareEnabled(HwLaaController.this.mIsMobileDataEnabled);
                            }
                        }
                    } else if (msg.arg2 == 0) {
                        HwLaaContentAware hwLaaContentAware = HwLaaController.this.mHwLaaContentAware;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        hwLaaContentAware.setLaaContentAwareEnabled(z);
                    }
                } else if (HwLaaController.this.mLastRequestCmd != -1) {
                    HwLaaController hwLaaController2 = HwLaaController.this;
                    hwLaaController2.sendLaaCmdToRil(hwLaaController2.mLastRequestCmd);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLaaCmdToRil(int cmd) {
        HwLaaUtils.logD(TAG, false, "trysendLaaCmdToRil,cmd = %{public}d", Integer.valueOf(cmd));
        if (!this.mHwLaaCellStatusObserver.isPermitSendLaaCmd()) {
            HwLaaUtils.logW(TAG, false, "ServiceState is not permit send laa cmd", new Object[0]);
        } else if ((cmd == 1 || cmd == 0) && !sendLaaCmdToTelephony(cmd)) {
            HwLaaUtils.logW(TAG, false, "sendLaaCmdToRil is fail", new Object[0]);
        }
    }

    private boolean sendLaaCmdToTelephony(int cmd) {
        Class<?> cls = this.hwTelephonyManagerProxyClass;
        if (cls == null || this.hwTelephonyManagerProxy == null) {
            HwLaaUtils.logD(TAG, false, "[hwTelephonyManagerProxyClass == null]", new Object[0]);
            return false;
        }
        Method method = null;
        Object result = null;
        try {
            method = cls.getMethod("sendLaaCmd", Integer.TYPE, String.class, Message.class);
        } catch (NoSuchMethodException e1) {
            HwLaaUtils.logD(TAG, false, "NoSuchMethodException:%{public}s", e1.getMessage());
        }
        if (method != null) {
            try {
                result = method.invoke(this.hwTelephonyManagerProxy, Integer.valueOf(cmd), null, null);
            } catch (IllegalAccessException e) {
                HwLaaUtils.logD(TAG, false, "IllegalAccessException:%{public}s", e.getMessage());
            } catch (IllegalArgumentException e2) {
                HwLaaUtils.logD(TAG, false, "IllegalArgumentException:%{public}s", e2.getMessage());
            } catch (InvocationTargetException e3) {
                HwLaaUtils.logD(TAG, false, "InvocationTargetException:%{public}s", e3.getMessage());
            }
            if (result != null) {
                return ((Boolean) result).booleanValue();
            }
            HwLaaUtils.logD(TAG, false, "[HwTelephonyManager+]sendLaaCmd,result==null!", new Object[0]);
            return false;
        }
        HwLaaUtils.logD(TAG, false, "[HwTelephonyManager+]sendLaaCmd,method==null!", new Object[0]);
        return false;
    }
}
