package com.android.server.policy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;

public interface IInternetAudioService extends IInterface {

    public static abstract class Stub extends Binder implements IInternetAudioService {
        private static final String DESCRIPTOR = "com.huawei.internetaudioservice.IInternetAudioService";
        static final int TRANSACTION_dispatchMediaKeyEvent = 3;
        static final int TRANSACTION_enableInternetAudioService = 1;
        static final int TRANSACTION_getCurrentTarget = 4;
        static final int TRANSACTION_isInternetAudioServiceEnable = 2;
        static final int TRANSACTION_setCurrentTarget = 5;

        private static class Proxy implements IInternetAudioService {
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

            public void enableInternetAudioService(boolean enable) throws RemoteException {
                int i = Stub.TRANSACTION_enableInternetAudioService;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enableInternetAudioService, _data, null, Stub.TRANSACTION_enableInternetAudioService);
                } finally {
                    _data.recycle();
                }
            }

            public boolean isInternetAudioServiceEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isInternetAudioServiceEnable, _data, _reply, 0);
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

            public void dispatchMediaKeyEvent(KeyEvent keyevent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyevent != null) {
                        _data.writeInt(Stub.TRANSACTION_enableInternetAudioService);
                        keyevent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_dispatchMediaKeyEvent, _data, null, Stub.TRANSACTION_enableInternetAudioService);
                } finally {
                    _data.recycle();
                }
            }

            public String getCurrentTarget() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentTarget, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCurrentTarget(String newTarget) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(newTarget);
                    this.mRemote.transact(Stub.TRANSACTION_setCurrentTarget, _data, null, Stub.TRANSACTION_enableInternetAudioService);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInternetAudioService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInternetAudioService)) {
                return new Proxy(obj);
            }
            return (IInternetAudioService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0 = 0;
            switch (code) {
                case TRANSACTION_enableInternetAudioService /*1*/:
                    boolean _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = true;
                    }
                    enableInternetAudioService(_arg02);
                    return true;
                case TRANSACTION_isInternetAudioServiceEnable /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isInternetAudioServiceEnable();
                    reply.writeNoException();
                    if (_result) {
                        _arg0 = TRANSACTION_enableInternetAudioService;
                    }
                    reply.writeInt(_arg0);
                    return true;
                case TRANSACTION_dispatchMediaKeyEvent /*3*/:
                    KeyEvent keyEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        keyEvent = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        keyEvent = null;
                    }
                    dispatchMediaKeyEvent(keyEvent);
                    return true;
                case TRANSACTION_getCurrentTarget /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = getCurrentTarget();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_setCurrentTarget /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCurrentTarget(data.readString());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void dispatchMediaKeyEvent(KeyEvent keyEvent) throws RemoteException;

    void enableInternetAudioService(boolean z) throws RemoteException;

    String getCurrentTarget() throws RemoteException;

    boolean isInternetAudioServiceEnable() throws RemoteException;

    void setCurrentTarget(String str) throws RemoteException;
}
