package android.media.midi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.io.FileDescriptor;

public interface IMidiDeviceServer extends IInterface {
    void closeDevice() throws RemoteException;

    void closePort(IBinder iBinder) throws RemoteException;

    int connectPorts(IBinder iBinder, FileDescriptor fileDescriptor, int i) throws RemoteException;

    MidiDeviceInfo getDeviceInfo() throws RemoteException;

    FileDescriptor openInputPort(IBinder iBinder, int i) throws RemoteException;

    FileDescriptor openOutputPort(IBinder iBinder, int i) throws RemoteException;

    void setDeviceInfo(MidiDeviceInfo midiDeviceInfo) throws RemoteException;

    public static class Default implements IMidiDeviceServer {
        @Override // android.media.midi.IMidiDeviceServer
        public FileDescriptor openInputPort(IBinder token, int portNumber) throws RemoteException {
            return null;
        }

        @Override // android.media.midi.IMidiDeviceServer
        public FileDescriptor openOutputPort(IBinder token, int portNumber) throws RemoteException {
            return null;
        }

        @Override // android.media.midi.IMidiDeviceServer
        public void closePort(IBinder token) throws RemoteException {
        }

        @Override // android.media.midi.IMidiDeviceServer
        public void closeDevice() throws RemoteException {
        }

        @Override // android.media.midi.IMidiDeviceServer
        public int connectPorts(IBinder token, FileDescriptor fd, int outputPortNumber) throws RemoteException {
            return 0;
        }

        @Override // android.media.midi.IMidiDeviceServer
        public MidiDeviceInfo getDeviceInfo() throws RemoteException {
            return null;
        }

        @Override // android.media.midi.IMidiDeviceServer
        public void setDeviceInfo(MidiDeviceInfo deviceInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMidiDeviceServer {
        private static final String DESCRIPTOR = "android.media.midi.IMidiDeviceServer";
        static final int TRANSACTION_closeDevice = 4;
        static final int TRANSACTION_closePort = 3;
        static final int TRANSACTION_connectPorts = 5;
        static final int TRANSACTION_getDeviceInfo = 6;
        static final int TRANSACTION_openInputPort = 1;
        static final int TRANSACTION_openOutputPort = 2;
        static final int TRANSACTION_setDeviceInfo = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMidiDeviceServer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMidiDeviceServer)) {
                return new Proxy(obj);
            }
            return (IMidiDeviceServer) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "openInputPort";
                case 2:
                    return "openOutputPort";
                case 3:
                    return "closePort";
                case 4:
                    return "closeDevice";
                case 5:
                    return "connectPorts";
                case 6:
                    return "getDeviceInfo";
                case 7:
                    return "setDeviceInfo";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            MidiDeviceInfo _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        FileDescriptor _result = openInputPort(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        reply.writeRawFileDescriptor(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        FileDescriptor _result2 = openOutputPort(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        reply.writeRawFileDescriptor(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        closePort(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        closeDevice();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = connectPorts(data.readStrongBinder(), data.readRawFileDescriptor(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        MidiDeviceInfo _result4 = getDeviceInfo();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = MidiDeviceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setDeviceInfo(_arg0);
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
        public static class Proxy implements IMidiDeviceServer {
            public static IMidiDeviceServer sDefaultImpl;
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

            @Override // android.media.midi.IMidiDeviceServer
            public FileDescriptor openInputPort(IBinder token, int portNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(portNumber);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openInputPort(token, portNumber);
                    }
                    _reply.readException();
                    FileDescriptor _result = _reply.readRawFileDescriptor();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public FileDescriptor openOutputPort(IBinder token, int portNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(portNumber);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openOutputPort(token, portNumber);
                    }
                    _reply.readException();
                    FileDescriptor _result = _reply.readRawFileDescriptor();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public void closePort(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closePort(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public void closeDevice() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().closeDevice();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public int connectPorts(IBinder token, FileDescriptor fd, int outputPortNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeRawFileDescriptor(fd);
                    _data.writeInt(outputPortNumber);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectPorts(token, fd, outputPortNumber);
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

            @Override // android.media.midi.IMidiDeviceServer
            public MidiDeviceInfo getDeviceInfo() throws RemoteException {
                MidiDeviceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MidiDeviceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public void setDeviceInfo(MidiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setDeviceInfo(deviceInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMidiDeviceServer impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMidiDeviceServer getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
