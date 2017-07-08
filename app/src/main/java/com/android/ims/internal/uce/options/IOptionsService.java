package com.android.ims.internal.uce.options;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.uce.common.CapInfo;
import com.android.ims.internal.uce.common.StatusCode;
import com.android.ims.internal.uce.common.UceLong;

public interface IOptionsService extends IInterface {

    public static abstract class Stub extends Binder implements IOptionsService {
        private static final String DESCRIPTOR = "com.android.ims.internal.uce.options.IOptionsService";
        static final int TRANSACTION_addListener = 2;
        static final int TRANSACTION_getContactCap = 6;
        static final int TRANSACTION_getContactListCap = 7;
        static final int TRANSACTION_getMyInfo = 5;
        static final int TRANSACTION_getVersion = 1;
        static final int TRANSACTION_removeListener = 3;
        static final int TRANSACTION_responseIncomingOptions = 8;
        static final int TRANSACTION_setMyInfo = 4;

        private static class Proxy implements IOptionsService {
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

            public StatusCode getVersion(int optionsServiceHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
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

            public StatusCode addListener(int optionsServiceHandle, IOptionsListener optionsListener, UceLong optionsServiceListenerHdl) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    if (optionsListener != null) {
                        iBinder = optionsListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (optionsServiceListenerHdl != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        optionsServiceListenerHdl.writeToParcel(_data, 0);
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
                        optionsServiceListenerHdl.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return statusCode;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StatusCode removeListener(int optionsServiceHandle, UceLong optionsServiceListenerHdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    if (optionsServiceListenerHdl != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        optionsServiceListenerHdl.writeToParcel(_data, 0);
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

            public StatusCode setMyInfo(int optionsServiceHandle, CapInfo capInfo, int reqUserData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    if (capInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        capInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(reqUserData);
                    this.mRemote.transact(Stub.TRANSACTION_setMyInfo, _data, _reply, 0);
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

            public StatusCode getMyInfo(int optionsServiceHandle, int reqUserdata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    _data.writeInt(reqUserdata);
                    this.mRemote.transact(Stub.TRANSACTION_getMyInfo, _data, _reply, 0);
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

            public StatusCode getContactCap(int optionsServiceHandle, String remoteURI, int reqUserData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    _data.writeString(remoteURI);
                    _data.writeInt(reqUserData);
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

            public StatusCode getContactListCap(int optionsServiceHandle, String[] remoteURIList, int reqUserData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    _data.writeStringArray(remoteURIList);
                    _data.writeInt(reqUserData);
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

            public StatusCode responseIncomingOptions(int optionsServiceHandle, int tId, int sipResponseCode, String reasonPhrase, OptionsCapInfo capInfo, boolean bContactInBL) throws RemoteException {
                int i = Stub.TRANSACTION_getVersion;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    StatusCode statusCode;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(optionsServiceHandle);
                    _data.writeInt(tId);
                    _data.writeInt(sipResponseCode);
                    _data.writeString(reasonPhrase);
                    if (capInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersion);
                        capInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!bContactInBL) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_responseIncomingOptions, _data, _reply, 0);
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

        public static IOptionsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOptionsService)) {
                return new Proxy(obj);
            }
            return (IOptionsService) iin;
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
                    IOptionsListener _arg1 = com.android.ims.internal.uce.options.IOptionsListener.Stub.asInterface(data.readStrongBinder());
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
                case TRANSACTION_setMyInfo /*4*/:
                    CapInfo capInfo;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        capInfo = (CapInfo) CapInfo.CREATOR.createFromParcel(data);
                    } else {
                        capInfo = null;
                    }
                    _result = setMyInfo(_arg0, capInfo, data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getVersion);
                        _result.writeToParcel(reply, TRANSACTION_getVersion);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getMyInfo /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMyInfo(data.readInt(), data.readInt());
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
                case TRANSACTION_responseIncomingOptions /*8*/:
                    OptionsCapInfo optionsCapInfo;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    int _arg12 = data.readInt();
                    int _arg2 = data.readInt();
                    String _arg3 = data.readString();
                    if (data.readInt() != 0) {
                        optionsCapInfo = (OptionsCapInfo) OptionsCapInfo.CREATOR.createFromParcel(data);
                    } else {
                        optionsCapInfo = null;
                    }
                    _result = responseIncomingOptions(_arg0, _arg12, _arg2, _arg3, optionsCapInfo, data.readInt() != 0);
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

    StatusCode addListener(int i, IOptionsListener iOptionsListener, UceLong uceLong) throws RemoteException;

    StatusCode getContactCap(int i, String str, int i2) throws RemoteException;

    StatusCode getContactListCap(int i, String[] strArr, int i2) throws RemoteException;

    StatusCode getMyInfo(int i, int i2) throws RemoteException;

    StatusCode getVersion(int i) throws RemoteException;

    StatusCode removeListener(int i, UceLong uceLong) throws RemoteException;

    StatusCode responseIncomingOptions(int i, int i2, int i3, String str, OptionsCapInfo optionsCapInfo, boolean z) throws RemoteException;

    StatusCode setMyInfo(int i, CapInfo capInfo, int i2) throws RemoteException;
}
