package com.android.ims;

import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsReasonInfo;

public class HwImsUtExImpl implements IHwImsUtEx {
    private static final boolean DBG = true;
    private static final String TAG = "HwImsUtExImpl";
    private IHwImsUtManager imsUtManager;
    private int mPhoneId = 0;

    public HwImsUtExImpl(IHwImsUtManager hwImsUtManager, int phoneId) {
        this.imsUtManager = hwImsUtManager;
        this.mPhoneId = phoneId;
        log("HwImsUtExImpl:imsUtManager=" + this.imsUtManager + ", mPhoneId = " + this.mPhoneId);
    }

    public boolean isSupportCFT() {
        try {
            return this.imsUtManager.isSupportCFT(this.mPhoneId);
        } catch (RemoteException e) {
            loge("isSupportCFT exception");
            return false;
        }
    }

    public boolean isUtEnable() {
        log("isUtEnable");
        try {
            return this.imsUtManager.isUtEnable(this.mPhoneId);
        } catch (RemoteException e) {
            loge("isUtEnable exception");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x008e A[Catch:{ all -> 0x00a5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009a A[Catch:{ all -> 0x00a5 }] */
    public void updateCallForwardUncondTimer(int startHour, int startMinute, int endHour, int endMinute, int action, int condition, String number, Message result, ImsUt mImsUt) {
        Object obj;
        int i;
        int id;
        Message message = result;
        ImsUt imsUt = mImsUt;
        StringBuilder sb = new StringBuilder();
        sb.append("updateCallForwardUncondTimer :: , action=");
        int i2 = action;
        sb.append(i2);
        sb.append(", condition=");
        int i3 = condition;
        sb.append(i3);
        sb.append(", startHour=");
        int i4 = startHour;
        sb.append(i4);
        sb.append(", startMinute=");
        int i5 = startMinute;
        sb.append(i5);
        sb.append(", endHour=");
        int i6 = endHour;
        sb.append(i6);
        sb.append(", endMinute=");
        int i7 = endMinute;
        sb.append(i7);
        log(sb.toString());
        if (imsUt != null) {
            Object obj2 = imsUt.mLockObj;
            synchronized (obj2) {
                try {
                    int i8 = i4;
                    i = 0;
                    obj = obj2;
                    try {
                        id = this.imsUtManager.updateCallForwardUncondTimer(this.mPhoneId, i8, i5, i6, i7, i2, i3, number);
                    } catch (RemoteException e) {
                        try {
                            loge("updateCallForwardUncondTimer exception");
                            imsUt.sendFailureReport(message, new ImsReasonInfo(802, i));
                            id = -1;
                            if (id < 0) {
                            }
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (RemoteException e2) {
                    i = 0;
                    obj = obj2;
                    loge("updateCallForwardUncondTimer exception");
                    imsUt.sendFailureReport(message, new ImsReasonInfo(802, i));
                    id = -1;
                    if (id < 0) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    obj = obj2;
                    throw th;
                }
                if (id < 0) {
                    imsUt.sendFailureReport(message, new ImsReasonInfo(802, i));
                    return;
                }
                imsUt.mPendingCmds.put(Integer.valueOf(id), message);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0067 A[Catch:{ all -> 0x007e, all -> 0x008b }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0073 A[Catch:{ all -> 0x007e, all -> 0x008b }] */
    public void updateCallBarringOption(String password, int cbType, boolean enable, int serviceClass, Message result, String[] barrList, ImsUt mImsUt) {
        int id;
        int id2;
        int id3;
        Message message = result;
        ImsUt imsUt = mImsUt;
        if (imsUt != null) {
            synchronized (imsUt.mLockObj) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("updateCallBarringOption: password= ***, cbType= ");
                    int i = cbType;
                    try {
                        sb.append(i);
                        sb.append(", enable= ");
                        boolean z = enable;
                        try {
                            sb.append(z);
                            sb.append(", serviceclass = ");
                            int i2 = serviceClass;
                            sb.append(i2);
                            log(sb.toString());
                            try {
                                id3 = -1;
                                id = 0;
                                try {
                                    id2 = this.imsUtManager.updateCallBarringOption(this.mPhoneId, password, i, z, i2, barrList);
                                } catch (RemoteException e) {
                                    loge("updateCallBarringOption exception");
                                    imsUt.sendFailureReport(message, new ImsReasonInfo(802, id));
                                    id2 = id3;
                                    if (id2 < 0) {
                                    }
                                }
                            } catch (RemoteException e2) {
                                id3 = -1;
                                id = 0;
                                loge("updateCallBarringOption exception");
                                imsUt.sendFailureReport(message, new ImsReasonInfo(802, id));
                                id2 = id3;
                                if (id2 < 0) {
                                }
                            }
                            if (id2 < 0) {
                                imsUt.sendFailureReport(message, new ImsReasonInfo(802, id));
                            } else {
                                imsUt.mPendingCmds.put(Integer.valueOf(id2), message);
                            }
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        boolean z2 = enable;
                        int i3 = serviceClass;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    int i4 = cbType;
                    boolean z22 = enable;
                    int i32 = serviceClass;
                    throw th;
                }
            }
        }
    }

    public void queryCallForwardForServiceClass(int condition, String number, int serviceClass, Message result, ImsUt mImsUt) {
        log("queryCallForward :: mImsUt = " + mImsUt + ", condition=" + condition + ", serviceClass:" + serviceClass);
        if (mImsUt != null) {
            synchronized (mImsUt.mLockObj) {
                try {
                    int id = this.imsUtManager.queryCallForwardForServiceClass(this.mPhoneId, condition, number, serviceClass);
                    if (id < 0) {
                        mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                        return;
                    }
                    mImsUt.mPendingCmds.put(Integer.valueOf(id), result);
                } catch (RemoteException e) {
                    mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                }
            }
        }
    }

    public String getUtIMPUFromNetwork(ImsUt mImsUt) {
        if (mImsUt == null) {
            return null;
        }
        String impu = null;
        synchronized (mImsUt.mLockObj) {
            try {
                impu = this.imsUtManager.getUtIMPUFromNetwork(this.mPhoneId);
            } catch (RemoteException e) {
                mImsUt.sendFailureReport(null, new ImsReasonInfo(802, 0));
            }
        }
        return impu;
    }

    public void processECT(ImsUt mImsUt) {
        log("processECT :: mPhoneId=" + this.mPhoneId);
        if (mImsUt != null) {
            synchronized (mImsUt.mLockObj) {
                try {
                    this.imsUtManager.processECT(this.mPhoneId);
                } catch (RemoteException e) {
                    mImsUt.sendFailureReport(null, new ImsReasonInfo(802, 0));
                }
            }
        }
    }

    private void log(String s) {
        Rlog.d("HwImsUtExImpl[" + this.mPhoneId + "]", s);
    }

    private void loge(String s) {
        Rlog.e("HwImsUtExImpl[" + this.mPhoneId + "]", s);
    }
}
