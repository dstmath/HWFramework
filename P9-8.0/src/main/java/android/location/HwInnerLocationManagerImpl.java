package android.location;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

public class HwInnerLocationManagerImpl implements IHwInnerLocationManager {
    static final int CODE_GET_POWR_TYPE = 1001;
    static final int CODE_LOG_EVENT = 1002;
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    private static final String TAG = "HwInnerLocationManagerImpl";
    private static volatile HwInnerLocationManagerImpl mInstance = new HwInnerLocationManagerImpl();

    public static IHwInnerLocationManager getDefault() {
        return mInstance;
    }

    public int getPowerTypeByPackageName(String packageName) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("location");
        int _result = -1;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeString(packageName);
            b.transact(CODE_GET_POWR_TYPE, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    public int logEvent(int type, int event, String parameter) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("location");
        int _result = -1;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(type);
            _data.writeInt(event);
            _data.writeString(parameter);
            b.transact(CODE_LOG_EVENT, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }
}
