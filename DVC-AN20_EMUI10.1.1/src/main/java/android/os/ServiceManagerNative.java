package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.IPermissionController;

public abstract class ServiceManagerNative extends Binder implements IServiceManager {
    @UnsupportedAppUsage
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

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        if (code == 1) {
            data.enforceInterface(IServiceManager.descriptor);
            reply.writeStrongBinder(getService(data.readString()));
            return true;
        } else if (code == 2) {
            data.enforceInterface(IServiceManager.descriptor);
            reply.writeStrongBinder(checkService(data.readString()));
            return true;
        } else if (code == 3) {
            data.enforceInterface(IServiceManager.descriptor);
            addService(data.readString(), data.readStrongBinder(), data.readInt() != 0, data.readInt());
            return true;
        } else if (code != 4) {
            if (code == 6) {
                try {
                    data.enforceInterface(IServiceManager.descriptor);
                    setPermissionController(IPermissionController.Stub.asInterface(data.readStrongBinder()));
                    return true;
                } catch (RemoteException e) {
                }
            }
            return false;
        } else {
            data.enforceInterface(IServiceManager.descriptor);
            reply.writeStringArray(listServices(data.readInt()));
            return true;
        }
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}
