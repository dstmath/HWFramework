package android.net.wifi.HwQoE;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwQoECallback extends IInterface {

    public static abstract class Stub extends Binder implements IHwQoECallback {
        private static final String DESCRIPTOR = "android.net.wifi.HwQoE.IHwQoECallback";
        static final int TRANSACTION_onNetworkEvaluate = 1;
        static final int TRANSACTION_onNetworkStateChange = 2;

        private static class Proxy implements IHwQoECallback {
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

            public void onNetworkEvaluate(boolean result, HwQoEQualityInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNetworkStateChange(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(2, _data, _reply, 0);
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

        public static IHwQoECallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwQoECallback)) {
                return new Proxy(obj);
            }
            return (IHwQoECallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwQoEQualityInfo _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg0 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = HwQoEQualityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onNetworkEvaluate(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onNetworkStateChange(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onNetworkEvaluate(boolean z, HwQoEQualityInfo hwQoEQualityInfo) throws RemoteException;

    void onNetworkStateChange(int i) throws RemoteException;
}
