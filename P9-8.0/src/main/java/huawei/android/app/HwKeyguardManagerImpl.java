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
    private IWindowManager mWM = WindowManagerGlobal.getWindowManagerService();

    public static HwKeyguardManager getDefault() {
        return mInstance;
    }

    public boolean isLockScreenDisabled(Context context) {
        boolean result = false;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (this.mWM != null) {
                IBinder windowManagerBinder = this.mWM.asBinder();
                if (windowManagerBinder != null) {
                    data.writeInterfaceToken("android.view.IWindowManager");
                    windowManagerBinder.transact(1001, data, reply, 0);
                    reply.readException();
                    result = reply.readInt() == 1;
                    Log.d("HwKeyguardManagerImpl", "isLockScreenDisabled HwKeyguardManagerImpl result = " + result);
                }
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException e) {
            return false;
        }
    }
}
