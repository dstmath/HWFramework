package android.media.session;

import android.content.ComponentName;
import android.media.IRemoteVolumeController;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;
import java.util.List;

public interface ISessionManager extends IInterface {

    public static abstract class Stub extends Binder implements ISessionManager {
        private static final String DESCRIPTOR = "android.media.session.ISessionManager";
        static final int TRANSACTION_addSessionsListener = 5;
        static final int TRANSACTION_createSession = 1;
        static final int TRANSACTION_dispatchAdjustVolume = 4;
        static final int TRANSACTION_dispatchMediaKeyEvent = 3;
        static final int TRANSACTION_getSessions = 2;
        static final int TRANSACTION_isGlobalPriorityActive = 8;
        static final int TRANSACTION_removeSessionsListener = 6;
        static final int TRANSACTION_setRemoteVolumeController = 7;

        private static class Proxy implements ISessionManager {
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

            public ISession createSession(String packageName, ISessionCallback cb, String tag, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(tag);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_createSession, _data, _reply, 0);
                    _reply.readException();
                    ISession _result = android.media.session.ISession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<IBinder> getSessions(ComponentName compName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(Stub.TRANSACTION_createSession);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getSessions, _data, _reply, 0);
                    _reply.readException();
                    List<IBinder> _result = _reply.createBinderArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dispatchMediaKeyEvent(KeyEvent keyEvent, boolean needWakeLock) throws RemoteException {
                int i = Stub.TRANSACTION_createSession;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyEvent != null) {
                        _data.writeInt(Stub.TRANSACTION_createSession);
                        keyEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!needWakeLock) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_dispatchMediaKeyEvent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dispatchAdjustVolume(int suggestedStream, int delta, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(suggestedStream);
                    _data.writeInt(delta);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_dispatchAdjustVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSessionsListener(IActiveSessionsListener listener, ComponentName compName, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (compName != null) {
                        _data.writeInt(Stub.TRANSACTION_createSession);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addSessionsListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeSessionsListener(IActiveSessionsListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeSessionsListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRemoteVolumeController(IRemoteVolumeController rvc) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rvc != null) {
                        iBinder = rvc.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setRemoteVolumeController, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isGlobalPriorityActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isGlobalPriorityActive, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISessionManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISessionManager)) {
                return new Proxy(obj);
            }
            return (ISessionManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_createSession /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    ISession _result = createSession(data.readString(), android.media.session.ISessionCallback.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_getSessions /*2*/:
                    ComponentName componentName;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    List<IBinder> _result2 = getSessions(componentName, data.readInt());
                    reply.writeNoException();
                    reply.writeBinderList(_result2);
                    return true;
                case TRANSACTION_dispatchMediaKeyEvent /*3*/:
                    KeyEvent keyEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        keyEvent = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        keyEvent = null;
                    }
                    dispatchMediaKeyEvent(keyEvent, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_dispatchAdjustVolume /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchAdjustVolume(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addSessionsListener /*5*/:
                    ComponentName componentName2;
                    data.enforceInterface(DESCRIPTOR);
                    IActiveSessionsListener _arg0 = android.media.session.IActiveSessionsListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        componentName2 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName2 = null;
                    }
                    addSessionsListener(_arg0, componentName2, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeSessionsListener /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeSessionsListener(android.media.session.IActiveSessionsListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRemoteVolumeController /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRemoteVolumeController(android.media.IRemoteVolumeController.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isGlobalPriorityActive /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = isGlobalPriorityActive();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_createSession : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addSessionsListener(IActiveSessionsListener iActiveSessionsListener, ComponentName componentName, int i) throws RemoteException;

    ISession createSession(String str, ISessionCallback iSessionCallback, String str2, int i) throws RemoteException;

    void dispatchAdjustVolume(int i, int i2, int i3) throws RemoteException;

    void dispatchMediaKeyEvent(KeyEvent keyEvent, boolean z) throws RemoteException;

    List<IBinder> getSessions(ComponentName componentName, int i) throws RemoteException;

    boolean isGlobalPriorityActive() throws RemoteException;

    void removeSessionsListener(IActiveSessionsListener iActiveSessionsListener) throws RemoteException;

    void setRemoteVolumeController(IRemoteVolumeController iRemoteVolumeController) throws RemoteException;
}
