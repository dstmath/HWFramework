package android.hardware.display;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.ParceledListSlice;
import android.graphics.Point;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.IVirtualDisplayCallback;
import android.media.projection.IMediaProjection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.DisplayInfo;
import android.view.Surface;

public interface IDisplayManager extends IInterface {
    void connectWifiDisplay(String str) throws RemoteException;

    int createVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback, IMediaProjection iMediaProjection, String str, String str2, int i, int i2, int i3, Surface surface, int i4, String str3) throws RemoteException;

    void disconnectWifiDisplay() throws RemoteException;

    void forgetWifiDisplay(String str) throws RemoteException;

    ParceledListSlice getAmbientBrightnessStats() throws RemoteException;

    BrightnessConfiguration getBrightnessConfigurationForUser(int i) throws RemoteException;

    ParceledListSlice getBrightnessEvents(String str) throws RemoteException;

    BrightnessConfiguration getDefaultBrightnessConfiguration() throws RemoteException;

    int[] getDisplayIds() throws RemoteException;

    @UnsupportedAppUsage
    DisplayInfo getDisplayInfo(int i) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    Curve getMinimumBrightnessCurve() throws RemoteException;

    int getPreferredWideGamutColorSpaceId() throws RemoteException;

    Point getStableDisplaySize() throws RemoteException;

    WifiDisplayStatus getWifiDisplayStatus() throws RemoteException;

    boolean isUidPresentOnDisplay(int i, int i2) throws RemoteException;

    void pauseWifiDisplay() throws RemoteException;

    void registerCallback(IDisplayManagerCallback iDisplayManagerCallback) throws RemoteException;

    void releaseVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback) throws RemoteException;

    void renameWifiDisplay(String str, String str2) throws RemoteException;

    void requestColorMode(int i, int i2) throws RemoteException;

    void resizeVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback, int i, int i2, int i3) throws RemoteException;

    void resumeWifiDisplay() throws RemoteException;

    void setBrightnessConfigurationForUser(BrightnessConfiguration brightnessConfiguration, int i, String str) throws RemoteException;

    void setSaturationLevelEx(float f) throws RemoteException;

    void setTemporaryAutoBrightnessAdjustment(float f) throws RemoteException;

    void setTemporaryBrightness(int i) throws RemoteException;

    void setVirtualDisplayState(IVirtualDisplayCallback iVirtualDisplayCallback, boolean z) throws RemoteException;

    void setVirtualDisplaySurface(IVirtualDisplayCallback iVirtualDisplayCallback, Surface surface) throws RemoteException;

    void startWifiDisplayScan() throws RemoteException;

    void stopWifiDisplayScan() throws RemoteException;

    public static class Default implements IDisplayManager {
        @Override // android.hardware.display.IDisplayManager
        public DisplayInfo getDisplayInfo(int displayId) throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public int[] getDisplayIds() throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public boolean isUidPresentOnDisplay(int uid, int displayId) throws RemoteException {
            return false;
        }

        @Override // android.hardware.display.IDisplayManager
        public void registerCallback(IDisplayManagerCallback callback) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void startWifiDisplayScan() throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void stopWifiDisplayScan() throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void connectWifiDisplay(String address) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void disconnectWifiDisplay() throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void renameWifiDisplay(String address, String alias) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void forgetWifiDisplay(String address) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void pauseWifiDisplay() throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void resumeWifiDisplay() throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public WifiDisplayStatus getWifiDisplayStatus() throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public void requestColorMode(int displayId, int colorMode) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public int createVirtualDisplay(IVirtualDisplayCallback callback, IMediaProjection projectionToken, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.display.IDisplayManager
        public void resizeVirtualDisplay(IVirtualDisplayCallback token, int width, int height, int densityDpi) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void setVirtualDisplaySurface(IVirtualDisplayCallback token, Surface surface) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void releaseVirtualDisplay(IVirtualDisplayCallback token) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void setVirtualDisplayState(IVirtualDisplayCallback token, boolean isOn) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public Point getStableDisplaySize() throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public ParceledListSlice getBrightnessEvents(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public ParceledListSlice getAmbientBrightnessStats() throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public void setBrightnessConfigurationForUser(BrightnessConfiguration c, int userId, String packageName) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public BrightnessConfiguration getBrightnessConfigurationForUser(int userId) throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public BrightnessConfiguration getDefaultBrightnessConfiguration() throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public void setTemporaryBrightness(int brightness) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public void setTemporaryAutoBrightnessAdjustment(float adjustment) throws RemoteException {
        }

        @Override // android.hardware.display.IDisplayManager
        public Curve getMinimumBrightnessCurve() throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.hardware.display.IDisplayManager
        public int getPreferredWideGamutColorSpaceId() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.display.IDisplayManager
        public void setSaturationLevelEx(float level) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDisplayManager {
        private static final String DESCRIPTOR = "android.hardware.display.IDisplayManager";
        static final int TRANSACTION_connectWifiDisplay = 7;
        static final int TRANSACTION_createVirtualDisplay = 15;
        static final int TRANSACTION_disconnectWifiDisplay = 8;
        static final int TRANSACTION_forgetWifiDisplay = 10;
        static final int TRANSACTION_getAmbientBrightnessStats = 22;
        static final int TRANSACTION_getBrightnessConfigurationForUser = 24;
        static final int TRANSACTION_getBrightnessEvents = 21;
        static final int TRANSACTION_getDefaultBrightnessConfiguration = 25;
        static final int TRANSACTION_getDisplayIds = 2;
        static final int TRANSACTION_getDisplayInfo = 1;
        static final int TRANSACTION_getHwInnerService = 29;
        static final int TRANSACTION_getMinimumBrightnessCurve = 28;
        static final int TRANSACTION_getPreferredWideGamutColorSpaceId = 30;
        static final int TRANSACTION_getStableDisplaySize = 20;
        static final int TRANSACTION_getWifiDisplayStatus = 13;
        static final int TRANSACTION_isUidPresentOnDisplay = 3;
        static final int TRANSACTION_pauseWifiDisplay = 11;
        static final int TRANSACTION_registerCallback = 4;
        static final int TRANSACTION_releaseVirtualDisplay = 18;
        static final int TRANSACTION_renameWifiDisplay = 9;
        static final int TRANSACTION_requestColorMode = 14;
        static final int TRANSACTION_resizeVirtualDisplay = 16;
        static final int TRANSACTION_resumeWifiDisplay = 12;
        static final int TRANSACTION_setBrightnessConfigurationForUser = 23;
        static final int TRANSACTION_setSaturationLevelEx = 31;
        static final int TRANSACTION_setTemporaryAutoBrightnessAdjustment = 27;
        static final int TRANSACTION_setTemporaryBrightness = 26;
        static final int TRANSACTION_setVirtualDisplayState = 19;
        static final int TRANSACTION_setVirtualDisplaySurface = 17;
        static final int TRANSACTION_startWifiDisplayScan = 5;
        static final int TRANSACTION_stopWifiDisplayScan = 6;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getDisplayInfo";
                case 2:
                    return "getDisplayIds";
                case 3:
                    return "isUidPresentOnDisplay";
                case 4:
                    return "registerCallback";
                case 5:
                    return "startWifiDisplayScan";
                case 6:
                    return "stopWifiDisplayScan";
                case 7:
                    return "connectWifiDisplay";
                case 8:
                    return "disconnectWifiDisplay";
                case 9:
                    return "renameWifiDisplay";
                case 10:
                    return "forgetWifiDisplay";
                case 11:
                    return "pauseWifiDisplay";
                case 12:
                    return "resumeWifiDisplay";
                case 13:
                    return "getWifiDisplayStatus";
                case 14:
                    return "requestColorMode";
                case 15:
                    return "createVirtualDisplay";
                case 16:
                    return "resizeVirtualDisplay";
                case 17:
                    return "setVirtualDisplaySurface";
                case 18:
                    return "releaseVirtualDisplay";
                case 19:
                    return "setVirtualDisplayState";
                case 20:
                    return "getStableDisplaySize";
                case 21:
                    return "getBrightnessEvents";
                case 22:
                    return "getAmbientBrightnessStats";
                case 23:
                    return "setBrightnessConfigurationForUser";
                case 24:
                    return "getBrightnessConfigurationForUser";
                case 25:
                    return "getDefaultBrightnessConfiguration";
                case 26:
                    return "setTemporaryBrightness";
                case 27:
                    return "setTemporaryAutoBrightnessAdjustment";
                case 28:
                    return "getMinimumBrightnessCurve";
                case 29:
                    return "getHwInnerService";
                case 30:
                    return "getPreferredWideGamutColorSpaceId";
                case 31:
                    return "setSaturationLevelEx";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Surface _arg7;
            Surface _arg1;
            BrightnessConfiguration _arg0;
            if (code != 1598968902) {
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        DisplayInfo _result = getDisplayInfo(data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result2 = getDisplayIds();
                        reply.writeNoException();
                        reply.writeIntArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUidPresentOnDisplay = isUidPresentOnDisplay(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUidPresentOnDisplay ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallback(IDisplayManagerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        startWifiDisplayScan();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        stopWifiDisplayScan();
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        connectWifiDisplay(data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        disconnectWifiDisplay();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        renameWifiDisplay(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        forgetWifiDisplay(data.readString());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        pauseWifiDisplay();
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        resumeWifiDisplay();
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        WifiDisplayStatus _result3 = getWifiDisplayStatus();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        requestColorMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        IVirtualDisplayCallback _arg02 = IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder());
                        IMediaProjection _arg13 = IMediaProjection.Stub.asInterface(data.readStrongBinder());
                        String _arg2 = data.readString();
                        String _arg3 = data.readString();
                        int _arg4 = data.readInt();
                        int _arg5 = data.readInt();
                        int _arg6 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg7 = Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg7 = null;
                        }
                        int _result4 = createVirtualDisplay(_arg02, _arg13, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        resizeVirtualDisplay(IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        IVirtualDisplayCallback _arg03 = IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setVirtualDisplaySurface(_arg03, _arg1);
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        releaseVirtualDisplay(IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IVirtualDisplayCallback _arg04 = IVirtualDisplayCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setVirtualDisplayState(_arg04, _arg12);
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        Point _result5 = getStableDisplaySize();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result6 = getBrightnessEvents(data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result7 = getAmbientBrightnessStats();
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BrightnessConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setBrightnessConfigurationForUser(_arg0, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        BrightnessConfiguration _result8 = getBrightnessConfigurationForUser(data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        BrightnessConfiguration _result9 = getDefaultBrightnessConfiguration();
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        setTemporaryBrightness(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        setTemporaryAutoBrightnessAdjustment(data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        Curve _result10 = getMinimumBrightnessCurve();
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result11 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result11);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getPreferredWideGamutColorSpaceId();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        setSaturationLevelEx(data.readFloat());
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

        /* access modifiers changed from: private */
        public static class Proxy implements IDisplayManager {
            public static IDisplayManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.hardware.display.IDisplayManager
            public DisplayInfo getDisplayInfo(int displayId) throws RemoteException {
                DisplayInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayInfo(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = DisplayInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public int[] getDisplayIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayIds();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public boolean isUidPresentOnDisplay(int uid, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUidPresentOnDisplay(uid, displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void registerCallback(IDisplayManagerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void startWifiDisplayScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWifiDisplayScan();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void stopWifiDisplayScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopWifiDisplayScan();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void connectWifiDisplay(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().connectWifiDisplay(address);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void disconnectWifiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disconnectWifiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void renameWifiDisplay(String address, String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeString(alias);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().renameWifiDisplay(address, alias);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void forgetWifiDisplay(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forgetWifiDisplay(address);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void pauseWifiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pauseWifiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void resumeWifiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resumeWifiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public WifiDisplayStatus getWifiDisplayStatus() throws RemoteException {
                WifiDisplayStatus _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWifiDisplayStatus();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiDisplayStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void requestColorMode(int displayId, int colorMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(colorMode);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestColorMode(displayId, colorMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public int createVirtualDisplay(IVirtualDisplayCallback callback, IMediaProjection projectionToken, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (projectionToken != null) {
                        iBinder = projectionToken.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    try {
                        _data.writeString(packageName);
                        _data.writeString(name);
                        _data.writeInt(width);
                        _data.writeInt(height);
                        _data.writeInt(densityDpi);
                        if (surface != null) {
                            _data.writeInt(1);
                            surface.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeInt(flags);
                        _data.writeString(uniqueId);
                        if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int createVirtualDisplay = Stub.getDefaultImpl().createVirtualDisplay(callback, projectionToken, packageName, name, width, height, densityDpi, surface, flags, uniqueId);
                        _reply.recycle();
                        _data.recycle();
                        return createVirtualDisplay;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void resizeVirtualDisplay(IVirtualDisplayCallback token, int width, int height, int densityDpi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(densityDpi);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resizeVirtualDisplay(token, width, height, densityDpi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void setVirtualDisplaySurface(IVirtualDisplayCallback token, Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVirtualDisplaySurface(token, surface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void releaseVirtualDisplay(IVirtualDisplayCallback token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseVirtualDisplay(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void setVirtualDisplayState(IVirtualDisplayCallback token, boolean isOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(isOn ? 1 : 0);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVirtualDisplayState(token, isOn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public Point getStableDisplaySize() throws RemoteException {
                Point _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStableDisplaySize();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Point.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public ParceledListSlice getBrightnessEvents(String callingPackage) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBrightnessEvents(callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public ParceledListSlice getAmbientBrightnessStats() throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAmbientBrightnessStats();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void setBrightnessConfigurationForUser(BrightnessConfiguration c, int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (c != null) {
                        _data.writeInt(1);
                        c.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBrightnessConfigurationForUser(c, userId, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public BrightnessConfiguration getBrightnessConfigurationForUser(int userId) throws RemoteException {
                BrightnessConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBrightnessConfigurationForUser(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BrightnessConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public BrightnessConfiguration getDefaultBrightnessConfiguration() throws RemoteException {
                BrightnessConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultBrightnessConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BrightnessConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void setTemporaryBrightness(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTemporaryBrightness(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void setTemporaryAutoBrightnessAdjustment(float adjustment) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(adjustment);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTemporaryAutoBrightnessAdjustment(adjustment);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public Curve getMinimumBrightnessCurve() throws RemoteException {
                Curve _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMinimumBrightnessCurve();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Curve.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public int getPreferredWideGamutColorSpaceId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredWideGamutColorSpaceId();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.display.IDisplayManager
            public void setSaturationLevelEx(float level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(level);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSaturationLevelEx(level);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDisplayManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDisplayManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
