package com.huawei.android.view;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.IHwRotateObserver;
import com.huawei.android.view.IHwWMDAMonitorCallback;
import java.util.List;

public interface IHwWindowManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwWindowManager {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwWindowManager";
        static final int TRANSACTION_dismissKeyguardLw = 22;
        static final int TRANSACTION_freezeOrThawRotation = 18;
        static final int TRANSACTION_getAppUseNotchMode = 7;
        static final int TRANSACTION_getCurrFocusedWinInExtDisplay = 13;
        static final int TRANSACTION_getDeviceMaxRatio = 2;
        static final int TRANSACTION_getFocusWindowWidth = 10;
        static final int TRANSACTION_getForegroundTaskSnapshotWrapper = 16;
        static final int TRANSACTION_getNotchSystemApps = 6;
        static final int TRANSACTION_getRestrictedScreenHeight = 19;
        static final int TRANSACTION_getTopAppDisplayBounds = 3;
        static final int TRANSACTION_getVisibleWindows = 9;
        static final int TRANSACTION_hasLighterViewInPCCastMode = 14;
        static final int TRANSACTION_isFullScreenDevice = 1;
        static final int TRANSACTION_isNavigationBarVisible = 21;
        static final int TRANSACTION_isWindowSupportKnuckle = 20;
        static final int TRANSACTION_registerRotateObserver = 4;
        static final int TRANSACTION_registerWMMonitorCallback = 8;
        static final int TRANSACTION_setCoverManagerState = 17;
        static final int TRANSACTION_setGestureNavMode = 23;
        static final int TRANSACTION_shouldDropMotionEventForTouchPad = 15;
        static final int TRANSACTION_startNotifyWindowFocusChange = 11;
        static final int TRANSACTION_stopNotifyWindowFocusChange = 12;
        static final int TRANSACTION_unregisterRotateObserver = 5;

        private static class Proxy implements IHwWindowManager {
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

            public boolean isFullScreenDevice() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public float getDeviceMaxRatio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readFloat();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Rect getTopAppDisplayBounds(float appMaxRatio, int rotation) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(appMaxRatio);
                    _data.writeInt(rotation);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Rect) Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerRotateObserver(IHwRotateObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterRotateObserver(IHwRotateObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getNotchSystemApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppUseNotchMode(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerWMMonitorCallback(IHwWMDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
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

            public List<Bundle> getVisibleWindows(int ops) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ops);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(Bundle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFocusWindowWidth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startNotifyWindowFocusChange() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopNotifyWindowFocusChange() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getCurrFocusedWinInExtDisplay(Bundle outBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        outBundle.readFromParcel(_reply);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasLighterViewInPCCastMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public boolean shouldDropMotionEventForTouchPad(float x, float y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean refresh) throws RemoteException {
                HwTaskSnapshotWrapper _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(refresh);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwTaskSnapshotWrapper.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCoverManagerState(boolean isCoverOpen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isCoverOpen);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freezeOrThawRotation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRestrictedScreenHeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWindowSupportKnuckle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
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

            public boolean isNavigationBarVisible() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public void dismissKeyguardLw() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setGestureNavMode(String packageName, int leftMode, int rightMode, int bottomMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(leftMode);
                    _data.writeInt(rightMode);
                    _data.writeInt(bottomMode);
                    this.mRemote.transact(23, _data, _reply, 0);
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

        public static IHwWindowManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwWindowManager)) {
                return new Proxy(obj);
            }
            return (IHwWindowManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = isFullScreenDevice();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        float _result2 = getDeviceMaxRatio();
                        reply.writeNoException();
                        reply.writeFloat(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result3 = getTopAppDisplayBounds(data.readFloat(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerRotateObserver(IHwRotateObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterRotateObserver(IHwRotateObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = getNotchSystemApps();
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getAppUseNotchMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = registerWMMonitorCallback(IHwWMDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        List<Bundle> _result7 = getVisibleWindows(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getFocusWindowWidth();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        startNotifyWindowFocusChange();
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        stopNotifyWindowFocusChange();
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _arg02 = new Bundle();
                        getCurrFocusedWinInExtDisplay(_arg02);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg02.writeToParcel(reply, 1);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result9 = hasLighterViewInPCCastMode();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result10 = shouldDropMotionEventForTouchPad(data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        HwTaskSnapshotWrapper _result11 = getForegroundTaskSnapshotWrapper(data.readInt() != 0);
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setCoverManagerState(_arg0);
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        freezeOrThawRotation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getRestrictedScreenHeight();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result13 = isWindowSupportKnuckle();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result14 = isNavigationBarVisible();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        dismissKeyguardLw();
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        setGestureNavMode(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
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

    void dismissKeyguardLw() throws RemoteException;

    void freezeOrThawRotation(int i) throws RemoteException;

    int getAppUseNotchMode(String str) throws RemoteException;

    void getCurrFocusedWinInExtDisplay(Bundle bundle) throws RemoteException;

    float getDeviceMaxRatio() throws RemoteException;

    int getFocusWindowWidth() throws RemoteException;

    HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean z) throws RemoteException;

    List<String> getNotchSystemApps() throws RemoteException;

    int getRestrictedScreenHeight() throws RemoteException;

    Rect getTopAppDisplayBounds(float f, int i) throws RemoteException;

    List<Bundle> getVisibleWindows(int i) throws RemoteException;

    boolean hasLighterViewInPCCastMode() throws RemoteException;

    boolean isFullScreenDevice() throws RemoteException;

    boolean isNavigationBarVisible() throws RemoteException;

    boolean isWindowSupportKnuckle() throws RemoteException;

    void registerRotateObserver(IHwRotateObserver iHwRotateObserver) throws RemoteException;

    boolean registerWMMonitorCallback(IHwWMDAMonitorCallback iHwWMDAMonitorCallback) throws RemoteException;

    void setCoverManagerState(boolean z) throws RemoteException;

    void setGestureNavMode(String str, int i, int i2, int i3) throws RemoteException;

    boolean shouldDropMotionEventForTouchPad(float f, float f2) throws RemoteException;

    void startNotifyWindowFocusChange() throws RemoteException;

    void stopNotifyWindowFocusChange() throws RemoteException;

    void unregisterRotateObserver(IHwRotateObserver iHwRotateObserver) throws RemoteException;
}
