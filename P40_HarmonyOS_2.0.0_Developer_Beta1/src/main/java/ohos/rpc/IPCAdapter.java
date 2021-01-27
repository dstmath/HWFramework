package ohos.rpc;

import android.os.IBinder;
import android.os.Parcel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class IPCAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "IPCAdapter");

    private static native IBinder nativeTranslateToIBinder(IRemoteObject iRemoteObject);

    private static native IRemoteObject nativeTranslateToIRemoteObject(Parcel parcel);

    private static IBinder getBinder(long j) {
        try {
            Class<?> cls = Class.forName("android.os.Parcel");
            if (cls == null) {
                HiLog.error(TAG, "get null parcelClass", new Object[0]);
                return null;
            }
            Constructor<?> declaredConstructor = cls.getDeclaredConstructor(Long.TYPE);
            if (declaredConstructor == null) {
                HiLog.error(TAG, "get null constructor", new Object[0]);
                return null;
            }
            declaredConstructor.setAccessible(true);
            Object newInstance = declaredConstructor.newInstance(Long.valueOf(j));
            if (newInstance instanceof Parcel) {
                Parcel parcel = (Parcel) newInstance;
                IBinder readStrongBinder = parcel.readStrongBinder();
                parcel.recycle();
                return readStrongBinder;
            }
            HiLog.error(TAG, "getBinder fail", new Object[0]);
            return null;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
        }
    }

    public static Optional<Object> translateToIBinder(IRemoteObject iRemoteObject) {
        IBinder nativeTranslateToIBinder = nativeTranslateToIBinder(iRemoteObject);
        if (nativeTranslateToIBinder != null) {
            return Optional.of(nativeTranslateToIBinder);
        }
        HiLog.error(TAG, "get null binder", new Object[0]);
        return Optional.empty();
    }

    public static Optional<IRemoteObject> translateToIRemoteObject(Object obj) {
        if (!(obj instanceof IBinder)) {
            HiLog.error(TAG, "input is not IBinder", new Object[0]);
            return Optional.empty();
        }
        Parcel obtain = Parcel.obtain();
        int dataPosition = obtain.dataPosition();
        obtain.writeStrongBinder((IBinder) obj);
        obtain.setDataPosition(dataPosition);
        IRemoteObject nativeTranslateToIRemoteObject = nativeTranslateToIRemoteObject(obtain);
        obtain.recycle();
        if (nativeTranslateToIRemoteObject != null) {
            return Optional.of(nativeTranslateToIRemoteObject);
        }
        HiLog.error(TAG, "failed to get remote object", new Object[0]);
        return Optional.empty();
    }
}
