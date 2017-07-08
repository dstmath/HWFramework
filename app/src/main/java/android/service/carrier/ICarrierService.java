package android.service.carrier;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;

public interface ICarrierService extends IInterface {

    public static abstract class Stub extends Binder implements ICarrierService {
        private static final String DESCRIPTOR = "android.service.carrier.ICarrierService";
        static final int TRANSACTION_getCarrierConfig = 1;

        private static class Proxy implements ICarrierService {
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

            public PersistableBundle getCarrierConfig(CarrierIdentifier id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PersistableBundle persistableBundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (id != null) {
                        _data.writeInt(Stub.TRANSACTION_getCarrierConfig);
                        id.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getCarrierConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        persistableBundle = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(_reply);
                    } else {
                        persistableBundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return persistableBundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICarrierService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICarrierService)) {
                return new Proxy(obj);
            }
            return (ICarrierService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_getCarrierConfig /*1*/:
                    CarrierIdentifier carrierIdentifier;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        carrierIdentifier = (CarrierIdentifier) CarrierIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        carrierIdentifier = null;
                    }
                    PersistableBundle _result = getCarrierConfig(carrierIdentifier);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getCarrierConfig);
                        _result.writeToParcel(reply, TRANSACTION_getCarrierConfig);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    PersistableBundle getCarrierConfig(CarrierIdentifier carrierIdentifier) throws RemoteException;
}
