package android.hardware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConsumerIrService extends IInterface {
    int[] getCarrierFrequencies() throws RemoteException;

    boolean hasIrEmitter() throws RemoteException;

    void transmit(String str, int i, int[] iArr) throws RemoteException;

    public static class Default implements IConsumerIrService {
        @Override // android.hardware.IConsumerIrService
        public boolean hasIrEmitter() throws RemoteException {
            return false;
        }

        @Override // android.hardware.IConsumerIrService
        public void transmit(String packageName, int carrierFrequency, int[] pattern) throws RemoteException {
        }

        @Override // android.hardware.IConsumerIrService
        public int[] getCarrierFrequencies() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConsumerIrService {
        private static final String DESCRIPTOR = "android.hardware.IConsumerIrService";
        static final int TRANSACTION_getCarrierFrequencies = 3;
        static final int TRANSACTION_hasIrEmitter = 1;
        static final int TRANSACTION_transmit = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConsumerIrService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConsumerIrService)) {
                return new Proxy(obj);
            }
            return (IConsumerIrService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "hasIrEmitter";
            }
            if (transactionCode == 2) {
                return "transmit";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "getCarrierFrequencies";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean hasIrEmitter = hasIrEmitter();
                reply.writeNoException();
                reply.writeInt(hasIrEmitter ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                transmit(data.readString(), data.readInt(), data.createIntArray());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int[] _result = getCarrierFrequencies();
                reply.writeNoException();
                reply.writeIntArray(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IConsumerIrService {
            public static IConsumerIrService sDefaultImpl;
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

            @Override // android.hardware.IConsumerIrService
            public boolean hasIrEmitter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasIrEmitter();
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

            @Override // android.hardware.IConsumerIrService
            public void transmit(String packageName, int carrierFrequency, int[] pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(carrierFrequency);
                    _data.writeIntArray(pattern);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().transmit(packageName, carrierFrequency, pattern);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.IConsumerIrService
            public int[] getCarrierFrequencies() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierFrequencies();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IConsumerIrService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConsumerIrService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
