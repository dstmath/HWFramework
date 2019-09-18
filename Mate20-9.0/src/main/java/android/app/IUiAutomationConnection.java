package android.app;

import android.accessibilityservice.IAccessibilityServiceClient;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.view.InputEvent;
import android.view.WindowAnimationFrameStats;
import android.view.WindowContentFrameStats;

public interface IUiAutomationConnection extends IInterface {

    public static abstract class Stub extends Binder implements IUiAutomationConnection {
        private static final String DESCRIPTOR = "android.app.IUiAutomationConnection";
        static final int TRANSACTION_clearWindowAnimationFrameStats = 8;
        static final int TRANSACTION_clearWindowContentFrameStats = 6;
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_disconnect = 2;
        static final int TRANSACTION_executeShellCommand = 10;
        static final int TRANSACTION_getWindowAnimationFrameStats = 9;
        static final int TRANSACTION_getWindowContentFrameStats = 7;
        static final int TRANSACTION_grantRuntimePermission = 11;
        static final int TRANSACTION_injectInputEvent = 3;
        static final int TRANSACTION_revokeRuntimePermission = 12;
        static final int TRANSACTION_setRotation = 4;
        static final int TRANSACTION_shutdown = 13;
        static final int TRANSACTION_takeScreenshot = 5;

        private static class Proxy implements IUiAutomationConnection {
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

            public void connect(IAccessibilityServiceClient client, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(flags);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean injectInputEvent(InputEvent event, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sync);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRotation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bitmap takeScreenshot(Rect crop, int rotation) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (crop != null) {
                        _data.writeInt(1);
                        crop.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(rotation);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearWindowContentFrameStats(int windowId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(windowId);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WindowContentFrameStats getWindowContentFrameStats(int windowId) throws RemoteException {
                WindowContentFrameStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(windowId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (WindowContentFrameStats) WindowContentFrameStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearWindowAnimationFrameStats() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WindowAnimationFrameStats getWindowAnimationFrameStats() throws RemoteException {
                WindowAnimationFrameStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (WindowAnimationFrameStats) WindowAnimationFrameStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void executeShellCommand(String command, ParcelFileDescriptor sink, ParcelFileDescriptor source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (sink != null) {
                        _data.writeInt(1);
                        sink.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (source != null) {
                        _data.writeInt(1);
                        source.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantRuntimePermission(String packageName, String permission, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permission);
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void revokeRuntimePermission(String packageName, String permission, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permission);
                    _data.writeInt(userId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUiAutomationConnection asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUiAutomationConnection)) {
                return new Proxy(obj);
            }
            return (IUiAutomationConnection) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: android.graphics.Rect} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v2, types: [android.view.InputEvent] */
        /* JADX WARNING: type inference failed for: r3v14, types: [android.os.ParcelFileDescriptor] */
        /* JADX WARNING: type inference failed for: r3v20 */
        /* JADX WARNING: type inference failed for: r3v21 */
        /* JADX WARNING: type inference failed for: r3v22 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _arg1;
            if (code != 1598968902) {
                boolean _arg12 = false;
                ? _arg0 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        connect(IAccessibilityServiceClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        disconnect();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        boolean _result = injectInputEvent(_arg0, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = setRotation(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Rect.CREATOR.createFromParcel(data);
                        }
                        Bitmap _result3 = takeScreenshot(_arg0, data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result4 = clearWindowContentFrameStats(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        WindowContentFrameStats _result5 = getWindowContentFrameStats(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        clearWindowAnimationFrameStats();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        WindowAnimationFrameStats _result6 = getWindowAnimationFrameStats();
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        }
                        executeShellCommand(_arg02, _arg1, _arg0);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        grantRuntimePermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        revokeRuntimePermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        shutdown();
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

    void clearWindowAnimationFrameStats() throws RemoteException;

    boolean clearWindowContentFrameStats(int i) throws RemoteException;

    void connect(IAccessibilityServiceClient iAccessibilityServiceClient, int i) throws RemoteException;

    void disconnect() throws RemoteException;

    void executeShellCommand(String str, ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2) throws RemoteException;

    WindowAnimationFrameStats getWindowAnimationFrameStats() throws RemoteException;

    WindowContentFrameStats getWindowContentFrameStats(int i) throws RemoteException;

    void grantRuntimePermission(String str, String str2, int i) throws RemoteException;

    boolean injectInputEvent(InputEvent inputEvent, boolean z) throws RemoteException;

    void revokeRuntimePermission(String str, String str2, int i) throws RemoteException;

    boolean setRotation(int i) throws RemoteException;

    void shutdown() throws RemoteException;

    Bitmap takeScreenshot(Rect rect, int i) throws RemoteException;
}
