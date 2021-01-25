package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.securityprofile.ApkDigest;
import huawei.android.security.securityprofile.HwSignedInfo;
import java.util.List;

public interface ISecurityProfileService extends IInterface {
    int addDomainPolicy(byte[] bArr) throws RemoteException;

    HwSignedInfo getActiveHwSignedInfo(String str, int i) throws RemoteException;

    List<String> getLabels(String str, ApkDigest apkDigest) throws RemoteException;

    boolean isBlackApp(String str) throws RemoteException;

    void updateBlackApp(List<String> list, int i) throws RemoteException;

    boolean updateMdmCertBlacklist(List<String> list, int i) throws RemoteException;

    public static class Default implements ISecurityProfileService {
        @Override // huawei.android.security.ISecurityProfileService
        public void updateBlackApp(List<String> list, int action) throws RemoteException {
        }

        @Override // huawei.android.security.ISecurityProfileService
        public boolean updateMdmCertBlacklist(List<String> list, int action) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ISecurityProfileService
        public boolean isBlackApp(String packageName) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ISecurityProfileService
        public int addDomainPolicy(byte[] domainPolicy) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.ISecurityProfileService
        public List<String> getLabels(String packageName, ApkDigest digest) throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.ISecurityProfileService
        public HwSignedInfo getActiveHwSignedInfo(String packageName, int flags) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISecurityProfileService {
        private static final String DESCRIPTOR = "huawei.android.security.ISecurityProfileService";
        static final int TRANSACTION_addDomainPolicy = 4;
        static final int TRANSACTION_getActiveHwSignedInfo = 6;
        static final int TRANSACTION_getLabels = 5;
        static final int TRANSACTION_isBlackApp = 3;
        static final int TRANSACTION_updateBlackApp = 1;
        static final int TRANSACTION_updateMdmCertBlacklist = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecurityProfileService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecurityProfileService)) {
                return new Proxy(obj);
            }
            return (ISecurityProfileService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ApkDigest _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        updateBlackApp(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateMdmCertBlacklist = updateMdmCertBlacklist(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(updateMdmCertBlacklist ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBlackApp = isBlackApp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isBlackApp ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = addDomainPolicy(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (ApkDigest) ApkDigest.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        List<String> _result2 = getLabels(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        HwSignedInfo _result3 = getActiveHwSignedInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
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
        public static class Proxy implements ISecurityProfileService {
            public static ISecurityProfileService sDefaultImpl;
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

            @Override // huawei.android.security.ISecurityProfileService
            public void updateBlackApp(List<String> packageList, int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageList);
                    _data.writeInt(action);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateBlackApp(packageList, action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ISecurityProfileService
            public boolean updateMdmCertBlacklist(List<String> blacklist, int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(blacklist);
                    _data.writeInt(action);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateMdmCertBlacklist(blacklist, action);
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

            @Override // huawei.android.security.ISecurityProfileService
            public boolean isBlackApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isBlackApp(packageName);
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

            @Override // huawei.android.security.ISecurityProfileService
            public int addDomainPolicy(byte[] domainPolicy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(domainPolicy);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addDomainPolicy(domainPolicy);
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

            @Override // huawei.android.security.ISecurityProfileService
            public List<String> getLabels(String packageName, ApkDigest digest) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (digest != null) {
                        _data.writeInt(1);
                        digest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLabels(packageName, digest);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ISecurityProfileService
            public HwSignedInfo getActiveHwSignedInfo(String packageName, int flags) throws RemoteException {
                HwSignedInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveHwSignedInfo(packageName, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (HwSignedInfo) HwSignedInfo.CREATOR.createFromParcel(_reply);
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

        public static boolean setDefaultImpl(ISecurityProfileService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISecurityProfileService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
