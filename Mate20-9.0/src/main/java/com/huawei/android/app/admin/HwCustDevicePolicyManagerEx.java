package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;

public class HwCustDevicePolicyManagerEx {
    private static final String DESCRIPTOR = "android.app.admin.IDevicePolicyManager";
    private static final String TAG = "DevicePolicyManagerEx";
    private static int TRANSACTION_getAllowSimplePassword = 7002;
    private static int TRANSACTION_saveCurrentPwdStatus = 7003;
    private static int TRANSACTION_setAllowSimplePassword = 7001;

    public static void setAllowSimplePassword(ComponentName who, boolean mode) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(mode);
                _data.writeInt(UserHandle.myUserId());
                binder.transact(TRANSACTION_setAllowSimplePassword, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "Call remote setAllowSimplePassword failed, can't get DEVICE_POLICY_SERVICE");
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public static boolean getAllowSimplePassword(ComponentName who) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean _result = false;
        boolean _result2 = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (who != null) {
                    _data.writeInt(1);
                    who.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                _data.writeInt(UserHandle.myUserId());
                binder.transact(TRANSACTION_getAllowSimplePassword, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 0) {
                    _result = true;
                }
                _result2 = _result;
            } else {
                Log.e(TAG, "Call remote getAllowSimplePassword failed, can't get DEVICE_POLICY_SERVICE");
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result2;
    }

    public static void saveCurrentPwdStatus(boolean isCurrentPwdSimple) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(isCurrentPwdSimple);
                _data.writeInt(UserHandle.myUserId());
                binder.transact(TRANSACTION_saveCurrentPwdStatus, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "Call remote saveCurrentPwdStatus failed, can't get DEVICE_POLICY_SERVICE");
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }
}
