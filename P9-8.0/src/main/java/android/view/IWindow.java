package android.view;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import com.android.internal.os.IResultReceiver;

public interface IWindow extends IInterface {

    public static abstract class Stub extends Binder implements IWindow {
        private static final String DESCRIPTOR = "android.view.IWindow";
        static final int TRANSACTION_closeSystemDialogs = 7;
        static final int TRANSACTION_dispatchAppVisibility = 4;
        static final int TRANSACTION_dispatchDragEvent = 10;
        static final int TRANSACTION_dispatchGetNewSurface = 5;
        static final int TRANSACTION_dispatchPointerCaptureChanged = 15;
        static final int TRANSACTION_dispatchSystemUiVisibilityChanged = 12;
        static final int TRANSACTION_dispatchWallpaperCommand = 9;
        static final int TRANSACTION_dispatchWallpaperOffsets = 8;
        static final int TRANSACTION_dispatchWindowShown = 13;
        static final int TRANSACTION_executeCommand = 1;
        static final int TRANSACTION_moved = 3;
        static final int TRANSACTION_registerWindowObserver = 16;
        static final int TRANSACTION_requestAppKeyboardShortcuts = 14;
        static final int TRANSACTION_resized = 2;
        static final int TRANSACTION_unRegisterWindowObserver = 17;
        static final int TRANSACTION_updatePointerIcon = 11;
        static final int TRANSACTION_updateSurfaceStatus = 18;
        static final int TRANSACTION_windowFocusChanged = 6;

        private static class Proxy implements IWindow {
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

            public void executeCommand(String command, String parameters, ParcelFileDescriptor descriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    _data.writeString(parameters);
                    if (descriptor != null) {
                        _data.writeInt(1);
                        descriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration newMergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (frame != null) {
                        _data.writeInt(1);
                        frame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (overscanInsets != null) {
                        _data.writeInt(1);
                        overscanInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (contentInsets != null) {
                        _data.writeInt(1);
                        contentInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (visibleInsets != null) {
                        _data.writeInt(1);
                        visibleInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (stableInsets != null) {
                        _data.writeInt(1);
                        stableInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (outsets != null) {
                        _data.writeInt(1);
                        outsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(reportDraw ? 1 : 0);
                    if (newMergedConfiguration != null) {
                        _data.writeInt(1);
                        newMergedConfiguration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (backDropFrame != null) {
                        _data.writeInt(1);
                        backDropFrame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(forceLayout ? 1 : 0);
                    _data.writeInt(alwaysConsumeNavBar ? 1 : 0);
                    _data.writeInt(displayId);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void moved(int newX, int newY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newX);
                    _data.writeInt(newY);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchAppVisibility(boolean visible) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchGetNewSurface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hasFocus ? 1 : 0);
                    if (!inTouchMode) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    _data.writeFloat(xStep);
                    _data.writeFloat(yStep);
                    if (!sync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    _data.writeInt(z);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!sync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchDragEvent(DragEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updatePointerIcon(float x, float y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(seq);
                    _data.writeInt(globalVisibility);
                    _data.writeInt(localValue);
                    _data.writeInt(localChanges);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWindowShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchPointerCaptureChanged(boolean hasCapture) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!hasCapture) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registerWindowObserver(IWindowLayoutObserver observer, long period) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(period);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unRegisterWindowObserver(IWindowLayoutObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateSurfaceStatus(boolean status) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!status) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWindow asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWindow)) {
                return new Proxy(obj);
            }
            return (IWindow) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            switch (code) {
                case 1:
                    ParcelFileDescriptor _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    executeCommand(_arg0, _arg1, _arg2);
                    return true;
                case 2:
                    Rect _arg02;
                    Rect _arg12;
                    Rect _arg22;
                    Rect _arg3;
                    Rect _arg4;
                    Rect _arg5;
                    MergedConfiguration _arg7;
                    Rect _arg8;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg12 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg22 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg4 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg4 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg5 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg5 = null;
                    }
                    boolean _arg6 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg7 = (MergedConfiguration) MergedConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        _arg7 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg8 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg8 = null;
                    }
                    resized(_arg02, _arg12, _arg22, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, data.readInt() != 0, data.readInt() != 0, data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    moved(data.readInt(), data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchAppVisibility(data.readInt() != 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchGetNewSurface();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    windowFocusChanged(data.readInt() != 0, data.readInt() != 0);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    closeSystemDialogs(data.readString());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchWallpaperOffsets(data.readFloat(), data.readFloat(), data.readFloat(), data.readFloat(), data.readInt() != 0);
                    return true;
                case 9:
                    Bundle _arg42;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    int _arg13 = data.readInt();
                    int _arg23 = data.readInt();
                    int _arg32 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg42 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg42 = null;
                    }
                    dispatchWallpaperCommand(_arg0, _arg13, _arg23, _arg32, _arg42, data.readInt() != 0);
                    return true;
                case 10:
                    DragEvent _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (DragEvent) DragEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    dispatchDragEvent(_arg03);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    updatePointerIcon(data.readFloat(), data.readFloat());
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchSystemUiVisibilityChanged(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchWindowShown();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    requestAppKeyboardShortcuts(com.android.internal.os.IResultReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchPointerCaptureChanged(data.readInt() != 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    registerWindowObserver(android.view.IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    unRegisterWindowObserver(android.view.IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    updateSurfaceStatus(data.readInt() != 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void closeSystemDialogs(String str) throws RemoteException;

    void dispatchAppVisibility(boolean z) throws RemoteException;

    void dispatchDragEvent(DragEvent dragEvent) throws RemoteException;

    void dispatchGetNewSurface() throws RemoteException;

    void dispatchPointerCaptureChanged(boolean z) throws RemoteException;

    void dispatchSystemUiVisibilityChanged(int i, int i2, int i3, int i4) throws RemoteException;

    void dispatchWallpaperCommand(String str, int i, int i2, int i3, Bundle bundle, boolean z) throws RemoteException;

    void dispatchWallpaperOffsets(float f, float f2, float f3, float f4, boolean z) throws RemoteException;

    void dispatchWindowShown() throws RemoteException;

    void executeCommand(String str, String str2, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void moved(int i, int i2) throws RemoteException;

    void registerWindowObserver(IWindowLayoutObserver iWindowLayoutObserver, long j) throws RemoteException;

    void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) throws RemoteException;

    void resized(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, boolean z, MergedConfiguration mergedConfiguration, Rect rect7, boolean z2, boolean z3, int i) throws RemoteException;

    void unRegisterWindowObserver(IWindowLayoutObserver iWindowLayoutObserver) throws RemoteException;

    void updatePointerIcon(float f, float f2) throws RemoteException;

    void updateSurfaceStatus(boolean z) throws RemoteException;

    void windowFocusChanged(boolean z, boolean z2) throws RemoteException;
}
