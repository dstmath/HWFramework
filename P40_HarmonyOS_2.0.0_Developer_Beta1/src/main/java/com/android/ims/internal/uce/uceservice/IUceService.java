package com.android.ims.internal.uce.uceservice;

import android.annotation.UnsupportedAppUsage;
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
    @UnsupportedAppUsage
    int createOptionsService(IOptionsListener iOptionsListener, UceLong uceLong) throws RemoteException;

    int createOptionsServiceForSubscription(IOptionsListener iOptionsListener, UceLong uceLong, String str) throws RemoteException;

    @UnsupportedAppUsage
    int createPresenceService(IPresenceListener iPresenceListener, UceLong uceLong) throws RemoteException;

    int createPresenceServiceForSubscription(IPresenceListener iPresenceListener, UceLong uceLong, String str) throws RemoteException;

    @UnsupportedAppUsage
    void destroyOptionsService(int i) throws RemoteException;

    @UnsupportedAppUsage
    void destroyPresenceService(int i) throws RemoteException;

    @UnsupportedAppUsage
    IOptionsService getOptionsService() throws RemoteException;

    IOptionsService getOptionsServiceForSubscription(String str) throws RemoteException;

    @UnsupportedAppUsage
    IPresenceService getPresenceService() throws RemoteException;

    IPresenceService getPresenceServiceForSubscription(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean getServiceStatus() throws RemoteException;

    @UnsupportedAppUsage
    boolean isServiceStarted() throws RemoteException;

    @UnsupportedAppUsage
    boolean startService(IUceListener iUceListener) throws RemoteException;

    @UnsupportedAppUsage
    boolean stopService() throws RemoteException;

    public static class Default implements IUceService {
        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public boolean startService(IUceListener uceListener) throws RemoteException {
            return false;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public boolean stopService() throws RemoteException {
            return false;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public boolean isServiceStarted() throws RemoteException {
            return false;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public int createOptionsService(IOptionsListener optionsListener, UceLong optionsServiceListenerHdl) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public int createOptionsServiceForSubscription(IOptionsListener optionsListener, UceLong optionsServiceListenerHdl, String iccId) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public void destroyOptionsService(int optionsServiceHandle) throws RemoteException {
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public int createPresenceService(IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public int createPresenceServiceForSubscription(IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl, String iccId) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public void destroyPresenceService(int presenceServiceHdl) throws RemoteException {
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public boolean getServiceStatus() throws RemoteException {
            return false;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public IPresenceService getPresenceService() throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public IPresenceService getPresenceServiceForSubscription(String iccId) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public IOptionsService getOptionsService() throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.uceservice.IUceService
        public IOptionsService getOptionsServiceForSubscription(String iccId) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUceService {
        private static final String DESCRIPTOR = "com.android.ims.internal.uce.uceservice.IUceService";
        static final int TRANSACTION_createOptionsService = 4;
        static final int TRANSACTION_createOptionsServiceForSubscription = 5;
        static final int TRANSACTION_createPresenceService = 7;
        static final int TRANSACTION_createPresenceServiceForSubscription = 8;
        static final int TRANSACTION_destroyOptionsService = 6;
        static final int TRANSACTION_destroyPresenceService = 9;
        static final int TRANSACTION_getOptionsService = 13;
        static final int TRANSACTION_getOptionsServiceForSubscription = 14;
        static final int TRANSACTION_getPresenceService = 11;
        static final int TRANSACTION_getPresenceServiceForSubscription = 12;
        static final int TRANSACTION_getServiceStatus = 10;
        static final int TRANSACTION_isServiceStarted = 3;
        static final int TRANSACTION_startService = 1;
        static final int TRANSACTION_stopService = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "startService";
                case 2:
                    return "stopService";
                case 3:
                    return "isServiceStarted";
                case 4:
                    return "createOptionsService";
                case 5:
                    return "createOptionsServiceForSubscription";
                case 6:
                    return "destroyOptionsService";
                case 7:
                    return "createPresenceService";
                case 8:
                    return "createPresenceServiceForSubscription";
                case 9:
                    return "destroyPresenceService";
                case 10:
                    return "getServiceStatus";
                case 11:
                    return "getPresenceService";
                case 12:
                    return "getPresenceServiceForSubscription";
                case 13:
                    return "getOptionsService";
                case 14:
                    return "getOptionsServiceForSubscription";
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
            UceLong _arg1;
            UceLong _arg12;
            UceLong _arg13;
            UceLong _arg14;
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startService = startService(IUceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(startService ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopService = stopService();
                        reply.writeNoException();
                        reply.writeInt(stopService ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isServiceStarted = isServiceStarted();
                        reply.writeNoException();
                        reply.writeInt(isServiceStarted ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IOptionsListener _arg0 = IOptionsListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = UceLong.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result = createOptionsService(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        if (_arg1 != null) {
                            reply.writeInt(1);
                            _arg1.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IOptionsListener _arg02 = IOptionsListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = UceLong.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result2 = createOptionsServiceForSubscription(_arg02, _arg12, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        if (_arg12 != null) {
                            reply.writeInt(1);
                            _arg12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        destroyOptionsService(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IPresenceListener _arg03 = IPresenceListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = UceLong.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        int _result3 = createPresenceService(_arg03, _arg13);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        if (_arg13 != null) {
                            reply.writeInt(1);
                            _arg13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        IPresenceListener _arg04 = IPresenceListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = UceLong.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        int _result4 = createPresenceServiceForSubscription(_arg04, _arg14, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        if (_arg14 != null) {
                            reply.writeInt(1);
                            _arg14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        destroyPresenceService(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean serviceStatus = getServiceStatus();
                        reply.writeNoException();
                        reply.writeInt(serviceStatus ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IPresenceService _result5 = getPresenceService();
                        reply.writeNoException();
                        if (_result5 != null) {
                            iBinder = _result5.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        IPresenceService _result6 = getPresenceServiceForSubscription(data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            iBinder = _result6.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        IOptionsService _result7 = getOptionsService();
                        reply.writeNoException();
                        if (_result7 != null) {
                            iBinder = _result7.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        IOptionsService _result8 = getOptionsServiceForSubscription(data.readString());
                        reply.writeNoException();
                        if (_result8 != null) {
                            iBinder = _result8.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
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
        public static class Proxy implements IUceService {
            public static IUceService sDefaultImpl;
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

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public boolean startService(IUceListener uceListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(uceListener != null ? uceListener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startService(uceListener);
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

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public boolean stopService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopService();
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

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public boolean isServiceStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isServiceStarted();
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

            @Override // com.android.ims.internal.uce.uceservice.IUceService
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
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createOptionsService(optionsListener, optionsServiceListenerHdl);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        optionsServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public int createOptionsServiceForSubscription(IOptionsListener optionsListener, UceLong optionsServiceListenerHdl, String iccId) throws RemoteException {
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
                    _data.writeString(iccId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createOptionsServiceForSubscription(optionsListener, optionsServiceListenerHdl, iccId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        optionsServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public void destroyOptionsService(int optionsServiceHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyOptionsService(optionsServiceHandle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
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
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createPresenceService(presenceServiceListener, presenceServiceListenerHdl);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        presenceServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public int createPresenceServiceForSubscription(IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl, String iccId) throws RemoteException {
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
                    _data.writeString(iccId);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createPresenceServiceForSubscription(presenceServiceListener, presenceServiceListenerHdl, iccId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        presenceServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public void destroyPresenceService(int presenceServiceHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyPresenceService(presenceServiceHdl);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public boolean getServiceStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceStatus();
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

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public IPresenceService getPresenceService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPresenceService();
                    }
                    _reply.readException();
                    IPresenceService _result = IPresenceService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public IPresenceService getPresenceServiceForSubscription(String iccId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iccId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPresenceServiceForSubscription(iccId);
                    }
                    _reply.readException();
                    IPresenceService _result = IPresenceService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public IOptionsService getOptionsService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOptionsService();
                    }
                    _reply.readException();
                    IOptionsService _result = IOptionsService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.uce.uceservice.IUceService
            public IOptionsService getOptionsServiceForSubscription(String iccId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iccId);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOptionsServiceForSubscription(iccId);
                    }
                    _reply.readException();
                    IOptionsService _result = IOptionsService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUceService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUceService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
