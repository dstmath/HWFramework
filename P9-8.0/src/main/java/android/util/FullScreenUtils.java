package android.util;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.Parcel;
import android.os.RemoteException;

public class FullScreenUtils {
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String TAG = "FullScreenUtils";
    private static final int TRANSACTION_CODE_GET_MAX_ASPECT_RATIO = 1013;
    private static final int TRANSACTION_CODE_SET_MAX_ASPECT_RATIO = 1012;

    public static boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        boolean res = false;
        IPackageManager iPackageManager = AppGlobals.getPackageManager();
        if (iPackageManager != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
                data.writeString(packageName);
                data.writeFloat(ar);
                iPackageManager.asBinder().transact(1012, data, reply, 0);
                reply.readException();
                res = reply.readInt() != 0;
                reply.recycle();
                data.recycle();
            } catch (RemoteException e) {
                Log.e(TAG, "failed to set Application max aspect ratio");
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
