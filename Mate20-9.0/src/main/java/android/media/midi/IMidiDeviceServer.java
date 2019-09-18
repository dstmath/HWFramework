package android.media.midi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.io.FileDescriptor;

public interface IMidiDeviceServer extends IInterface {

    public static abstract class Stub extends Binder implements IMidiDeviceServer {
        private static final String DESCRIPTOR = "android.media.midi.IMidiDeviceServer";
        static final int TRANSACTION_closeDevice = 4;
        static final int TRANSACTION_closePort = 3;
        static final int TRANSACTION_connectPorts = 5;
        static final int TRANSACTION_getDeviceInfo = 6;
        static final int TRANSACTION_openInputPort = 1;
        static final int TRANSACTION_openOutputPort = 2;
        static final int TRANSACTION_setDeviceInfo = 7;

        private static class Proxy implements IMidiDeviceServer {
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

            public FileDescriptor openInputPort(IBinder token, int portNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(portNumber);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readRawFileDescriptor();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FileDescriptor openOutputPort(IBinder token, int portNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(portNumber);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readRawFileDescriptor();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closePort(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeDevice() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public int connectPorts(IBinder token, FileDescriptor fd, int outputPortNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeRawFileDescriptor(fd);
                    _data.writeInt(outputPortNumber);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MidiDeviceInfo getDeviceInfo() throws RemoteException {
                MidiDeviceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MidiDeviceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

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
    }

    void closeDevice() throws RemoteException;

    void closePort(IBinder iBinder) throws RemoteException;

    int connectPorts(IBinder iBinder, FileDescriptor fileDescriptor, int i) throws RemoteException;

    MidiDeviceInfo getDeviceInfo() throws RemoteException;

    FileDescriptor openInputPort(IBinder iBinder, int i) throws RemoteException;

    FileDescriptor openOutputPort(IBinder iBinder, int i) throws RemoteException;

    void setDeviceInfo(MidiDeviceInfo midiDeviceInfo) throws RemoteException;
}
