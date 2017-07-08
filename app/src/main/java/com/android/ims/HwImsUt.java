package com.android.ims;

import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.ims.AbstractImsUt.ImsUtReference;

public class HwImsUt implements ImsUtReference {
    private static final int CODE_IS_SUPPORT_CFT = 2001;
    private static final int CODE_IS_UT_ENABLE = 2002;
    private static final int CODE_UPDATE_CALLBARRING_OPT = 2004;
    private static final int CODE_UPDATE_CFU_TIMER = 2003;
    private static final boolean DBG = true;
    private static final String DESCRIPTOR = "com.android.ims.internal.IImsUt";
    private static final String TAG = "HwImsUt";
    private ImsUt mImsUt;

    public HwImsUt(ImsUt imsUt) {
        this.mImsUt = imsUt;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isSupportCFT() {
        boolean z = DBG;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("ims_ut");
        log("isSupportCFT");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
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
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
            }
        } else {
            _reply.recycle();
            _data.recycle();
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isUtEnable() {
        boolean z = DBG;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("ims_ut");
        log("isUtEnable");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
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
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
            }
        } else {
            _reply.recycle();
            _data.recycle();
            return false;
        }
    }

    public void updateCallBarringOption(String password, int cbType, boolean enable, Message result, String[] barrList) {
        int i = 0;
        synchronized (this.mImsUt.mLockObj) {
            int id = -1;
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            IBinder b = ServiceManager.getService("ims_ut");
            if (b != null) {
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
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
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            _reply.recycle();
            _data.recycle();
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
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            IBinder b = ServiceManager.getService("ims_ut");
            if (b != null) {
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
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
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            _reply.recycle();
            _data.recycle();
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
        Rlog.d(TAG, s);
    }
}
