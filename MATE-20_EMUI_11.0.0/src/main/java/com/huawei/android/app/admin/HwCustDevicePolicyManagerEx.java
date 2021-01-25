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
    private static final int TRANSACTION_GET_ALLOW_SIMPLE_PASSWORD = 7002;
    private static final int TRANSACTION_SAVE_CURRENT_PWD_STATUS = 7003;
    private static final int TRANSACTION_SET_ALLOW_SIMPLE_PASSWORD = 7001;

    public static void setAllowSimplePassword(ComponentName who, boolean mode) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                data.writeInterfaceToken(DESCRIPTOR);
                int i = 1;
                if (who != null) {
                    data.writeInt(1);
                    who.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                if (!mode) {
                    i = 0;
                }
                data.writeInt(i);
                data.writeInt(UserHandle.myUserId());
                binder.transact(TRANSACTION_SET_ALLOW_SIMPLE_PASSWORD, data, reply, 0);
                reply.readException();
            } else {
                Log.e(TAG, "Call remote setAllowSimplePassword failed, can't get DEVICE_POLICY_SERVICE");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "setAllowSimplePassword remote failed!");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public static boolean getAllowSimplePassword(ComponentName who) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result = false;
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                data.writeInterfaceToken(DESCRIPTOR);
                boolean z = true;
                if (who != null) {
                    data.writeInt(1);
                    who.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                data.writeInt(UserHandle.myUserId());
                binder.transact(TRANSACTION_GET_ALLOW_SIMPLE_PASSWORD, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 0) {
                    z = false;
                }
                result = z;
            } else {
                Log.e(TAG, "Call remote getAllowSimplePassword failed, can't get DEVICE_POLICY_SERVICE");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getAllowSimplePassword remote failed!");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public static void saveCurrentPwdStatus(boolean isCurrentPwdSimple) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder binder = ServiceManager.getService("device_policy");
            if (binder != null) {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(isCurrentPwdSimple ? 1 : 0);
                data.writeInt(UserHandle.myUserId());
                binder.transact(TRANSACTION_SAVE_CURRENT_PWD_STATUS, data, reply, 0);
                reply.readException();
            } else {
                Log.e(TAG, "Call remote saveCurrentPwdStatus failed, can't get DEVICE_POLICY_SERVICE");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "saveCurrentPwdStatus remote failed!");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }
}
