package android.rms.iaware;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class IForegroundAppTypeCallback extends Binder {
    private static final int TRANSACTION_ASYNC_REPORT_FG_APP_TYPE_WITH_CALLBACK = 1;
    private String DESCRIPTOR = "com.huawei.iaware.sdk.IForegroundAppTypeCallback";

    public abstract void reportForegroundAppType(int i, String str);

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != 1) {
            return super.onTransact(code, data, reply, flags);
        }
        data.enforceInterface(this.DESCRIPTOR);
        reportForegroundAppType(data.readInt(), data.readString());
        reply.writeNoException();
        return true;
    }
}
