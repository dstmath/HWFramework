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
            HwLaaUtils.logD(TAG, "createHwLaaController");
        }
        return mHwLaaController;
    }

    public static HwLaaController getInstrance() {
        if (HwLaaUtils.isLaaPlusEnable()) {
            return mHwLaaController;
        }
        HwLaaUtils.logD(TAG, "HwLaaController getInstrance is null");
        return null;
    }

    private HwLaaController(Context context) {
        this.mContext = context;
        try {
            this.hwTelephonyManagerProxyClass = Class.forName("android.telephony.HwTelephonyManager");
            this.hwTelephonyManagerProxy = this.hwTelephonyManagerProxyClass.newInstance();
        } catch (Exception e) {
            HwLaaUtils.logD(TAG, "Exception,e:" + e.getMessage());
        }
        initHwLaaContentAwareHandler();
        initialLaaContorlVoteRecords();
        this.mHwLaaContentAware = new HwLaaContentAware(this.mContext, this.mHwLaaControllerHandler);
        HwLaaWifiStatusObserver hwLaaWifiStatusObserver = new HwLaaWifiStatusObserver(this.mContext, this.mHwLaaControllerHandler);
        this.mHwLaaCellStatusObserver = new HwLaaCellStatusObserver(this.mContext, this.mHwLaaControllerHandler);
    }

    public synchronized boolean setLAAEnabled(boolean enable, int type) {
        return requestSendLaaCmd(enable ? 1 : 0, type);
    }

    /* JADX WARNING: Missing block: B:18:0x0055, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean requestSendLaaCmd(int cmd, int type) {
        if (!HwLaaUtils.isLaaPlusEnable()) {
            return false;
        }
        HwLaaUtils.logD(TAG, "requestSendLaaCmd type: " + type + " , cmd:" + cmd);
        if (requestIsLegal(cmd, type)) {
            this.mLastRequestCmd = cmd;
            this.mLaaContorlVoteRecords.put(type, Integer.valueOf(cmd));
            if (cmd == 0) {
                this.mLastRequestCmd = 0;
                sendLaaCmdToRil(this.mLastRequestCmd);
            } else if (isVoteRequestEnableLaa()) {
                this.mLastRequestCmd = 1;
                sendLaaCmdToRil(this.mLastRequestCmd);
            } else {
                HwLaaUtils.logD(TAG, "Can Not sendLaaCmdToRil ");
                return false;
            }
        }
        HwLaaUtils.logD(TAG, "request is illegal !!");
        return false;
    }

    public synchronized int getLaaDetailedState() {
        if (!HwLaaUtils.isLaaPlusEnable()) {
            return -1;
        }
        return this.mHwLaaCellStatusObserver.getLaaDetailedState();
    }

    private void initialLaaContorlVoteRecords() {
        this.mLaaContorlVoteRecords = new SparseArray();
        this.mLaaContorlVoteRecords.append(1, Integer.valueOf(1));
        this.mLaaContorlVoteRecords.append(2, Integer.valueOf(1));
        this.mLaaContorlVoteRecords.append(3, Integer.valueOf(1));
        this.mLaaContorlVoteRecords.append(4, Integer.valueOf(1));
        this.mLaaContorlVoteRecords.append(5, Integer.valueOf(1));
    }

    private boolean requestIsLegal(int cmd, int type) {
        if (type > 5 || type < 1) {
            return false;
        }
        return cmd == 1 || cmd == 0;
    }

    private boolean isVoteRequestEnableLaa() {
        if (this.mLaaContorlVoteRecords == null || this.mLaaContorlVoteRecords.size() == 0) {
            HwLaaUtils.logD(TAG, " mLaaContorlVoteRecords is null,Can not EnableLaa");
            return false;
        }
        int size = this.mLaaContorlVoteRecords.size();
        for (int i = 0; i < size; i++) {
            int key = this.mLaaContorlVoteRecords.keyAt(i);
            if (((Integer) this.mLaaContorlVoteRecords.get(key)).intValue() == 0) {
                HwLaaUtils.logD(TAG, key + " opposes enabling LAA!");
                return false;
            }
        }
        return true;
    }

    private void initHwLaaContentAwareHandler() {
        this.mHwLaaControllerHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = true;
                switch (msg.what) {
                    case 1:
                        HwLaaController.this.requestSendLaaCmd(msg.arg1, msg.arg2);
                        return;
                    case 2:
                        if (HwLaaController.this.mLastRequestCmd != -1) {
                            HwLaaController.this.sendLaaCmdToRil(HwLaaController.this.mLastRequestCmd);
                            return;
                        }
                        return;
                    case 3:
                        if (msg.arg2 == 0) {
                            HwLaaContentAware -get1 = HwLaaController.this.mHwLaaContentAware;
                            if (msg.arg1 != 1) {
                                z = false;
                            }
                            -get1.setLaaContentAwareEnabled(z);
                            return;
                        }
                        return;
                    case 4:
                        HwLaaController.this.mIsMobileDataEnabled = HwLaaController.this.mHwLaaCellStatusObserver.getMobileDataEnabled();
                        if (!HwLaaController.this.mIsMobileDataEnabled) {
                            HwLaaController.this.mHwLaaContentAware.setLaaContentAwareEnabled(HwLaaController.this.mIsMobileDataEnabled);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void sendLaaCmdToRil(int cmd) {
        HwLaaUtils.logD(TAG, "trysendLaaCmdToRil,cmd = " + cmd);
        if (this.mHwLaaCellStatusObserver.isPermitSendLaaCmd()) {
            if ((cmd == 1 || cmd == 0) && !sendLaaCmdToTelephony(cmd)) {
                HwLaaUtils.logW(TAG, "sendLaaCmdToRil is fail");
            }
            return;
        }
        HwLaaUtils.logW(TAG, "ServiceState is not permit send laa cmd");
    }

    private boolean sendLaaCmdToTelephony(int cmd) {
        if (this.hwTelephonyManagerProxyClass == null || this.hwTelephonyManagerProxy == null) {
            HwLaaUtils.logD(TAG, "[hwTelephonyManagerProxyClass == null]");
            return false;
        }
        Method method = null;
        Object result = null;
        try {
            method = this.hwTelephonyManagerProxyClass.getMethod("sendLaaCmd", new Class[]{Integer.TYPE, String.class, Message.class});
        } catch (NoSuchMethodException e1) {
            HwLaaUtils.logD(TAG, "NoSuchMethodException:" + e1.getMessage());
        }
        if (method != null) {
            try {
                result = method.invoke(this.hwTelephonyManagerProxy, new Object[]{Integer.valueOf(cmd), null, null});
            } catch (IllegalAccessException e) {
                HwLaaUtils.logD(TAG, "IllegalAccessException:" + e.getMessage());
            } catch (IllegalArgumentException e2) {
                HwLaaUtils.logD(TAG, "IllegalArgumentException:" + e2.getMessage());
            } catch (InvocationTargetException e3) {
                HwLaaUtils.logD(TAG, "InvocationTargetException:" + e3.getMessage());
            }
        } else {
            HwLaaUtils.logD(TAG, "[HwTelephonyManager+]sendLaaCmd,method==null!");
            return false;
        }
        if (result != null) {
            return ((Boolean) result).booleanValue();
        }
        HwLaaUtils.logD(TAG, "[HwTelephonyManager+]sendLaaCmd,result==null!");
        return false;
    }
}
