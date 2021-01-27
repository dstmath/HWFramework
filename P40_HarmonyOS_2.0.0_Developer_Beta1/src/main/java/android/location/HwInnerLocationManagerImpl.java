package android.location;

import android.content.ContentResolver;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.UserHandleEx;
import java.util.ArrayList;

public class HwInnerLocationManagerImpl extends DefaultHwInnerLocationManager {
    private static final int CODE_GET_POWR_TYPE = 1001;
    private static final int CODE_GNSS_DETECT = 1007;
    private static final int CODE_LOG_EVENT = 1002;
    private static final int DEFAULT_BUFFER_SIZE = 0;
    private static final int DEFAULT_ERROR_CODE = -1;
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    private static final int SETTINGS_LOCATION_MODE = 1006;
    private static final String TAG = "HwInnerLocationManagerImpl";
    private static volatile DefaultHwInnerLocationManager mInstance = new HwInnerLocationManagerImpl();

    public static DefaultHwInnerLocationManager getDefault() {
        return mInstance;
    }

    public int getPowerTypeByPackageName(String packageName) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = ServiceManagerEx.getService("location");
        int result = -1;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(packageName);
                binder.transact(CODE_GET_POWR_TYPE, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "getPowerTypeByPackageName catch RemoteException in HwInnerLocationManagerImpl.");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public int logEvent(int type, int event, String parameter) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = ServiceManagerEx.getService("location");
        int result = -1;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(type);
                data.writeInt(event);
                data.writeString(parameter);
                binder.transact(CODE_LOG_EVENT, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "logEvent catch RemoteException in HwInnerLocationManagerImpl.");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public int checkLocationSettings(ContentResolver resolver, String name, String value, int userHandle) {
        if (resolver == null) {
            return -1;
        }
        int userId = UserHandleEx.myUserId();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = ServiceManagerEx.getService("location");
        int result = -1;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(userId);
                data.writeString(name);
                data.writeString(value);
                binder.transact(SETTINGS_LOCATION_MODE, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.d(TAG, "checkLocationSettings catch RemoteException in HwInnerLocationManagerImpl.");
                reply.recycle();
                data.recycle();
                return -1;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public ArrayList<String> gnssDetect(String packageName) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = ServiceManagerEx.getService("location");
        ArrayList<String> results = new ArrayList<>(0);
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(packageName);
                binder.transact(CODE_GNSS_DETECT, data, reply, 0);
                reply.readException();
                reply.readStringList(results);
            } catch (RemoteException e) {
                Log.d(TAG, "gnssDetect catch RemoteException in HwInnerLocationManagerImpl.");
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return results;
    }
}
