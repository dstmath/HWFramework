package org.simalliance.openmobileapi.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISmartcardService extends IInterface {

    public static abstract class Stub extends Binder implements ISmartcardService {
        private static final String DESCRIPTOR = "org.simalliance.openmobileapi.service.ISmartcardService";
        static final int TRANSACTION_closeChannel = 1;
        static final int TRANSACTION_getAtr = 4;
        static final int TRANSACTION_getReader = 10;
        static final int TRANSACTION_getReaders = 2;
        static final int TRANSACTION_getSelectResponse = 9;
        static final int TRANSACTION_isCardPresent = 3;
        static final int TRANSACTION_isNFCEventAllowed = 11;
        static final int TRANSACTION_openBasicChannel = 5;
        static final int TRANSACTION_openBasicChannelAid = 6;
        static final int TRANSACTION_openLogicalChannel = 7;
        static final int TRANSACTION_transmit = 8;

        private static class Proxy implements ISmartcardService {
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

            public void closeChannel(long hChannel, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(hChannel);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getReaders(SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCardPresent(String reader, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getAtr(String reader, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long openBasicChannel(String reader, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long openBasicChannelAid(String reader, byte[] aid, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    _data.writeByteArray(aid);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long openLogicalChannel(String reader, byte[] aid, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    _data.writeByteArray(aid);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] transmit(long hChannel, byte[] command, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(hChannel);
                    _data.writeByteArray(command);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getSelectResponse(long hChannel, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(hChannel);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ISmartcardServiceReader getReader(String reader, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    ISmartcardServiceReader _result = org.simalliance.openmobileapi.service.ISmartcardServiceReader.Stub.asInterface(_reply.readStrongBinder());
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean[] isNFCEventAllowed(String reader, byte[] aid, String[] packageNames, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reader);
                    _data.writeByteArray(aid);
                    _data.writeStringArray(packageNames);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    boolean[] _result = _reply.createBooleanArray();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
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

        public static ISmartcardService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISmartcardService)) {
                return new Proxy(obj);
            }
            return (ISmartcardService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _arg0;
            SmartcardError _arg1;
            String _arg02;
            byte[] _result;
            SmartcardError _arg2;
            long _result2;
            byte[] _arg12;
            ISmartcardServiceCallback _arg22;
            SmartcardError _arg3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg1 = new SmartcardError();
                    closeChannel(_arg0, _arg1);
                    reply.writeNoException();
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    SmartcardError _arg03 = new SmartcardError();
                    String[] _result3 = getReaders(_arg03);
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    if (_arg03 != null) {
                        reply.writeInt(1);
                        _arg03.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    _arg1 = new SmartcardError();
                    boolean _result4 = isCardPresent(_arg02, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? 1 : 0);
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    _arg1 = new SmartcardError();
                    _result = getAtr(_arg02, _arg1);
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    ISmartcardServiceCallback _arg13 = org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub.asInterface(data.readStrongBinder());
                    _arg2 = new SmartcardError();
                    _result2 = openBasicChannel(_arg02, _arg13, _arg2);
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    if (_arg2 != null) {
                        reply.writeInt(1);
                        _arg2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    _arg12 = data.createByteArray();
                    _arg22 = org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub.asInterface(data.readStrongBinder());
                    _arg3 = new SmartcardError();
                    _result2 = openBasicChannelAid(_arg02, _arg12, _arg22, _arg3);
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    if (_arg3 != null) {
                        reply.writeInt(1);
                        _arg3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    _arg12 = data.createByteArray();
                    _arg22 = org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub.asInterface(data.readStrongBinder());
                    _arg3 = new SmartcardError();
                    _result2 = openLogicalChannel(_arg02, _arg12, _arg22, _arg3);
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    if (_arg3 != null) {
                        reply.writeInt(1);
                        _arg3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg12 = data.createByteArray();
                    _arg2 = new SmartcardError();
                    _result = transmit(_arg0, _arg12, _arg2);
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    if (_arg2 != null) {
                        reply.writeInt(1);
                        _arg2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg1 = new SmartcardError();
                    _result = getSelectResponse(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    _arg1 = new SmartcardError();
                    ISmartcardServiceReader _result5 = getReader(_arg02, _arg1);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    _arg12 = data.createByteArray();
                    String[] _arg23 = data.createStringArray();
                    ISmartcardServiceCallback _arg32 = org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub.asInterface(data.readStrongBinder());
                    SmartcardError _arg4 = new SmartcardError();
                    boolean[] _result6 = isNFCEventAllowed(_arg02, _arg12, _arg23, _arg32, _arg4);
                    reply.writeNoException();
                    reply.writeBooleanArray(_result6);
                    if (_arg4 != null) {
                        reply.writeInt(1);
                        _arg4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void closeChannel(long j, SmartcardError smartcardError) throws RemoteException;

    byte[] getAtr(String str, SmartcardError smartcardError) throws RemoteException;

    ISmartcardServiceReader getReader(String str, SmartcardError smartcardError) throws RemoteException;

    String[] getReaders(SmartcardError smartcardError) throws RemoteException;

    byte[] getSelectResponse(long j, SmartcardError smartcardError) throws RemoteException;

    boolean isCardPresent(String str, SmartcardError smartcardError) throws RemoteException;

    boolean[] isNFCEventAllowed(String str, byte[] bArr, String[] strArr, ISmartcardServiceCallback iSmartcardServiceCallback, SmartcardError smartcardError) throws RemoteException;

    long openBasicChannel(String str, ISmartcardServiceCallback iSmartcardServiceCallback, SmartcardError smartcardError) throws RemoteException;

    long openBasicChannelAid(String str, byte[] bArr, ISmartcardServiceCallback iSmartcardServiceCallback, SmartcardError smartcardError) throws RemoteException;

    long openLogicalChannel(String str, byte[] bArr, ISmartcardServiceCallback iSmartcardServiceCallback, SmartcardError smartcardError) throws RemoteException;

    byte[] transmit(long j, byte[] bArr, SmartcardError smartcardError) throws RemoteException;
}
