package android.os;

import android.os.IPermissionController.Stub;

public abstract class ServiceManagerNative extends Binder implements IServiceManager {
    public static IServiceManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IServiceManager in = (IServiceManager) obj.queryLocalInterface(IServiceManager.descriptor);
        if (in != null) {
            return in;
        }
        return new ServiceManagerProxy(obj);
    }

    public ServiceManagerNative() {
        attachInterface(this, IServiceManager.descriptor);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        switch (code) {
            case 1:
                try {
                    data.enforceInterface(IServiceManager.descriptor);
                    reply.writeStrongBinder(getService(data.readString()));
                    return true;
                } catch (RemoteException e) {
                    break;
                }
            case 2:
                data.enforceInterface(IServiceManager.descriptor);
                reply.writeStrongBinder(checkService(data.readString()));
                return true;
            case 3:
                data.enforceInterface(IServiceManager.descriptor);
                addService(data.readString(), data.readStrongBinder(), data.readInt() != 0);
                return true;
            case 4:
                data.enforceInterface(IServiceManager.descriptor);
                reply.writeStringArray(listServices());
                return true;
            case 6:
                data.enforceInterface(IServiceManager.descriptor);
                setPermissionController(Stub.asInterface(data.readStrongBinder()));
                return true;
        }
        return false;
    }

    public IBinder asBinder() {
        return this;
    }
}
