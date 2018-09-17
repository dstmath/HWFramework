package com.android.internal.widget;

import android.app.trust.IStrongAuthTracker;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILockSettings extends IInterface {

    public static abstract class Stub extends Binder implements ILockSettings {
        private static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
        static final int TRANSACTION_addEscrowToken = 23;
        static final int TRANSACTION_checkCredential = 9;
        static final int TRANSACTION_checkVoldPassword = 12;
        static final int TRANSACTION_clearExtendLockScreenPassword = 29;
        static final int TRANSACTION_getBoolean = 4;
        static final int TRANSACTION_getLong = 5;
        static final int TRANSACTION_getSeparateProfileChallengeEnabled = 16;
        static final int TRANSACTION_getString = 6;
        static final int TRANSACTION_getStrongAuthForUser = 22;
        static final int TRANSACTION_havePassword = 14;
        static final int TRANSACTION_havePattern = 13;
        static final int TRANSACTION_isEscrowTokenActive = 25;
        static final int TRANSACTION_registerStrongAuthTracker = 17;
        static final int TRANSACTION_removeEscrowToken = 24;
        static final int TRANSACTION_requireStrongAuth = 19;
        static final int TRANSACTION_resetKeyStore = 8;
        static final int TRANSACTION_setBoolean = 1;
        static final int TRANSACTION_setExtendLockScreenPassword = 28;
        static final int TRANSACTION_setLockCredential = 7;
        static final int TRANSACTION_setLockCredentialWithToken = 26;
        static final int TRANSACTION_setLong = 2;
        static final int TRANSACTION_setSeparateProfileChallengeEnabled = 15;
        static final int TRANSACTION_setString = 3;
        static final int TRANSACTION_systemReady = 20;
        static final int TRANSACTION_unlockUserWithToken = 27;
        static final int TRANSACTION_unregisterStrongAuthTracker = 18;
        static final int TRANSACTION_userPresent = 21;
        static final int TRANSACTION_verifyCredential = 10;
        static final int TRANSACTION_verifyTiedProfileChallenge = 11;

        private static class Proxy implements ILockSettings {
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

            public void setBoolean(String key, boolean value, int userId) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (!value) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLong(String key, long value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(value);
                    _data.writeInt(userId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setString(String key, String value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    _data.writeInt(userId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (defaultValue) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public long getLong(String key, long defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(defaultValue);
                    _data.writeInt(userId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getString(String key, String defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(defaultValue);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLockCredential(String credential, int type, String savedCredential, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeString(savedCredential);
                    _data.writeInt(userId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetKeyStore(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse checkCredential(String credential, int type, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeInt(userId);
                    if (progressCallback != null) {
                        iBinder = progressCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
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

            public VerifyCredentialResponse verifyCredential(String credential, int type, long challenge, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
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

            public VerifyCredentialResponse verifyTiedProfileChallenge(String credential, int type, long challenge, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
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

            public boolean checkVoldPassword(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(12, _data, _reply, 0);
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

            public boolean havePattern(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(13, _data, _reply, 0);
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

            public boolean havePassword(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, String managedUserPassword) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (enabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(managedUserPassword);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getSeparateProfileChallengeEnabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(16, _data, _reply, 0);
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

            public void registerStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tracker != null) {
                        iBinder = tracker.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tracker != null) {
                        iBinder = tracker.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requireStrongAuth(int strongAuthReason, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(strongAuthReason);
                    _data.writeInt(userId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void systemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void userPresent(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStrongAuthForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long addEscrowToken(byte[] token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(token);
                    _data.writeInt(userId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeEscrowToken(long handle, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeInt(userId);
                    this.mRemote.transact(24, _data, _reply, 0);
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

            public boolean isEscrowTokenActive(long handle, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(handle);
                    _data.writeInt(userId);
                    this.mRemote.transact(25, _data, _reply, 0);
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

            public boolean setLockCredentialWithToken(String credential, int type, long tokenHandle, byte[] token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeLong(tokenHandle);
                    _data.writeByteArray(token);
                    _data.writeInt(userId);
                    this.mRemote.transact(26, _data, _reply, 0);
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

            public void unlockUserWithToken(long tokenHandle, byte[] token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(tokenHandle);
                    _data.writeByteArray(token);
                    _data.writeInt(userId);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeString(phoneNumber);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(28, _data, _reply, 0);
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

            public boolean clearExtendLockScreenPassword(String password, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(29, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILockSettings asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILockSettings)) {
                return new Proxy(obj);
            }
            return (ILockSettings) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            long _result2;
            VerifyCredentialResponse _result3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setBoolean(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    setLong(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    setString(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBoolean(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLong(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    String _result4 = getString(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    setLockCredential(data.readString(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    resetKeyStore(data.readInt());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = checkCredential(data.readString(), data.readInt(), data.readInt(), com.android.internal.widget.ICheckCredentialProgressCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = verifyCredential(data.readString(), data.readInt(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = verifyTiedProfileChallenge(data.readString(), data.readInt(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkVoldPassword(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result = havePattern(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result = havePassword(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    setSeparateProfileChallengeEnabled(data.readInt(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSeparateProfileChallengeEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    registerStrongAuthTracker(android.app.trust.IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterStrongAuthTracker(android.app.trust.IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    requireStrongAuth(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    systemReady();
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    userPresent(data.readInt());
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    int _result5 = getStrongAuthForUser(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = addEscrowToken(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result = removeEscrowToken(data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isEscrowTokenActive(data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setLockCredentialWithToken(data.readString(), data.readInt(), data.readLong(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    unlockUserWithToken(data.readLong(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setExtendLockScreenPassword(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result = clearExtendLockScreenPassword(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    long addEscrowToken(byte[] bArr, int i) throws RemoteException;

    VerifyCredentialResponse checkCredential(String str, int i, int i2, ICheckCredentialProgressCallback iCheckCredentialProgressCallback) throws RemoteException;

    boolean checkVoldPassword(int i) throws RemoteException;

    boolean clearExtendLockScreenPassword(String str, int i) throws RemoteException;

    boolean getBoolean(String str, boolean z, int i) throws RemoteException;

    long getLong(String str, long j, int i) throws RemoteException;

    boolean getSeparateProfileChallengeEnabled(int i) throws RemoteException;

    String getString(String str, String str2, int i) throws RemoteException;

    int getStrongAuthForUser(int i) throws RemoteException;

    boolean havePassword(int i) throws RemoteException;

    boolean havePattern(int i) throws RemoteException;

    boolean isEscrowTokenActive(long j, int i) throws RemoteException;

    void registerStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    boolean removeEscrowToken(long j, int i) throws RemoteException;

    void requireStrongAuth(int i, int i2) throws RemoteException;

    void resetKeyStore(int i) throws RemoteException;

    void setBoolean(String str, boolean z, int i) throws RemoteException;

    boolean setExtendLockScreenPassword(String str, String str2, int i) throws RemoteException;

    void setLockCredential(String str, int i, String str2, int i2) throws RemoteException;

    boolean setLockCredentialWithToken(String str, int i, long j, byte[] bArr, int i2) throws RemoteException;

    void setLong(String str, long j, int i) throws RemoteException;

    void setSeparateProfileChallengeEnabled(int i, boolean z, String str) throws RemoteException;

    void setString(String str, String str2, int i) throws RemoteException;

    void systemReady() throws RemoteException;

    void unlockUserWithToken(long j, byte[] bArr, int i) throws RemoteException;

    void unregisterStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    void userPresent(int i) throws RemoteException;

    VerifyCredentialResponse verifyCredential(String str, int i, long j, int i2) throws RemoteException;

    VerifyCredentialResponse verifyTiedProfileChallenge(String str, int i, long j, int i2) throws RemoteException;
}
