package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.wifi.wificond.NativeMssResult;

public interface IHwVendorEvent extends IInterface {

    public static abstract class Stub extends Binder implements IHwVendorEvent {
        private static final String DESCRIPTOR = "android.net.wifi.IHwVendorEvent";
        static final int TRANSACTION_OnMssSyncReport = 1;
        static final int TRANSACTION_OnTasRssiReport = 2;

        private static class Proxy implements IHwVendorEvent {
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

            public void OnMssSyncReport(NativeMssResult mssresult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mssresult != null) {
                        _data.writeInt(1);
                        mssresult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnTasRssiReport(int index, int rssi, int[] rsv) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    _data.writeInt(rssi);
                    _data.writeIntArray(rsv);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwVendorEvent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwVendorEvent)) {
                return new Proxy(obj);
            }
            return (IHwVendorEvent) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NativeMssResult _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = NativeMssResult.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        OnMssSyncReport(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        OnTasRssiReport(data.readInt(), data.readInt(), data.createIntArray());
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

    void OnMssSyncReport(NativeMssResult nativeMssResult) throws RemoteException;

    void OnTasRssiReport(int i, int i2, int[] iArr) throws RemoteException;
}
