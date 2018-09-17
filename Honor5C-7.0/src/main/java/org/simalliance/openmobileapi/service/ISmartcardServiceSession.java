package org.simalliance.openmobileapi.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISmartcardServiceSession extends IInterface {

    public static abstract class Stub extends Binder implements ISmartcardServiceSession {
        private static final String DESCRIPTOR = "org.simalliance.openmobileapi.service.ISmartcardServiceSession";
        static final int TRANSACTION_close = 3;
        static final int TRANSACTION_closeChannels = 4;
        static final int TRANSACTION_getAtr = 2;
        static final int TRANSACTION_getReader = 1;
        static final int TRANSACTION_isClosed = 5;
        static final int TRANSACTION_openBasicChannel = 6;
        static final int TRANSACTION_openBasicChannelAid = 7;
        static final int TRANSACTION_openLogicalChannel = 8;

        private static class Proxy implements ISmartcardServiceSession {
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

            public ISmartcardServiceReader getReader() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getReader, _data, _reply, 0);
                    _reply.readException();
                    ISmartcardServiceReader _result = org.simalliance.openmobileapi.service.ISmartcardServiceReader.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getAtr() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAtr, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void close(SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);
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

            public void closeChannels(SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_closeChannels, _data, _reply, 0);
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

            public boolean isClosed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isClosed, _data, _reply, 0);
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

            public ISmartcardServiceChannel openBasicChannel(ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_openBasicChannel, _data, _reply, 0);
                    _reply.readException();
                    ISmartcardServiceChannel _result = org.simalliance.openmobileapi.service.ISmartcardServiceChannel.Stub.asInterface(_reply.readStrongBinder());
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

            public ISmartcardServiceChannel openBasicChannelAid(byte[] aid, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(aid);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_openBasicChannelAid, _data, _reply, 0);
                    _reply.readException();
                    ISmartcardServiceChannel _result = org.simalliance.openmobileapi.service.ISmartcardServiceChannel.Stub.asInterface(_reply.readStrongBinder());
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

            public ISmartcardServiceChannel openLogicalChannel(byte[] aid, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(aid);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_openLogicalChannel, _data, _reply, 0);
                    _reply.readException();
                    ISmartcardServiceChannel _result = org.simalliance.openmobileapi.service.ISmartcardServiceChannel.Stub.asInterface(_reply.readStrongBinder());
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

        public static ISmartcardServiceSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISmartcardServiceSession)) {
                return new Proxy(obj);
            }
            return (ISmartcardServiceSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SmartcardError _arg0;
            ISmartcardServiceChannel _result;
            byte[] _arg02;
            ISmartcardServiceCallback _arg1;
            SmartcardError _arg2;
            switch (code) {
                case TRANSACTION_getReader /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    ISmartcardServiceReader _result2 = getReader();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                    return true;
                case TRANSACTION_getAtr /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result3 = getAtr();
                    reply.writeNoException();
                    reply.writeByteArray(_result3);
                    return true;
                case TRANSACTION_close /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = new SmartcardError();
                    close(_arg0);
                    reply.writeNoException();
                    if (_arg0 != null) {
                        reply.writeInt(TRANSACTION_getReader);
                        _arg0.writeToParcel(reply, TRANSACTION_getReader);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_closeChannels /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = new SmartcardError();
                    closeChannels(_arg0);
                    reply.writeNoException();
                    if (_arg0 != null) {
                        reply.writeInt(TRANSACTION_getReader);
                        _arg0.writeToParcel(reply, TRANSACTION_getReader);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isClosed /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result4 = isClosed();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getReader : 0);
                    return true;
                case TRANSACTION_openBasicChannel /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    ISmartcardServiceCallback _arg03 = org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub.asInterface(data.readStrongBinder());
                    SmartcardError _arg12 = new SmartcardError();
                    _result = openBasicChannel(_arg03, _arg12);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    if (_arg12 != null) {
                        reply.writeInt(TRANSACTION_getReader);
                        _arg12.writeToParcel(reply, TRANSACTION_getReader);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_openBasicChannelAid /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.createByteArray();
                    _arg1 = org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub.asInterface(data.readStrongBinder());
                    _arg2 = new SmartcardError();
                    _result = openBasicChannelAid(_arg02, _arg1, _arg2);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    if (_arg2 != null) {
                        reply.writeInt(TRANSACTION_getReader);
                        _arg2.writeToParcel(reply, TRANSACTION_getReader);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_openLogicalChannel /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.createByteArray();
                    _arg1 = org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub.asInterface(data.readStrongBinder());
                    _arg2 = new SmartcardError();
                    _result = openLogicalChannel(_arg02, _arg1, _arg2);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    if (_arg2 != null) {
                        reply.writeInt(TRANSACTION_getReader);
                        _arg2.writeToParcel(reply, TRANSACTION_getReader);
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

    void close(SmartcardError smartcardError) throws RemoteException;

    void closeChannels(SmartcardError smartcardError) throws RemoteException;

    byte[] getAtr() throws RemoteException;

    ISmartcardServiceReader getReader() throws RemoteException;

    boolean isClosed() throws RemoteException;

    ISmartcardServiceChannel openBasicChannel(ISmartcardServiceCallback iSmartcardServiceCallback, SmartcardError smartcardError) throws RemoteException;

    ISmartcardServiceChannel openBasicChannelAid(byte[] bArr, ISmartcardServiceCallback iSmartcardServiceCallback, SmartcardError smartcardError) throws RemoteException;

    ISmartcardServiceChannel openLogicalChannel(byte[] bArr, ISmartcardServiceCallback iSmartcardServiceCallback, SmartcardError smartcardError) throws RemoteException;
}
