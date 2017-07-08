package com.android.internal.telephony.imsphone;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;

public class HwImsPhoneMmiCode {
    private static final int CODE_IS_UNSUPPORT_MMI_CODE = 3001;
    private static final String DESCRIPTOR = "com.android.ims.internal.IImsConfig";
    private static final String TAG = "HwImsPhoneMmiCode";

    public static void isUnSupportMMICode(String mmiCode) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("ims_config");
        Rlog.d(TAG, "isUnSupportMMICode");
        boolean isUnSupport = false;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(mmiCode);
                b.transact(CODE_IS_UNSUPPORT_MMI_CODE, _data, _reply, 0);
                _reply.readException();
                isUnSupport = _reply.readInt() == 1;
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
        if (isUnSupport) {
            Rlog.d(TAG, "Not Support MMI Code=" + mmiCode);
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }
}
