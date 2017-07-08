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
                    this.mRemote.transact(Stub.TRANSACTION_getVersionCb, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        statusCode.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_serviceAvailable, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        statusCode.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_serviceUnAvailable, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        publishTrigger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_publishTriggering, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        cmdStatus.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_cmdStatus, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        sipResponse.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sipResponseReceived, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_capInfoReceived, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        rlmiInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(resInfo, 0);
                    this.mRemote.transact(Stub.TRANSACTION_listCapInfoReceived, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            StatusCode statusCode;
            switch (code) {
                case TRANSACTION_getVersionCb /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    getVersionCb(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_serviceAvailable /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(data);
                    } else {
                        statusCode = null;
                    }
                    serviceAvailable(statusCode);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_serviceUnAvailable /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(data);
                    } else {
                        statusCode = null;
                    }
                    serviceUnAvailable(statusCode);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_publishTriggering /*4*/:
                    PresPublishTriggerType presPublishTriggerType;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        presPublishTriggerType = (PresPublishTriggerType) PresPublishTriggerType.CREATOR.createFromParcel(data);
                    } else {
                        presPublishTriggerType = null;
                    }
                    publishTriggering(presPublishTriggerType);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cmdStatus /*5*/:
                    PresCmdStatus presCmdStatus;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        presCmdStatus = (PresCmdStatus) PresCmdStatus.CREATOR.createFromParcel(data);
                    } else {
                        presCmdStatus = null;
                    }
                    cmdStatus(presCmdStatus);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sipResponseReceived /*6*/:
                    PresSipResponse presSipResponse;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        presSipResponse = (PresSipResponse) PresSipResponse.CREATOR.createFromParcel(data);
                    } else {
                        presSipResponse = null;
                    }
                    sipResponseReceived(presSipResponse);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_capInfoReceived /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    capInfoReceived(data.readString(), (PresTupleInfo[]) data.createTypedArray(PresTupleInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_listCapInfoReceived /*8*/:
                    PresRlmiInfo presRlmiInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        presRlmiInfo = (PresRlmiInfo) PresRlmiInfo.CREATOR.createFromParcel(data);
                    } else {
                        presRlmiInfo = null;
                    }
                    listCapInfoReceived(presRlmiInfo, (PresResInfo[]) data.createTypedArray(PresResInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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
}
