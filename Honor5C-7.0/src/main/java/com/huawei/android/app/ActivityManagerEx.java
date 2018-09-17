package com.huawei.android.app;

import android.app.ActivityManagerNative;
import android.os.Parcel;
import android.util.Log;

public class ActivityManagerEx {
    private static final String TAG = "ActivityManagerEx";

    public static boolean isClonedProcess(int pid) {
        boolean res = false;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManagerNative.getDefault().asBinder().transact(503, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
        } catch (Exception e) {
            Log.e(TAG, "isClonedProcess", e);
        }
        return res;
    }
}
