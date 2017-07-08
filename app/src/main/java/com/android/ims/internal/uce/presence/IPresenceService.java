package com.android.ims.internal.uce.presence;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.uce.common.StatusCode;
import com.android.ims.internal.uce.common.UceLong;

public interface IPresenceService extends IInterface {

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

        private static class Proxy implements IPresenceService {
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

            public StatusCode getVersion(int presenceServiceHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    this.mRemote.transact(Stub.TRANSACTION_getVersion, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode addListener(int presenceServiceHdl, IPresenceListener presenceServiceListener, UceLong presenceServiceListenerHdl) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    if (presenceServiceListener != null) {
                        iBinder = presenceServiceListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (presenceServiceListenerHdl != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        presenceServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addListener, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    if (_reply.readInt() != 0) {
                        presenceServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode removeListener(int presenceServiceHdl, UceLong presenceServiceListenerHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    if (presenceServiceListenerHdl != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        presenceServiceListenerHdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_removeListener, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode reenableService(int presenceServiceHdl, int userData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeInt(userData);
                    this.mRemote.transact(Stub.TRANSACTION_reenableService, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode publishMyCap(int presenceServiceHdl, PresCapInfo myCapInfo, int userData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    if (myCapInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        myCapInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userData);
                    this.mRemote.transact(Stub.TRANSACTION_publishMyCap, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode getContactCap(int presenceServiceHdl, String remoteUri, int userData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeString(remoteUri);
                    _data.writeInt(userData);
                    this.mRemote.transact(Stub.TRANSACTION_getContactCap, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode getContactListCap(int presenceServiceHdl, String[] remoteUriList, int userData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeStringArray(remoteUriList);
                    _data.writeInt(userData);
                    this.mRemote.transact(Stub.TRANSACTION_getContactListCap, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode setNewFeatureTag(int presenceServiceHdl, String featureTag, PresServiceInfo serviceInfo, int userData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presenceServiceHdl);
                    _data.writeString(featureTag);
                    if (serviceInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        serviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userData);
                    this.mRemote.transact(Stub.TRANSACTION_setNewFeatureTag, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(_reply);
                    } else {
                        statusCode = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            StatusCode _result;
            int _arg0;
            switch (code) {
                case TRANSACTION_getVersion /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getVersion(data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_addListener /*2*/:
                    UceLong uceLong;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    IPresenceListener _arg1 = com.android.ims.internal.uce.presence.IPresenceListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        uceLong = (UceLong) UceLong.CREATOR.createFromParcel(data);
                    } else {
                        uceLong = null;
                    }
                    _result = addListener(_arg0, _arg1, uceLong);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    if (uceLong != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        uceLong.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_removeListener /*3*/:
                    UceLong uceLong2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        uceLong2 = (UceLong) UceLong.CREATOR.createFromParcel(data);
                    } else {
                        uceLong2 = null;
                    }
                    _result = removeListener(_arg0, uceLong2);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_reenableService /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = reenableService(data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_publishMyCap /*5*/:
                    PresCapInfo presCapInfo;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        presCapInfo = (PresCapInfo) PresCapInfo.CREATOR.createFromParcel(data);
                    } else {
                        presCapInfo = null;
                    }
                    _result = publishMyCap(_arg0, presCapInfo, data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getContactCap /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getContactCap(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getContactListCap /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getContactListCap(data.readInt(), data.createStringArray(), data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setNewFeatureTag /*8*/:
                    PresServiceInfo presServiceInfo;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    String _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        presServiceInfo = (PresServiceInfo) PresServiceInfo.CREATOR.createFromParcel(data);
                    } else {
                        presServiceInfo = null;
                    }
                    _result = setNewFeatureTag(_arg0, _arg12, presServiceInfo, data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
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

    StatusCode addListener(int i, IPresenceListener iPresenceListener, UceLong uceLong) throws RemoteException;

    StatusCode getContactCap(int i, String str, int i2) throws RemoteException;

    StatusCode getContactListCap(int i, String[] strArr, int i2) throws RemoteException;

    StatusCode getVersion(int i) throws RemoteException;

    StatusCode publishMyCap(int i, PresCapInfo presCapInfo, int i2) throws RemoteException;

    StatusCode reenableService(int i, int i2) throws RemoteException;

    StatusCode removeListener(int i, UceLong uceLong) throws RemoteException;

    StatusCode setNewFeatureTag(int i, String str, PresServiceInfo presServiceInfo, int i2) throws RemoteException;
}
