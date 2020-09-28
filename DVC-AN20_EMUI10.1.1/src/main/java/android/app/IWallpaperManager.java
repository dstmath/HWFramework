package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.IWallpaperManagerCallback;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IWallpaperManager extends IInterface {
    void clearWallpaper(String str, int i, int i2) throws RemoteException;

    ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback iWallpaperManagerCallback) throws RemoteException;

    int[] getCurrOffsets() throws RemoteException;

    @UnsupportedAppUsage
    int getHeightHint(int i) throws RemoteException;

    String getName() throws RemoteException;

    @UnsupportedAppUsage
    ParcelFileDescriptor getWallpaper(String str, IWallpaperManagerCallback iWallpaperManagerCallback, int i, Bundle bundle, int i2) throws RemoteException;

    WallpaperColors getWallpaperColors(int i, int i2, int i3) throws RemoteException;

    int getWallpaperIdForUser(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    WallpaperInfo getWallpaperInfo(int i) throws RemoteException;

    int getWallpaperUserId() throws RemoteException;

    @UnsupportedAppUsage
    int getWidthHint(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean hasNamedWallpaper(String str) throws RemoteException;

    boolean isSetWallpaperAllowed(String str) throws RemoteException;

    boolean isWallpaperBackupEligible(int i, int i2) throws RemoteException;

    boolean isWallpaperSupported(String str) throws RemoteException;

    void registerWallpaperColorsCallback(IWallpaperManagerCallback iWallpaperManagerCallback, int i, int i2) throws RemoteException;

    Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) throws RemoteException;

    void setCurrOffsets(int[] iArr) throws RemoteException;

    void setDimensionHints(int i, int i2, String str, int i3) throws RemoteException;

    void setDisplayPadding(Rect rect, String str, int i) throws RemoteException;

    void setInAmbientMode(boolean z, long j) throws RemoteException;

    boolean setLockWallpaperCallback(IWallpaperManagerCallback iWallpaperManagerCallback) throws RemoteException;

    void setNextOffsets(int[] iArr) throws RemoteException;

    ParcelFileDescriptor setWallpaper(String str, String str2, Rect rect, boolean z, Bundle bundle, int i, IWallpaperManagerCallback iWallpaperManagerCallback, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void setWallpaperComponent(ComponentName componentName) throws RemoteException;

    void setWallpaperComponentChecked(ComponentName componentName, String str, int i) throws RemoteException;

    void settingsRestored() throws RemoteException;

    void unregisterWallpaperColorsCallback(IWallpaperManagerCallback iWallpaperManagerCallback, int i, int i2) throws RemoteException;

    public static class Default implements IWallpaperManager {
        @Override // android.app.IWallpaperManager
        public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IWallpaperManager
        public void setWallpaperComponentChecked(ComponentName name, String callingPackage, int userId) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public void setWallpaperComponent(ComponentName name) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public ParcelFileDescriptor getWallpaper(String callingPkg, IWallpaperManagerCallback cb, int which, Bundle outParams, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IWallpaperManager
        public int getWallpaperIdForUser(int which, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IWallpaperManager
        public WallpaperInfo getWallpaperInfo(int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IWallpaperManager
        public void clearWallpaper(String callingPackage, int which, int userId) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public boolean hasNamedWallpaper(String name) throws RemoteException {
            return false;
        }

        @Override // android.app.IWallpaperManager
        public void setDimensionHints(int width, int height, String callingPackage, int displayId) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public int getWidthHint(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IWallpaperManager
        public int getHeightHint(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.app.IWallpaperManager
        public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) throws RemoteException {
            return null;
        }

        @Override // android.app.IWallpaperManager
        public void setDisplayPadding(Rect padding, String callingPackage, int displayId) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public String getName() throws RemoteException {
            return null;
        }

        @Override // android.app.IWallpaperManager
        public void settingsRestored() throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public boolean isWallpaperSupported(String callingPackage) throws RemoteException {
            return false;
        }

        @Override // android.app.IWallpaperManager
        public boolean isSetWallpaperAllowed(String callingPackage) throws RemoteException {
            return false;
        }

        @Override // android.app.IWallpaperManager
        public boolean isWallpaperBackupEligible(int which, int userId) throws RemoteException {
            return false;
        }

        @Override // android.app.IWallpaperManager
        public boolean setLockWallpaperCallback(IWallpaperManagerCallback cb) throws RemoteException {
            return false;
        }

        @Override // android.app.IWallpaperManager
        public WallpaperColors getWallpaperColors(int which, int userId, int displayId) throws RemoteException {
            return null;
        }

        @Override // android.app.IWallpaperManager
        public void registerWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public void unregisterWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public void setInAmbientMode(boolean inAmbientMode, long animationDuration) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public int[] getCurrOffsets() throws RemoteException {
            return null;
        }

        @Override // android.app.IWallpaperManager
        public void setCurrOffsets(int[] offsets) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public void setNextOffsets(int[] offsets) throws RemoteException {
        }

        @Override // android.app.IWallpaperManager
        public int getWallpaperUserId() throws RemoteException {
            return 0;
        }

        @Override // android.app.IWallpaperManager
        public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWallpaperManager {
        private static final String DESCRIPTOR = "android.app.IWallpaperManager";
        static final int TRANSACTION_clearWallpaper = 7;
        static final int TRANSACTION_getBlurWallpaper = 12;
        static final int TRANSACTION_getCurrOffsets = 24;
        static final int TRANSACTION_getHeightHint = 11;
        static final int TRANSACTION_getName = 14;
        static final int TRANSACTION_getWallpaper = 4;
        static final int TRANSACTION_getWallpaperColors = 20;
        static final int TRANSACTION_getWallpaperIdForUser = 5;
        static final int TRANSACTION_getWallpaperInfo = 6;
        static final int TRANSACTION_getWallpaperUserId = 27;
        static final int TRANSACTION_getWidthHint = 10;
        static final int TRANSACTION_hasNamedWallpaper = 8;
        static final int TRANSACTION_isSetWallpaperAllowed = 17;
        static final int TRANSACTION_isWallpaperBackupEligible = 18;
        static final int TRANSACTION_isWallpaperSupported = 16;
        static final int TRANSACTION_registerWallpaperColorsCallback = 21;
        static final int TRANSACTION_scaleWallpaperBitmapToScreenSize = 28;
        static final int TRANSACTION_setCurrOffsets = 25;
        static final int TRANSACTION_setDimensionHints = 9;
        static final int TRANSACTION_setDisplayPadding = 13;
        static final int TRANSACTION_setInAmbientMode = 23;
        static final int TRANSACTION_setLockWallpaperCallback = 19;
        static final int TRANSACTION_setNextOffsets = 26;
        static final int TRANSACTION_setWallpaper = 1;
        static final int TRANSACTION_setWallpaperComponent = 3;
        static final int TRANSACTION_setWallpaperComponentChecked = 2;
        static final int TRANSACTION_settingsRestored = 15;
        static final int TRANSACTION_unregisterWallpaperColorsCallback = 22;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWallpaperManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWallpaperManager)) {
                return new Proxy(obj);
            }
            return (IWallpaperManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setWallpaper";
                case 2:
                    return "setWallpaperComponentChecked";
                case 3:
                    return "setWallpaperComponent";
                case 4:
                    return "getWallpaper";
                case 5:
                    return "getWallpaperIdForUser";
                case 6:
                    return "getWallpaperInfo";
                case 7:
                    return "clearWallpaper";
                case 8:
                    return "hasNamedWallpaper";
                case 9:
                    return "setDimensionHints";
                case 10:
                    return "getWidthHint";
                case 11:
                    return "getHeightHint";
                case 12:
                    return "getBlurWallpaper";
                case 13:
                    return "setDisplayPadding";
                case 14:
                    return "getName";
                case 15:
                    return "settingsRestored";
                case 16:
                    return "isWallpaperSupported";
                case 17:
                    return "isSetWallpaperAllowed";
                case 18:
                    return "isWallpaperBackupEligible";
                case 19:
                    return "setLockWallpaperCallback";
                case 20:
                    return "getWallpaperColors";
                case 21:
                    return "registerWallpaperColorsCallback";
                case 22:
                    return "unregisterWallpaperColorsCallback";
                case 23:
                    return "setInAmbientMode";
                case 24:
                    return "getCurrOffsets";
                case 25:
                    return "setCurrOffsets";
                case 26:
                    return "setNextOffsets";
                case 27:
                    return "getWallpaperUserId";
                case 28:
                    return "scaleWallpaperBitmapToScreenSize";
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
            Rect _arg2;
            ComponentName _arg0;
            ComponentName _arg02;
            Rect _arg03;
            Bitmap _arg04;
            if (code != 1598968902) {
                boolean _arg05 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean _arg3 = data.readInt() != 0;
                        Bundle _arg4 = new Bundle();
                        ParcelFileDescriptor _result = setWallpaper(_arg06, _arg1, _arg2, _arg3, _arg4, data.readInt(), IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        reply.writeInt(1);
                        _arg4.writeToParcel(reply, 1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setWallpaperComponentChecked(_arg0, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setWallpaperComponent(_arg02);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        IWallpaperManagerCallback _arg12 = IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg22 = data.readInt();
                        Bundle _arg32 = new Bundle();
                        ParcelFileDescriptor _result2 = getWallpaper(_arg07, _arg12, _arg22, _arg32, data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        reply.writeInt(1);
                        _arg32.writeToParcel(reply, 1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getWallpaperIdForUser(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        WallpaperInfo _result4 = getWallpaperInfo(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        clearWallpaper(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasNamedWallpaper = hasNamedWallpaper(data.readString());
                        reply.writeNoException();
                        reply.writeInt(hasNamedWallpaper ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setDimensionHints(data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getWidthHint(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getHeightHint(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result7 = getBlurWallpaper(IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        setDisplayPadding(_arg03, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = getName();
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        settingsRestored();
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWallpaperSupported = isWallpaperSupported(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isWallpaperSupported ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSetWallpaperAllowed = isSetWallpaperAllowed(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isSetWallpaperAllowed ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWallpaperBackupEligible = isWallpaperBackupEligible(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isWallpaperBackupEligible ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean lockWallpaperCallback = setLockWallpaperCallback(IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(lockWallpaperCallback ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        WallpaperColors _result9 = getWallpaperColors(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        registerWallpaperColorsCallback(IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterWallpaperColorsCallback(IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        setInAmbientMode(_arg05, data.readLong());
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result10 = getCurrOffsets();
                        reply.writeNoException();
                        reply.writeIntArray(_result10);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        setCurrOffsets(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        setNextOffsets(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getWallpaperUserId();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Bitmap.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        Bitmap _result12 = scaleWallpaperBitmapToScreenSize(_arg04);
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements IWallpaperManager {
            public static IWallpaperManager sDefaultImpl;
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

            @Override // android.app.IWallpaperManager
            public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion, int userId) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(name);
                        try {
                            _data.writeString(callingPackage);
                            if (cropHint != null) {
                                _data.writeInt(1);
                                cropHint.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeInt(allowBackup ? 1 : 0);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(which);
                            _data.writeStrongBinder(completion != null ? completion.asBinder() : null);
                            _data.writeInt(userId);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                if (_reply.readInt() != 0) {
                                    try {
                                        extras.readFromParcel(_reply);
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            ParcelFileDescriptor wallpaper = Stub.getDefaultImpl().setWallpaper(name, callingPackage, cropHint, allowBackup, extras, which, completion, userId);
                            _reply.recycle();
                            _data.recycle();
                            return wallpaper;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IWallpaperManager
            public void setWallpaperComponentChecked(ComponentName name, String callingPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (name != null) {
                        _data.writeInt(1);
                        name.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWallpaperComponentChecked(name, callingPackage, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public void setWallpaperComponent(ComponentName name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (name != null) {
                        _data.writeInt(1);
                        name.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWallpaperComponent(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public ParcelFileDescriptor getWallpaper(String callingPkg, IWallpaperManagerCallback cb, int which, Bundle outParams, int userId) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPkg);
                        _result = null;
                        _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                        try {
                            _data.writeInt(which);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                            }
                            if (_reply.readInt() != 0) {
                                try {
                                    outParams.readFromParcel(_reply);
                                } catch (Throwable th4) {
                                    th = th4;
                                }
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        ParcelFileDescriptor wallpaper = Stub.getDefaultImpl().getWallpaper(callingPkg, cb, which, outParams, userId);
                        _reply.recycle();
                        _data.recycle();
                        return wallpaper;
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IWallpaperManager
            public int getWallpaperIdForUser(int which, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(which);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWallpaperIdForUser(which, userId);
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

            @Override // android.app.IWallpaperManager
            public WallpaperInfo getWallpaperInfo(int userId) throws RemoteException {
                WallpaperInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWallpaperInfo(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WallpaperInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.IWallpaperManager
            public void clearWallpaper(String callingPackage, int which, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(which);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearWallpaper(callingPackage, which, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public boolean hasNamedWallpaper(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasNamedWallpaper(name);
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

            @Override // android.app.IWallpaperManager
            public void setDimensionHints(int width, int height, String callingPackage, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeString(callingPackage);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDimensionHints(width, height, callingPackage, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public int getWidthHint(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWidthHint(displayId);
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

            @Override // android.app.IWallpaperManager
            public int getHeightHint(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHeightHint(displayId);
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

            @Override // android.app.IWallpaperManager
            public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBlurWallpaper(cb);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.IWallpaperManager
            public void setDisplayPadding(Rect padding, String callingPackage, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (padding != null) {
                        _data.writeInt(1);
                        padding.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDisplayPadding(padding, callingPackage, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getName();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public void settingsRestored() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().settingsRestored();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public boolean isWallpaperSupported(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWallpaperSupported(callingPackage);
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

            @Override // android.app.IWallpaperManager
            public boolean isSetWallpaperAllowed(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSetWallpaperAllowed(callingPackage);
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

            @Override // android.app.IWallpaperManager
            public boolean isWallpaperBackupEligible(int which, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(which);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWallpaperBackupEligible(which, userId);
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

            @Override // android.app.IWallpaperManager
            public boolean setLockWallpaperCallback(IWallpaperManagerCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setLockWallpaperCallback(cb);
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

            @Override // android.app.IWallpaperManager
            public WallpaperColors getWallpaperColors(int which, int userId, int displayId) throws RemoteException {
                WallpaperColors _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(which);
                    _data.writeInt(userId);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWallpaperColors(which, userId, displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WallpaperColors.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.IWallpaperManager
            public void registerWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeInt(userId);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerWallpaperColorsCallback(cb, userId, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public void unregisterWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeInt(userId);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterWallpaperColorsCallback(cb, userId, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public void setInAmbientMode(boolean inAmbientMode, long animationDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(inAmbientMode ? 1 : 0);
                    _data.writeLong(animationDuration);
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setInAmbientMode(inAmbientMode, animationDuration);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public int[] getCurrOffsets() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrOffsets();
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

            @Override // android.app.IWallpaperManager
            public void setCurrOffsets(int[] offsets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(offsets);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCurrOffsets(offsets);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public void setNextOffsets(int[] offsets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(offsets);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNextOffsets(offsets);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IWallpaperManager
            public int getWallpaperUserId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWallpaperUserId();
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

            @Override // android.app.IWallpaperManager
            public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bitmap != null) {
                        _data.writeInt(1);
                        bitmap.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().scaleWallpaperBitmapToScreenSize(bitmap);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
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
        }

        public static boolean setDefaultImpl(IWallpaperManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWallpaperManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
