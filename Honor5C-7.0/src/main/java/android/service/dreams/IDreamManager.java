package android.service.dreams;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDreamManager extends IInterface {

    public static abstract class Stub extends Binder implements IDreamManager {
        private static final String DESCRIPTOR = "android.service.dreams.IDreamManager";
        static final int TRANSACTION_awaken = 2;
        static final int TRANSACTION_dream = 1;
        static final int TRANSACTION_finishSelf = 8;
        static final int TRANSACTION_getDefaultDreamComponent = 5;
        static final int TRANSACTION_getDreamComponents = 4;
        static final int TRANSACTION_isChargingAlbumEnabled = 11;
        static final int TRANSACTION_isDreaming = 7;
        static final int TRANSACTION_setDreamComponents = 3;
        static final int TRANSACTION_startDozing = 9;
        static final int TRANSACTION_stopDozing = 10;
        static final int TRANSACTION_testDream = 6;

        private static class Proxy implements IDreamManager {
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

            public void dream() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dream, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void awaken() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_awaken, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDreamComponents(ComponentName[] componentNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(componentNames, 0);
                    this.mRemote.transact(Stub.TRANSACTION_setDreamComponents, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName[] getDreamComponents() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDreamComponents, _data, _reply, 0);
                    _reply.readException();
                    ComponentName[] _result = (ComponentName[]) _reply.createTypedArray(ComponentName.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getDefaultDreamComponent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ComponentName componentName;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultDreamComponent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        componentName = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return componentName;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void testDream(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(Stub.TRANSACTION_dream);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_testDream, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDreaming() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isDreaming, _data, _reply, 0);
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

            public void finishSelf(IBinder token, boolean immediate) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (immediate) {
                        i = Stub.TRANSACTION_dream;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_finishSelf, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startDozing(IBinder token, int screenState, int screenBrightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(screenState);
                    _data.writeInt(screenBrightness);
                    this.mRemote.transact(Stub.TRANSACTION_startDozing, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopDozing(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_stopDozing, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isChargingAlbumEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isChargingAlbumEnabled, _data, _reply, 0);
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

        public static IDreamManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDreamManager)) {
                return new Proxy(obj);
            }
            return (IDreamManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            switch (code) {
                case TRANSACTION_dream /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    dream();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_awaken /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    awaken();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDreamComponents /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDreamComponents((ComponentName[]) data.createTypedArray(ComponentName.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDreamComponents /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    ComponentName[] _result2 = getDreamComponents();
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_dream);
                    return true;
                case TRANSACTION_getDefaultDreamComponent /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    ComponentName _result3 = getDefaultDreamComponent();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_dream);
                        _result3.writeToParcel(reply, (int) TRANSACTION_dream);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_testDream /*6*/:
                    ComponentName componentName;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    testDream(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isDreaming /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDreaming();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dream : 0);
                    return true;
                case TRANSACTION_finishSelf /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    finishSelf(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startDozing /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    startDozing(data.readStrongBinder(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopDozing /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopDozing(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isChargingAlbumEnabled /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isChargingAlbumEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dream : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void awaken() throws RemoteException;

    void dream() throws RemoteException;

    void finishSelf(IBinder iBinder, boolean z) throws RemoteException;

    ComponentName getDefaultDreamComponent() throws RemoteException;

    ComponentName[] getDreamComponents() throws RemoteException;

    boolean isChargingAlbumEnabled() throws RemoteException;

    boolean isDreaming() throws RemoteException;

    void setDreamComponents(ComponentName[] componentNameArr) throws RemoteException;

    void startDozing(IBinder iBinder, int i, int i2) throws RemoteException;

    void stopDozing(IBinder iBinder) throws RemoteException;

    void testDream(ComponentName componentName) throws RemoteException;
}
