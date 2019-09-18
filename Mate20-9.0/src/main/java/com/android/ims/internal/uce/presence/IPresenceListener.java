package com.android.ims.internal.uce.presence;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.uce.common.StatusCode;

public interface IPresenceListener extends IInterface {

    public static abstract class Stub extends Binder implements IPresenceListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.uce.presence.IPresenceListener";
        static final int TRANSACTION_capInfoReceived = 7;
        static final int TRANSACTION_cmdStatus = 5;
        static final int TRANSACTION_getVersionCb = 1;
        static final int TRANSACTION_listCapInfoReceived = 8;
        static final int TRANSACTION_publishTriggering = 4;
        static final int TRANSACTION_serviceAvailable = 2;
        static final int TRANSACTION_serviceUnAvailable = 3;
        static final int TRANSACTION_sipResponseReceived = 6;
        static final int TRANSACTION_unpublishMessageSent = 9;

        private static class Proxy implements IPresenceListener {
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

            public void getVersionCb(String version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(version);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serviceAvailable(StatusCode statusCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (statusCode != null) {
                        _data.writeInt(1);
                        statusCode.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serviceUnAvailable(StatusCode statusCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (statusCode != null) {
                        _data.writeInt(1);
                        statusCode.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void publishTriggering(PresPublishTriggerType publishTrigger) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (publishTrigger != null) {
                        _data.writeInt(1);
                        publishTrigger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cmdStatus(PresCmdStatus cmdStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cmdStatus != null) {
                        _data.writeInt(1);
                        cmdStatus.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sipResponseReceived(PresSipResponse sipResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sipResponse != null) {
                        _data.writeInt(1);
                        sipResponse.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void capInfoReceived(String presentityURI, PresTupleInfo[] tupleInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(presentityURI);
                    _data.writeTypedArray(tupleInfo, 0);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void listCapInfoReceived(PresRlmiInfo rlmiInfo, PresResInfo[] resInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rlmiInfo != null) {
                        _data.writeInt(1);
                        rlmiInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(resInfo, 0);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unpublishMessageSent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPresenceListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPresenceListener)) {
                return new Proxy(obj);
            }
            return (IPresenceListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: com.android.ims.internal.uce.common.StatusCode} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: com.android.ims.internal.uce.common.StatusCode} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v11, resolved type: com.android.ims.internal.uce.presence.PresPublishTriggerType} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: com.android.ims.internal.uce.presence.PresCmdStatus} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v19, resolved type: com.android.ims.internal.uce.presence.PresSipResponse} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v24, resolved type: com.android.ims.internal.uce.presence.PresRlmiInfo} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v29 */
        /* JADX WARNING: type inference failed for: r1v30 */
        /* JADX WARNING: type inference failed for: r1v31 */
        /* JADX WARNING: type inference failed for: r1v32 */
        /* JADX WARNING: type inference failed for: r1v33 */
        /* JADX WARNING: type inference failed for: r1v34 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg0 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        getVersionCb(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = StatusCode.CREATOR.createFromParcel(data);
                        }
                        serviceAvailable(_arg0);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = StatusCode.CREATOR.createFromParcel(data);
                        }
                        serviceUnAvailable(_arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PresPublishTriggerType.CREATOR.createFromParcel(data);
                        }
                        publishTriggering(_arg0);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PresCmdStatus.CREATOR.createFromParcel(data);
                        }
                        cmdStatus(_arg0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PresSipResponse.CREATOR.createFromParcel(data);
                        }
                        sipResponseReceived(_arg0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        capInfoReceived(data.readString(), (PresTupleInfo[]) data.createTypedArray(PresTupleInfo.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PresRlmiInfo.CREATOR.createFromParcel(data);
                        }
                        listCapInfoReceived(_arg0, (PresResInfo[]) data.createTypedArray(PresResInfo.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        unpublishMessageSent();
                        reply.writeNoException();
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

    void capInfoReceived(String str, PresTupleInfo[] presTupleInfoArr) throws RemoteException;

    void cmdStatus(PresCmdStatus presCmdStatus) throws RemoteException;

    void getVersionCb(String str) throws RemoteException;

    void listCapInfoReceived(PresRlmiInfo presRlmiInfo, PresResInfo[] presResInfoArr) throws RemoteException;

    void publishTriggering(PresPublishTriggerType presPublishTriggerType) throws RemoteException;

    void serviceAvailable(StatusCode statusCode) throws RemoteException;

    void serviceUnAvailable(StatusCode statusCode) throws RemoteException;

    void sipResponseReceived(PresSipResponse presSipResponse) throws RemoteException;

    void unpublishMessageSent() throws RemoteException;
}
