package com.android.ims;

import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsReasonInfo;

public class HwImsUtExImpl implements IHwImsUtEx {
    private static final String TAG = "HwImsUtExImpl";
    private IHwImsUtManager imsUtManager;
    private int mPhoneId = 0;

    public HwImsUtExImpl(IHwImsUtManager hwImsUtManager, int phoneId) {
        this.imsUtManager = hwImsUtManager;
        this.mPhoneId = phoneId;
        logi("HwImsUtExImpl:imsUtManager=" + this.imsUtManager + ", mPhoneId = " + this.mPhoneId);
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
        logi("isUtEnable");
        try {
            return this.imsUtManager.isUtEnable(this.mPhoneId);
        } catch (RemoteException e) {
            loge("isUtEnable exception");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0091 A[Catch:{ all -> 0x00a9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009d A[Catch:{ all -> 0x00a9 }] */
    public void updateCallForwardUncondTimer(int startHour, int startMinute, int endHour, int endMinute, int action, int condition, String number, Message result, ImsUt mImsUt) {
        Object obj;
        int i;
        logi("updateCallForwardUncondTimer :: , action=" + action + ", condition=" + condition + ", startHour=" + startHour + ", startMinute=" + startMinute + ", endHour=" + endHour + ", endMinute=" + endMinute);
        if (mImsUt != null) {
            Object obj2 = mImsUt.mLockObj;
            synchronized (obj2) {
                int id = -1;
                try {
                    i = 0;
                    obj = obj2;
                    try {
                        id = this.imsUtManager.updateCallForwardUncondTimer(this.mPhoneId, startHour, startMinute, endHour, endMinute, action, condition, number);
                    } catch (RemoteException e) {
                        try {
                            loge("updateCallForwardUncondTimer exception");
                            mImsUt.sendFailureReport(result, new ImsReasonInfo(802, i));
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
                    mImsUt.sendFailureReport(result, new ImsReasonInfo(802, i));
                    if (id < 0) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    obj = obj2;
                    throw th;
                }
                if (id < 0) {
                    mImsUt.sendFailureReport(result, new ImsReasonInfo(802, i));
                    return;
                }
                mImsUt.mPendingCmds.put(Integer.valueOf(id), result);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0076  */
    public void updateCallBarringOption(String password, int cbType, boolean enable, int serviceClass, Message result, String[] barrList, ImsUt mImsUt) {
        int id;
        int id2;
        int id3;
        if (mImsUt != null) {
            synchronized (mImsUt.mLockObj) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("updateCallBarringOption: password= ***, cbType= ");
                    try {
                        sb.append(cbType);
                        sb.append(", enable= ");
                        try {
                            sb.append(enable);
                            sb.append(", serviceclass = ");
                            try {
                                sb.append(serviceClass);
                                logi(sb.toString());
                                try {
                                    id3 = -1;
                                    id = 0;
                                    try {
                                        id2 = this.imsUtManager.updateCallBarringOption(this.mPhoneId, password, cbType, enable, serviceClass, barrList);
                                    } catch (RemoteException e) {
                                    }
                                } catch (RemoteException e2) {
                                    id3 = -1;
                                    id = 0;
                                    loge("updateCallBarringOption exception");
                                    mImsUt.sendFailureReport(result, new ImsReasonInfo(802, id));
                                    id2 = id3;
                                    if (id2 >= 0) {
                                    }
                                }
                                if (id2 >= 0) {
                                    mImsUt.sendFailureReport(result, new ImsReasonInfo(802, id));
                                } else {
                                    mImsUt.mPendingCmds.put(Integer.valueOf(id2), result);
                                }
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
        }
    }

    public void queryCallForwardForServiceClass(int condition, String number, int serviceClass, Message result, ImsUt mImsUt) {
        logi("queryCallForward :: mImsUt = " + mImsUt + ", condition=" + condition + ", serviceClass:" + serviceClass);
        if (mImsUt != null) {
            synchronized (mImsUt.mLockObj) {
                try {
                    int id = this.imsUtManager.queryCallForwardForServiceClass(this.mPhoneId, condition, number, serviceClass);
                    if (id < 0) {
                        mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                    } else {
                        mImsUt.mPendingCmds.put(Integer.valueOf(id), result);
                    }
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
                mImsUt.sendFailureReport((Message) null, new ImsReasonInfo(802, 0));
            }
        }
        return impu;
    }

    public void processECT(ImsUt mImsUt) {
        logi("processECT :: mPhoneId=" + this.mPhoneId);
        if (mImsUt != null) {
            synchronized (mImsUt.mLockObj) {
                try {
                    this.imsUtManager.processECT(this.mPhoneId);
                } catch (RemoteException e) {
                    mImsUt.sendFailureReport((Message) null, new ImsReasonInfo(802, 0));
                }
            }
        }
    }

    private void log(String s) {
        Rlog.d("HwImsUtExImpl[" + this.mPhoneId + "]", s);
    }

    private void logi(String s) {
        Rlog.i("HwImsUtExImpl[" + this.mPhoneId + "]", s);
    }

    private void loge(String s) {
        Rlog.e("HwImsUtExImpl[" + this.mPhoneId + "]", s);
    }
}
