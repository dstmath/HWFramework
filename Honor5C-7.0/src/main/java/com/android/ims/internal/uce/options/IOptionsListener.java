package com.android.ims.internal.uce.options;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.uce.common.StatusCode;

public interface IOptionsListener extends IInterface {

    public static abstract class Stub extends Binder implements IOptionsListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.uce.options.IOptionsListener";
        static final int TRANSACTION_cmdStatus = 5;
        static final int TRANSACTION_getVersionCb = 1;
        static final int TRANSACTION_incomingOptions = 6;
        static final int TRANSACTION_serviceAvailable = 2;
        static final int TRANSACTION_serviceUnavailable = 3;
        static final int TRANSACTION_sipResponseReceived = 4;

        private static class Proxy implements IOptionsListener {
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

            public void serviceUnavailable(StatusCode statusCode) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_serviceUnavailable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sipResponseReceived(String uri, OptionsSipResponse sipResponse, OptionsCapInfo capInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    if (sipResponse != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        sipResponse.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (capInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        capInfo.writeToParcel(_data, 0);
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

            public void cmdStatus(OptionsCmdStatus cmdStatus) throws RemoteException {
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

            public void incomingOptions(String uri, OptionsCapInfo capInfo, int tID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    if (capInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getVersionCb);
                        capInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(tID);
                    this.mRemote.transact(Stub.TRANSACTION_incomingOptions, _data, _reply, 0);
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

        public static IOptionsListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOptionsListener)) {
                return new Proxy(obj);
            }
            return (IOptionsListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            StatusCode statusCode;
            String _arg0;
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
                case TRANSACTION_serviceUnavailable /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        statusCode = (StatusCode) StatusCode.CREATOR.createFromParcel(data);
                    } else {
                        statusCode = null;
                    }
                    serviceUnavailable(statusCode);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sipResponseReceived /*4*/:
                    OptionsSipResponse optionsSipResponse;
                    OptionsCapInfo optionsCapInfo;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        optionsSipResponse = (OptionsSipResponse) OptionsSipResponse.CREATOR.createFromParcel(data);
                    } else {
                        optionsSipResponse = null;
                    }
                    if (data.readInt() != 0) {
                        optionsCapInfo = (OptionsCapInfo) OptionsCapInfo.CREATOR.createFromParcel(data);
                    } else {
                        optionsCapInfo = null;
                    }
                    sipResponseReceived(_arg0, optionsSipResponse, optionsCapInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cmdStatus /*5*/:
                    OptionsCmdStatus optionsCmdStatus;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        optionsCmdStatus = (OptionsCmdStatus) OptionsCmdStatus.CREATOR.createFromParcel(data);
                    } else {
                        optionsCmdStatus = null;
                    }
                    cmdStatus(optionsCmdStatus);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_incomingOptions /*6*/:
                    OptionsCapInfo optionsCapInfo2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        optionsCapInfo2 = (OptionsCapInfo) OptionsCapInfo.CREATOR.createFromParcel(data);
                    } else {
                        optionsCapInfo2 = null;
                    }
                    incomingOptions(_arg0, optionsCapInfo2, data.readInt());
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

    void cmdStatus(OptionsCmdStatus optionsCmdStatus) throws RemoteException;

    void getVersionCb(String str) throws RemoteException;

    void incomingOptions(String str, OptionsCapInfo optionsCapInfo, int i) throws RemoteException;

    void serviceAvailable(StatusCode statusCode) throws RemoteException;

    void serviceUnavailable(StatusCode statusCode) throws RemoteException;

    void sipResponseReceived(String str, OptionsSipResponse optionsSipResponse, OptionsCapInfo optionsCapInfo) throws RemoteException;
}
