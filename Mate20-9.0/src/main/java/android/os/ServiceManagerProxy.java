package android.os;

import java.util.ArrayList;

/* compiled from: ServiceManagerNative */
class ServiceManagerProxy implements IServiceManager {
    private IBinder mRemote;

    public ServiceManagerProxy(IBinder remote) {
        this.mRemote = remote;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public IBinder getService(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeString(name);
        this.mRemote.transact(1, data, reply, 0);
        IBinder binder = reply.readStrongBinder();
        reply.recycle();
        data.recycle();
        return binder;
    }

    public IBinder checkService(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeString(name);
        this.mRemote.transact(2, data, reply, 0);
        IBinder binder = reply.readStrongBinder();
        reply.recycle();
        data.recycle();
        return binder;
    }

    public void addService(String name, IBinder service, boolean allowIsolated, int dumpPriority) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeString(name);
        data.writeStrongBinder(service);
        data.writeInt(allowIsolated);
        data.writeInt(dumpPriority);
        this.mRemote.transact(3, data, reply, 0);
        reply.recycle();
        data.recycle();
    }

    public String[] listServices(int dumpPriority) throws RemoteException {
        ArrayList<String> services = new ArrayList<>();
        int n = 0;
        while (true) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(IServiceManager.descriptor);
            data.writeInt(n);
            data.writeInt(dumpPriority);
            n++;
            try {
                if (!this.mRemote.transact(4, data, reply, 0)) {
                    break;
                }
                services.add(reply.readString());
                reply.recycle();
                data.recycle();
            } catch (RuntimeException e) {
            }
        }
        String[] array = new String[services.size()];
        services.toArray(array);
        return array;
    }

    public void setPermissionController(IPermissionController controller) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeStrongBinder(controller.asBinder());
        this.mRemote.transact(6, data, reply, 0);
        reply.recycle();
        data.recycle();
    }
}
