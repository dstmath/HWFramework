package huawei.android.pfw;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwPFWManager extends IInterface {
    void appendExtStartupControlScope(HwPFWStartupControlScope hwPFWStartupControlScope) throws RemoteException;

    HwPFWStartupControlScope getStartupControlScope() throws RemoteException;

    HwPFWStartupPackageList getStartupPackageList(int i) throws RemoteException;

    String getTopAppInfo(int i) throws RemoteException;

    void removeStartupSetting(String str) throws RemoteException;

    void setPolicyEnabled(int i, boolean z) throws RemoteException;

    void setStartupPackageList(HwPFWStartupPackageList hwPFWStartupPackageList) throws RemoteException;

    void updateStartupSettings(List<HwPFWStartupSetting> list, boolean z) throws RemoteException;

    public static class Default implements IHwPFWManager {
        @Override // huawei.android.pfw.IHwPFWManager
        public HwPFWStartupControlScope getStartupControlScope() throws RemoteException {
            return null;
        }

        @Override // huawei.android.pfw.IHwPFWManager
        public void appendExtStartupControlScope(HwPFWStartupControlScope scope) throws RemoteException {
        }

        @Override // huawei.android.pfw.IHwPFWManager
        public void updateStartupSettings(List<HwPFWStartupSetting> list, boolean clearFirst) throws RemoteException {
        }

        @Override // huawei.android.pfw.IHwPFWManager
        public void removeStartupSetting(String pkgName) throws RemoteException {
        }

        @Override // huawei.android.pfw.IHwPFWManager
        public void setPolicyEnabled(int policyType, boolean enabled) throws RemoteException {
        }

        @Override // huawei.android.pfw.IHwPFWManager
        public HwPFWStartupPackageList getStartupPackageList(int type) throws RemoteException {
            return null;
        }

        @Override // huawei.android.pfw.IHwPFWManager
        public void setStartupPackageList(HwPFWStartupPackageList startupPkgList) throws RemoteException {
        }

        @Override // huawei.android.pfw.IHwPFWManager
        public String getTopAppInfo(int topNum) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwPFWStartupControlScope _arg0;
            HwPFWStartupPackageList _arg02;
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        HwPFWStartupControlScope _result = getStartupControlScope();
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
                        if (data.readInt() != 0) {
                            _arg0 = HwPFWStartupControlScope.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        appendExtStartupControlScope(_arg0);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<HwPFWStartupSetting> _arg03 = data.createTypedArrayList(HwPFWStartupSetting.CREATOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        updateStartupSettings(_arg03, _arg1);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        removeStartupSetting(data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setPolicyEnabled(_arg04, _arg1);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        HwPFWStartupPackageList _result2 = getStartupPackageList(data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = HwPFWStartupPackageList.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setStartupPackageList(_arg02);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getTopAppInfo(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result3);
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
        public static class Proxy implements IHwPFWManager {
            public static IHwPFWManager sDefaultImpl;
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

            @Override // huawei.android.pfw.IHwPFWManager
            public HwPFWStartupControlScope getStartupControlScope() throws RemoteException {
                HwPFWStartupControlScope _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStartupControlScope();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwPFWStartupControlScope.CREATOR.createFromParcel(_reply);
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

            @Override // huawei.android.pfw.IHwPFWManager
            public void appendExtStartupControlScope(HwPFWStartupControlScope scope) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scope != null) {
                        _data.writeInt(1);
                        scope.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().appendExtStartupControlScope(scope);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.pfw.IHwPFWManager
            public void updateStartupSettings(List<HwPFWStartupSetting> settings, boolean clearFirst) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(settings);
                    _data.writeInt(clearFirst ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateStartupSettings(settings, clearFirst);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.pfw.IHwPFWManager
            public void removeStartupSetting(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeStartupSetting(pkgName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.pfw.IHwPFWManager
            public void setPolicyEnabled(int policyType, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(policyType);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPolicyEnabled(policyType, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.pfw.IHwPFWManager
            public HwPFWStartupPackageList getStartupPackageList(int type) throws RemoteException {
                HwPFWStartupPackageList _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStartupPackageList(type);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwPFWStartupPackageList.CREATOR.createFromParcel(_reply);
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

            @Override // huawei.android.pfw.IHwPFWManager
            public void setStartupPackageList(HwPFWStartupPackageList startupPkgList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (startupPkgList != null) {
                        _data.writeInt(1);
                        startupPkgList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStartupPackageList(startupPkgList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.pfw.IHwPFWManager
            public String getTopAppInfo(int topNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(topNum);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopAppInfo(topNum);
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
        }

        public static boolean setDefaultImpl(IHwPFWManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwPFWManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
