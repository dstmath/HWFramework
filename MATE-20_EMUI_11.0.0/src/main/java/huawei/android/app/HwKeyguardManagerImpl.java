package huawei.android.app;

import android.app.HwKeyguardManager;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

public class HwKeyguardManagerImpl implements HwKeyguardManager {
    private static final int CODE_IS_LOCKSCREEND_DISABLED = 1000;
    private static HwKeyguardManager mInstance = new HwKeyguardManagerImpl();
    private IWindowManager mWm = WindowManagerGlobal.getWindowManagerService();

    public static HwKeyguardManager getDefault() {
        return mInstance;
    }

    public boolean isLockScreenDisabled(Context context) {
        IBinder windowManagerBinder;
        boolean isDisabled = false;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (!(this.mWm == null || (windowManagerBinder = this.mWm.asBinder()) == null)) {
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1001, data, reply, 0);
                reply.readException();
                boolean z = true;
                if (reply.readInt() != 1) {
                    z = false;
                }
                isDisabled = z;
                Log.d("HwKeyguardManagerImpl", "isLockScreenDisabled HwKeyguardManagerImpl isDisabled = " + isDisabled);
            }
            reply.recycle();
            data.recycle();
            return isDisabled;
        } catch (RemoteException e) {
            return false;
        }
    }
}
