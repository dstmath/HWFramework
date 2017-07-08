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
        static final int TRANSACTION_abort = 31;
        static final int TRANSACTION_addAuthToken = 33;
        static final int TRANSACTION_addRngEntropy = 23;
        static final int TRANSACTION_attestKey = 36;
        static final int TRANSACTION_begin = 28;
        static final int TRANSACTION_clear_uid = 22;
        static final int TRANSACTION_del = 4;
        static final int TRANSACTION_duplicate = 20;
        static final int TRANSACTION_exist = 5;
        static final int TRANSACTION_exportKey = 27;
        static final int TRANSACTION_finish = 30;
        static final int TRANSACTION_generate = 12;
        static final int TRANSACTION_generateKey = 24;
        static final int TRANSACTION_get = 2;
        static final int TRANSACTION_getKeyCharacteristics = 25;
        static final int TRANSACTION_getState = 1;
        static final int TRANSACTION_get_pubkey = 16;
        static final int TRANSACTION_getmtime = 19;
        static final int TRANSACTION_grant = 17;
        static final int TRANSACTION_importKey = 26;
        static final int TRANSACTION_import_key = 13;
        static final int TRANSACTION_insert = 3;
        static final int TRANSACTION_isEmpty = 11;
        static final int TRANSACTION_isOperationAuthorized = 32;
        static final int TRANSACTION_is_hardware_backed = 21;
        static final int TRANSACTION_list = 6;
        static final int TRANSACTION_lock = 9;
        static final int TRANSACTION_onUserAdded = 34;
        static final int TRANSACTION_onUserPasswordChanged = 8;
        static final int TRANSACTION_onUserRemoved = 35;
        static final int TRANSACTION_reset = 7;
        static final int TRANSACTION_sign = 14;
        static final int TRANSACTION_ungrant = 18;
        static final int TRANSACTION_unlock = 10;
        static final int TRANSACTION_update = 29;
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
                    this.mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_get, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_insert, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_del, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_exist, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_list, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_reset, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_onUserPasswordChanged, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_lock, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_unlock, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isEmpty, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_getState);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_generate, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_import_key, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_sign, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_verify, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_get_pubkey, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int grant(String name, int granteeUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(granteeUid);
                    this.mRemote.transact(Stub.TRANSACTION_grant, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_ungrant, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getmtime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int duplicate(String srcKey, int srcUid, String destKey, int destUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(srcKey);
                    _data.writeInt(srcUid);
                    _data.writeString(destKey);
                    _data.writeInt(destUid);
                    this.mRemote.transact(Stub.TRANSACTION_duplicate, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_is_hardware_backed, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_clear_uid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addRngEntropy(byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    this.mRemote.transact(Stub.TRANSACTION_addRngEntropy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_getState);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(entropy);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_generateKey, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        characteristics.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, int uid, KeyCharacteristics characteristics) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (clientId != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        clientId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (appId != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getKeyCharacteristics, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        characteristics.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_getState);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(format);
                    _data.writeByteArray(keyData);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_importKey, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        characteristics.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ExportResult exportResult;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(format);
                    if (clientId != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        clientId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (appId != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_exportKey, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        exportResult = (ExportResult) ExportResult.CREATOR.createFromParcel(_reply);
                    } else {
                        exportResult = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return exportResult;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult begin(IBinder appToken, String alias, int purpose, boolean pruneable, KeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
                int i = Stub.TRANSACTION_getState;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    OperationResult operationResult;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    _data.writeString(alias);
                    _data.writeInt(purpose);
                    if (!pruneable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(entropy);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_begin, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        operationResult = (OperationResult) OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        operationResult = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return operationResult;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult update(IBinder token, KeymasterArguments params, byte[] input) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    OperationResult operationResult;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(input);
                    this.mRemote.transact(Stub.TRANSACTION_update, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        operationResult = (OperationResult) OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        operationResult = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return operationResult;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult finish(IBinder token, KeymasterArguments params, byte[] signature, byte[] entropy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    OperationResult operationResult;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(signature);
                    _data.writeByteArray(entropy);
                    this.mRemote.transact(Stub.TRANSACTION_finish, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        operationResult = (OperationResult) OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        operationResult = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return operationResult;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_abort, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isOperationAuthorized, _data, _reply, 0);
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

            public int addAuthToken(byte[] authToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(authToken);
                    this.mRemote.transact(Stub.TRANSACTION_addAuthToken, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_onUserAdded, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_onUserRemoved, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_getState);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_attestKey, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        chain.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            byte[] _result2;
            String _arg0;
            int _arg1;
            int _arg2;
            int _arg3;
            int _arg4;
            KeymasterArguments keymasterArguments;
            KeymasterBlob keymasterBlob;
            IBinder _arg02;
            OperationResult _result3;
            switch (code) {
                case TRANSACTION_getState /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_get /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = get(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_insert /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = insert(data.readString(), data.createByteArray(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_del /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = del(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_exist /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = exist(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_list /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result4 = list(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result4);
                    return true;
                case TRANSACTION_reset /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = reset();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_onUserPasswordChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = onUserPasswordChanged(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_lock /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = lock(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_unlock /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unlock(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_isEmpty /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isEmpty(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_generate /*12*/:
                    KeystoreArguments keystoreArguments;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    _arg3 = data.readInt();
                    _arg4 = data.readInt();
                    if (data.readInt() != 0) {
                        keystoreArguments = (KeystoreArguments) KeystoreArguments.CREATOR.createFromParcel(data);
                    } else {
                        keystoreArguments = null;
                    }
                    _result = generate(_arg0, _arg1, _arg2, _arg3, _arg4, keystoreArguments);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_import_key /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = import_key(data.readString(), data.createByteArray(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sign /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = sign(data.readString(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_verify /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = verify(data.readString(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_get_pubkey /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = get_pubkey(data.readString());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_grant /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = grant(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_ungrant /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = ungrant(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getmtime /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result5 = getmtime(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result5);
                    return true;
                case TRANSACTION_duplicate /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = duplicate(data.readString(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_is_hardware_backed /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = is_hardware_backed(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_clear_uid /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = clear_uid(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_addRngEntropy /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addRngEntropy(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_generateKey /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        keymasterArguments = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        keymasterArguments = null;
                    }
                    byte[] _arg22 = data.createByteArray();
                    _arg3 = data.readInt();
                    _arg4 = data.readInt();
                    KeyCharacteristics _arg5 = new KeyCharacteristics();
                    _result = generateKey(_arg0, keymasterArguments, _arg22, _arg3, _arg4, _arg5);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg5 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _arg5.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getKeyCharacteristics /*25*/:
                    KeymasterBlob keymasterBlob2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        keymasterBlob2 = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        keymasterBlob2 = null;
                    }
                    if (data.readInt() != 0) {
                        keymasterBlob = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        keymasterBlob = null;
                    }
                    _arg3 = data.readInt();
                    KeyCharacteristics _arg42 = new KeyCharacteristics();
                    _result = getKeyCharacteristics(_arg0, keymasterBlob2, keymasterBlob, _arg3, _arg42);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg42 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _arg42.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_importKey /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        keymasterArguments = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        keymasterArguments = null;
                    }
                    _arg2 = data.readInt();
                    byte[] _arg32 = data.createByteArray();
                    _arg4 = data.readInt();
                    int _arg52 = data.readInt();
                    KeyCharacteristics _arg6 = new KeyCharacteristics();
                    _result = importKey(_arg0, keymasterArguments, _arg2, _arg32, _arg4, _arg52, _arg6);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg6 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _arg6.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_exportKey /*27*/:
                    KeymasterBlob keymasterBlob3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        keymasterBlob = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        keymasterBlob = null;
                    }
                    if (data.readInt() != 0) {
                        keymasterBlob3 = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        keymasterBlob3 = null;
                    }
                    ExportResult _result6 = exportKey(_arg0, _arg1, keymasterBlob, keymasterBlob3, data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _result6.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_begin /*28*/:
                    KeymasterArguments keymasterArguments2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    String _arg12 = data.readString();
                    _arg2 = data.readInt();
                    boolean _arg33 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        keymasterArguments2 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        keymasterArguments2 = null;
                    }
                    _result3 = begin(_arg02, _arg12, _arg2, _arg33, keymasterArguments2, data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _result3.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_update /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        keymasterArguments = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        keymasterArguments = null;
                    }
                    _result3 = update(_arg02, keymasterArguments, data.createByteArray());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _result3.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_finish /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        keymasterArguments = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        keymasterArguments = null;
                    }
                    _result3 = finish(_arg02, keymasterArguments, data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _result3.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_abort /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = abort(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_isOperationAuthorized /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result7 = isOperationAuthorized(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result7 ? TRANSACTION_getState : 0);
                    return true;
                case TRANSACTION_addAuthToken /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addAuthToken(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_onUserAdded /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = onUserAdded(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_onUserRemoved /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = onUserRemoved(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_attestKey /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        keymasterArguments = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        keymasterArguments = null;
                    }
                    KeymasterCertificateChain _arg23 = new KeymasterCertificateChain();
                    _result = attestKey(_arg0, keymasterArguments, _arg23);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg23 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _arg23.writeToParcel(reply, TRANSACTION_getState);
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

    int abort(IBinder iBinder) throws RemoteException;

    int addAuthToken(byte[] bArr) throws RemoteException;

    int addRngEntropy(byte[] bArr) throws RemoteException;

    int attestKey(String str, KeymasterArguments keymasterArguments, KeymasterCertificateChain keymasterCertificateChain) throws RemoteException;

    OperationResult begin(IBinder iBinder, String str, int i, boolean z, KeymasterArguments keymasterArguments, byte[] bArr, int i2) throws RemoteException;

    int clear_uid(long j) throws RemoteException;

    int del(String str, int i) throws RemoteException;

    int duplicate(String str, int i, String str2, int i2) throws RemoteException;

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

    int grant(String str, int i) throws RemoteException;

    int importKey(String str, KeymasterArguments keymasterArguments, int i, byte[] bArr, int i2, int i3, KeyCharacteristics keyCharacteristics) throws RemoteException;

    int import_key(String str, byte[] bArr, int i, int i2) throws RemoteException;

    int insert(String str, byte[] bArr, int i, int i2) throws RemoteException;

    int isEmpty(int i) throws RemoteException;

    boolean isOperationAuthorized(IBinder iBinder) throws RemoteException;

    int is_hardware_backed(String str) throws RemoteException;

    String[] list(String str, int i) throws RemoteException;

    int lock(int i) throws RemoteException;

    int onUserAdded(int i, int i2) throws RemoteException;

    int onUserPasswordChanged(int i, String str) throws RemoteException;

    int onUserRemoved(int i) throws RemoteException;

    int reset() throws RemoteException;

    byte[] sign(String str, byte[] bArr) throws RemoteException;

    int ungrant(String str, int i) throws RemoteException;

    int unlock(int i, String str) throws RemoteException;

    OperationResult update(IBinder iBinder, KeymasterArguments keymasterArguments, byte[] bArr) throws RemoteException;

    int verify(String str, byte[] bArr, byte[] bArr2) throws RemoteException;
}
