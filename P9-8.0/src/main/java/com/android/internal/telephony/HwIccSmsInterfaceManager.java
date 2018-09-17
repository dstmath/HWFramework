package com.android.internal.telephony;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.telephony.uicc.IccFileHandler;

public class HwIccSmsInterfaceManager extends IccSmsInterfaceManager {
    protected static final boolean DBG = true;
    private static final int EVENT_GET_SMSC_DONE = 101;
    private static final int EVENT_SET_SMSC_DONE = 102;
    protected static final String LOG_TAG = "HwIccSmsInterfaceManager";
    protected Handler mHwHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            AsyncResult ar;
            Object obj;
            switch (msg.what) {
                case HwIccSmsInterfaceManager.EVENT_GET_SMSC_DONE /*101*/:
                    ar = msg.obj;
                    if (ar.exception != null) {
                        HwIccSmsInterfaceManager.this.smscAddr = null;
                    } else {
                        HwIccSmsInterfaceManager.this.smscAddr = (String) ar.result;
                    }
                    obj = HwIccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
                        HwIccSmsInterfaceManager hwIccSmsInterfaceManager = HwIccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        hwIccSmsInterfaceManager.mSuccess = z;
                        HwIccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case HwIccSmsInterfaceManager.EVENT_SET_SMSC_DONE /*102*/:
                    ar = (AsyncResult) msg.obj;
                    obj = HwIccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
                        boolean z2;
                        HwIccSmsInterfaceManager hwIccSmsInterfaceManager2 = HwIccSmsInterfaceManager.this;
                        if (ar.exception == null) {
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                        hwIccSmsInterfaceManager2.mSuccess = z2;
                        HwIccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    private String smscAddr;

    public HwIccSmsInterfaceManager(Phone phone) {
        super(phone);
    }

    protected byte[] getNewbyte() {
        if (2 == this.mPhone.getPhoneType()) {
            return new byte[HwSubscriptionManager.SUB_INIT_STATE];
        }
        return new byte[176];
    }

    protected int getRecordLength() {
        if (2 == this.mPhone.getPhoneType()) {
            return HwSubscriptionManager.SUB_INIT_STATE;
        }
        return 176;
    }

    protected IccFileHandler getIccFileHandler() {
        return this.mPhone.getIccFileHandler();
    }

    public String getSmscAddr() {
        log("getSmscAddress()");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_GET_SMSC_DONE);
            if (getIccFileHandler() == null) {
                return null;
            }
            getIccFileHandler().getSmscAddress(response);
            boolean isWait = true;
            while (isWait) {
                try {
                    this.mLock.wait();
                    isWait = false;
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                    return this.smscAddr;
                }
            }
        }
    }

    public boolean setSmscAddr(String smscAddr) {
        log("setSmscAddr() ");
        this.mPhone.getContext().enforceCallingPermission("huawei.permission.SET_SMSC_ADDRESS", "Requires Set Smsc Address permission");
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHwHandler.obtainMessage(EVENT_SET_SMSC_DONE);
            if (getIccFileHandler() == null) {
                boolean z = this.mSuccess;
                return z;
            }
            getIccFileHandler().setSmscAddress(smscAddr, response);
            boolean isWait = true;
            while (isWait) {
                try {
                    this.mLock.wait();
                    isWait = false;
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                    return this.mSuccess;
                }
            }
        }
    }

    protected boolean isHwMmsUid(int uid) {
        Log.d("XXXXXX", "HwIccSmsInterfaceManager isHwMmsUid begin");
        String HWMMS_PKG = "com.huawei.message";
        int mmsUid = -1;
        try {
            mmsUid = this.mContext.getPackageManager().getPackageUid("com.huawei.message", UserHandle.getUserId(uid));
        } catch (NameNotFoundException e) {
        }
        return mmsUid == uid;
    }
}
