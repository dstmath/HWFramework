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
        static final int TRANSACTION_attestDeviceIds = 37;
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
        static final int TRANSACTION_onDeviceOffBody = 38;
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
                    this.mRemote.transact(1, _data, _reply, 0);
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
                    this.mRemote.transact(2, _data, _reply, 0);
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
                    this.mRemote.transact(3, _data, _reply, 0);
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
                    this.mRemote.transact(4, _data, _reply, 0);
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
                    this.mRemote.transact(5, _data, _reply, 0);
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
                    this.mRemote.transact(6, _data, _reply, 0);
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
                    this.mRemote.transact(7, _data, _reply, 0);
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
                    this.mRemote.transact(8, _data, _reply, 0);
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
                    this.mRemote.transact(9, _data, _reply, 0);
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
                    this.mRemote.transact(10, _data, _reply, 0);
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
                    this.mRemote.transact(11, _data, _reply, 0);
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
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
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
                    this.mRemote.transact(13, _data, _reply, 0);
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
                    this.mRemote.transact(14, _data, _reply, 0);
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
                    this.mRemote.transact(15, _data, _reply, 0);
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
                    this.mRemote.transact(16, _data, _reply, 0);
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
                    this.mRemote.transact(17, _data, _reply, 0);
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
                    this.mRemote.transact(18, _data, _reply, 0);
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
                    this.mRemote.transact(19, _data, _reply, 0);
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
                    this.mRemote.transact(20, _data, _reply, 0);
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
                    this.mRemote.transact(21, _data, _reply, 0);
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
                    this.mRemote.transact(22, _data, _reply, 0);
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
                    this.mRemote.transact(23, _data, _reply, 0);
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
                        _data.writeInt(1);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(entropy);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(24, _data, _reply, 0);
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
                    this.mRemote.transact(25, _data, _reply, 0);
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
                        _data.writeInt(1);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(format);
                    _data.writeByteArray(keyData);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    this.mRemote.transact(26, _data, _reply, 0);
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
                    ExportResult _result;
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
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ExportResult) ExportResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult begin(IBinder appToken, String alias, int purpose, boolean pruneable, KeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    OperationResult _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    _data.writeString(alias);
                    _data.writeInt(purpose);
                    if (!pruneable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(entropy);
                    _data.writeInt(uid);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (OperationResult) OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult update(IBinder token, KeymasterArguments params, byte[] input) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    OperationResult _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(input);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (OperationResult) OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public OperationResult finish(IBinder token, KeymasterArguments params, byte[] signature, byte[] entropy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    OperationResult _result;
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
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (OperationResult) OperationResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
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
                    this.mRemote.transact(31, _data, _reply, 0);
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
                    this.mRemote.transact(32, _data, _reply, 0);
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
                    this.mRemote.transact(33, _data, _reply, 0);
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
                    this.mRemote.transact(34, _data, _reply, 0);
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
                    this.mRemote.transact(35, _data, _reply, 0);
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
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(37, _data, _reply, 0);
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

            public int onDeviceOffBody() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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
            KeymasterArguments _arg12;
            KeymasterBlob _arg22;
            IBinder _arg02;
            OperationResult _result3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = get(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = insert(data.readString(), data.createByteArray(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = del(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = exist(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result4 = list(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result4);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = reset();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = onUserPasswordChanged(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = lock(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unlock(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isEmpty(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 12:
                    KeystoreArguments _arg5;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    _arg3 = data.readInt();
                    _arg4 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg5 = (KeystoreArguments) KeystoreArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg5 = null;
                    }
                    _result = generate(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result = import_key(data.readString(), data.createByteArray(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = sign(data.readString(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result = verify(data.readString(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = get_pubkey(data.readString());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result = grant(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result = ungrant(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    long _result5 = getmtime(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result5);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result = duplicate(data.readString(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result = is_hardware_backed(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result = clear_uid(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addRngEntropy(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg12 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    byte[] _arg23 = data.createByteArray();
                    _arg3 = data.readInt();
                    _arg4 = data.readInt();
                    KeyCharacteristics _arg52 = new KeyCharacteristics();
                    _result = generateKey(_arg0, _arg12, _arg23, _arg3, _arg4, _arg52);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg52 != null) {
                        reply.writeInt(1);
                        _arg52.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 25:
                    KeymasterBlob _arg13;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg13 = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg22 = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    _arg3 = data.readInt();
                    KeyCharacteristics _arg42 = new KeyCharacteristics();
                    _result = getKeyCharacteristics(_arg0, _arg13, _arg22, _arg3, _arg42);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg42 != null) {
                        reply.writeInt(1);
                        _arg42.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg12 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    _arg2 = data.readInt();
                    byte[] _arg32 = data.createByteArray();
                    _arg4 = data.readInt();
                    int _arg53 = data.readInt();
                    KeyCharacteristics _arg6 = new KeyCharacteristics();
                    _result = importKey(_arg0, _arg12, _arg2, _arg32, _arg4, _arg53, _arg6);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg6 != null) {
                        reply.writeInt(1);
                        _arg6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 27:
                    KeymasterBlob _arg33;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg22 = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg33 = (KeymasterBlob) KeymasterBlob.CREATOR.createFromParcel(data);
                    } else {
                        _arg33 = null;
                    }
                    ExportResult _result6 = exportKey(_arg0, _arg1, _arg22, _arg33, data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 28:
                    KeymasterArguments _arg43;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    String _arg14 = data.readString();
                    _arg2 = data.readInt();
                    boolean _arg34 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg43 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg43 = null;
                    }
                    _result3 = begin(_arg02, _arg14, _arg2, _arg34, _arg43, data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg12 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    _result3 = update(_arg02, _arg12, data.createByteArray());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg12 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    _result3 = finish(_arg02, _arg12, data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result = abort(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result7 = isOperationAuthorized(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result7 ? 1 : 0);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addAuthToken(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _result = onUserAdded(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    _result = onUserRemoved(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg12 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    KeymasterCertificateChain _arg24 = new KeymasterCertificateChain();
                    _result = attestKey(_arg0, _arg12, _arg24);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg24 != null) {
                        reply.writeInt(1);
                        _arg24.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 37:
                    KeymasterArguments _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    KeymasterCertificateChain _arg15 = new KeymasterCertificateChain();
                    _result = attestDeviceIds(_arg03, _arg15);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg15 != null) {
                        reply.writeInt(1);
                        _arg15.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 38:
                    data.enforceInterface(DESCRIPTOR);
                    _result = onDeviceOffBody();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
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

    int attestDeviceIds(KeymasterArguments keymasterArguments, KeymasterCertificateChain keymasterCertificateChain) throws RemoteException;

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

    int onDeviceOffBody() throws RemoteException;

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
