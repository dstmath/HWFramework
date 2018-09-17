package android.irself;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIrSelfLearningManager extends IInterface {

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

        private static class Proxy implements IIrSelfLearningManager {
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

            public boolean hasIrSelfLearning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hasIrSelfLearning, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deviceInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_deviceInit, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deviceExit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_deviceExit, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean transmit(int carrierFrequency, int[] pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(carrierFrequency);
                    _data.writeIntArray(pattern);
                    this.mRemote.transact(Stub.TRANSACTION_transmit, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startLearning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_startLearning, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopLearning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopLearning, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getLearningStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLearningStatus, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int readIRFrequency() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_readIRFrequency, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] readIRCode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_readIRCode, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            boolean _result;
            switch (code) {
                case TRANSACTION_hasIrSelfLearning /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasIrSelfLearning();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_hasIrSelfLearning;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_deviceInit /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deviceInit();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_hasIrSelfLearning;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_deviceExit /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deviceExit();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_hasIrSelfLearning;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_transmit /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = transmit(data.readInt(), data.createIntArray());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_hasIrSelfLearning;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_startLearning /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startLearning();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_hasIrSelfLearning;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_stopLearning /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopLearning();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_hasIrSelfLearning;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_getLearningStatus /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getLearningStatus();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_hasIrSelfLearning;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_readIRFrequency /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = readIRFrequency();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_readIRCode /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result3 = readIRCode();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean deviceExit() throws RemoteException;

    boolean deviceInit() throws RemoteException;

    boolean getLearningStatus() throws RemoteException;

    boolean hasIrSelfLearning() throws RemoteException;

    int[] readIRCode() throws RemoteException;

    int readIRFrequency() throws RemoteException;

    boolean startLearning() throws RemoteException;

    boolean stopLearning() throws RemoteException;

    boolean transmit(int i, int[] iArr) throws RemoteException;
}
