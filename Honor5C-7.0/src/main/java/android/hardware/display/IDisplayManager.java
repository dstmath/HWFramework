package android.hardware.display;

import android.media.projection.IMediaProjection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.DisplayInfo;
import android.view.Surface;

public interface IDisplayManager extends IInterface {

    public static abstract class Stub extends Binder implements IDisplayManager {
        private static final String DESCRIPTOR = "android.hardware.display.IDisplayManager";
        static final int TRANSACTION_connectWifiDisplay = 6;
        static final int TRANSACTION_createVirtualDisplay = 14;
        static final int TRANSACTION_disconnectWifiDisplay = 7;
        static final int TRANSACTION_forgetWifiDisplay = 9;
        static final int TRANSACTION_getDisplayIds = 2;
        static final int TRANSACTION_getDisplayInfo = 1;
        static final int TRANSACTION_getLowPowerDisplayLevel = 18;
        static final int TRANSACTION_getWifiDisplayStatus = 12;
        static final int TRANSACTION_pauseWifiDisplay = 10;
        static final int TRANSACTION_registerCallback = 3;
        static final int TRANSACTION_releaseVirtualDisplay = 17;
        static final int TRANSACTION_renameWifiDisplay = 8;
        static final int TRANSACTION_requestColorTransform = 13;
        static final int TRANSACTION_resizeVirtualDisplay = 15;
        static final int TRANSACTION_resumeWifiDisplay = 11;
        static final int TRANSACTION_setLowPowerDisplayLevel = 19;
        static final int TRANSACTION_setVirtualDisplaySurface = 16;
        static final int TRANSACTION_startWifiDisplayScan = 4;
        static final int TRANSACTION_stopWifiDisplayScan = 5;

        private static class Proxy implements IDisplayManager {
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

            public DisplayInfo getDisplayInfo(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    DisplayInfo displayInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(Stub.TRANSACTION_getDisplayInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        displayInfo = (DisplayInfo) DisplayInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        displayInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return displayInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getDisplayIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDisplayIds, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallback(IDisplayManagerCallback callback) throws RemoteException {
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
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startWifiDisplayScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_startWifiDisplayScan, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopWifiDisplayScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopWifiDisplayScan, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void connectWifiDisplay(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_connectWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnectWifiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disconnectWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void renameWifiDisplay(String address, String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeString(alias);
                    this.mRemote.transact(Stub.TRANSACTION_renameWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forgetWifiDisplay(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_forgetWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pauseWifiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_pauseWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumeWifiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_resumeWifiDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiDisplayStatus getWifiDisplayStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WifiDisplayStatus wifiDisplayStatus;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWifiDisplayStatus, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wifiDisplayStatus = (WifiDisplayStatus) WifiDisplayStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        wifiDisplayStatus = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wifiDisplayStatus;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestColorTransform(int displayId, int colorTransformId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(colorTransformId);
                    this.mRemote.transact(Stub.TRANSACTION_requestColorTransform, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createVirtualDisplay(IVirtualDisplayCallback callback, IMediaProjection projectionToken, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        asBinder = callback.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (projectionToken != null) {
                        iBinder = projectionToken.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(packageName);
                    _data.writeString(name);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(densityDpi);
                    if (surface != null) {
                        _data.writeInt(Stub.TRANSACTION_getDisplayInfo);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_createVirtualDisplay, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resizeVirtualDisplay(IVirtualDisplayCallback token, int width, int height, int densityDpi) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(densityDpi);
                    this.mRemote.transact(Stub.TRANSACTION_resizeVirtualDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVirtualDisplaySurface(IVirtualDisplayCallback token, Surface surface) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (surface != null) {
                        _data.writeInt(Stub.TRANSACTION_getDisplayInfo);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setVirtualDisplaySurface, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releaseVirtualDisplay(IVirtualDisplayCallback token) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_releaseVirtualDisplay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLowPowerDisplayLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLowPowerDisplayLevel, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLowPowerDisplayLevel(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    this.mRemote.transact(Stub.TRANSACTION_setLowPowerDisplayLevel, _data, _reply, 0);
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

        public static IDisplayManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDisplayManager)) {
                return new Proxy(obj);
            }
            return (IDisplayManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IVirtualDisplayCallback _arg0;
            int _result;
            switch (code) {
                case TRANSACTION_getDisplayInfo /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    DisplayInfo _result2 = getDisplayInfo(data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getDisplayInfo);
                        _result2.writeToParcel(reply, TRANSACTION_getDisplayInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getDisplayIds /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result3 = getDisplayIds();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_registerCallback /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(android.hardware.display.IDisplayManagerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startWifiDisplayScan /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    startWifiDisplayScan();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopWifiDisplayScan /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopWifiDisplayScan();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_connectWifiDisplay /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    connectWifiDisplay(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disconnectWifiDisplay /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnectWifiDisplay();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_renameWifiDisplay /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    renameWifiDisplay(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forgetWifiDisplay /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    forgetWifiDisplay(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_pauseWifiDisplay /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    pauseWifiDisplay();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resumeWifiDisplay /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    resumeWifiDisplay();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWifiDisplayStatus /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    WifiDisplayStatus _result4 = getWifiDisplayStatus();
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getDisplayInfo);
                        _result4.writeToParcel(reply, TRANSACTION_getDisplayInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_requestColorTransform /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestColorTransform(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createVirtualDisplay /*14*/:
                    Surface surface;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.hardware.display.IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder());
                    IMediaProjection _arg1 = android.media.projection.IMediaProjection.Stub.asInterface(data.readStrongBinder());
                    String _arg2 = data.readString();
                    String _arg3 = data.readString();
                    int _arg4 = data.readInt();
                    int _arg5 = data.readInt();
                    int _arg6 = data.readInt();
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    _result = createVirtualDisplay(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, surface, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_resizeVirtualDisplay /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    resizeVirtualDisplay(android.hardware.display.IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setVirtualDisplaySurface /*16*/:
                    Surface surface2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.hardware.display.IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        surface2 = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface2 = null;
                    }
                    setVirtualDisplaySurface(_arg0, surface2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_releaseVirtualDisplay /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    releaseVirtualDisplay(android.hardware.display.IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLowPowerDisplayLevel /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getLowPowerDisplayLevel();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setLowPowerDisplayLevel /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLowPowerDisplayLevel(data.readInt());
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

    void connectWifiDisplay(String str) throws RemoteException;

    int createVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback, IMediaProjection iMediaProjection, String str, String str2, int i, int i2, int i3, Surface surface, int i4) throws RemoteException;

    void disconnectWifiDisplay() throws RemoteException;

    void forgetWifiDisplay(String str) throws RemoteException;

    int[] getDisplayIds() throws RemoteException;

    DisplayInfo getDisplayInfo(int i) throws RemoteException;

    int getLowPowerDisplayLevel() throws RemoteException;

    WifiDisplayStatus getWifiDisplayStatus() throws RemoteException;

    void pauseWifiDisplay() throws RemoteException;

    void registerCallback(IDisplayManagerCallback iDisplayManagerCallback) throws RemoteException;

    void releaseVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback) throws RemoteException;

    void renameWifiDisplay(String str, String str2) throws RemoteException;

    void requestColorTransform(int i, int i2) throws RemoteException;

    void resizeVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback, int i, int i2, int i3) throws RemoteException;

    void resumeWifiDisplay() throws RemoteException;

    void setLowPowerDisplayLevel(int i) throws RemoteException;

    void setVirtualDisplaySurface(IVirtualDisplayCallback iVirtualDisplayCallback, Surface surface) throws RemoteException;

    void startWifiDisplayScan() throws RemoteException;

    void stopWifiDisplayScan() throws RemoteException;
}
