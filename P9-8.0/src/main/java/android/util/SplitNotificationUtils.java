package android.util;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.Parcel;
import android.os.RemoteException;

public class SplitNotificationUtils {
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String TAG = "SplitNotificationUtils";
    private static final int TRANSACTION_CODE_IS_NOTIFICATION_SPLIT = 1021;

    public static boolean isNotificationAddSplitButton(String pkgName) {
        boolean res = false;
        IPackageManager iPackageManager = AppGlobals.getPackageManager();
        if (iPackageManager != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
                data.writeString(pkgName);
                iPackageManager.asBinder().transact(1021, data, reply, 0);
                reply.readException();
                res = reply.readInt() != 0;
                reply.recycle();
                data.recycle();
            } catch (RemoteException e) {
                Log.e(TAG, "failed to get notification is split by RemoteException");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        return res;
    }
}
