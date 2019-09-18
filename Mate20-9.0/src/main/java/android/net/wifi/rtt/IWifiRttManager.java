package android.net.wifi.rtt;

import android.net.wifi.rtt.IRttCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.WorkSource;

public interface IWifiRttManager extends IInterface {

    public static abstract class Stub extends Binder implements IWifiRttManager {
        private static final String DESCRIPTOR = "android.net.wifi.rtt.IWifiRttManager";
        static final int TRANSACTION_cancelRanging = 3;
        static final int TRANSACTION_isAvailable = 1;
        static final int TRANSACTION_startRanging = 2;

        private static class Proxy implements IWifiRttManager {
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

            public boolean isAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startRanging(IBinder binder, String callingPackage, WorkSource workSource, RangingRequest request, IRttCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeString(callingPackage);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelRanging(WorkSource workSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
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

        public static IWifiRttManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiRttManager)) {
                return new Proxy(obj);
            }
            return (IWifiRttManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: android.net.wifi.rtt.RangingRequest} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: android.os.WorkSource} */
        /* JADX WARNING: type inference failed for: r1v8, types: [android.net.wifi.rtt.RangingRequest] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            WorkSource _arg2;
            if (code != 1598968902) {
                WorkSource _arg0 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = isAvailable();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = RangingRequest.CREATOR.createFromParcel(data);
                        }
                        startRanging(_arg02, _arg1, _arg2, _arg0, IRttCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = WorkSource.CREATOR.createFromParcel(data);
                        }
                        cancelRanging(_arg0);
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

    void cancelRanging(WorkSource workSource) throws RemoteException;

    boolean isAvailable() throws RemoteException;

    void startRanging(IBinder iBinder, String str, WorkSource workSource, RangingRequest rangingRequest, IRttCallback iRttCallback) throws RemoteException;
}
