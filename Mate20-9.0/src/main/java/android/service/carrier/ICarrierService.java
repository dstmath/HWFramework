package android.service.carrier;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;

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

            public void getCarrierConfig(CarrierIdentifier id, ResultReceiver result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (id != null) {
                        _data.writeInt(1);
                        id.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
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
            CarrierIdentifier _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                ResultReceiver _arg1 = null;
                if (data.readInt() != 0) {
                    _arg0 = CarrierIdentifier.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = ResultReceiver.CREATOR.createFromParcel(data);
                }
                getCarrierConfig(_arg0, _arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void getCarrierConfig(CarrierIdentifier carrierIdentifier, ResultReceiver resultReceiver) throws RemoteException;
}
