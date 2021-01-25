package android.os.storage;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IHwStorageManager;
import android.os.storage.IStorageManager;
import android.util.Log;
import android.util.Singleton;

public class HwStorageManager {
    private static final Singleton<IHwStorageManager> IStorageManagerSingleton = new Singleton<IHwStorageManager>() {
        /* class android.os.storage.HwStorageManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwStorageManager create() {
            try {
                IStorageManager sms = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                if (sms == null) {
                    return null;
                }
                return IHwStorageManager.Stub.asInterface(sms.getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwStorageManager.TAG, "IHwStorageManager create() fail: " + e.getMessage());
                return null;
            }
        }
    };
    private static final String TAG = "HwStorageManager";

    public static IHwStorageManager getService() {
        return IStorageManagerSingleton.get();
    }
}
