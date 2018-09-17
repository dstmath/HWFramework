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
        static final int TRANSACTION_checkPassword = 12;
        static final int TRANSACTION_checkPattern = 9;
        static final int TRANSACTION_checkVoldPassword = 15;
        static final int TRANSACTION_getBoolean = 4;
        static final int TRANSACTION_getLong = 5;
        static final int TRANSACTION_getSeparateProfileChallengeEnabled = 19;
        static final int TRANSACTION_getString = 6;
        static final int TRANSACTION_getStrongAuthForUser = 25;
        static final int TRANSACTION_havePassword = 17;
        static final int TRANSACTION_havePattern = 16;
        static final int TRANSACTION_registerStrongAuthTracker = 20;
        static final int TRANSACTION_requireStrongAuth = 22;
        static final int TRANSACTION_resetKeyStore = 8;
        static final int TRANSACTION_setBoolean = 1;
        static final int TRANSACTION_setLockPassword = 11;
        static final int TRANSACTION_setLockPattern = 7;
        static final int TRANSACTION_setLong = 2;
        static final int TRANSACTION_setSeparateProfileChallengeEnabled = 18;
        static final int TRANSACTION_setString = 3;
        static final int TRANSACTION_systemReady = 23;
        static final int TRANSACTION_unregisterStrongAuthTracker = 21;
        static final int TRANSACTION_userPresent = 24;
        static final int TRANSACTION_verifyPassword = 13;
        static final int TRANSACTION_verifyPattern = 10;
        static final int TRANSACTION_verifyTiedProfileChallenge = 14;

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
                int i = Stub.TRANSACTION_setBoolean;
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
                    this.mRemote.transact(Stub.TRANSACTION_setBoolean, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setLong, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setString, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_setBoolean;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getBoolean, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getLong, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getString, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLockPattern(String pattern, String savedPattern, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pattern);
                    _data.writeString(savedPattern);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setLockPattern, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_resetKeyStore, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse checkPattern(String pattern, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse verifyCredentialResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pattern);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_checkPattern, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        verifyCredentialResponse = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        verifyCredentialResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return verifyCredentialResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse verifyPattern(String pattern, long challenge, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse verifyCredentialResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pattern);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_verifyPattern, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        verifyCredentialResponse = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        verifyCredentialResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return verifyCredentialResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLockPassword(String password, String savedPassword, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeString(savedPassword);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setLockPassword, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse checkPassword(String password, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse verifyCredentialResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_checkPassword, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        verifyCredentialResponse = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        verifyCredentialResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return verifyCredentialResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse verifyPassword(String password, long challenge, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse verifyCredentialResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_verifyPassword, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        verifyCredentialResponse = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        verifyCredentialResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return verifyCredentialResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse verifyTiedProfileChallenge(String password, boolean isPattern, long challenge, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VerifyCredentialResponse verifyCredentialResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (isPattern) {
                        i = Stub.TRANSACTION_setBoolean;
                    }
                    _data.writeInt(i);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_verifyTiedProfileChallenge, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        verifyCredentialResponse = (VerifyCredentialResponse) VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        verifyCredentialResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return verifyCredentialResponse;
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
                    this.mRemote.transact(Stub.TRANSACTION_checkVoldPassword, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_havePattern, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_havePassword, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_setBoolean;
                    }
                    _data.writeInt(i);
                    _data.writeString(managedUserPassword);
                    this.mRemote.transact(Stub.TRANSACTION_setSeparateProfileChallengeEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getSeparateProfileChallengeEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_registerStrongAuthTracker, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_unregisterStrongAuthTracker, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_requireStrongAuth, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_systemReady, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_userPresent, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getStrongAuthForUser, _data, _reply, 0);
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
            VerifyCredentialResponse _result2;
            switch (code) {
                case TRANSACTION_setBoolean /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBoolean(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setLong /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLong(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setString /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setString(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getBoolean /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getBoolean(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setBoolean : 0);
                    return true;
                case TRANSACTION_getLong /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result3 = getLong(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_getString /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result4 = getString(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_setLockPattern /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLockPattern(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resetKeyStore /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    resetKeyStore(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_checkPattern /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkPattern(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setBoolean);
                        _result2.writeToParcel(reply, TRANSACTION_setBoolean);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_verifyPattern /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = verifyPattern(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setBoolean);
                        _result2.writeToParcel(reply, TRANSACTION_setBoolean);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setLockPassword /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLockPassword(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_checkPassword /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkPassword(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setBoolean);
                        _result2.writeToParcel(reply, TRANSACTION_setBoolean);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_verifyPassword /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = verifyPassword(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setBoolean);
                        _result2.writeToParcel(reply, TRANSACTION_setBoolean);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_verifyTiedProfileChallenge /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = verifyTiedProfileChallenge(data.readString(), data.readInt() != 0, data.readLong(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setBoolean);
                        _result2.writeToParcel(reply, TRANSACTION_setBoolean);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_checkVoldPassword /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkVoldPassword(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setBoolean : 0);
                    return true;
                case TRANSACTION_havePattern /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = havePattern(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setBoolean : 0);
                    return true;
                case TRANSACTION_havePassword /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = havePassword(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setBoolean : 0);
                    return true;
                case TRANSACTION_setSeparateProfileChallengeEnabled /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSeparateProfileChallengeEnabled(data.readInt(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSeparateProfileChallengeEnabled /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSeparateProfileChallengeEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setBoolean : 0);
                    return true;
                case TRANSACTION_registerStrongAuthTracker /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerStrongAuthTracker(android.app.trust.IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterStrongAuthTracker /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterStrongAuthTracker(android.app.trust.IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requireStrongAuth /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    requireStrongAuth(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_systemReady /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    systemReady();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_userPresent /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    userPresent(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getStrongAuthForUser /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result5 = getStrongAuthForUser(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    VerifyCredentialResponse checkPassword(String str, int i) throws RemoteException;

    VerifyCredentialResponse checkPattern(String str, int i) throws RemoteException;

    boolean checkVoldPassword(int i) throws RemoteException;

    boolean getBoolean(String str, boolean z, int i) throws RemoteException;

    long getLong(String str, long j, int i) throws RemoteException;

    boolean getSeparateProfileChallengeEnabled(int i) throws RemoteException;

    String getString(String str, String str2, int i) throws RemoteException;

    int getStrongAuthForUser(int i) throws RemoteException;

    boolean havePassword(int i) throws RemoteException;

    boolean havePattern(int i) throws RemoteException;

    void registerStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    void requireStrongAuth(int i, int i2) throws RemoteException;

    void resetKeyStore(int i) throws RemoteException;

    void setBoolean(String str, boolean z, int i) throws RemoteException;

    void setLockPassword(String str, String str2, int i) throws RemoteException;

    void setLockPattern(String str, String str2, int i) throws RemoteException;

    void setLong(String str, long j, int i) throws RemoteException;

    void setSeparateProfileChallengeEnabled(int i, boolean z, String str) throws RemoteException;

    void setString(String str, String str2, int i) throws RemoteException;

    void systemReady() throws RemoteException;

    void unregisterStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    void userPresent(int i) throws RemoteException;

    VerifyCredentialResponse verifyPassword(String str, long j, int i) throws RemoteException;

    VerifyCredentialResponse verifyPattern(String str, long j, int i) throws RemoteException;

    VerifyCredentialResponse verifyTiedProfileChallenge(String str, boolean z, long j, int i) throws RemoteException;
}
