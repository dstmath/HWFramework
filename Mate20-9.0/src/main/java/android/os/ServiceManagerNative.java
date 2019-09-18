package android.os;

import android.os.IPermissionController;

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
        if (code != 6) {
            switch (code) {
                case 1:
                    data.enforceInterface(IServiceManager.descriptor);
                    reply.writeStrongBinder(getService(data.readString()));
                    return true;
                case 2:
                    data.enforceInterface(IServiceManager.descriptor);
                    reply.writeStrongBinder(checkService(data.readString()));
                    return true;
                case 3:
                    data.enforceInterface(IServiceManager.descriptor);
                    addService(data.readString(), data.readStrongBinder(), data.readInt() != 0, data.readInt());
                    return true;
                case 4:
                    try {
                        data.enforceInterface(IServiceManager.descriptor);
                        reply.writeStringArray(listServices(data.readInt()));
                        return true;
                    } catch (RemoteException e) {
                        break;
                    }
            }
            return false;
        }
        data.enforceInterface(IServiceManager.descriptor);
        setPermissionController(IPermissionController.Stub.asInterface(data.readStrongBinder()));
        return true;
    }

    public IBinder asBinder() {
        return this;
    }
}
