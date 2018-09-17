package android.rog;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwRogManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwRogManager {
        private static final String DESCRIPTOR = "android.rog.IHwRogManager";
        static final int TRANSACTION_getAppRogInfos = 7;
        static final int TRANSACTION_getOwnAppRogInfo = 5;
        static final int TRANSACTION_getRogSwitchState = 4;
        static final int TRANSACTION_getSpecifiedAppRogInfo = 6;
        static final int TRANSACTION_registerRogListener = 1;
        static final int TRANSACTION_setRogSwitchState = 3;
        static final int TRANSACTION_unRegisterRogListener = 2;
        static final int TRANSACTION_updateAppRogInfo = 9;
        static final int TRANSACTION_updateBatchAppRogInfo = 8;

        private static class Proxy implements IHwRogManager {
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

            public boolean registerRogListener(IHwRogListener listener, String pkgName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_registerRogListener, _data, _reply, 0);
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

            public void unRegisterRogListener(IHwRogListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unRegisterRogListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRogSwitchState(boolean open) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (open) {
                        i = Stub.TRANSACTION_registerRogListener;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRogSwitchState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getRogSwitchState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRogSwitchState, _data, _reply, 0);
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

            public AppRogInfo getOwnAppRogInfo(IHwRogListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AppRogInfo appRogInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getOwnAppRogInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        appRogInfo = (AppRogInfo) AppRogInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        appRogInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return appRogInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AppRogInfo getSpecifiedAppRogInfo(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AppRogInfo appRogInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getSpecifiedAppRogInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        appRogInfo = (AppRogInfo) AppRogInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        appRogInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return appRogInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AppRogInfo> getAppRogInfos() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAppRogInfos, _data, _reply, 0);
                    _reply.readException();
                    List<AppRogInfo> _result = _reply.createTypedArrayList(AppRogInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AppRogInfo> updateBatchAppRogInfo(List<AppRogInfo> newRogInfos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(newRogInfos);
                    this.mRemote.transact(Stub.TRANSACTION_updateBatchAppRogInfo, _data, _reply, 0);
                    _reply.readException();
                    List<AppRogInfo> _result = _reply.createTypedArrayList(AppRogInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AppRogInfo updateAppRogInfo(AppRogInfo newRogInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AppRogInfo appRogInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newRogInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_registerRogListener);
                        newRogInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateAppRogInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        appRogInfo = (AppRogInfo) AppRogInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        appRogInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return appRogInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwRogManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwRogManager)) {
                return new Proxy(obj);
            }
            return (IHwRogManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            AppRogInfo _result2;
            List<AppRogInfo> _result3;
            switch (code) {
                case TRANSACTION_registerRogListener /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerRogListener(android.rog.IHwRogListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerRogListener : 0);
                    return true;
                case TRANSACTION_unRegisterRogListener /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unRegisterRogListener(android.rog.IHwRogListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRogSwitchState /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRogSwitchState(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRogSwitchState /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getRogSwitchState();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_registerRogListener : 0);
                    return true;
                case TRANSACTION_getOwnAppRogInfo /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getOwnAppRogInfo(android.rog.IHwRogListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_registerRogListener);
                        _result2.writeToParcel(reply, TRANSACTION_registerRogListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSpecifiedAppRogInfo /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSpecifiedAppRogInfo(data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_registerRogListener);
                        _result2.writeToParcel(reply, TRANSACTION_registerRogListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAppRogInfos /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAppRogInfos();
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case TRANSACTION_updateBatchAppRogInfo /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateBatchAppRogInfo(data.createTypedArrayList(AppRogInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case TRANSACTION_updateAppRogInfo /*9*/:
                    AppRogInfo appRogInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        appRogInfo = (AppRogInfo) AppRogInfo.CREATOR.createFromParcel(data);
                    } else {
                        appRogInfo = null;
                    }
                    _result2 = updateAppRogInfo(appRogInfo);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_registerRogListener);
                        _result2.writeToParcel(reply, TRANSACTION_registerRogListener);
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

    List<AppRogInfo> getAppRogInfos() throws RemoteException;

    AppRogInfo getOwnAppRogInfo(IHwRogListener iHwRogListener) throws RemoteException;

    boolean getRogSwitchState() throws RemoteException;

    AppRogInfo getSpecifiedAppRogInfo(String str) throws RemoteException;

    boolean registerRogListener(IHwRogListener iHwRogListener, String str) throws RemoteException;

    void setRogSwitchState(boolean z) throws RemoteException;

    void unRegisterRogListener(IHwRogListener iHwRogListener) throws RemoteException;

    AppRogInfo updateAppRogInfo(AppRogInfo appRogInfo) throws RemoteException;

    List<AppRogInfo> updateBatchAppRogInfo(List<AppRogInfo> list) throws RemoteException;
}
