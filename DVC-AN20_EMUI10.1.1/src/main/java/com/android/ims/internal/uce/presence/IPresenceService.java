package com.android.ims.internal.uce.presence;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.uce.common.StatusCode;
import com.android.ims.internal.uce.common.UceLong;
import com.android.ims.internal.uce.presence.IPresenceListener;

public interface IPresenceService extends IInterface {
    @UnsupportedAppUsage
    StatusCode addListener(int i, IPresenceListener iPresenceListener, UceLong uceLong) throws RemoteException;

    @UnsupportedAppUsage
    StatusCode getContactCap(int i, String str, int i2) throws RemoteException;

    @UnsupportedAppUsage
    StatusCode getContactListCap(int i, String[] strArr, int i2) throws RemoteException;

    @UnsupportedAppUsage
    StatusCode getVersion(int i) throws RemoteException;

    @UnsupportedAppUsage
    StatusCode publishMyCap(int i, PresCapInfo presCapInfo, int i2) throws RemoteException;

    @UnsupportedAppUsage
    StatusCode reenableService(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    StatusCode removeListener(int i, UceLong uceLong) throws RemoteException;

    @UnsupportedAppUsage
    StatusCode setNewFeatureTag(int i, String str, PresServiceInfo presServiceInfo, int i2) throws RemoteException;

    public static class Default implements IPresenceService {
        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode getVersion(int presenceServiceHdl) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode addListener(int presenceServiceHdl, IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode removeListener(int presenceServiceHdl, UceLong presenceServiceListenerHdl) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode reenableService(int presenceServiceHdl, int userData) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode publishMyCap(int presenceServiceHdl, PresCapInfo myCapInfo, int userData) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode getContactCap(int presenceServiceHdl, String remoteUri, int userData) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode getContactListCap(int presenceServiceHdl, String[] remoteUriList, int userData) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.uce.presence.IPresenceService
        public StatusCode setNewFeatureTag(int presenceServiceHdl, String featureTag, PresServiceInfo serviceInfo, int userData) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPresenceService {
        private static final String DESCRIPTOR = "com.android.ims.internal.uce.presence.IPresenceService";
        static final int TRANSACTION_addListener = 2;
        static final int TRANSACTION_getContactCap = 6;
        static final int TRANSACTION_getContactListCap = 7;
        static final int TRANSACTION_getVersion = 1;
        static final int TRANSACTION_publishMyCap = 5;
        static final int TRANSACTION_reenableService = 4;
        static final int TRANSACTION_removeListener = 3;
        static final int TRANSACTION_setNewFeatureTag = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPresenceService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPresenceService)) {
                return new Proxy(obj);
            }
            return (IPresenceService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getVersion";
                case 2:
                    return "addListener";
                case 3:
                    return "removeListener";
                case 4:
                    return "reenableService";
                case 5:
                    return "publishMyCap";
                case 6:
                    return "getContactCap";
                case 7:
                    return "getContactListCap";
                case 8:
                    return "setNewFeatureTag";
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
            UceLong _arg2;
            UceLong _arg1;
            PresCapInfo _arg12;
            PresServiceInfo _arg22;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        StatusCode _result = getVersion(data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        IPresenceListener _arg13 = IPresenceListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg2 = UceLong.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        StatusCode _result2 = addListener(_arg0, _arg13, _arg2);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        if (_arg2 != null) {
                            reply.writeInt(1);
                            _arg2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = UceLong.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        StatusCode _result3 = removeListener(_arg02, _arg1);
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        StatusCode _result4 = reenableService(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = PresCapInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        StatusCode _result5 = publishMyCap(_arg03, _arg12, data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        StatusCode _result6 = getContactCap(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        StatusCode _result7 = getContactListCap(data.readInt(), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = PresServiceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        StatusCode _result8 = setNewFeatureTag(_arg04, _arg14, _arg22, data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements IPresenceService {
            public static IPresenceService sDefaultImpl;
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode getVersion(int presenceServiceHdl) throws RemoteException {
                StatusCode _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVersion(presenceServiceHdl);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode addListener(int presenceServiceHdl, IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    StatusCode _result = null;
                    _data.writeStrongBinder(presenceServiceListener != null ? presenceServiceListener.asBinder() : null);
                    if (presenceServiceListenerHdl != null) {
                        _data.writeInt(1);
                        presenceServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addListener(presenceServiceHdl, presenceServiceListener, presenceServiceListenerHdl);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
                    }
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode removeListener(int presenceServiceHdl, UceLong presenceServiceListenerHdl) throws RemoteException {
                StatusCode _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    if (presenceServiceListenerHdl != null) {
                        _data.writeInt(1);
                        presenceServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeListener(presenceServiceHdl, presenceServiceListenerHdl);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode reenableService(int presenceServiceHdl, int userData) throws RemoteException {
                StatusCode _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeInt(userData);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reenableService(presenceServiceHdl, userData);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode publishMyCap(int presenceServiceHdl, PresCapInfo myCapInfo, int userData) throws RemoteException {
                StatusCode _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    if (myCapInfo != null) {
                        _data.writeInt(1);
                        myCapInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userData);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().publishMyCap(presenceServiceHdl, myCapInfo, userData);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode getContactCap(int presenceServiceHdl, String remoteUri, int userData) throws RemoteException {
                StatusCode _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeString(remoteUri);
                    _data.writeInt(userData);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getContactCap(presenceServiceHdl, remoteUri, userData);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode getContactListCap(int presenceServiceHdl, String[] remoteUriList, int userData) throws RemoteException {
                StatusCode _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeStringArray(remoteUriList);
                    _data.writeInt(userData);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getContactListCap(presenceServiceHdl, remoteUriList, userData);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.ims.internal.uce.presence.IPresenceService
            public StatusCode setNewFeatureTag(int presenceServiceHdl, String featureTag, PresServiceInfo serviceInfo, int userData) throws RemoteException {
                StatusCode _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeString(featureTag);
                    if (serviceInfo != null) {
                        _data.writeInt(1);
                        serviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userData);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNewFeatureTag(presenceServiceHdl, featureTag, serviceInfo, userData);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StatusCode.CREATOR.createFromParcel(_reply);
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
        }

        public static boolean setDefaultImpl(IPresenceService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPresenceService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
