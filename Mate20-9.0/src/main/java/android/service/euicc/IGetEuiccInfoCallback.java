package android.service.euicc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.euicc.EuiccInfo;

public interface IGetEuiccInfoCallback extends IInterface {

    public static abstract class Stub extends Binder implements IGetEuiccInfoCallback {
        private static final String DESCRIPTOR = "android.service.euicc.IGetEuiccInfoCallback";
        static final int TRANSACTION_onSuccess = 1;

        private static class Proxy implements IGetEuiccInfoCallback {
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

            public void onSuccess(EuiccInfo euiccInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (euiccInfo != null) {
                        _data.writeInt(1);
                        euiccInfo.writeToParcel(_data, 0);
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

        public static IGetEuiccInfoCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGetEuiccInfoCallback)) {
                return new Proxy(obj);
            }
            return (IGetEuiccInfoCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            EuiccInfo _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = EuiccInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onSuccess(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onSuccess(EuiccInfo euiccInfo) throws RemoteException;
}
