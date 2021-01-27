package huawei.android.app;

import android.common.IHwHarmonyServiceManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class HwHarmonyServiceManagerImpl implements IHwHarmonyServiceManager {
    private static final int BMS_SERVICE_NAME = 401;
    private static final int CHECK_SYSTEM_ABILITY_TRANSACTION = 2;
    private static final String DESCRIPTOR = "OHOS.AppExecFwk.IBundleMgr";
    private static final int GET_IS_SILENT_INSTALLED = 40;
    private static final Object LOCK = new Object();
    private static final String SAMGR_SERVICE_NAME = "SamgrService";
    private static final String TAG = "HwHarmonyServiceManagerImpl";
    private static HwHarmonyServiceManagerImpl instance = new HwHarmonyServiceManagerImpl();
    private IBinder bmsBinderProxy = null;
    private IBinder.DeathRecipient mDeathHandler = new IBinder.DeathRecipient() {
        /* class huawei.android.app.HwHarmonyServiceManagerImpl.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HwHarmonyServiceManagerImpl.LOCK) {
                HwHarmonyServiceManagerImpl.this.bmsBinderProxy = null;
            }
        }
    };

    private HwHarmonyServiceManagerImpl() {
    }

    public static HwHarmonyServiceManagerImpl getInstance() {
        return instance;
    }

    private IBinder getBmsProxy() {
        IBinder iBinder = this.bmsBinderProxy;
        if (iBinder != null) {
            return iBinder;
        }
        Log.i(TAG, "The binder of bms is null, waiting for initialization");
        IBinder binder = ServiceManager.getService(SAMGR_SERVICE_NAME);
        if (binder == null) {
            Log.e(TAG, "Cannot get the service of samgr");
            return null;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInt(BMS_SERVICE_NAME);
        try {
            binder.transact(2, data, reply, 0);
            this.bmsBinderProxy = reply.readStrongBinder();
            if (this.bmsBinderProxy != null) {
                this.bmsBinderProxy.linkToDeath(this.mDeathHandler, 0);
            }
            return this.bmsBinderProxy;
        } catch (RemoteException e) {
            Log.e(TAG, "Get the service of bms or call linkToDeath function throws exception:" + e);
            return null;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public boolean isSilentInstalled(String packageName) {
        synchronized (LOCK) {
            if (packageName != null) {
                if (!packageName.isEmpty()) {
                    IBinder bmsProxy = getBmsProxy();
                    if (bmsProxy == null) {
                        Log.e(TAG, "Cannot get the service of bms");
                        return false;
                    }
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeString(packageName);
                    try {
                        bmsProxy.transact(40, data, reply, 0);
                        return reply.readBoolean();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Perform the action with the service of bms throws exception:" + e);
                        return false;
                    } finally {
                        data.recycle();
                        reply.recycle();
                    }
                }
            }
            Log.e(TAG, "The package name is null or empty");
            return false;
        }
    }
}
