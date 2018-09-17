package android.view;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.android.internal.os.IResultReceiver;

public interface IWindow extends IInterface {

    public static abstract class Stub extends Binder implements IWindow {
        private static final String DESCRIPTOR = "android.view.IWindow";
        static final int TRANSACTION_closeSystemDialogs = 7;
        static final int TRANSACTION_dispatchAppVisibility = 4;
        static final int TRANSACTION_dispatchDragEvent = 10;
        static final int TRANSACTION_dispatchGetNewSurface = 5;
        static final int TRANSACTION_dispatchSystemUiVisibilityChanged = 12;
        static final int TRANSACTION_dispatchWallpaperCommand = 9;
        static final int TRANSACTION_dispatchWallpaperOffsets = 8;
        static final int TRANSACTION_dispatchWindowShown = 13;
        static final int TRANSACTION_executeCommand = 1;
        static final int TRANSACTION_moved = 3;
        static final int TRANSACTION_requestAppKeyboardShortcuts = 14;
        static final int TRANSACTION_resized = 2;
        static final int TRANSACTION_updatePointerIcon = 11;
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
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        descriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_executeCommand, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (frame != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        frame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (overscanInsets != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        overscanInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (contentInsets != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        contentInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (visibleInsets != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        visibleInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (stableInsets != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        stableInsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (outsets != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        outsets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(reportDraw ? Stub.TRANSACTION_executeCommand : 0);
                    if (newConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        newConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (backDropFrame != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        backDropFrame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(forceLayout ? Stub.TRANSACTION_executeCommand : 0);
                    _data.writeInt(alwaysConsumeNavBar ? Stub.TRANSACTION_executeCommand : 0);
                    this.mRemote.transact(Stub.TRANSACTION_resized, _data, null, Stub.TRANSACTION_executeCommand);
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
                    this.mRemote.transact(Stub.TRANSACTION_moved, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchAppVisibility(boolean visible) throws RemoteException {
                int i = Stub.TRANSACTION_executeCommand;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_dispatchAppVisibility, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchGetNewSurface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dispatchGetNewSurface, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) throws RemoteException {
                int i = Stub.TRANSACTION_executeCommand;
                Parcel _data = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hasFocus) {
                        i2 = Stub.TRANSACTION_executeCommand;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!inTouchMode) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_windowFocusChanged, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    this.mRemote.transact(Stub.TRANSACTION_closeSystemDialogs, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) throws RemoteException {
                int i = Stub.TRANSACTION_executeCommand;
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
                    this.mRemote.transact(Stub.TRANSACTION_dispatchWallpaperOffsets, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
                int i = Stub.TRANSACTION_executeCommand;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    _data.writeInt(z);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!sync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_dispatchWallpaperCommand, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchDragEvent(DragEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_executeCommand);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_dispatchDragEvent, _data, null, Stub.TRANSACTION_executeCommand);
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
                    this.mRemote.transact(Stub.TRANSACTION_updatePointerIcon, _data, null, Stub.TRANSACTION_executeCommand);
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
                    this.mRemote.transact(Stub.TRANSACTION_dispatchSystemUiVisibilityChanged, _data, null, Stub.TRANSACTION_executeCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWindowShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dispatchWindowShown, _data, null, Stub.TRANSACTION_executeCommand);
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
                    this.mRemote.transact(Stub.TRANSACTION_requestAppKeyboardShortcuts, _data, null, Stub.TRANSACTION_executeCommand);
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
                case TRANSACTION_executeCommand /*1*/:
                    ParcelFileDescriptor parcelFileDescriptor;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    executeCommand(_arg0, _arg1, parcelFileDescriptor);
                    return true;
                case TRANSACTION_resized /*2*/:
                    Rect rect;
                    Rect rect2;
                    Rect rect3;
                    Rect rect4;
                    Rect rect5;
                    Rect rect6;
                    Configuration configuration;
                    Rect rect7;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    if (data.readInt() != 0) {
                        rect2 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect2 = null;
                    }
                    if (data.readInt() != 0) {
                        rect3 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect3 = null;
                    }
                    if (data.readInt() != 0) {
                        rect4 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect4 = null;
                    }
                    if (data.readInt() != 0) {
                        rect5 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect5 = null;
                    }
                    if (data.readInt() != 0) {
                        rect6 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect6 = null;
                    }
                    boolean _arg6 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        configuration = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        configuration = null;
                    }
                    if (data.readInt() != 0) {
                        rect7 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect7 = null;
                    }
                    resized(rect, rect2, rect3, rect4, rect5, rect6, _arg6, configuration, rect7, data.readInt() != 0, data.readInt() != 0);
                    return true;
                case TRANSACTION_moved /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    moved(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_dispatchAppVisibility /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchAppVisibility(data.readInt() != 0);
                    return true;
                case TRANSACTION_dispatchGetNewSurface /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchGetNewSurface();
                    return true;
                case TRANSACTION_windowFocusChanged /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    windowFocusChanged(data.readInt() != 0, data.readInt() != 0);
                    return true;
                case TRANSACTION_closeSystemDialogs /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    closeSystemDialogs(data.readString());
                    return true;
                case TRANSACTION_dispatchWallpaperOffsets /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchWallpaperOffsets(data.readFloat(), data.readFloat(), data.readFloat(), data.readFloat(), data.readInt() != 0);
                    return true;
                case TRANSACTION_dispatchWallpaperCommand /*9*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    int _arg12 = data.readInt();
                    int _arg2 = data.readInt();
                    int _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    dispatchWallpaperCommand(_arg0, _arg12, _arg2, _arg3, bundle, data.readInt() != 0);
                    return true;
                case TRANSACTION_dispatchDragEvent /*10*/:
                    DragEvent dragEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        dragEvent = (DragEvent) DragEvent.CREATOR.createFromParcel(data);
                    } else {
                        dragEvent = null;
                    }
                    dispatchDragEvent(dragEvent);
                    return true;
                case TRANSACTION_updatePointerIcon /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    updatePointerIcon(data.readFloat(), data.readFloat());
                    return true;
                case TRANSACTION_dispatchSystemUiVisibilityChanged /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchSystemUiVisibilityChanged(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_dispatchWindowShown /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchWindowShown();
                    return true;
                case TRANSACTION_requestAppKeyboardShortcuts /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestAppKeyboardShortcuts(com.android.internal.os.IResultReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
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

    void dispatchSystemUiVisibilityChanged(int i, int i2, int i3, int i4) throws RemoteException;

    void dispatchWallpaperCommand(String str, int i, int i2, int i3, Bundle bundle, boolean z) throws RemoteException;

    void dispatchWallpaperOffsets(float f, float f2, float f3, float f4, boolean z) throws RemoteException;

    void dispatchWindowShown() throws RemoteException;

    void executeCommand(String str, String str2, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void moved(int i, int i2) throws RemoteException;

    void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) throws RemoteException;

    void resized(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, boolean z, Configuration configuration, Rect rect7, boolean z2, boolean z3) throws RemoteException;

    void updatePointerIcon(float f, float f2) throws RemoteException;

    void windowFocusChanged(boolean z, boolean z2) throws RemoteException;
}
