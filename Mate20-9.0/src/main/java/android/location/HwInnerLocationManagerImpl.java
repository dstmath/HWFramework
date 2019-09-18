package android.location;

import android.content.ContentResolver;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.ArrayList;

public class HwInnerLocationManagerImpl implements IHwInnerLocationManager {
    static final int CODE_GET_POWR_TYPE = 1001;
    static final int CODE_GNSS_DETECT = 1007;
    static final int CODE_LOG_EVENT = 1002;
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    static final int SETTINGS_LOCATION_MODE = 1006;
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
            b.transact(1001, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "getPowerTypeByPackageName catch RemoteException in HwInnerLocationManagerImpl.");
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
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
            b.transact(1002, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "logEvent catch RemoteException in HwInnerLocationManagerImpl.");
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public int checkLocationSettings(ContentResolver resolver, String name, String value, int userHandle) {
        int _result = -1;
        if (resolver == null) {
            return -1;
        }
        int userId = resolver.getUserId();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("location");
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(userId);
            _data.writeString(name);
            _data.writeString(value);
            b.transact(1006, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Log.d(TAG, "checkLocationSettings catch RemoteException in HwInnerLocationManagerImpl.");
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public ArrayList<String> gnssDetect(String packageName) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("location");
        ArrayList<String> _result = new ArrayList<>();
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeString(packageName);
            b.transact(1007, _data, _reply, 0);
            _reply.readException();
            _reply.readStringList(_result);
        } catch (RemoteException e) {
            Log.d(TAG, "gnssDetect catch RemoteException in HwInnerLocationManagerImpl.");
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }
}
