package huawei.android.pfw;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwPFWManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwPFWManager {
        private static final String DESCRIPTOR = "huawei.android.pfw.IHwPFWManager";
        static final int TRANSACTION_appendExtStartupControlScope = 2;
        static final int TRANSACTION_getStartupControlScope = 1;
        static final int TRANSACTION_getStartupPackageList = 6;
        static final int TRANSACTION_getTopAppInfo = 8;
        static final int TRANSACTION_removeStartupSetting = 4;
        static final int TRANSACTION_setPolicyEnabled = 5;
        static final int TRANSACTION_setStartupPackageList = 7;
        static final int TRANSACTION_updateStartupSettings = 3;

        private static class Proxy implements IHwPFWManager {
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

            public HwPFWStartupControlScope getStartupControlScope() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HwPFWStartupControlScope hwPFWStartupControlScope;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStartupControlScope, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        hwPFWStartupControlScope = (HwPFWStartupControlScope) HwPFWStartupControlScope.CREATOR.createFromParcel(_reply);
                    } else {
                        hwPFWStartupControlScope = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return hwPFWStartupControlScope;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void appendExtStartupControlScope(HwPFWStartupControlScope scope) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scope != null) {
                        _data.writeInt(Stub.TRANSACTION_getStartupControlScope);
                        scope.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_appendExtStartupControlScope, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateStartupSettings(List<HwPFWStartupSetting> settings, boolean clearFirst) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(settings);
                    if (clearFirst) {
                        i = Stub.TRANSACTION_getStartupControlScope;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateStartupSettings, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeStartupSetting(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_removeStartupSetting, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPolicyEnabled(int policyType, boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(policyType);
                    if (enabled) {
                        i = Stub.TRANSACTION_getStartupControlScope;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setPolicyEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwPFWStartupPackageList getStartupPackageList(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HwPFWStartupPackageList hwPFWStartupPackageList;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_getStartupPackageList, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        hwPFWStartupPackageList = (HwPFWStartupPackageList) HwPFWStartupPackageList.CREATOR.createFromParcel(_reply);
                    } else {
                        hwPFWStartupPackageList = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return hwPFWStartupPackageList;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStartupPackageList(HwPFWStartupPackageList startupPkgList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (startupPkgList != null) {
                        _data.writeInt(Stub.TRANSACTION_getStartupControlScope);
                        startupPkgList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setStartupPackageList, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTopAppInfo(int topNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(topNum);
                    this.mRemote.transact(Stub.TRANSACTION_getTopAppInfo, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwPFWManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwPFWManager)) {
                return new Proxy(obj);
            }
            return (IHwPFWManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            switch (code) {
                case TRANSACTION_getStartupControlScope /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    HwPFWStartupControlScope _result = getStartupControlScope();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getStartupControlScope);
                        _result.writeToParcel(reply, TRANSACTION_getStartupControlScope);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_appendExtStartupControlScope /*2*/:
                    HwPFWStartupControlScope hwPFWStartupControlScope;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        hwPFWStartupControlScope = (HwPFWStartupControlScope) HwPFWStartupControlScope.CREATOR.createFromParcel(data);
                    } else {
                        hwPFWStartupControlScope = null;
                    }
                    appendExtStartupControlScope(hwPFWStartupControlScope);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateStartupSettings /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<HwPFWStartupSetting> _arg0 = data.createTypedArrayList(HwPFWStartupSetting.CREATOR);
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    updateStartupSettings(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeStartupSetting /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeStartupSetting(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPolicyEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    setPolicyEnabled(_arg02, _arg1);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getStartupPackageList /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    HwPFWStartupPackageList _result2 = getStartupPackageList(data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getStartupControlScope);
                        _result2.writeToParcel(reply, TRANSACTION_getStartupControlScope);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setStartupPackageList /*7*/:
                    HwPFWStartupPackageList hwPFWStartupPackageList;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        hwPFWStartupPackageList = (HwPFWStartupPackageList) HwPFWStartupPackageList.CREATOR.createFromParcel(data);
                    } else {
                        hwPFWStartupPackageList = null;
                    }
                    setStartupPackageList(hwPFWStartupPackageList);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTopAppInfo /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result3 = getTopAppInfo(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void appendExtStartupControlScope(HwPFWStartupControlScope hwPFWStartupControlScope) throws RemoteException;

    HwPFWStartupControlScope getStartupControlScope() throws RemoteException;

    HwPFWStartupPackageList getStartupPackageList(int i) throws RemoteException;

    String getTopAppInfo(int i) throws RemoteException;

    void removeStartupSetting(String str) throws RemoteException;

    void setPolicyEnabled(int i, boolean z) throws RemoteException;

    void setStartupPackageList(HwPFWStartupPackageList hwPFWStartupPackageList) throws RemoteException;

    void updateStartupSettings(List<HwPFWStartupSetting> list, boolean z) throws RemoteException;
}
