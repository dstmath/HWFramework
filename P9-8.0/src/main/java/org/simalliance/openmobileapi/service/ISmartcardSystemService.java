package org.simalliance.openmobileapi.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISmartcardSystemService extends IInterface {

    public static abstract class Stub extends Binder implements ISmartcardSystemService {
        private static final String DESCRIPTOR = "org.simalliance.openmobileapi.service.ISmartcardSystemService";
        static final int TRANSACTION_closeChannel = 1;
        static final int TRANSACTION_connectSmartCardService = 9;
        static final int TRANSACTION_getLastError = 8;
        static final int TRANSACTION_getReaders = 2;
        static final int TRANSACTION_isCardPresent = 3;
        static final int TRANSACTION_openBasicChannel = 4;
        static final int TRANSACTION_openBasicChannelAid = 5;
        static final int TRANSACTION_openLogicalChannel = 6;
        static final int TRANSACTION_transmit = 7;

        private static class Proxy implements ISmartcardSystemService {
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

            public void closeChannel(long hChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(hChannel);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getReaders() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCardPresent(String reader) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public long openBasicChannel(String reader) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long openBasicChannelAid(String reader, String aid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    _data.writeString(aid);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long openLogicalChannel(String reader, String aid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    _data.writeString(aid);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String transmit(long hChannel, String command) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(hChannel);
                    _data.writeString(command);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLastError() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean connectSmartCardService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISmartcardSystemService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISmartcardSystemService)) {
                return new Proxy(obj);
            }
            return (ISmartcardSystemService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            String _result;
            boolean _result2;
            long _result3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    closeChannel(data.readLong());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getReaders();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isCardPresent(data.readString());
                    reply.writeNoException();
                    if (_result2) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = openBasicChannel(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = openBasicChannelAid(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = openLogicalChannel(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = transmit(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getLastError();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = connectSmartCardService();
                    reply.writeNoException();
                    if (_result2) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void closeChannel(long j) throws RemoteException;

    boolean connectSmartCardService() throws RemoteException;

    String getLastError() throws RemoteException;

    String getReaders() throws RemoteException;

    boolean isCardPresent(String str) throws RemoteException;

    long openBasicChannel(String str) throws RemoteException;

    long openBasicChannelAid(String str, String str2) throws RemoteException;

    long openLogicalChannel(String str, String str2) throws RemoteException;

    String transmit(long j, String str) throws RemoteException;
}
