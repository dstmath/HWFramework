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
import com.android.ims.internal.uce.uceservice.IUceListener;

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
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(uceListener != null ? uceListener.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isServiceStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createOptionsService(IOptionsListener optionsListener, UceLong optionsServiceListenerHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(optionsListener != null ? optionsListener.asBinder() : null);
                    if (optionsServiceListenerHdl != null) {
                        _data.writeInt(1);
                        optionsServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        optionsServiceListenerHdl.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createPresenceService(IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(presenceServiceListener != null ? presenceServiceListener.asBinder() : null);
                    if (presenceServiceListenerHdl != null) {
                        _data.writeInt(1);
                        presenceServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        presenceServiceListenerHdl.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(7, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IPresenceService getPresenceService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return IPresenceService.Stub.asInterface(_reply.readStrongBinder());
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
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return IOptionsService.Stub.asInterface(_reply.readStrongBinder());
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: com.android.ims.internal.uce.common.UceLong} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v6, resolved type: com.android.ims.internal.uce.common.UceLong} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v10, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v12, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v14 */
        /* JADX WARNING: type inference failed for: r3v15 */
        /* JADX WARNING: type inference failed for: r3v16 */
        /* JADX WARNING: type inference failed for: r3v17 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg1 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = startService(IUceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = stopService();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isServiceStarted();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IOptionsListener _arg0 = IOptionsListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = UceLong.CREATOR.createFromParcel(data);
                        }
                        int _result4 = createOptionsService(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        if (_arg1 != 0) {
                            reply.writeInt(1);
                            _arg1.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        destroyOptionsService(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IPresenceListener _arg02 = IPresenceListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = UceLong.CREATOR.createFromParcel(data);
                        }
                        int _result5 = createPresenceService(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        if (_arg1 != 0) {
                            reply.writeInt(1);
                            _arg1.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        destroyPresenceService(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = getServiceStatus();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        IPresenceService _result7 = getPresenceService();
                        reply.writeNoException();
                        if (_result7 != null) {
                            _arg1 = _result7.asBinder();
                        }
                        reply.writeStrongBinder(_arg1);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IOptionsService _result8 = getOptionsService();
                        reply.writeNoException();
                        if (_result8 != null) {
                            _arg1 = _result8.asBinder();
                        }
                        reply.writeStrongBinder(_arg1);
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
