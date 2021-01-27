package com.huawei.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.hwassetmanager.IHwAssetObserver;
import com.huawei.security.keymaster.HwExportResult;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwOperationResult;

public interface IHwKeystoreService extends IInterface {
    int abort(IBinder iBinder) throws RemoteException;

    int assetHandleReq(HwKeymasterArguments hwKeymasterArguments, HwKeymasterCertificateChain hwKeymasterCertificateChain) throws RemoteException;

    int attestDeviceIds(HwKeymasterArguments hwKeymasterArguments, HwKeymasterCertificateChain hwKeymasterCertificateChain) throws RemoteException;

    int attestKey(String str, int i, HwKeymasterArguments hwKeymasterArguments, HwKeymasterCertificateChain hwKeymasterCertificateChain) throws RemoteException;

    HwOperationResult begin(IBinder iBinder, String str, int i, boolean z, HwKeymasterArguments hwKeymasterArguments, byte[] bArr, int i2) throws RemoteException;

    int contains(String str) throws RemoteException;

    int del(String str, int i) throws RemoteException;

    HwExportResult exportKey(String str, int i, HwKeymasterBlob hwKeymasterBlob, HwKeymasterBlob hwKeymasterBlob2, int i2) throws RemoteException;

    int exportTrustCert(HwKeymasterCertificateChain hwKeymasterCertificateChain) throws RemoteException;

    HwOperationResult finish(IBinder iBinder, HwKeymasterArguments hwKeymasterArguments, byte[] bArr, byte[] bArr2) throws RemoteException;

    int generateKey(String str, HwKeymasterArguments hwKeymasterArguments, byte[] bArr, int i, int i2, HwKeyCharacteristics hwKeyCharacteristics) throws RemoteException;

    HwExportResult get(String str, int i) throws RemoteException;

    String getHuksServiceVersion() throws RemoteException;

    int getKeyCharacteristics(String str, HwKeymasterBlob hwKeymasterBlob, HwKeymasterBlob hwKeymasterBlob2, int i, HwKeyCharacteristics hwKeyCharacteristics) throws RemoteException;

    int getSecurityCapabilities(HwKeymasterArguments hwKeymasterArguments, HwKeymasterCertificateChain hwKeymasterCertificateChain) throws RemoteException;

    int getSecurityChallenge(HwKeymasterArguments hwKeymasterArguments, HwKeymasterCertificateChain hwKeymasterCertificateChain) throws RemoteException;

    int importKey(String str, HwKeymasterArguments hwKeymasterArguments, HwKeymasterBlob hwKeymasterBlob) throws RemoteException;

    int onUserCredentialChanged(int i, byte[] bArr) throws RemoteException;

    int registerObserver(IHwAssetObserver iHwAssetObserver, String str, int i, int i2) throws RemoteException;

    int set(String str, HwKeymasterBlob hwKeymasterBlob, int i) throws RemoteException;

    int setKeyProtection(String str, HwKeymasterArguments hwKeymasterArguments) throws RemoteException;

    int unRegisterObserver(String str, int i, int i2) throws RemoteException;

    int unlock(int i, byte[] bArr) throws RemoteException;

    HwOperationResult update(IBinder iBinder, HwKeymasterArguments hwKeymasterArguments, byte[] bArr) throws RemoteException;

    int verifySecurityChallenge(HwKeymasterArguments hwKeymasterArguments) throws RemoteException;

    public static class Default implements IHwKeystoreService {
        @Override // com.huawei.security.IHwKeystoreService
        public int del(String name, int uid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int generateKey(String alias, HwKeymasterArguments arguments, byte[] entropy, int uid, int flags, HwKeyCharacteristics characteristics) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int getKeyCharacteristics(String alias, HwKeymasterBlob clientId, HwKeymasterBlob appId, int uid, HwKeyCharacteristics characteristics) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public HwExportResult exportKey(String alias, int format, HwKeymasterBlob clientId, HwKeymasterBlob appId, int uid) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public HwOperationResult begin(IBinder appToken, String alias, int purpose, boolean pruneable, HwKeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public HwOperationResult update(IBinder token, HwKeymasterArguments params, byte[] input) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public HwOperationResult finish(IBinder token, HwKeymasterArguments params, byte[] signature, byte[] entropy) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int abort(IBinder handle) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int attestKey(String alias, int uid, HwKeymasterArguments params, HwKeymasterCertificateChain chain) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public HwExportResult get(String alias, int uid) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int set(String alias, HwKeymasterBlob blob, int uid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public String getHuksServiceVersion() throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int attestDeviceIds(HwKeymasterArguments params, HwKeymasterCertificateChain outChain) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int assetHandleReq(HwKeymasterArguments params, HwKeymasterCertificateChain outResult) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int exportTrustCert(HwKeymasterCertificateChain chain) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int setKeyProtection(String alias, HwKeymasterArguments params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int contains(String alias) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int registerObserver(IHwAssetObserver observer, String dataOwner, int event, int dataType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int unRegisterObserver(String dataOwner, int event, int dataType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int getSecurityCapabilities(HwKeymasterArguments params, HwKeymasterCertificateChain chain) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int getSecurityChallenge(HwKeymasterArguments params, HwKeymasterCertificateChain chain) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int verifySecurityChallenge(HwKeymasterArguments params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int onUserCredentialChanged(int userId, byte[] newCredential) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int unlock(int userId, byte[] credential) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.IHwKeystoreService
        public int importKey(String alias, HwKeymasterArguments params, HwKeymasterBlob keyData) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwKeystoreService {
        private static final String DESCRIPTOR = "com.huawei.security.IHwKeystoreService";
        static final int TRANSACTION_abort = 8;
        static final int TRANSACTION_assetHandleReq = 14;
        static final int TRANSACTION_attestDeviceIds = 13;
        static final int TRANSACTION_attestKey = 9;
        static final int TRANSACTION_begin = 5;
        static final int TRANSACTION_contains = 17;
        static final int TRANSACTION_del = 1;
        static final int TRANSACTION_exportKey = 4;
        static final int TRANSACTION_exportTrustCert = 15;
        static final int TRANSACTION_finish = 7;
        static final int TRANSACTION_generateKey = 2;
        static final int TRANSACTION_get = 10;
        static final int TRANSACTION_getHuksServiceVersion = 12;
        static final int TRANSACTION_getKeyCharacteristics = 3;
        static final int TRANSACTION_getSecurityCapabilities = 20;
        static final int TRANSACTION_getSecurityChallenge = 21;
        static final int TRANSACTION_importKey = 25;
        static final int TRANSACTION_onUserCredentialChanged = 23;
        static final int TRANSACTION_registerObserver = 18;
        static final int TRANSACTION_set = 11;
        static final int TRANSACTION_setKeyProtection = 16;
        static final int TRANSACTION_unRegisterObserver = 19;
        static final int TRANSACTION_unlock = 24;
        static final int TRANSACTION_update = 6;
        static final int TRANSACTION_verifySecurityChallenge = 22;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwKeystoreService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwKeystoreService)) {
                return new Proxy(obj);
            }
            return (IHwKeystoreService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwKeymasterArguments _arg1;
            HwKeymasterBlob _arg12;
            HwKeymasterBlob _arg2;
            HwKeymasterBlob _arg22;
            HwKeymasterBlob _arg3;
            HwKeymasterArguments _arg4;
            HwKeymasterArguments _arg13;
            HwKeymasterArguments _arg14;
            HwKeymasterArguments _arg23;
            HwKeymasterBlob _arg15;
            HwKeymasterArguments _arg0;
            HwKeymasterArguments _arg02;
            HwKeymasterArguments _arg16;
            HwKeymasterArguments _arg03;
            HwKeymasterArguments _arg04;
            HwKeymasterArguments _arg05;
            HwKeymasterArguments _arg17;
            HwKeymasterBlob _arg24;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = del(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        byte[] _arg25 = data.createByteArray();
                        int _arg32 = data.readInt();
                        int _arg42 = data.readInt();
                        HwKeyCharacteristics _arg5 = new HwKeyCharacteristics();
                        int _result2 = generateKey(_arg06, _arg1, _arg25, _arg32, _arg42, _arg5);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        reply.writeInt(1);
                        _arg5.writeToParcel(reply, 1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (HwKeymasterBlob) HwKeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (HwKeymasterBlob) HwKeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _arg33 = data.readInt();
                        HwKeyCharacteristics _arg43 = new HwKeyCharacteristics();
                        int _result3 = getKeyCharacteristics(_arg07, _arg12, _arg2, _arg33, _arg43);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        reply.writeInt(1);
                        _arg43.writeToParcel(reply, 1);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        int _arg18 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (HwKeymasterBlob) HwKeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = (HwKeymasterBlob) HwKeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        HwExportResult _result4 = exportKey(_arg08, _arg18, _arg22, _arg3, data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg09 = data.readStrongBinder();
                        String _arg19 = data.readString();
                        int _arg26 = data.readInt();
                        boolean _arg34 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg4 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        HwOperationResult _result5 = begin(_arg09, _arg19, _arg26, _arg34, _arg4, data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg010 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg13 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        HwOperationResult _result6 = update(_arg010, _arg13, data.createByteArray());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg011 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        HwOperationResult _result7 = finish(_arg011, _arg14, data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = abort(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        int _arg110 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        HwKeymasterCertificateChain _arg35 = new HwKeymasterCertificateChain();
                        int _result9 = attestKey(_arg012, _arg110, _arg23, _arg35);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        reply.writeInt(1);
                        _arg35.writeToParcel(reply, 1);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        HwExportResult _result10 = get(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = (HwKeymasterBlob) HwKeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        int _result11 = set(_arg013, _arg15, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getHuksServiceVersion();
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        HwKeymasterCertificateChain _arg111 = new HwKeymasterCertificateChain();
                        int _result13 = attestDeviceIds(_arg0, _arg111);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        reply.writeInt(1);
                        _arg111.writeToParcel(reply, 1);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        HwKeymasterCertificateChain _arg112 = new HwKeymasterCertificateChain();
                        int _result14 = assetHandleReq(_arg02, _arg112);
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        reply.writeInt(1);
                        _arg112.writeToParcel(reply, 1);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        HwKeymasterCertificateChain _arg014 = new HwKeymasterCertificateChain();
                        int _result15 = exportTrustCert(_arg014);
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        reply.writeInt(1);
                        _arg014.writeToParcel(reply, 1);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        int _result16 = setKeyProtection(_arg015, _arg16);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = contains(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = registerObserver(IHwAssetObserver.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = unRegisterObserver(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        HwKeymasterCertificateChain _arg113 = new HwKeymasterCertificateChain();
                        int _result20 = getSecurityCapabilities(_arg03, _arg113);
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        reply.writeInt(1);
                        _arg113.writeToParcel(reply, 1);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        HwKeymasterCertificateChain _arg114 = new HwKeymasterCertificateChain();
                        int _result21 = getSecurityChallenge(_arg04, _arg114);
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        reply.writeInt(1);
                        _arg114.writeToParcel(reply, 1);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        int _result22 = verifySecurityChallenge(_arg05);
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = onUserCredentialChanged(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = unlock(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = (HwKeymasterArguments) HwKeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg24 = (HwKeymasterBlob) HwKeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        int _result25 = importKey(_arg016, _arg17, _arg24);
                        reply.writeNoException();
                        reply.writeInt(_result25);
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
        public static class Proxy implements IHwKeystoreService {
            public static IHwKeystoreService sDefaultImpl;
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

            @Override // com.huawei.security.IHwKeystoreService
            public int del(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().del(name, uid);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int generateKey(String alias, HwKeymasterArguments arguments, byte[] entropy, int uid, int flags, HwKeyCharacteristics characteristics) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(alias);
                        if (arguments != null) {
                            _data.writeInt(1);
                            arguments.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeByteArray(entropy);
                            try {
                                _data.writeInt(uid);
                                try {
                                    _data.writeInt(flags);
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
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                if (_reply.readInt() != 0) {
                                    try {
                                        characteristics.readFromParcel(_reply);
                                    } catch (Throwable th5) {
                                        th = th5;
                                    }
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int generateKey = Stub.getDefaultImpl().generateKey(alias, arguments, entropy, uid, flags, characteristics);
                            _reply.recycle();
                            _data.recycle();
                            return generateKey;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public int getKeyCharacteristics(String alias, HwKeymasterBlob clientId, HwKeymasterBlob appId, int uid, HwKeyCharacteristics characteristics) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (clientId != null) {
                        _data.writeInt(1);
                        clientId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (appId != null) {
                        _data.writeInt(1);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyCharacteristics(alias, clientId, appId, uid, characteristics);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        characteristics.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public HwExportResult exportKey(String alias, int format, HwKeymasterBlob clientId, HwKeymasterBlob appId, int uid) throws RemoteException {
                HwExportResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(format);
                    if (clientId != null) {
                        _data.writeInt(1);
                        clientId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (appId != null) {
                        _data.writeInt(1);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exportKey(alias, format, clientId, appId, uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (HwExportResult) HwExportResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.security.IHwKeystoreService
            public HwOperationResult begin(IBinder appToken, String alias, int purpose, boolean pruneable, HwKeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
                Throwable th;
                HwOperationResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(appToken);
                        try {
                            _data.writeString(alias);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(purpose);
                            _data.writeInt(pruneable ? 1 : 0);
                            if (params != null) {
                                _data.writeInt(1);
                                params.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeByteArray(entropy);
                                _data.writeInt(uid);
                                if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    if (_reply.readInt() != 0) {
                                        _result = (HwOperationResult) HwOperationResult.CREATOR.createFromParcel(_reply);
                                    } else {
                                        _result = null;
                                    }
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                }
                                HwOperationResult begin = Stub.getDefaultImpl().begin(appToken, alias, purpose, pruneable, params, entropy, uid);
                                _reply.recycle();
                                _data.recycle();
                                return begin;
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
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public HwOperationResult update(IBinder token, HwKeymasterArguments params, byte[] input) throws RemoteException {
                HwOperationResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(input);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().update(token, params, input);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (HwOperationResult) HwOperationResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.security.IHwKeystoreService
            public HwOperationResult finish(IBinder token, HwKeymasterArguments params, byte[] signature, byte[] entropy) throws RemoteException {
                HwOperationResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(signature);
                    _data.writeByteArray(entropy);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().finish(token, params, signature, entropy);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (HwOperationResult) HwOperationResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int abort(IBinder handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(handle);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().abort(handle);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int attestKey(String alias, int uid, HwKeymasterArguments params, HwKeymasterCertificateChain chain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(uid);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().attestKey(alias, uid, params, chain);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        chain.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public HwExportResult get(String alias, int uid) throws RemoteException {
                HwExportResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().get(alias, uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (HwExportResult) HwExportResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int set(String alias, HwKeymasterBlob blob, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (blob != null) {
                        _data.writeInt(1);
                        blob.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().set(alias, blob, uid);
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

            @Override // com.huawei.security.IHwKeystoreService
            public String getHuksServiceVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHuksServiceVersion();
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

            @Override // com.huawei.security.IHwKeystoreService
            public int attestDeviceIds(HwKeymasterArguments params, HwKeymasterCertificateChain outChain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().attestDeviceIds(params, outChain);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outChain.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public int assetHandleReq(HwKeymasterArguments params, HwKeymasterCertificateChain outResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().assetHandleReq(params, outResult);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outResult.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public int exportTrustCert(HwKeymasterCertificateChain chain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exportTrustCert(chain);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        chain.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public int setKeyProtection(String alias, HwKeymasterArguments params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setKeyProtection(alias, params);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int contains(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().contains(alias);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int registerObserver(IHwAssetObserver observer, String dataOwner, int event, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeString(dataOwner);
                    _data.writeInt(event);
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerObserver(observer, dataOwner, event, dataType);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int unRegisterObserver(String dataOwner, int event, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(dataOwner);
                    _data.writeInt(event);
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterObserver(dataOwner, event, dataType);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int getSecurityCapabilities(HwKeymasterArguments params, HwKeymasterCertificateChain chain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecurityCapabilities(params, chain);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        chain.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public int getSecurityChallenge(HwKeymasterArguments params, HwKeymasterCertificateChain chain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecurityChallenge(params, chain);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        chain.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.IHwKeystoreService
            public int verifySecurityChallenge(HwKeymasterArguments params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifySecurityChallenge(params);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int onUserCredentialChanged(int userId, byte[] newCredential) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeByteArray(newCredential);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onUserCredentialChanged(userId, newCredential);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int unlock(int userId, byte[] credential) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeByteArray(credential);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unlock(userId, credential);
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

            @Override // com.huawei.security.IHwKeystoreService
            public int importKey(String alias, HwKeymasterArguments params, HwKeymasterBlob keyData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (keyData != null) {
                        _data.writeInt(1);
                        keyData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().importKey(alias, params, keyData);
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
        }

        public static boolean setDefaultImpl(IHwKeystoreService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwKeystoreService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
