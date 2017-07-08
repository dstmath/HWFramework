package android.service.quicksettings;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQSService extends IInterface {

    public static abstract class Stub extends Binder implements IQSService {
        private static final String DESCRIPTOR = "android.service.quicksettings.IQSService";
        static final int TRANSACTION_getTile = 1;
        static final int TRANSACTION_isLocked = 6;
        static final int TRANSACTION_isSecure = 7;
        static final int TRANSACTION_onDialogHidden = 9;
        static final int TRANSACTION_onShowDialog = 4;
        static final int TRANSACTION_onStartActivity = 5;
        static final int TRANSACTION_onStartSuccessful = 10;
        static final int TRANSACTION_startUnlockAndRun = 8;
        static final int TRANSACTION_updateQsTile = 2;
        static final int TRANSACTION_updateStatusIcon = 3;

        private static class Proxy implements IQSService {
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

            public Tile getTile(ComponentName component) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Tile tile;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getTile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(_reply);
                    } else {
                        tile = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return tile;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateQsTile(Tile tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateQsTile, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateStatusIcon(Tile tile, Icon icon, String contentDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (icon != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(contentDescription);
                    this.mRemote.transact(Stub.TRANSACTION_updateStatusIcon, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onShowDialog(Tile tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onShowDialog, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onStartActivity(Tile tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onStartActivity, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isLocked, _data, _reply, 0);
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

            public boolean isSecure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSecure, _data, _reply, 0);
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

            public void startUnlockAndRun(Tile tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startUnlockAndRun, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onDialogHidden(Tile tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDialogHidden, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onStartSuccessful(Tile tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_getTile);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onStartSuccessful, _data, _reply, 0);
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

        public static IQSService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IQSService)) {
                return new Proxy(obj);
            }
            return (IQSService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            Tile tile;
            boolean _result;
            switch (code) {
                case TRANSACTION_getTile /*1*/:
                    ComponentName componentName;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    Tile _result2 = getTile(componentName);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getTile);
                        _result2.writeToParcel(reply, TRANSACTION_getTile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_updateQsTile /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(data);
                    } else {
                        tile = null;
                    }
                    updateQsTile(tile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateStatusIcon /*3*/:
                    Icon icon;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(data);
                    } else {
                        tile = null;
                    }
                    if (data.readInt() != 0) {
                        icon = (Icon) Icon.CREATOR.createFromParcel(data);
                    } else {
                        icon = null;
                    }
                    updateStatusIcon(tile, icon, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onShowDialog /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(data);
                    } else {
                        tile = null;
                    }
                    onShowDialog(tile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onStartActivity /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(data);
                    } else {
                        tile = null;
                    }
                    onStartActivity(tile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isLocked /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isLocked();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_getTile;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_isSecure /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSecure();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_getTile;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_startUnlockAndRun /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(data);
                    } else {
                        tile = null;
                    }
                    startUnlockAndRun(tile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onDialogHidden /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(data);
                    } else {
                        tile = null;
                    }
                    onDialogHidden(tile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onStartSuccessful /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tile = (Tile) Tile.CREATOR.createFromParcel(data);
                    } else {
                        tile = null;
                    }
                    onStartSuccessful(tile);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    Tile getTile(ComponentName componentName) throws RemoteException;

    boolean isLocked() throws RemoteException;

    boolean isSecure() throws RemoteException;

    void onDialogHidden(Tile tile) throws RemoteException;

    void onShowDialog(Tile tile) throws RemoteException;

    void onStartActivity(Tile tile) throws RemoteException;

    void onStartSuccessful(Tile tile) throws RemoteException;

    void startUnlockAndRun(Tile tile) throws RemoteException;

    void updateQsTile(Tile tile) throws RemoteException;

    void updateStatusIcon(Tile tile, Icon icon, String str) throws RemoteException;
}
