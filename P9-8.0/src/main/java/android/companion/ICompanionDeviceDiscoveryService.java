package android.companion;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICompanionDeviceDiscoveryService extends IInterface {

    public static abstract class Stub extends Binder implements ICompanionDeviceDiscoveryService {
        private static final String DESCRIPTOR = "android.companion.ICompanionDeviceDiscoveryService";
        static final int TRANSACTION_startDiscovery = 1;

        private static class Proxy implements ICompanionDeviceDiscoveryService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void startDiscovery(AssociationRequest request, String callingPackage, IFindDeviceCallback findCallback, ICompanionDeviceDiscoveryServiceCallback serviceCallback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(findCallback != null ? findCallback.asBinder() : null);
                    if (serviceCallback != null) {
                        iBinder = serviceCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICompanionDeviceDiscoveryService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICompanionDeviceDiscoveryService)) {
                return new Proxy(obj);
            }
            return (ICompanionDeviceDiscoveryService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    AssociationRequest _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (AssociationRequest) AssociationRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    startDiscovery(_arg0, data.readString(), android.companion.IFindDeviceCallback.Stub.asInterface(data.readStrongBinder()), android.companion.ICompanionDeviceDiscoveryServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void startDiscovery(AssociationRequest associationRequest, String str, IFindDeviceCallback iFindDeviceCallback, ICompanionDeviceDiscoveryServiceCallback iCompanionDeviceDiscoveryServiceCallback) throws RemoteException;
}
