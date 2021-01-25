package huawei.android.hwsmartdisplay;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwSmartDisplayService extends IInterface {
    int getDisplayEffectSupported(int i) throws RemoteException;

    boolean isFeatureSupported(int i) throws RemoteException;

    int setDisplayEffectParam(int i, int[] iArr, int i2) throws RemoteException;

    public static class Default implements IHwSmartDisplayService {
        @Override // huawei.android.hwsmartdisplay.IHwSmartDisplayService
        public boolean isFeatureSupported(int feature) throws RemoteException {
            return false;
        }

        @Override // huawei.android.hwsmartdisplay.IHwSmartDisplayService
        public int setDisplayEffectParam(int type, int[] buffer, int length) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.hwsmartdisplay.IHwSmartDisplayService
        public int getDisplayEffectSupported(int type) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSmartDisplayService {
        private static final String DESCRIPTOR = "huawei.android.hwsmartdisplay.IHwSmartDisplayService";
        static final int TRANSACTION_getDisplayEffectSupported = 3;
        static final int TRANSACTION_isFeatureSupported = 1;
        static final int TRANSACTION_setDisplayEffectParam = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSmartDisplayService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSmartDisplayService)) {
                return new Proxy(obj);
            }
            return (IHwSmartDisplayService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean isFeatureSupported = isFeatureSupported(data.readInt());
                reply.writeNoException();
                reply.writeInt(isFeatureSupported ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result = setDisplayEffectParam(data.readInt(), data.createIntArray(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = getDisplayEffectSupported(data.readInt());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSmartDisplayService {
            public static IHwSmartDisplayService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // huawei.android.hwsmartdisplay.IHwSmartDisplayService
            public boolean isFeatureSupported(int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFeatureSupported(feature);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.hwsmartdisplay.IHwSmartDisplayService
            public int setDisplayEffectParam(int type, int[] buffer, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeIntArray(buffer);
                    _data.writeInt(length);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDisplayEffectParam(type, buffer, length);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.hwsmartdisplay.IHwSmartDisplayService
            public int getDisplayEffectSupported(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayEffectSupported(type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwSmartDisplayService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSmartDisplayService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
