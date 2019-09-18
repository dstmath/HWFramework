package com.huawei.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.keymaster.HwExportResult;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwOperationResult;

public interface IHwKeystoreService extends IInterface {

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
        static final int TRANSACTION_set = 11;
        static final int TRANSACTION_setKeyProtection = 16;
        static final int TRANSACTION_update = 6;

        private static class Proxy implements IHwKeystoreService {
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

            public int del(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int generateKey(String alias, HwKeymasterArguments arguments, byte[] entropy, int uid, int flags, HwKeyCharacteristics characteristics) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (arguments != null) {
                        _data.writeInt(1);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(entropy);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        characteristics.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        characteristics.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwExportResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwOperationResult begin(IBinder appToken, String alias, int purpose, boolean pruneable, HwKeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
                HwOperationResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    _data.writeString(alias);
                    _data.writeInt(purpose);
                    _data.writeInt(pruneable);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(entropy);
                    _data.writeInt(uid);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwOperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwOperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwOperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int abort(IBinder handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(handle);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        chain.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwExportResult get(String alias, int uid) throws RemoteException {
                HwExportResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(uid);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwExportResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(Stub.TRANSACTION_set, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getHuksServiceVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getHuksServiceVersion, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(Stub.TRANSACTION_attestDeviceIds, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outChain.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(Stub.TRANSACTION_assetHandleReq, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        outResult.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int exportTrustCert(HwKeymasterCertificateChain chain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        chain.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int contains(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    this.mRemote.transact(Stub.TRANSACTION_contains, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v29, resolved type: com.huawei.security.keymaster.HwKeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v33, resolved type: com.huawei.security.keymaster.HwKeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v38, resolved type: com.huawei.security.keymaster.HwKeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v43, resolved type: com.huawei.security.keymaster.HwKeymasterBlob} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: com.huawei.security.keymaster.HwKeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v52, resolved type: com.huawei.security.keymaster.HwKeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v57, resolved type: com.huawei.security.keymaster.HwKeymasterArguments} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v3 */
        /* JADX WARNING: type inference failed for: r0v10 */
        /* JADX WARNING: type inference failed for: r0v17 */
        /* JADX WARNING: type inference failed for: r0v23 */
        /* JADX WARNING: type inference failed for: r0v63 */
        /* JADX WARNING: type inference failed for: r0v64 */
        /* JADX WARNING: type inference failed for: r0v65 */
        /* JADX WARNING: type inference failed for: r0v66 */
        /* JADX WARNING: type inference failed for: r0v67 */
        /* JADX WARNING: type inference failed for: r0v68 */
        /* JADX WARNING: type inference failed for: r0v69 */
        /* JADX WARNING: type inference failed for: r0v70 */
        /* JADX WARNING: type inference failed for: r0v71 */
        /* JADX WARNING: type inference failed for: r0v72 */
        /* JADX WARNING: type inference failed for: r0v73 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwKeymasterBlob _arg1;
            HwKeymasterBlob _arg2;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg12 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result = del(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        HwKeymasterArguments _arg13 = _arg12;
                        byte[] _arg22 = data.createByteArray();
                        int _arg3 = data.readInt();
                        int _arg4 = data.readInt();
                        HwKeyCharacteristics _arg5 = new HwKeyCharacteristics();
                        int _result2 = generateKey(_arg0, _arg13, _arg22, _arg3, _arg4, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        parcel2.writeInt(1);
                        _arg5.writeToParcel(parcel2, 1);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = HwKeymasterBlob.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterBlob.CREATOR.createFromParcel(parcel);
                        }
                        HwKeymasterBlob _arg23 = _arg12;
                        int _arg32 = data.readInt();
                        HwKeyCharacteristics _arg42 = new HwKeyCharacteristics();
                        int _result3 = getKeyCharacteristics(_arg02, _arg1, _arg23, _arg32, _arg42);
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        parcel2.writeInt(1);
                        _arg42.writeToParcel(parcel2, 1);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = HwKeymasterBlob.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterBlob.CREATOR.createFromParcel(parcel);
                        }
                        HwExportResult _result4 = exportKey(_arg03, _arg14, _arg2, _arg12, data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            parcel2.writeInt(1);
                            _result4.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        String _arg15 = data.readString();
                        int _arg24 = data.readInt();
                        boolean _arg33 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        HwOperationResult _result5 = begin(_arg04, _arg15, _arg24, _arg33, _arg12, data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            parcel2.writeInt(1);
                            _result5.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg05 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        HwOperationResult _result6 = update(_arg05, _arg12, data.createByteArray());
                        reply.writeNoException();
                        if (_result6 != null) {
                            parcel2.writeInt(1);
                            _result6.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg06 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        HwOperationResult _result7 = finish(_arg06, _arg12, data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result7 != null) {
                            parcel2.writeInt(1);
                            _result7.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result8 = abort(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        HwKeymasterCertificateChain _arg34 = new HwKeymasterCertificateChain();
                        int _result9 = attestKey(_arg07, _arg16, _arg12, _arg34);
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        parcel2.writeInt(1);
                        _arg34.writeToParcel(parcel2, 1);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        HwExportResult _result10 = get(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            parcel2.writeInt(1);
                            _result10.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_set /*11*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterBlob.CREATOR.createFromParcel(parcel);
                        }
                        int _result11 = set(_arg08, _arg12, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case TRANSACTION_getHuksServiceVersion /*12*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result12 = getHuksServiceVersion();
                        reply.writeNoException();
                        parcel2.writeString(_result12);
                        return true;
                    case TRANSACTION_attestDeviceIds /*13*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        HwKeymasterCertificateChain _arg17 = new HwKeymasterCertificateChain();
                        int _result13 = attestDeviceIds(_arg12, _arg17);
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        parcel2.writeInt(1);
                        _arg17.writeToParcel(parcel2, 1);
                        return true;
                    case TRANSACTION_assetHandleReq /*14*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        HwKeymasterCertificateChain _arg18 = new HwKeymasterCertificateChain();
                        int _result14 = assetHandleReq(_arg12, _arg18);
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        parcel2.writeInt(1);
                        _arg18.writeToParcel(parcel2, 1);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        HwKeymasterCertificateChain _arg09 = new HwKeymasterCertificateChain();
                        int _result15 = exportTrustCert(_arg09);
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        parcel2.writeInt(1);
                        _arg09.writeToParcel(parcel2, 1);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = HwKeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        int _result16 = setKeyProtection(_arg010, _arg12);
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case TRANSACTION_contains /*17*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result17 = contains(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

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

    int set(String str, HwKeymasterBlob hwKeymasterBlob, int i) throws RemoteException;

    int setKeyProtection(String str, HwKeymasterArguments hwKeymasterArguments) throws RemoteException;

    HwOperationResult update(IBinder iBinder, HwKeymasterArguments hwKeymasterArguments, byte[] bArr) throws RemoteException;
}
