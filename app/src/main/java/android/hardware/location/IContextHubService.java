package android.hardware.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IContextHubService extends IInterface {

    public static abstract class Stub extends Binder implements IContextHubService {
        private static final String DESCRIPTOR = "android.hardware.location.IContextHubService";
        static final int TRANSACTION_findNanoAppOnHub = 7;
        static final int TRANSACTION_getContextHubHandles = 2;
        static final int TRANSACTION_getContextHubInfo = 3;
        static final int TRANSACTION_getNanoAppInstanceInfo = 6;
        static final int TRANSACTION_loadNanoApp = 4;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_sendMessage = 8;
        static final int TRANSACTION_unloadNanoApp = 5;

        private static class Proxy implements IContextHubService {
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

            public int registerCallback(IContextHubCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getContextHubHandles() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getContextHubHandles, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ContextHubInfo getContextHubInfo(int contextHubHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ContextHubInfo contextHubInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(contextHubHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getContextHubInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        contextHubInfo = (ContextHubInfo) ContextHubInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        contextHubInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return contextHubInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int loadNanoApp(int hubHandle, NanoApp app) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hubHandle);
                    if (app != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        app.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_loadNanoApp, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unloadNanoApp(int nanoAppInstanceHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nanoAppInstanceHandle);
                    this.mRemote.transact(Stub.TRANSACTION_unloadNanoApp, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppInstanceHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NanoAppInstanceInfo nanoAppInstanceInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nanoAppInstanceHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getNanoAppInstanceInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        nanoAppInstanceInfo = (NanoAppInstanceInfo) NanoAppInstanceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        nanoAppInstanceInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return nanoAppInstanceInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] findNanoAppOnHub(int hubHandle, NanoAppFilter filter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hubHandle);
                    if (filter != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_findNanoAppOnHub, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hubHandle);
                    _data.writeInt(nanoAppHandle);
                    if (msg != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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

        public static IContextHubService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IContextHubService)) {
                return new Proxy(obj);
            }
            return (IContextHubService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            int[] _result2;
            int _arg0;
            switch (code) {
                case TRANSACTION_registerCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerCallback(android.hardware.location.IContextHubCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getContextHubHandles /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getContextHubHandles();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_getContextHubInfo /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    ContextHubInfo _result3 = getContextHubInfo(data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_registerCallback);
                        _result3.writeToParcel(reply, TRANSACTION_registerCallback);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_loadNanoApp /*4*/:
                    NanoApp nanoApp;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        nanoApp = (NanoApp) NanoApp.CREATOR.createFromParcel(data);
                    } else {
                        nanoApp = null;
                    }
                    _result = loadNanoApp(_arg0, nanoApp);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_unloadNanoApp /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unloadNanoApp(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getNanoAppInstanceInfo /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    NanoAppInstanceInfo _result4 = getNanoAppInstanceInfo(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_registerCallback);
                        _result4.writeToParcel(reply, TRANSACTION_registerCallback);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_findNanoAppOnHub /*7*/:
                    NanoAppFilter nanoAppFilter;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        nanoAppFilter = (NanoAppFilter) NanoAppFilter.CREATOR.createFromParcel(data);
                    } else {
                        nanoAppFilter = null;
                    }
                    _result2 = findNanoAppOnHub(_arg0, nanoAppFilter);
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_sendMessage /*8*/:
                    ContextHubMessage contextHubMessage;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        contextHubMessage = (ContextHubMessage) ContextHubMessage.CREATOR.createFromParcel(data);
                    } else {
                        contextHubMessage = null;
                    }
                    _result = sendMessage(_arg0, _arg1, contextHubMessage);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int[] findNanoAppOnHub(int i, NanoAppFilter nanoAppFilter) throws RemoteException;

    int[] getContextHubHandles() throws RemoteException;

    ContextHubInfo getContextHubInfo(int i) throws RemoteException;

    NanoAppInstanceInfo getNanoAppInstanceInfo(int i) throws RemoteException;

    int loadNanoApp(int i, NanoApp nanoApp) throws RemoteException;

    int registerCallback(IContextHubCallback iContextHubCallback) throws RemoteException;

    int sendMessage(int i, int i2, ContextHubMessage contextHubMessage) throws RemoteException;

    int unloadNanoApp(int i) throws RemoteException;
}
