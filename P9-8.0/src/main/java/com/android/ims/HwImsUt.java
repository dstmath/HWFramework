package com.android.ims;

import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.ims.AbstractImsUt.ImsUtReference;
import com.android.ims.internal.IImsUt;
import com.android.internal.telephony.HuaweiTelephonyConfigs;

public class HwImsUt implements ImsUtReference {
    private static final int CODE_IS_SUPPORT_CFT = 2001;
    private static final int CODE_IS_UT_ENABLE = 2002;
    private static final int CODE_UPDATE_CALLBARRING_OPT = 2004;
    private static final int CODE_UPDATE_CFU_TIMER = 2003;
    private static final boolean DBG = true;
    private static final String DESCRIPTOR = "com.android.ims.internal.IImsUt";
    private static final String IMS_UT_SERVICE_NAME = "ims_ut";
    private static final String TAG = "HwImsUt";
    private static final boolean isHisiPlateform = HuaweiTelephonyConfigs.isHisiPlatform();
    private ImsUt mImsUt;
    private int mPhoneId = 0;
    private IImsUt miUt;

    public HwImsUt(ImsUt imsUt) {
        this.mImsUt = imsUt;
    }

    public HwImsUt(ImsUt imsUt, int phoneId) {
        this.mImsUt = imsUt;
        this.mPhoneId = phoneId;
        log("HwImsUt:imsUt = " + imsUt + ", mPhoneId = " + this.mPhoneId);
    }

    public HwImsUt(IImsUt iUt, ImsUt imsUt, int phoneId) {
        this.miUt = iUt;
        this.mImsUt = imsUt;
        this.mPhoneId = phoneId;
        log("HwImsUt:miUt=" + this.miUt + ",mImsUt = " + this.mImsUt + ", mPhoneId = " + this.mPhoneId);
    }

    public boolean isSupportCFT() {
        boolean z = DBG;
        log("isSupportCFT:isHisiPlateform i " + isHisiPlateform);
        if (isHisiPlateform) {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            IBinder b = ServiceManager.getService(IMS_UT_SERVICE_NAME);
            log("isSupportCFT");
            if (b != null) {
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(this.mPhoneId);
                    b.transact(CODE_IS_SUPPORT_CFT, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 1) {
                        z = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return z;
                } catch (RemoteException localRemoteException) {
                    localRemoteException.printStackTrace();
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
            log("isSupportCFT - can't get ims_ut service");
            _reply.recycle();
            _data.recycle();
            return false;
        } else if (this.miUt == null) {
            loge("The device is not Hisi plateform,but miUt is null");
            return false;
        } else {
            try {
                return this.miUt.isSupportCFT();
            } catch (RemoteException localRemoteException2) {
                localRemoteException2.printStackTrace();
                return false;
            }
        }
    }

    public boolean isUtEnable() {
        boolean z = DBG;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService(IMS_UT_SERVICE_NAME);
        log("isUtEnable");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(this.mPhoneId);
                b.transact(CODE_IS_UT_ENABLE, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 1) {
                    z = false;
                }
                _reply.recycle();
                _data.recycle();
                return z;
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        log("isUtEnable - can't get ims_ut service");
        _reply.recycle();
        _data.recycle();
        return false;
    }

    public void updateCallBarringOption(String password, int cbType, boolean enable, Message result, String[] barrList) {
        int i = 0;
        synchronized (this.mImsUt.mLockObj) {
            int id = -1;
            log("updateCallBarringOption:isHisiPlateform i " + isHisiPlateform);
            if (isHisiPlateform) {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                IBinder b = ServiceManager.getService(IMS_UT_SERVICE_NAME);
                if (b != null) {
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(this.mPhoneId);
                        _data.writeString(password);
                        _data.writeInt(cbType);
                        if (enable) {
                            i = 1;
                        }
                        _data.writeInt(i);
                        _data.writeStringArray(barrList);
                        b.transact(CODE_UPDATE_CALLBARRING_OPT, _data, _reply, 0);
                        _reply.readException();
                        id = _reply.readInt();
                    } catch (RemoteException localRemoteException) {
                        localRemoteException.printStackTrace();
                        this.mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                    } finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                } else {
                    log("updateCallBarringOption - can't get ims_ut service");
                }
                _reply.recycle();
                _data.recycle();
            } else if (this.miUt == null) {
                loge("The device is not Hisi plateform,but miUt is null");
                return;
            } else {
                try {
                    id = this.miUt.updateCallBarringOption(password, cbType, enable, barrList);
                } catch (RemoteException localRemoteException2) {
                    localRemoteException2.printStackTrace();
                    this.mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                }
            }
            if (id < 0) {
                this.mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                return;
            }
            this.mImsUt.mPendingCmds.put(Integer.valueOf(id), result);
        }
    }

    public void updateCallForwardUncondTimer(int startHour, int startMinute, int endHour, int endMinute, int action, int condition, String number, Message result) {
        log("updateCallForwardUncondTimer :: , action=" + action + ", condition=" + condition + ", startHour=" + startHour + ", startMinute=" + startMinute + ", endHour=" + endHour + ", endMinute=" + endMinute);
        synchronized (this.mImsUt.mLockObj) {
            int id = -1;
            log("updateCallForwardUncondTimer:isHisiPlateform i " + isHisiPlateform);
            if (isHisiPlateform) {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                IBinder b = ServiceManager.getService(IMS_UT_SERVICE_NAME);
                if (b != null) {
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(this.mPhoneId);
                        _data.writeInt(startHour);
                        _data.writeInt(startMinute);
                        _data.writeInt(endHour);
                        _data.writeInt(endMinute);
                        _data.writeInt(action);
                        _data.writeInt(condition);
                        _data.writeString(number);
                        b.transact(CODE_UPDATE_CFU_TIMER, _data, _reply, 0);
                        _reply.readException();
                        id = _reply.readInt();
                    } catch (RemoteException localRemoteException) {
                        localRemoteException.printStackTrace();
                        this.mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                    } finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                } else {
                    log("updateCallForwardUncondTimer - can't get ims_ut service");
                }
                _reply.recycle();
                _data.recycle();
            } else if (this.miUt == null) {
                loge("The device is not Hisi plateform,but miUt is null");
                return;
            } else {
                try {
                    id = this.miUt.updateCallForwardUncondTimer(startHour, startMinute, endHour, endMinute, action, condition, number);
                } catch (RemoteException localRemoteException2) {
                    localRemoteException2.printStackTrace();
                    this.mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                }
            }
            if (id < 0) {
                this.mImsUt.sendFailureReport(result, new ImsReasonInfo(802, 0));
                return;
            }
            this.mImsUt.mPendingCmds.put(Integer.valueOf(id), result);
        }
    }

    public Message popUtMessage(int id) {
        Message msg;
        Integer key = Integer.valueOf(id);
        synchronized (this.mImsUt.mLockObj) {
            msg = (Message) this.mImsUt.mPendingCmds.get(key);
            this.mImsUt.mPendingCmds.remove(key);
        }
        return msg;
    }

    private void log(String s) {
        Rlog.d("HwImsUt[" + this.mPhoneId + "]", s);
    }

    private void loge(String s) {
        Rlog.e("HwImsUt[" + this.mPhoneId + "]", s);
    }
}
