package com.huawei.hsm;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class MediaTransactWrapperEx {
    private static final String TAG = "MediaTransactWrapperEx";

    public static boolean registerMusicObserver(IHsmMusicWatch observer) {
        if (observer != null) {
            return operateMusicObserver(observer);
        }
        Log.e(TAG, "registerMusicObserver ->> register null observer not allowed.");
        return false;
    }

    public static boolean unregisterMusicObserver() {
        return operateMusicObserver(null);
    }

    private static boolean operateMusicObserver(IHsmMusicWatch observer) {
        boolean retVal = true;
        boolean register = true;
        if (observer == null) {
            register = false;
        }
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken("com.huawei.hsm.IHsmCoreService");
                data.writeInt(register ? 0 : 1);
                data.writeInt(Process.myPid());
                if (observer != null) {
                    data.writeStrongBinder(observer.asBinder());
                }
                b.transact(104, data, reply, 0);
                reply.readException();
                retVal = 1 == reply.readInt();
            }
            recycleParcel(data);
            recycleParcel(reply);
        } catch (RemoteException e) {
            Log.e(TAG, "operateMusicObserver transact catch remote exception: " + e.toString() + "when register: " + register);
            recycleParcel(null);
            recycleParcel(null);
        } catch (Exception e2) {
            Log.e(TAG, "operateMusicObserver transact catch exception: " + e2.toString() + "when register: " + register);
            recycleParcel(null);
            recycleParcel(null);
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
        return retVal;
    }

    private static synchronized IBinder getBinder() {
        IBinder service;
        synchronized (MediaTransactWrapperEx.class) {
            service = ServiceManager.getService("system.hsmcore");
        }
        return service;
    }

    private static void recycleParcel(Parcel p) {
        if (p != null) {
            p.recycle();
        }
    }
}
