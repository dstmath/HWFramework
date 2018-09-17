package com.android.ims.internal.uce.uceservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.uce.common.UceLong;
import com.android.ims.internal.uce.options.IOptionsListener;
import com.android.ims.internal.uce.options.IOptionsService;
import com.android.ims.internal.uce.presence.IPresenceListener;
import com.android.ims.internal.uce.presence.IPresenceService;

public interface IUceService extends IInterface {

    public static abstract class Stub extends Binder implements IUceService {
        private static final String DESCRIPTOR = "com.android.ims.internal.uce.uceservice.IUceService";
        static final int TRANSACTION_createOptionsService = 4;
        static final int TRANSACTION_createPresenceService = 6;
        static final int TRANSACTION_destroyOptionsService = 5;
        static final int TRANSACTION_destroyPresenceService = 7;
        static final int TRANSACTION_getOptionsService = 10;
        static final int TRANSACTION_getPresenceService = 9;
        static final int TRANSACTION_getServiceStatus = 8;
        static final int TRANSACTION_isServiceStarted = 3;
        static final int TRANSACTION_startService = 1;
        static final int TRANSACTION_stopService = 2;

        private static class Proxy implements IUceService {
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

            public boolean startService(IUceListener uceListener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uceListener != null) {
                        iBinder = uceListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_startService, _data, _reply, 0);
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

            public boolean stopService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopService, _data, _reply, 0);
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

            public boolean isServiceStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isServiceStarted, _data, _reply, 0);
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

            public int createOptionsService(IOptionsListener optionsListener, UceLong optionsServiceListenerHdl) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (optionsListener != null) {
                        iBinder = optionsListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (optionsServiceListenerHdl != null) {
                        _data.writeInt(Stub.TRANSACTION_startService);
                        optionsServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_createOptionsService, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        optionsServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyOptionsService(int optionsServiceHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    this.mRemote.transact(Stub.TRANSACTION_destroyOptionsService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createPresenceService(IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (presenceServiceListener != null) {
                        iBinder = presenceServiceListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (presenceServiceListenerHdl != null) {
                        _data.writeInt(Stub.TRANSACTION_startService);
                        presenceServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_createPresenceService, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        presenceServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyPresenceService(int presenceServiceHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    this.mRemote.transact(Stub.TRANSACTION_destroyPresenceService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getServiceStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getServiceStatus, _data, _reply, 0);
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

            public IPresenceService getPresenceService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPresenceService, _data, _reply, 0);
                    _reply.readException();
                    IPresenceService _result = com.android.ims.internal.uce.presence.IPresenceService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IOptionsService getOptionsService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getOptionsService, _data, _reply, 0);
                    _reply.readException();
                    IOptionsService _result = com.android.ims.internal.uce.options.IOptionsService.Stub.asInterface(_reply.readStrongBinder());
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

        public static IUceService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUceService)) {
                return new Proxy(obj);
            }
            return (IUceService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            UceLong uceLong;
            int _result2;
            switch (code) {
                case TRANSACTION_startService /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startService(com.android.ims.internal.uce.uceservice.IUceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startService : 0);
                    return true;
                case TRANSACTION_stopService /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopService();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startService : 0);
                    return true;
                case TRANSACTION_isServiceStarted /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isServiceStarted();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startService : 0);
                    return true;
                case TRANSACTION_createOptionsService /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    IOptionsListener _arg0 = com.android.ims.internal.uce.options.IOptionsListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        uceLong = (UceLong) UceLong.CREATOR.createFromParcel(data);
                    } else {
                        uceLong = null;
                    }
                    _result2 = createOptionsService(_arg0, uceLong);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    if (uceLong != null) {
                        reply.writeInt(TRANSACTION_startService);
                        uceLong.writeToParcel(reply, TRANSACTION_startService);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_destroyOptionsService /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroyOptionsService(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createPresenceService /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    IPresenceListener _arg02 = com.android.ims.internal.uce.presence.IPresenceListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        uceLong = (UceLong) UceLong.CREATOR.createFromParcel(data);
                    } else {
                        uceLong = null;
                    }
                    _result2 = createPresenceService(_arg02, uceLong);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    if (uceLong != null) {
                        reply.writeInt(TRANSACTION_startService);
                        uceLong.writeToParcel(reply, TRANSACTION_startService);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_destroyPresenceService /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroyPresenceService(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getServiceStatus /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getServiceStatus();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_startService : 0);
                    return true;
                case TRANSACTION_getPresenceService /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    IPresenceService _result3 = getPresenceService();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                    return true;
                case TRANSACTION_getOptionsService /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    IOptionsService _result4 = getOptionsService();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int createOptionsService(IOptionsListener iOptionsListener, UceLong uceLong) throws RemoteException;

    int createPresenceService(IPresenceListener iPresenceListener, UceLong uceLong) throws RemoteException;

    void destroyOptionsService(int i) throws RemoteException;

    void destroyPresenceService(int i) throws RemoteException;

    IOptionsService getOptionsService() throws RemoteException;

    IPresenceService getPresenceService() throws RemoteException;

    boolean getServiceStatus() throws RemoteException;

    boolean isServiceStarted() throws RemoteException;

    boolean startService(IUceListener iUceListener) throws RemoteException;

    boolean stopService() throws RemoteException;
}
