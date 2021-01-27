package android.hardware.hdmi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHdmiRecordListener extends IInterface {
    byte[] getOneTouchRecordSource(int i) throws RemoteException;

    void onClearTimerRecordingResult(int i, int i2) throws RemoteException;

    void onOneTouchRecordResult(int i, int i2) throws RemoteException;

    void onTimerRecordingResult(int i, int i2) throws RemoteException;

    public static class Default implements IHdmiRecordListener {
        @Override // android.hardware.hdmi.IHdmiRecordListener
        public byte[] getOneTouchRecordSource(int recorderAddress) throws RemoteException {
            return null;
        }

        @Override // android.hardware.hdmi.IHdmiRecordListener
        public void onOneTouchRecordResult(int recorderAddress, int result) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiRecordListener
        public void onTimerRecordingResult(int recorderAddress, int result) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiRecordListener
        public void onClearTimerRecordingResult(int recorderAddress, int result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHdmiRecordListener {
        private static final String DESCRIPTOR = "android.hardware.hdmi.IHdmiRecordListener";
        static final int TRANSACTION_getOneTouchRecordSource = 1;
        static final int TRANSACTION_onClearTimerRecordingResult = 4;
        static final int TRANSACTION_onOneTouchRecordResult = 2;
        static final int TRANSACTION_onTimerRecordingResult = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHdmiRecordListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHdmiRecordListener)) {
                return new Proxy(obj);
            }
            return (IHdmiRecordListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getOneTouchRecordSource";
            }
            if (transactionCode == 2) {
                return "onOneTouchRecordResult";
            }
            if (transactionCode == 3) {
                return "onTimerRecordingResult";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onClearTimerRecordingResult";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                byte[] _result = getOneTouchRecordSource(data.readInt());
                reply.writeNoException();
                reply.writeByteArray(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onOneTouchRecordResult(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onTimerRecordingResult(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onClearTimerRecordingResult(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHdmiRecordListener {
            public static IHdmiRecordListener sDefaultImpl;
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

            @Override // android.hardware.hdmi.IHdmiRecordListener
            public byte[] getOneTouchRecordSource(int recorderAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOneTouchRecordSource(recorderAddress);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiRecordListener
            public void onOneTouchRecordResult(int recorderAddress, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(result);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOneTouchRecordResult(recorderAddress, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiRecordListener
            public void onTimerRecordingResult(int recorderAddress, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(result);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onTimerRecordingResult(recorderAddress, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiRecordListener
            public void onClearTimerRecordingResult(int recorderAddress, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(result);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onClearTimerRecordingResult(recorderAddress, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHdmiRecordListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHdmiRecordListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
