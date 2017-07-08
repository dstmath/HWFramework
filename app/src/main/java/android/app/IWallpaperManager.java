package android.app;

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

    public static abstract class Stub extends Binder implements IWallpaperManager {
        private static final String DESCRIPTOR = "android.app.IWallpaperManager";
        static final int TRANSACTION_clearWallpaper = 7;
        static final int TRANSACTION_getBlurWallpaper = 12;
        static final int TRANSACTION_getCurrOffsets = 20;
        static final int TRANSACTION_getHeightHint = 11;
        static final int TRANSACTION_getName = 14;
        static final int TRANSACTION_getWallpaper = 4;
        static final int TRANSACTION_getWallpaperIdForUser = 5;
        static final int TRANSACTION_getWallpaperInfo = 6;
        static final int TRANSACTION_getWallpaperUserId = 23;
        static final int TRANSACTION_getWidthHint = 10;
        static final int TRANSACTION_hasNamedWallpaper = 8;
        static final int TRANSACTION_isSetWallpaperAllowed = 17;
        static final int TRANSACTION_isWallpaperBackupEligible = 18;
        static final int TRANSACTION_isWallpaperSupported = 16;
        static final int TRANSACTION_scaleWallpaperBitmapToScreenSize = 24;
        static final int TRANSACTION_setCurrOffsets = 21;
        static final int TRANSACTION_setDimensionHints = 9;
        static final int TRANSACTION_setDisplayPadding = 13;
        static final int TRANSACTION_setLockWallpaperCallback = 19;
        static final int TRANSACTION_setNextOffsets = 22;
        static final int TRANSACTION_setWallpaper = 1;
        static final int TRANSACTION_setWallpaperComponent = 3;
        static final int TRANSACTION_setWallpaperComponentChecked = 2;
        static final int TRANSACTION_settingsRestored = 15;

        private static class Proxy implements IWallpaperManager {
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

            public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion) throws RemoteException {
                IBinder iBinder = null;
                int i = Stub.TRANSACTION_setWallpaper;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(callingPackage);
                    if (cropHint != null) {
                        _data.writeInt(Stub.TRANSACTION_setWallpaper);
                        cropHint.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!allowBackup) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(which);
                    if (completion != null) {
                        iBinder = completion.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setWallpaper, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    if (_reply.readInt() != 0) {
                        extras.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWallpaperComponentChecked(ComponentName name, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (name != null) {
                        _data.writeInt(Stub.TRANSACTION_setWallpaper);
                        name.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_setWallpaperComponentChecked, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWallpaperComponent(ComponentName name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (name != null) {
                        _data.writeInt(Stub.TRANSACTION_setWallpaper);
                        name.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setWallpaperComponent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getWallpaper(IWallpaperManagerCallback cb, int which, Bundle outParams, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(which);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getWallpaper, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    if (_reply.readInt() != 0) {
                        outParams.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWallpaperIdForUser(int which, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(which);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getWallpaperIdForUser, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WallpaperInfo getWallpaperInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WallpaperInfo wallpaperInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWallpaperInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wallpaperInfo = (WallpaperInfo) WallpaperInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        wallpaperInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wallpaperInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearWallpaper(String callingPackage, int which, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(which);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_clearWallpaper, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasNamedWallpaper(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_hasNamedWallpaper, _data, _reply, 0);
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

            public void setDimensionHints(int width, int height, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_setDimensionHints, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWidthHint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWidthHint, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHeightHint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getHeightHint, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getBlurWallpaper, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDisplayPadding(Rect padding, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (padding != null) {
                        _data.writeInt(Stub.TRANSACTION_setWallpaper);
                        padding.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_setDisplayPadding, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void settingsRestored() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_settingsRestored, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWallpaperSupported(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isWallpaperSupported, _data, _reply, 0);
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

            public boolean isSetWallpaperAllowed(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isSetWallpaperAllowed, _data, _reply, 0);
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

            public boolean isWallpaperBackupEligible(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isWallpaperBackupEligible, _data, _reply, 0);
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

            public boolean setLockWallpaperCallback(IWallpaperManagerCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setLockWallpaperCallback, _data, _reply, 0);
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

            public int[] getCurrOffsets() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrOffsets, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCurrOffsets(int[] offsets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(offsets);
                    this.mRemote.transact(Stub.TRANSACTION_setCurrOffsets, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNextOffsets(int[] offsets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(offsets);
                    this.mRemote.transact(Stub.TRANSACTION_setNextOffsets, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWallpaperUserId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWallpaperUserId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bitmap bitmap2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bitmap != null) {
                        _data.writeInt(Stub.TRANSACTION_setWallpaper);
                        bitmap.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_scaleWallpaperBitmapToScreenSize, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bitmap2 = (Bitmap) Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        bitmap2 = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bitmap2;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _result;
            ComponentName componentName;
            int _result2;
            boolean _result3;
            switch (code) {
                case TRANSACTION_setWallpaper /*1*/:
                    Rect rect;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    boolean _arg3 = data.readInt() != 0;
                    Bundle _arg4 = new Bundle();
                    _result = setWallpaper(_arg0, _arg1, rect, _arg3, _arg4, data.readInt(), android.app.IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_setWallpaper);
                        _result.writeToParcel(reply, TRANSACTION_setWallpaper);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg4 != null) {
                        reply.writeInt(TRANSACTION_setWallpaper);
                        _arg4.writeToParcel(reply, TRANSACTION_setWallpaper);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setWallpaperComponentChecked /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    setWallpaperComponentChecked(componentName, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setWallpaperComponent /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    setWallpaperComponent(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWallpaper /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    IWallpaperManagerCallback _arg02 = android.app.IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder());
                    int _arg12 = data.readInt();
                    Bundle _arg2 = new Bundle();
                    _result = getWallpaper(_arg02, _arg12, _arg2, data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_setWallpaper);
                        _result.writeToParcel(reply, TRANSACTION_setWallpaper);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg2 != null) {
                        reply.writeInt(TRANSACTION_setWallpaper);
                        _arg2.writeToParcel(reply, TRANSACTION_setWallpaper);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getWallpaperIdForUser /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWallpaperIdForUser(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getWallpaperInfo /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    WallpaperInfo _result4 = getWallpaperInfo();
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_setWallpaper);
                        _result4.writeToParcel(reply, TRANSACTION_setWallpaper);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_clearWallpaper /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearWallpaper(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hasNamedWallpaper /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = hasNamedWallpaper(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setWallpaper : 0);
                    return true;
                case TRANSACTION_setDimensionHints /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDimensionHints(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWidthHint /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWidthHint();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getHeightHint /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getHeightHint();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getBlurWallpaper /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBlurWallpaper(android.app.IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_setWallpaper);
                        _result.writeToParcel(reply, TRANSACTION_setWallpaper);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setDisplayPadding /*13*/:
                    Rect rect2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rect2 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect2 = null;
                    }
                    setDisplayPadding(rect2, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getName /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result5 = getName();
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_settingsRestored /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    settingsRestored();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isWallpaperSupported /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isWallpaperSupported(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setWallpaper : 0);
                    return true;
                case TRANSACTION_isSetWallpaperAllowed /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isSetWallpaperAllowed(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setWallpaper : 0);
                    return true;
                case TRANSACTION_isWallpaperBackupEligible /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isWallpaperBackupEligible(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setWallpaper : 0);
                    return true;
                case TRANSACTION_setLockWallpaperCallback /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setLockWallpaperCallback(android.app.IWallpaperManagerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_setWallpaper : 0);
                    return true;
                case TRANSACTION_getCurrOffsets /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result6 = getCurrOffsets();
                    reply.writeNoException();
                    reply.writeIntArray(_result6);
                    return true;
                case TRANSACTION_setCurrOffsets /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCurrOffsets(data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setNextOffsets /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    setNextOffsets(data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWallpaperUserId /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getWallpaperUserId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_scaleWallpaperBitmapToScreenSize /*24*/:
                    Bitmap bitmap;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        bitmap = null;
                    }
                    Bitmap _result7 = scaleWallpaperBitmapToScreenSize(bitmap);
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_setWallpaper);
                        _result7.writeToParcel(reply, TRANSACTION_setWallpaper);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void clearWallpaper(String str, int i, int i2) throws RemoteException;

    ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback iWallpaperManagerCallback) throws RemoteException;

    int[] getCurrOffsets() throws RemoteException;

    int getHeightHint() throws RemoteException;

    String getName() throws RemoteException;

    ParcelFileDescriptor getWallpaper(IWallpaperManagerCallback iWallpaperManagerCallback, int i, Bundle bundle, int i2) throws RemoteException;

    int getWallpaperIdForUser(int i, int i2) throws RemoteException;

    WallpaperInfo getWallpaperInfo() throws RemoteException;

    int getWallpaperUserId() throws RemoteException;

    int getWidthHint() throws RemoteException;

    boolean hasNamedWallpaper(String str) throws RemoteException;

    boolean isSetWallpaperAllowed(String str) throws RemoteException;

    boolean isWallpaperBackupEligible(int i) throws RemoteException;

    boolean isWallpaperSupported(String str) throws RemoteException;

    Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) throws RemoteException;

    void setCurrOffsets(int[] iArr) throws RemoteException;

    void setDimensionHints(int i, int i2, String str) throws RemoteException;

    void setDisplayPadding(Rect rect, String str) throws RemoteException;

    boolean setLockWallpaperCallback(IWallpaperManagerCallback iWallpaperManagerCallback) throws RemoteException;

    void setNextOffsets(int[] iArr) throws RemoteException;

    ParcelFileDescriptor setWallpaper(String str, String str2, Rect rect, boolean z, Bundle bundle, int i, IWallpaperManagerCallback iWallpaperManagerCallback) throws RemoteException;

    void setWallpaperComponent(ComponentName componentName) throws RemoteException;

    void setWallpaperComponentChecked(ComponentName componentName, String str) throws RemoteException;

    void settingsRestored() throws RemoteException;
}
