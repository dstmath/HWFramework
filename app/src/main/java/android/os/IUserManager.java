package android.os;

import android.content.IntentSender;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import java.util.List;

public interface IUserManager extends IInterface {

    public static abstract class Stub extends Binder implements IUserManager {
        private static final String DESCRIPTOR = "android.os.IUserManager";
        static final int TRANSACTION_canAddMoreManagedProfiles = 14;
        static final int TRANSACTION_canHaveRestrictedProfile = 22;
        static final int TRANSACTION_clearSeedAccountData = 43;
        static final int TRANSACTION_createProfileForUser = 3;
        static final int TRANSACTION_createRestrictedProfile = 4;
        static final int TRANSACTION_createUser = 2;
        static final int TRANSACTION_getApplicationRestrictions = 31;
        static final int TRANSACTION_getApplicationRestrictionsForUser = 32;
        static final int TRANSACTION_getCredentialOwnerProfile = 1;
        static final int TRANSACTION_getDefaultGuestRestrictions = 34;
        static final int TRANSACTION_getPrimaryUser = 10;
        static final int TRANSACTION_getProfileIds = 13;
        static final int TRANSACTION_getProfileParent = 15;
        static final int TRANSACTION_getProfiles = 12;
        static final int TRANSACTION_getSeedAccountName = 40;
        static final int TRANSACTION_getSeedAccountOptions = 42;
        static final int TRANSACTION_getSeedAccountType = 41;
        static final int TRANSACTION_getUserAccount = 18;
        static final int TRANSACTION_getUserCreationTime = 20;
        static final int TRANSACTION_getUserHandle = 24;
        static final int TRANSACTION_getUserIcon = 9;
        static final int TRANSACTION_getUserInfo = 17;
        static final int TRANSACTION_getUserRestrictionSource = 25;
        static final int TRANSACTION_getUserRestrictions = 26;
        static final int TRANSACTION_getUserSerialNumber = 23;
        static final int TRANSACTION_getUsers = 11;
        static final int TRANSACTION_hasBaseUserRestriction = 27;
        static final int TRANSACTION_hasUserRestriction = 28;
        static final int TRANSACTION_isManagedProfile = 45;
        static final int TRANSACTION_isQuietModeEnabled = 37;
        static final int TRANSACTION_isRestricted = 21;
        static final int TRANSACTION_isSameProfileGroup = 16;
        static final int TRANSACTION_markGuestForDeletion = 35;
        static final int TRANSACTION_removeUser = 6;
        static final int TRANSACTION_setApplicationRestrictions = 30;
        static final int TRANSACTION_setDefaultGuestRestrictions = 33;
        static final int TRANSACTION_setQuietModeEnabled = 36;
        static final int TRANSACTION_setSeedAccountData = 39;
        static final int TRANSACTION_setUserAccount = 19;
        static final int TRANSACTION_setUserEnabled = 5;
        static final int TRANSACTION_setUserIcon = 8;
        static final int TRANSACTION_setUserName = 7;
        static final int TRANSACTION_setUserRestriction = 29;
        static final int TRANSACTION_someUserHasSeedAccount = 44;
        static final int TRANSACTION_trySetQuietModeDisabled = 38;

        private static class Proxy implements IUserManager {
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

            public int getCredentialOwnerProfile(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getCredentialOwnerProfile, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UserInfo createUser(String name, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UserInfo userInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_createUser, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        userInfo = (UserInfo) UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        userInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return userInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UserInfo createProfileForUser(String name, int flags, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UserInfo userInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_createProfileForUser, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        userInfo = (UserInfo) UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        userInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return userInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UserInfo createRestrictedProfile(String name, int parentUserHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UserInfo userInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(parentUserHandle);
                    this.mRemote.transact(Stub.TRANSACTION_createRestrictedProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        userInfo = (UserInfo) UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        userInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return userInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserEnabled(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_setUserEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeUser(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_removeUser, _data, _reply, 0);
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

            public void setUserName(int userHandle, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_setUserName, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserIcon(int userHandle, Bitmap icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (icon != null) {
                        _data.writeInt(Stub.TRANSACTION_getCredentialOwnerProfile);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setUserIcon, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getUserIcon(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getUserIcon, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UserInfo getPrimaryUser() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UserInfo userInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPrimaryUser, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        userInfo = (UserInfo) UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        userInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return userInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<UserInfo> getUsers(boolean excludeDying) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (excludeDying) {
                        i = Stub.TRANSACTION_getCredentialOwnerProfile;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getUsers, _data, _reply, 0);
                    _reply.readException();
                    List<UserInfo> _result = _reply.createTypedArrayList(UserInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<UserInfo> getProfiles(int userHandle, boolean enabledOnly) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (enabledOnly) {
                        i = Stub.TRANSACTION_getCredentialOwnerProfile;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getProfiles, _data, _reply, 0);
                    _reply.readException();
                    List<UserInfo> _result = _reply.createTypedArrayList(UserInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getProfileIds(int userId, boolean enabledOnly) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (enabledOnly) {
                        i = Stub.TRANSACTION_getCredentialOwnerProfile;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getProfileIds, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canAddMoreManagedProfiles(int userHandle, boolean allowedToRemoveOne) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (allowedToRemoveOne) {
                        i = Stub.TRANSACTION_getCredentialOwnerProfile;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_canAddMoreManagedProfiles, _data, _reply, 0);
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

            public UserInfo getProfileParent(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UserInfo userInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getProfileParent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        userInfo = (UserInfo) UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        userInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return userInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSameProfileGroup(int userHandle, int otherUserHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeInt(otherUserHandle);
                    this.mRemote.transact(Stub.TRANSACTION_isSameProfileGroup, _data, _reply, 0);
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

            public UserInfo getUserInfo(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UserInfo userInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getUserInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        userInfo = (UserInfo) UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        userInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return userInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getUserAccount(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getUserAccount, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserAccount(int userHandle, String accountName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(accountName);
                    this.mRemote.transact(Stub.TRANSACTION_setUserAccount, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getUserCreationTime(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getUserCreationTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRestricted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isRestricted, _data, _reply, 0);
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

            public boolean canHaveRestrictedProfile(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_canHaveRestrictedProfile, _data, _reply, 0);
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

            public int getUserSerialNumber(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getUserSerialNumber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUserHandle(int userSerialNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userSerialNumber);
                    this.mRemote.transact(Stub.TRANSACTION_getUserHandle, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUserRestrictionSource(String restrictionKey, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getUserRestrictionSource, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getUserRestrictions(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getUserRestrictions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasBaseUserRestriction(String restrictionKey, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_hasBaseUserRestriction, _data, _reply, 0);
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

            public boolean hasUserRestriction(String restrictionKey, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_hasUserRestriction, _data, _reply, 0);
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

            public void setUserRestriction(String key, boolean value, int userHandle) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (value) {
                        i = Stub.TRANSACTION_getCredentialOwnerProfile;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_setUserRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setApplicationRestrictions(String packageName, Bundle restrictions, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (restrictions != null) {
                        _data.writeInt(Stub.TRANSACTION_getCredentialOwnerProfile);
                        restrictions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_setApplicationRestrictions, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getApplicationRestrictions(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getApplicationRestrictions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getApplicationRestrictionsForUser(String packageName, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_getApplicationRestrictionsForUser, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefaultGuestRestrictions(Bundle restrictions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (restrictions != null) {
                        _data.writeInt(Stub.TRANSACTION_getCredentialOwnerProfile);
                        restrictions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultGuestRestrictions, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getDefaultGuestRestrictions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultGuestRestrictions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean markGuestForDeletion(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_markGuestForDeletion, _data, _reply, 0);
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

            public void setQuietModeEnabled(int userHandle, boolean enableQuietMode) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (enableQuietMode) {
                        i = Stub.TRANSACTION_getCredentialOwnerProfile;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setQuietModeEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isQuietModeEnabled(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    this.mRemote.transact(Stub.TRANSACTION_isQuietModeEnabled, _data, _reply, 0);
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

            public boolean trySetQuietModeDisabled(int userHandle, IntentSender target) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (target != null) {
                        _data.writeInt(Stub.TRANSACTION_getCredentialOwnerProfile);
                        target.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_trySetQuietModeDisabled, _data, _reply, 0);
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

            public void setSeedAccountData(int userHandle, String accountName, String accountType, PersistableBundle accountOptions, boolean persist) throws RemoteException {
                int i = Stub.TRANSACTION_getCredentialOwnerProfile;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(accountName);
                    _data.writeString(accountType);
                    if (accountOptions != null) {
                        _data.writeInt(Stub.TRANSACTION_getCredentialOwnerProfile);
                        accountOptions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!persist) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setSeedAccountData, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSeedAccountName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSeedAccountName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSeedAccountType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSeedAccountType, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PersistableBundle getSeedAccountOptions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PersistableBundle persistableBundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSeedAccountOptions, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        persistableBundle = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(_reply);
                    } else {
                        persistableBundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return persistableBundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearSeedAccountData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearSeedAccountData, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean someUserHasSeedAccount(String accountName, String accountType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(accountName);
                    _data.writeString(accountType);
                    this.mRemote.transact(Stub.TRANSACTION_someUserHasSeedAccount, _data, _reply, 0);
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

            public boolean isManagedProfile(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isManagedProfile, _data, _reply, 0);
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

        public static IUserManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUserManager)) {
                return new Proxy(obj);
            }
            return (IUserManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            UserInfo _result2;
            boolean _result3;
            int _arg0;
            List<UserInfo> _result4;
            String _result5;
            Bundle _result6;
            switch (code) {
                case TRANSACTION_getCredentialOwnerProfile /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCredentialOwnerProfile(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_createUser /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = createUser(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result2.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_createProfileForUser /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = createProfileForUser(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result2.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_createRestrictedProfile /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = createRestrictedProfile(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result2.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setUserEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUserEnabled(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeUser /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = removeUser(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_setUserName /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUserName(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setUserIcon /*8*/:
                    Bitmap bitmap;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        bitmap = null;
                    }
                    setUserIcon(_arg0, bitmap);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getUserIcon /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParcelFileDescriptor _result7 = getUserIcon(data.readInt());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result7.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPrimaryUser /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPrimaryUser();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result2.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getUsers /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getUsers(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case TRANSACTION_getProfiles /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getProfiles(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case TRANSACTION_getProfileIds /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result8 = getProfileIds(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeIntArray(_result8);
                    return true;
                case TRANSACTION_canAddMoreManagedProfiles /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = canAddMoreManagedProfiles(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_getProfileParent /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getProfileParent(data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result2.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isSameProfileGroup /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isSameProfileGroup(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_getUserInfo /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUserInfo(data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result2.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getUserAccount /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getUserAccount(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_setUserAccount /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUserAccount(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getUserCreationTime /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result9 = getUserCreationTime(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result9);
                    return true;
                case TRANSACTION_isRestricted /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isRestricted();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_canHaveRestrictedProfile /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = canHaveRestrictedProfile(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_getUserSerialNumber /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUserSerialNumber(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getUserHandle /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUserHandle(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getUserRestrictionSource /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUserRestrictionSource(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getUserRestrictions /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getUserRestrictions(data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result6.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_hasBaseUserRestriction /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = hasBaseUserRestriction(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_hasUserRestriction /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = hasUserRestriction(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_setUserRestriction /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUserRestriction(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setApplicationRestrictions /*30*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    setApplicationRestrictions(_arg02, bundle, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getApplicationRestrictions /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getApplicationRestrictions(data.readString());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result6.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getApplicationRestrictionsForUser /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getApplicationRestrictionsForUser(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result6.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setDefaultGuestRestrictions /*33*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    setDefaultGuestRestrictions(bundle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDefaultGuestRestrictions /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getDefaultGuestRestrictions();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result6.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_markGuestForDeletion /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = markGuestForDeletion(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_setQuietModeEnabled /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    setQuietModeEnabled(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isQuietModeEnabled /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isQuietModeEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_trySetQuietModeDisabled /*38*/:
                    IntentSender intentSender;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        intentSender = (IntentSender) IntentSender.CREATOR.createFromParcel(data);
                    } else {
                        intentSender = null;
                    }
                    _result3 = trySetQuietModeDisabled(_arg0, intentSender);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_setSeedAccountData /*39*/:
                    PersistableBundle persistableBundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        persistableBundle = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                    } else {
                        persistableBundle = null;
                    }
                    setSeedAccountData(_arg0, _arg1, _arg2, persistableBundle, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSeedAccountName /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getSeedAccountName();
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_getSeedAccountType /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getSeedAccountType();
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_getSeedAccountOptions /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    PersistableBundle _result10 = getSeedAccountOptions();
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(TRANSACTION_getCredentialOwnerProfile);
                        _result10.writeToParcel(reply, TRANSACTION_getCredentialOwnerProfile);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_clearSeedAccountData /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearSeedAccountData();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_someUserHasSeedAccount /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = someUserHasSeedAccount(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case TRANSACTION_isManagedProfile /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isManagedProfile(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getCredentialOwnerProfile : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean canAddMoreManagedProfiles(int i, boolean z) throws RemoteException;

    boolean canHaveRestrictedProfile(int i) throws RemoteException;

    void clearSeedAccountData() throws RemoteException;

    UserInfo createProfileForUser(String str, int i, int i2) throws RemoteException;

    UserInfo createRestrictedProfile(String str, int i) throws RemoteException;

    UserInfo createUser(String str, int i) throws RemoteException;

    Bundle getApplicationRestrictions(String str) throws RemoteException;

    Bundle getApplicationRestrictionsForUser(String str, int i) throws RemoteException;

    int getCredentialOwnerProfile(int i) throws RemoteException;

    Bundle getDefaultGuestRestrictions() throws RemoteException;

    UserInfo getPrimaryUser() throws RemoteException;

    int[] getProfileIds(int i, boolean z) throws RemoteException;

    UserInfo getProfileParent(int i) throws RemoteException;

    List<UserInfo> getProfiles(int i, boolean z) throws RemoteException;

    String getSeedAccountName() throws RemoteException;

    PersistableBundle getSeedAccountOptions() throws RemoteException;

    String getSeedAccountType() throws RemoteException;

    String getUserAccount(int i) throws RemoteException;

    long getUserCreationTime(int i) throws RemoteException;

    int getUserHandle(int i) throws RemoteException;

    ParcelFileDescriptor getUserIcon(int i) throws RemoteException;

    UserInfo getUserInfo(int i) throws RemoteException;

    int getUserRestrictionSource(String str, int i) throws RemoteException;

    Bundle getUserRestrictions(int i) throws RemoteException;

    int getUserSerialNumber(int i) throws RemoteException;

    List<UserInfo> getUsers(boolean z) throws RemoteException;

    boolean hasBaseUserRestriction(String str, int i) throws RemoteException;

    boolean hasUserRestriction(String str, int i) throws RemoteException;

    boolean isManagedProfile(int i) throws RemoteException;

    boolean isQuietModeEnabled(int i) throws RemoteException;

    boolean isRestricted() throws RemoteException;

    boolean isSameProfileGroup(int i, int i2) throws RemoteException;

    boolean markGuestForDeletion(int i) throws RemoteException;

    boolean removeUser(int i) throws RemoteException;

    void setApplicationRestrictions(String str, Bundle bundle, int i) throws RemoteException;

    void setDefaultGuestRestrictions(Bundle bundle) throws RemoteException;

    void setQuietModeEnabled(int i, boolean z) throws RemoteException;

    void setSeedAccountData(int i, String str, String str2, PersistableBundle persistableBundle, boolean z) throws RemoteException;

    void setUserAccount(int i, String str) throws RemoteException;

    void setUserEnabled(int i) throws RemoteException;

    void setUserIcon(int i, Bitmap bitmap) throws RemoteException;

    void setUserName(int i, String str) throws RemoteException;

    void setUserRestriction(String str, boolean z, int i) throws RemoteException;

    boolean someUserHasSeedAccount(String str, String str2) throws RemoteException;

    boolean trySetQuietModeDisabled(int i, IntentSender intentSender) throws RemoteException;
}
