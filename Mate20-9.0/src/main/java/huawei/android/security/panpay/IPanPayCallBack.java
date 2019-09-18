package huawei.android.security.panpay;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPanPayCallBack extends IInterface {

    public static abstract class Stub extends Binder implements IPanPayCallBack {
        private static final String DESCRIPTOR = "huawei.android.security.panpay.IPanPayCallBack";
        static final int TRANSACTION_onCallBack = 1;

        private static class Proxy implements IPanPayCallBack {
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

            public void onCallBack(int result, String key, String value, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    _data.writeString(key);
                    _data.writeString(value);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
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

        public static IPanPayCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPanPayCallBack)) {
                return new Proxy(obj);
            }
            return (IPanPayCallBack) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                String _arg1 = data.readString();
                String _arg2 = data.readString();
                if (data.readInt() != 0) {
                    _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                onCallBack(_arg0, _arg1, _arg2, _arg3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onCallBack(int i, String str, String str2, Bundle bundle) throws RemoteException;
}
