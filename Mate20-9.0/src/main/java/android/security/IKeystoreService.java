package android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.security.keymaster.ExportResult;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterBlob;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keymaster.OperationResult;

public interface IKeystoreService extends IInterface {

    public static abstract class Stub extends Binder implements IKeystoreService {
        private static final String DESCRIPTOR = "android.security.IKeystoreService";
        static final int TRANSACTION_abort = 30;
        static final int TRANSACTION_addAuthToken = 32;
        static final int TRANSACTION_addRngEntropy = 22;
        static final int TRANSACTION_attestDeviceIds = 36;
        static final int TRANSACTION_attestKey = 35;
        static final int TRANSACTION_begin = 27;
        static final int TRANSACTION_cancelConfirmationPrompt = 40;
        static final int TRANSACTION_clear_uid = 21;
        static final int TRANSACTION_del = 4;
        static final int TRANSACTION_exist = 5;
        static final int TRANSACTION_exportKey = 26;
        static final int TRANSACTION_finish = 29;
        static final int TRANSACTION_generate = 12;
        static final int TRANSACTION_generateKey = 23;
        static final int TRANSACTION_get = 2;
        static final int TRANSACTION_getKeyCharacteristics = 24;
        static final int TRANSACTION_getState = 1;
        static final int TRANSACTION_get_pubkey = 16;
        static final int TRANSACTION_getmtime = 19;
        static final int TRANSACTION_grant = 17;
        static final int TRANSACTION_importKey = 25;
        static final int TRANSACTION_importWrappedKey = 38;
        static final int TRANSACTION_import_key = 13;
        static final int TRANSACTION_insert = 3;
        static final int TRANSACTION_isConfirmationPromptSupported = 41;
        static final int TRANSACTION_isEmpty = 11;
        static final int TRANSACTION_isOperationAuthorized = 31;
        static final int TRANSACTION_is_hardware_backed = 20;
        static final int TRANSACTION_list = 6;
        static final int TRANSACTION_lock = 9;
        static final int TRANSACTION_onDeviceOffBody = 37;
        static final int TRANSACTION_onKeyguardVisibilityChanged = 42;
        static final int TRANSACTION_onUserAdded = 33;
        static final int TRANSACTION_onUserPasswordChanged = 8;
        static final int TRANSACTION_onUserRemoved = 34;
        static final int TRANSACTION_presentConfirmationPrompt = 39;
        static final int TRANSACTION_reset = 7;
        static final int TRANSACTION_sign = 14;
        static final int TRANSACTION_ungrant = 18;
        static final int TRANSACTION_unlock = 10;
        static final int TRANSACTION_update = 28;
        static final int TRANSACTION_verify = 15;

        private static class Proxy implements IKeystoreService {
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

            public int getState(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] get(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int insert(String name, byte[] item, int uid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeByteArray(item);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int del(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int exist(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] list(String namePrefix, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(namePrefix);
                    _data.writeInt(uid);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int reset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int onUserPasswordChanged(int userId, String newPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(newPassword);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int lock(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unlock(int userId, String userPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(userPassword);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isEmpty(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int generate(String name, int uid, int keyType, int keySize, int flags, KeystoreArguments args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    _data.writeInt(keyType);
                    _data.writeInt(keySize);
                    _data.writeInt(flags);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int import_key(String name, byte[] data, int uid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeByteArray(data);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] sign(String name, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeByteArray(data);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int verify(String name, byte[] data, byte[] signature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeByteArray(data);
                    _data.writeByteArray(signature);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] get_pubkey(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String grant(String name, int granteeUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(granteeUid);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ungrant(String name, int granteeUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(granteeUid);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getmtime(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int is_hardware_backed(String string) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(string);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int clear_uid(long uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(uid);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addRngEntropy(byte[] data, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    _data.writeInt(flags);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int generateKey(String alias, KeymasterArguments arguments, byte[] entropy, int uid, int flags, KeyCharacteristics characteristics) throws RemoteException {
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
                    this.mRemote.transact(23, _data, _reply, 0);
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

            public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appData, int uid, KeyCharacteristics characteristics) throws RemoteException {
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
                    if (appData != null) {
                        _data.writeInt(1);
                        appData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(24, _data, _reply, 0);
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

            public int importKey(String alias, KeymasterArguments arguments, int format, byte[] keyData, int uid, int flags, KeyCharacteristics characteristics) throws RemoteException {
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
                    _data.writeInt(format);
                    _data.writeByteArray(keyData);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(25, _data, _reply, 0);
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

            public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appData, int uid) throws RemoteException {
                ExportResult _result;
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
                    if (appData != null) {
                        _data.writeInt(1);
                        appData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ExportResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult begin(IBinder appToken, String alias, int purpose, boolean pruneable, KeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
                OperationResult _result;
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
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult update(IBinder token, KeymasterArguments params, byte[] input) throws RemoteException {
                OperationResult _result;
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
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult finish(IBinder token, KeymasterArguments params, byte[] signature, byte[] entropy) throws RemoteException {
                OperationResult _result;
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
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = OperationResult.CREATOR.createFromParcel(_reply);
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
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOperationAuthorized(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addAuthToken(byte[] authToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(authToken);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int onUserAdded(int userId, int parentId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(parentId);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int onUserRemoved(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int attestKey(String alias, KeymasterArguments params, KeymasterCertificateChain chain) throws RemoteException {
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
                    this.mRemote.transact(35, _data, _reply, 0);
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

            public int attestDeviceIds(KeymasterArguments params, KeymasterCertificateChain chain) throws RemoteException {
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
                    this.mRemote.transact(36, _data, _reply, 0);
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

            public int onDeviceOffBody() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int importWrappedKey(String wrappedKeyAlias, byte[] wrappedKey, String wrappingKeyAlias, byte[] maskingKey, KeymasterArguments arguments, long rootSid, long fingerprintSid, KeyCharacteristics characteristics) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(wrappedKeyAlias);
                    _data.writeByteArray(wrappedKey);
                    _data.writeString(wrappingKeyAlias);
                    _data.writeByteArray(maskingKey);
                    if (arguments != null) {
                        _data.writeInt(1);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(rootSid);
                    _data.writeLong(fingerprintSid);
                    this.mRemote.transact(38, _data, _reply, 0);
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

            public int presentConfirmationPrompt(IBinder listener, String promptText, byte[] extraData, String locale, int uiOptionsAsFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener);
                    _data.writeString(promptText);
                    _data.writeByteArray(extraData);
                    _data.writeString(locale);
                    _data.writeInt(uiOptionsAsFlags);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelConfirmationPrompt(IBinder listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConfirmationPromptSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int onKeyguardVisibilityChanged(boolean isShowing, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isShowing);
                    _data.writeInt(userId);
                    this.mRemote.transact(42, _data, _reply, 0);
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

        public static IKeystoreService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKeystoreService)) {
                return new Proxy(obj);
            }
            return (IKeystoreService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v13, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v0, resolved type: android.security.KeystoreArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v29, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v36, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: android.security.keymaster.KeymasterBlob} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v43, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v50, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: android.security.keymaster.KeymasterBlob} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v56, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v62, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v66, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v79, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v84, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v98, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v99, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v100, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v101, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v102, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v103, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v104, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v105, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v106, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v107, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v108, resolved type: android.security.keymaster.KeymasterArguments} */
        /* JADX WARNING: type inference failed for: r0v18, types: [android.security.KeystoreArguments] */
        /* JADX WARNING: type inference failed for: r0v42, types: [android.security.keymaster.KeymasterBlob] */
        /* JADX WARNING: type inference failed for: r0v55, types: [android.security.keymaster.KeymasterBlob] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeymasterBlob _arg1;
            KeymasterBlob _arg2;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg0 = false;
                KeymasterArguments _arg02 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result = getState(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = get(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result3 = insert(data.readString(), data.createByteArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = del(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result5 = exist(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result6 = list(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result7 = reset();
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result8 = onUserPasswordChanged(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result9 = lock(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result10 = unlock(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result11 = isEmpty(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg12 = data.readInt();
                        int _arg22 = data.readInt();
                        int _arg3 = data.readInt();
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = KeystoreArguments.CREATOR.createFromParcel(parcel);
                        }
                        int _result12 = generate(_arg03, _arg12, _arg22, _arg3, _arg4, _arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result13 = import_key(data.readString(), data.createByteArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result14 = sign(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result14);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result15 = verify(data.readString(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result16 = get_pubkey(data.readString());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result16);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result17 = grant(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result17);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result18 = ungrant(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result19 = getmtime(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeLong(_result19);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result20 = is_hardware_backed(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result21 = clear_uid(data.readLong());
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result22 = addRngEntropy(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        KeymasterArguments _arg13 = _arg02;
                        byte[] _arg23 = data.createByteArray();
                        int _arg32 = data.readInt();
                        int _arg42 = data.readInt();
                        KeyCharacteristics _arg5 = new KeyCharacteristics();
                        int _result23 = generateKey(_arg04, _arg13, _arg23, _arg32, _arg42, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        parcel2.writeInt(1);
                        _arg5.writeToParcel(parcel2, 1);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = KeymasterBlob.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterBlob.CREATOR.createFromParcel(parcel);
                        }
                        KeymasterBlob _arg24 = _arg02;
                        int _arg33 = data.readInt();
                        KeyCharacteristics _arg43 = new KeyCharacteristics();
                        int _result24 = getKeyCharacteristics(_arg05, _arg1, _arg24, _arg33, _arg43);
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        parcel2.writeInt(1);
                        _arg43.writeToParcel(parcel2, 1);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        KeymasterArguments _arg14 = _arg02;
                        int _arg25 = data.readInt();
                        byte[] _arg34 = data.createByteArray();
                        int _arg44 = data.readInt();
                        int _arg52 = data.readInt();
                        KeyCharacteristics _arg6 = new KeyCharacteristics();
                        int _result25 = importKey(_arg06, _arg14, _arg25, _arg34, _arg44, _arg52, _arg6);
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        parcel2.writeInt(1);
                        _arg6.writeToParcel(parcel2, 1);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = KeymasterBlob.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterBlob.CREATOR.createFromParcel(parcel);
                        }
                        ExportResult _result26 = exportKey(_arg07, _arg15, _arg2, _arg02, data.readInt());
                        reply.writeNoException();
                        if (_result26 != null) {
                            parcel2.writeInt(1);
                            _result26.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg08 = data.readStrongBinder();
                        String _arg16 = data.readString();
                        int _arg26 = data.readInt();
                        boolean _arg35 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        OperationResult _result27 = begin(_arg08, _arg16, _arg26, _arg35, _arg02, data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        if (_result27 != null) {
                            parcel2.writeInt(1);
                            _result27.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg09 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        OperationResult _result28 = update(_arg09, _arg02, data.createByteArray());
                        reply.writeNoException();
                        if (_result28 != null) {
                            parcel2.writeInt(1);
                            _result28.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg010 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        OperationResult _result29 = finish(_arg010, _arg02, data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result29 != null) {
                            parcel2.writeInt(1);
                            _result29.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result30 = abort(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result30);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result31 = isOperationAuthorized(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result31);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result32 = addAuthToken(data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result32);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result33 = onUserAdded(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result33);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result34 = onUserRemoved(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result34);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        KeymasterCertificateChain _arg27 = new KeymasterCertificateChain();
                        int _result35 = attestKey(_arg011, _arg02, _arg27);
                        reply.writeNoException();
                        parcel2.writeInt(_result35);
                        parcel2.writeInt(1);
                        _arg27.writeToParcel(parcel2, 1);
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        KeymasterCertificateChain _arg17 = new KeymasterCertificateChain();
                        int _result36 = attestDeviceIds(_arg02, _arg17);
                        reply.writeNoException();
                        parcel2.writeInt(_result36);
                        parcel2.writeInt(1);
                        _arg17.writeToParcel(parcel2, 1);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result37 = onDeviceOffBody();
                        reply.writeNoException();
                        parcel2.writeInt(_result37);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        byte[] _arg18 = data.createByteArray();
                        String _arg28 = data.readString();
                        byte[] _arg36 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg02 = KeymasterArguments.CREATOR.createFromParcel(parcel);
                        }
                        KeymasterArguments _arg45 = _arg02;
                        long _arg53 = data.readLong();
                        long _arg62 = data.readLong();
                        KeyCharacteristics _arg7 = new KeyCharacteristics();
                        int _result38 = importWrappedKey(_arg012, _arg18, _arg28, _arg36, _arg45, _arg53, _arg62, _arg7);
                        reply.writeNoException();
                        parcel2.writeInt(_result38);
                        parcel2.writeInt(1);
                        _arg7.writeToParcel(parcel2, 1);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result39 = presentConfirmationPrompt(data.readStrongBinder(), data.readString(), data.createByteArray(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result39);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result40 = cancelConfirmationPrompt(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result40);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result41 = isConfirmationPromptSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result41);
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        int _result42 = onKeyguardVisibilityChanged(_arg0, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result42);
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

    int addAuthToken(byte[] bArr) throws RemoteException;

    int addRngEntropy(byte[] bArr, int i) throws RemoteException;

    int attestDeviceIds(KeymasterArguments keymasterArguments, KeymasterCertificateChain keymasterCertificateChain) throws RemoteException;

    int attestKey(String str, KeymasterArguments keymasterArguments, KeymasterCertificateChain keymasterCertificateChain) throws RemoteException;

    OperationResult begin(IBinder iBinder, String str, int i, boolean z, KeymasterArguments keymasterArguments, byte[] bArr, int i2) throws RemoteException;

    int cancelConfirmationPrompt(IBinder iBinder) throws RemoteException;

    int clear_uid(long j) throws RemoteException;

    int del(String str, int i) throws RemoteException;

    int exist(String str, int i) throws RemoteException;

    ExportResult exportKey(String str, int i, KeymasterBlob keymasterBlob, KeymasterBlob keymasterBlob2, int i2) throws RemoteException;

    OperationResult finish(IBinder iBinder, KeymasterArguments keymasterArguments, byte[] bArr, byte[] bArr2) throws RemoteException;

    int generate(String str, int i, int i2, int i3, int i4, KeystoreArguments keystoreArguments) throws RemoteException;

    int generateKey(String str, KeymasterArguments keymasterArguments, byte[] bArr, int i, int i2, KeyCharacteristics keyCharacteristics) throws RemoteException;

    byte[] get(String str, int i) throws RemoteException;

    int getKeyCharacteristics(String str, KeymasterBlob keymasterBlob, KeymasterBlob keymasterBlob2, int i, KeyCharacteristics keyCharacteristics) throws RemoteException;

    int getState(int i) throws RemoteException;

    byte[] get_pubkey(String str) throws RemoteException;

    long getmtime(String str, int i) throws RemoteException;

    String grant(String str, int i) throws RemoteException;

    int importKey(String str, KeymasterArguments keymasterArguments, int i, byte[] bArr, int i2, int i3, KeyCharacteristics keyCharacteristics) throws RemoteException;

    int importWrappedKey(String str, byte[] bArr, String str2, byte[] bArr2, KeymasterArguments keymasterArguments, long j, long j2, KeyCharacteristics keyCharacteristics) throws RemoteException;

    int import_key(String str, byte[] bArr, int i, int i2) throws RemoteException;

    int insert(String str, byte[] bArr, int i, int i2) throws RemoteException;

    boolean isConfirmationPromptSupported() throws RemoteException;

    int isEmpty(int i) throws RemoteException;

    boolean isOperationAuthorized(IBinder iBinder) throws RemoteException;

    int is_hardware_backed(String str) throws RemoteException;

    String[] list(String str, int i) throws RemoteException;

    int lock(int i) throws RemoteException;

    int onDeviceOffBody() throws RemoteException;

    int onKeyguardVisibilityChanged(boolean z, int i) throws RemoteException;

    int onUserAdded(int i, int i2) throws RemoteException;

    int onUserPasswordChanged(int i, String str) throws RemoteException;

    int onUserRemoved(int i) throws RemoteException;

    int presentConfirmationPrompt(IBinder iBinder, String str, byte[] bArr, String str2, int i) throws RemoteException;

    int reset() throws RemoteException;

    byte[] sign(String str, byte[] bArr) throws RemoteException;

    int ungrant(String str, int i) throws RemoteException;

    int unlock(int i, String str) throws RemoteException;

    OperationResult update(IBinder iBinder, KeymasterArguments keymasterArguments, byte[] bArr) throws RemoteException;

    int verify(String str, byte[] bArr, byte[] bArr2) throws RemoteException;
}
