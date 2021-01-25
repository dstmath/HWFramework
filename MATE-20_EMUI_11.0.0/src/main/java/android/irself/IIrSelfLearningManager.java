package android.irself;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIrSelfLearningManager extends IInterface {
    boolean deviceExit() throws RemoteException;

    boolean deviceInit() throws RemoteException;

    boolean getLearningStatus() throws RemoteException;

    boolean hasIrSelfLearning() throws RemoteException;

    int[] readIRCode() throws RemoteException;

    int readIRFrequency() throws RemoteException;

    boolean startLearning() throws RemoteException;

    boolean stopLearning() throws RemoteException;

    boolean transmit(int i, int[] iArr) throws RemoteException;

    public static class Default implements IIrSelfLearningManager {
        @Override // android.irself.IIrSelfLearningManager
        public boolean hasIrSelfLearning() throws RemoteException {
            return false;
        }

        @Override // android.irself.IIrSelfLearningManager
        public boolean deviceInit() throws RemoteException {
            return false;
        }

        @Override // android.irself.IIrSelfLearningManager
        public boolean deviceExit() throws RemoteException {
            return false;
        }

        @Override // android.irself.IIrSelfLearningManager
        public boolean transmit(int carrierFrequency, int[] pattern) throws RemoteException {
            return false;
        }

        @Override // android.irself.IIrSelfLearningManager
        public boolean startLearning() throws RemoteException {
            return false;
        }

        @Override // android.irself.IIrSelfLearningManager
        public boolean stopLearning() throws RemoteException {
            return false;
        }

        @Override // android.irself.IIrSelfLearningManager
        public boolean getLearningStatus() throws RemoteException {
            return false;
        }

        @Override // android.irself.IIrSelfLearningManager
        public int readIRFrequency() throws RemoteException {
            return 0;
        }

        @Override // android.irself.IIrSelfLearningManager
        public int[] readIRCode() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIrSelfLearningManager {
        private static final String DESCRIPTOR = "android.irself.IIrSelfLearningManager";
        static final int TRANSACTION_deviceExit = 3;
        static final int TRANSACTION_deviceInit = 2;
        static final int TRANSACTION_getLearningStatus = 7;
        static final int TRANSACTION_hasIrSelfLearning = 1;
        static final int TRANSACTION_readIRCode = 9;
        static final int TRANSACTION_readIRFrequency = 8;
        static final int TRANSACTION_startLearning = 5;
        static final int TRANSACTION_stopLearning = 6;
        static final int TRANSACTION_transmit = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIrSelfLearningManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIrSelfLearningManager)) {
                return new Proxy(obj);
            }
            return (IIrSelfLearningManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasIrSelfLearning = hasIrSelfLearning();
                        reply.writeNoException();
                        reply.writeInt(hasIrSelfLearning ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceInit = deviceInit();
                        reply.writeNoException();
                        reply.writeInt(deviceInit ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceExit = deviceExit();
                        reply.writeNoException();
                        reply.writeInt(deviceExit ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean transmit = transmit(data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(transmit ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startLearning = startLearning();
                        reply.writeNoException();
                        reply.writeInt(startLearning ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopLearning = stopLearning();
                        reply.writeNoException();
                        reply.writeInt(stopLearning ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean learningStatus = getLearningStatus();
                        reply.writeNoException();
                        reply.writeInt(learningStatus ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = readIRFrequency();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result2 = readIRCode();
                        reply.writeNoException();
                        reply.writeIntArray(_result2);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIrSelfLearningManager {
            public static IIrSelfLearningManager sDefaultImpl;
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

            @Override // android.irself.IIrSelfLearningManager
            public boolean hasIrSelfLearning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasIrSelfLearning();
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

            @Override // android.irself.IIrSelfLearningManager
            public boolean deviceInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deviceInit();
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

            @Override // android.irself.IIrSelfLearningManager
            public boolean deviceExit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deviceExit();
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

            @Override // android.irself.IIrSelfLearningManager
            public boolean transmit(int carrierFrequency, int[] pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(carrierFrequency);
                    _data.writeIntArray(pattern);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().transmit(carrierFrequency, pattern);
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

            @Override // android.irself.IIrSelfLearningManager
            public boolean startLearning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startLearning();
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

            @Override // android.irself.IIrSelfLearningManager
            public boolean stopLearning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopLearning();
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

            @Override // android.irself.IIrSelfLearningManager
            public boolean getLearningStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLearningStatus();
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

            @Override // android.irself.IIrSelfLearningManager
            public int readIRFrequency() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readIRFrequency();
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

            @Override // android.irself.IIrSelfLearningManager
            public int[] readIRCode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readIRCode();
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

        public static boolean setDefaultImpl(IIrSelfLearningManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIrSelfLearningManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
