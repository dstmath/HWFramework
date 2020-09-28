package android.os;

import android.annotation.UnsupportedAppUsage;
import android.content.IntentSender;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.os.UserManager;
import java.util.List;

public interface IUserManager extends IInterface {
    boolean canAddMoreManagedProfiles(int i, boolean z) throws RemoteException;

    boolean canHaveRestrictedProfile(int i) throws RemoteException;

    void clearSeedAccountData() throws RemoteException;

    UserInfo createProfileForUser(String str, int i, int i2, String[] strArr) throws RemoteException;

    UserInfo createProfileForUserEvenWhenDisallowed(String str, int i, int i2, String[] strArr) throws RemoteException;

    UserInfo createRestrictedProfile(String str, int i) throws RemoteException;

    UserInfo createUser(String str, int i) throws RemoteException;

    void evictCredentialEncryptionKey(int i) throws RemoteException;

    Bundle getApplicationRestrictions(String str) throws RemoteException;

    Bundle getApplicationRestrictionsForUser(String str, int i) throws RemoteException;

    int getCredentialOwnerProfile(int i) throws RemoteException;

    Bundle getDefaultGuestRestrictions() throws RemoteException;

    int getManagedProfileBadge(int i) throws RemoteException;

    UserInfo getPrimaryUser() throws RemoteException;

    int[] getProfileIds(int i, boolean z) throws RemoteException;

    UserInfo getProfileParent(int i) throws RemoteException;

    int getProfileParentId(int i) throws RemoteException;

    List<UserInfo> getProfiles(int i, boolean z) throws RemoteException;

    String getSeedAccountName() throws RemoteException;

    PersistableBundle getSeedAccountOptions() throws RemoteException;

    String getSeedAccountType() throws RemoteException;

    String getUserAccount(int i) throws RemoteException;

    long getUserCreationTime(int i) throws RemoteException;

    int getUserHandle(int i) throws RemoteException;

    ParcelFileDescriptor getUserIcon(int i) throws RemoteException;

    @UnsupportedAppUsage
    UserInfo getUserInfo(int i) throws RemoteException;

    String getUserName() throws RemoteException;

    int getUserRestrictionSource(String str, int i) throws RemoteException;

    List<UserManager.EnforcingUser> getUserRestrictionSources(String str, int i) throws RemoteException;

    Bundle getUserRestrictions(int i) throws RemoteException;

    int getUserSerialNumber(int i) throws RemoteException;

    long getUserStartRealtime() throws RemoteException;

    long getUserUnlockRealtime() throws RemoteException;

    List<UserInfo> getUsers(boolean z) throws RemoteException;

    boolean hasBaseUserRestriction(String str, int i) throws RemoteException;

    boolean hasRestrictedProfiles() throws RemoteException;

    boolean hasUserRestriction(String str, int i) throws RemoteException;

    boolean hasUserRestrictionOnAnyUser(String str) throws RemoteException;

    boolean isDemoUser(int i) throws RemoteException;

    boolean isManagedProfile(int i) throws RemoteException;

    boolean isQuietModeEnabled(int i) throws RemoteException;

    boolean isRestricted() throws RemoteException;

    boolean isSameProfileGroup(int i, int i2) throws RemoteException;

    boolean isUserNameSet(int i) throws RemoteException;

    boolean isUserRunning(int i) throws RemoteException;

    boolean isUserUnlocked(int i) throws RemoteException;

    boolean isUserUnlockingOrUnlocked(int i) throws RemoteException;

    boolean markGuestForDeletion(int i) throws RemoteException;

    boolean removeUser(int i) throws RemoteException;

    boolean removeUserEvenWhenDisallowed(int i) throws RemoteException;

    boolean requestQuietModeEnabled(String str, boolean z, int i, IntentSender intentSender) throws RemoteException;

    void setApplicationRestrictions(String str, Bundle bundle, int i) throws RemoteException;

    void setDefaultGuestRestrictions(Bundle bundle) throws RemoteException;

    void setSeedAccountData(int i, String str, String str2, PersistableBundle persistableBundle, boolean z) throws RemoteException;

    void setUserAccount(int i, String str) throws RemoteException;

    void setUserAdmin(int i) throws RemoteException;

    void setUserEnabled(int i) throws RemoteException;

    void setUserIcon(int i, Bitmap bitmap) throws RemoteException;

    void setUserName(int i, String str) throws RemoteException;

    void setUserRestriction(String str, boolean z, int i) throws RemoteException;

    boolean someUserHasSeedAccount(String str, String str2) throws RemoteException;

    public static class Default implements IUserManager {
        @Override // android.os.IUserManager
        public int getCredentialOwnerProfile(int userHandle) throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public int getProfileParentId(int userHandle) throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public UserInfo createUser(String name, int flags) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public UserInfo createProfileForUser(String name, int flags, int userHandle, String[] disallowedPackages) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public UserInfo createRestrictedProfile(String name, int parentUserHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public void setUserEnabled(int userHandle) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public void setUserAdmin(int userId) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public void evictCredentialEncryptionKey(int userHandle) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public boolean removeUser(int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean removeUserEvenWhenDisallowed(int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public void setUserName(int userHandle, String name) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public void setUserIcon(int userHandle, Bitmap icon) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public ParcelFileDescriptor getUserIcon(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public UserInfo getPrimaryUser() throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public List<UserInfo> getUsers(boolean excludeDying) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public List<UserInfo> getProfiles(int userHandle, boolean enabledOnly) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public int[] getProfileIds(int userId, boolean enabledOnly) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public boolean canAddMoreManagedProfiles(int userHandle, boolean allowedToRemoveOne) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public UserInfo getProfileParent(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public boolean isSameProfileGroup(int userHandle, int otherUserHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public UserInfo getUserInfo(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public String getUserAccount(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public void setUserAccount(int userHandle, String accountName) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public long getUserCreationTime(int userHandle) throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public boolean isRestricted() throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean canHaveRestrictedProfile(int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public int getUserSerialNumber(int userHandle) throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public int getUserHandle(int userSerialNumber) throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public int getUserRestrictionSource(String restrictionKey, int userHandle) throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public List<UserManager.EnforcingUser> getUserRestrictionSources(String restrictionKey, int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public Bundle getUserRestrictions(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public boolean hasBaseUserRestriction(String restrictionKey, int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean hasUserRestriction(String restrictionKey, int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean hasUserRestrictionOnAnyUser(String restrictionKey) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public void setUserRestriction(String key, boolean value, int userHandle) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public void setApplicationRestrictions(String packageName, Bundle restrictions, int userHandle) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public Bundle getApplicationRestrictions(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public Bundle getApplicationRestrictionsForUser(String packageName, int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public void setDefaultGuestRestrictions(Bundle restrictions) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public Bundle getDefaultGuestRestrictions() throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public boolean markGuestForDeletion(int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean isQuietModeEnabled(int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public void setSeedAccountData(int userHandle, String accountName, String accountType, PersistableBundle accountOptions, boolean persist) throws RemoteException {
        }

        @Override // android.os.IUserManager
        public String getSeedAccountName() throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public String getSeedAccountType() throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public PersistableBundle getSeedAccountOptions() throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public void clearSeedAccountData() throws RemoteException {
        }

        @Override // android.os.IUserManager
        public boolean someUserHasSeedAccount(String accountName, String accountType) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean isManagedProfile(int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean isDemoUser(int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public UserInfo createProfileForUserEvenWhenDisallowed(String name, int flags, int userHandle, String[] disallowedPackages) throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public boolean isUserUnlockingOrUnlocked(int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public int getManagedProfileBadge(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public boolean isUserUnlocked(int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean isUserRunning(int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean isUserNameSet(int userHandle) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean hasRestrictedProfiles() throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public boolean requestQuietModeEnabled(String callingPackage, boolean enableQuietMode, int userHandle, IntentSender target) throws RemoteException {
            return false;
        }

        @Override // android.os.IUserManager
        public String getUserName() throws RemoteException {
            return null;
        }

        @Override // android.os.IUserManager
        public long getUserStartRealtime() throws RemoteException {
            return 0;
        }

        @Override // android.os.IUserManager
        public long getUserUnlockRealtime() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUserManager {
        private static final String DESCRIPTOR = "android.os.IUserManager";
        static final int TRANSACTION_canAddMoreManagedProfiles = 18;
        static final int TRANSACTION_canHaveRestrictedProfile = 26;
        static final int TRANSACTION_clearSeedAccountData = 47;
        static final int TRANSACTION_createProfileForUser = 4;
        static final int TRANSACTION_createProfileForUserEvenWhenDisallowed = 51;
        static final int TRANSACTION_createRestrictedProfile = 5;
        static final int TRANSACTION_createUser = 3;
        static final int TRANSACTION_evictCredentialEncryptionKey = 8;
        static final int TRANSACTION_getApplicationRestrictions = 37;
        static final int TRANSACTION_getApplicationRestrictionsForUser = 38;
        static final int TRANSACTION_getCredentialOwnerProfile = 1;
        static final int TRANSACTION_getDefaultGuestRestrictions = 40;
        static final int TRANSACTION_getManagedProfileBadge = 53;
        static final int TRANSACTION_getPrimaryUser = 14;
        static final int TRANSACTION_getProfileIds = 17;
        static final int TRANSACTION_getProfileParent = 19;
        static final int TRANSACTION_getProfileParentId = 2;
        static final int TRANSACTION_getProfiles = 16;
        static final int TRANSACTION_getSeedAccountName = 44;
        static final int TRANSACTION_getSeedAccountOptions = 46;
        static final int TRANSACTION_getSeedAccountType = 45;
        static final int TRANSACTION_getUserAccount = 22;
        static final int TRANSACTION_getUserCreationTime = 24;
        static final int TRANSACTION_getUserHandle = 28;
        static final int TRANSACTION_getUserIcon = 13;
        static final int TRANSACTION_getUserInfo = 21;
        static final int TRANSACTION_getUserName = 59;
        static final int TRANSACTION_getUserRestrictionSource = 29;
        static final int TRANSACTION_getUserRestrictionSources = 30;
        static final int TRANSACTION_getUserRestrictions = 31;
        static final int TRANSACTION_getUserSerialNumber = 27;
        static final int TRANSACTION_getUserStartRealtime = 60;
        static final int TRANSACTION_getUserUnlockRealtime = 61;
        static final int TRANSACTION_getUsers = 15;
        static final int TRANSACTION_hasBaseUserRestriction = 32;
        static final int TRANSACTION_hasRestrictedProfiles = 57;
        static final int TRANSACTION_hasUserRestriction = 33;
        static final int TRANSACTION_hasUserRestrictionOnAnyUser = 34;
        static final int TRANSACTION_isDemoUser = 50;
        static final int TRANSACTION_isManagedProfile = 49;
        static final int TRANSACTION_isQuietModeEnabled = 42;
        static final int TRANSACTION_isRestricted = 25;
        static final int TRANSACTION_isSameProfileGroup = 20;
        static final int TRANSACTION_isUserNameSet = 56;
        static final int TRANSACTION_isUserRunning = 55;
        static final int TRANSACTION_isUserUnlocked = 54;
        static final int TRANSACTION_isUserUnlockingOrUnlocked = 52;
        static final int TRANSACTION_markGuestForDeletion = 41;
        static final int TRANSACTION_removeUser = 9;
        static final int TRANSACTION_removeUserEvenWhenDisallowed = 10;
        static final int TRANSACTION_requestQuietModeEnabled = 58;
        static final int TRANSACTION_setApplicationRestrictions = 36;
        static final int TRANSACTION_setDefaultGuestRestrictions = 39;
        static final int TRANSACTION_setSeedAccountData = 43;
        static final int TRANSACTION_setUserAccount = 23;
        static final int TRANSACTION_setUserAdmin = 7;
        static final int TRANSACTION_setUserEnabled = 6;
        static final int TRANSACTION_setUserIcon = 12;
        static final int TRANSACTION_setUserName = 11;
        static final int TRANSACTION_setUserRestriction = 35;
        static final int TRANSACTION_someUserHasSeedAccount = 48;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getCredentialOwnerProfile";
                case 2:
                    return "getProfileParentId";
                case 3:
                    return "createUser";
                case 4:
                    return "createProfileForUser";
                case 5:
                    return "createRestrictedProfile";
                case 6:
                    return "setUserEnabled";
                case 7:
                    return "setUserAdmin";
                case 8:
                    return "evictCredentialEncryptionKey";
                case 9:
                    return "removeUser";
                case 10:
                    return "removeUserEvenWhenDisallowed";
                case 11:
                    return "setUserName";
                case 12:
                    return "setUserIcon";
                case 13:
                    return "getUserIcon";
                case 14:
                    return "getPrimaryUser";
                case 15:
                    return "getUsers";
                case 16:
                    return "getProfiles";
                case 17:
                    return "getProfileIds";
                case 18:
                    return "canAddMoreManagedProfiles";
                case 19:
                    return "getProfileParent";
                case 20:
                    return "isSameProfileGroup";
                case 21:
                    return "getUserInfo";
                case 22:
                    return "getUserAccount";
                case 23:
                    return "setUserAccount";
                case 24:
                    return "getUserCreationTime";
                case 25:
                    return "isRestricted";
                case 26:
                    return "canHaveRestrictedProfile";
                case 27:
                    return "getUserSerialNumber";
                case 28:
                    return "getUserHandle";
                case 29:
                    return "getUserRestrictionSource";
                case 30:
                    return "getUserRestrictionSources";
                case 31:
                    return "getUserRestrictions";
                case 32:
                    return "hasBaseUserRestriction";
                case 33:
                    return "hasUserRestriction";
                case 34:
                    return "hasUserRestrictionOnAnyUser";
                case 35:
                    return "setUserRestriction";
                case 36:
                    return "setApplicationRestrictions";
                case 37:
                    return "getApplicationRestrictions";
                case 38:
                    return "getApplicationRestrictionsForUser";
                case 39:
                    return "setDefaultGuestRestrictions";
                case 40:
                    return "getDefaultGuestRestrictions";
                case 41:
                    return "markGuestForDeletion";
                case 42:
                    return "isQuietModeEnabled";
                case 43:
                    return "setSeedAccountData";
                case 44:
                    return "getSeedAccountName";
                case 45:
                    return "getSeedAccountType";
                case 46:
                    return "getSeedAccountOptions";
                case 47:
                    return "clearSeedAccountData";
                case 48:
                    return "someUserHasSeedAccount";
                case 49:
                    return "isManagedProfile";
                case 50:
                    return "isDemoUser";
                case 51:
                    return "createProfileForUserEvenWhenDisallowed";
                case 52:
                    return "isUserUnlockingOrUnlocked";
                case 53:
                    return "getManagedProfileBadge";
                case 54:
                    return "isUserUnlocked";
                case 55:
                    return "isUserRunning";
                case 56:
                    return "isUserNameSet";
                case 57:
                    return "hasRestrictedProfiles";
                case 58:
                    return "requestQuietModeEnabled";
                case 59:
                    return "getUserName";
                case 60:
                    return "getUserStartRealtime";
                case 61:
                    return "getUserUnlockRealtime";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bitmap _arg1;
            Bundle _arg12;
            Bundle _arg0;
            PersistableBundle _arg3;
            IntentSender _arg32;
            if (code != 1598968902) {
                boolean _arg02 = false;
                boolean _arg13 = false;
                boolean _arg14 = false;
                boolean _arg15 = false;
                boolean _arg16 = false;
                boolean _arg17 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getCredentialOwnerProfile(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getProfileParentId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        UserInfo _result3 = createUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        UserInfo _result4 = createProfileForUser(data.readString(), data.readInt(), data.readInt(), data.createStringArray());
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
                        UserInfo _result5 = createRestrictedProfile(data.readString(), data.readInt());
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
                        setUserEnabled(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setUserAdmin(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        evictCredentialEncryptionKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeUser = removeUser(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeUser ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeUserEvenWhenDisallowed = removeUserEvenWhenDisallowed(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeUserEvenWhenDisallowed ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        setUserName(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Bitmap.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setUserIcon(_arg03, _arg1);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result6 = getUserIcon(data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        UserInfo _result7 = getPrimaryUser();
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        List<UserInfo> _result8 = getUsers(_arg02);
                        reply.writeNoException();
                        reply.writeTypedList(_result8);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg17 = true;
                        }
                        List<UserInfo> _result9 = getProfiles(_arg04, _arg17);
                        reply.writeNoException();
                        reply.writeTypedList(_result9);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        int[] _result10 = getProfileIds(_arg05, _arg16);
                        reply.writeNoException();
                        reply.writeIntArray(_result10);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean canAddMoreManagedProfiles = canAddMoreManagedProfiles(_arg06, _arg15);
                        reply.writeNoException();
                        reply.writeInt(canAddMoreManagedProfiles ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        UserInfo _result11 = getProfileParent(data.readInt());
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSameProfileGroup = isSameProfileGroup(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSameProfileGroup ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        UserInfo _result12 = getUserInfo(data.readInt());
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = getUserAccount(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        setUserAccount(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        long _result14 = getUserCreationTime(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result14);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRestricted = isRestricted();
                        reply.writeNoException();
                        reply.writeInt(isRestricted ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canHaveRestrictedProfile = canHaveRestrictedProfile(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(canHaveRestrictedProfile ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getUserSerialNumber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getUserHandle(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = getUserRestrictionSource(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        List<UserManager.EnforcingUser> _result18 = getUserRestrictionSources(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result18);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result19 = getUserRestrictions(data.readInt());
                        reply.writeNoException();
                        if (_result19 != null) {
                            reply.writeInt(1);
                            _result19.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasBaseUserRestriction = hasBaseUserRestriction(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasBaseUserRestriction ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasUserRestriction = hasUserRestriction(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasUserRestriction ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasUserRestrictionOnAnyUser = hasUserRestrictionOnAnyUser(data.readString());
                        reply.writeNoException();
                        reply.writeInt(hasUserRestrictionOnAnyUser ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = true;
                        }
                        setUserRestriction(_arg07, _arg14, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        setApplicationRestrictions(_arg08, _arg12, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result20 = getApplicationRestrictions(data.readString());
                        reply.writeNoException();
                        if (_result20 != null) {
                            reply.writeInt(1);
                            _result20.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result21 = getApplicationRestrictionsForUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result21 != null) {
                            reply.writeInt(1);
                            _result21.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setDefaultGuestRestrictions(_arg0);
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result22 = getDefaultGuestRestrictions();
                        reply.writeNoException();
                        if (_result22 != null) {
                            reply.writeInt(1);
                            _result22.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        boolean markGuestForDeletion = markGuestForDeletion(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(markGuestForDeletion ? 1 : 0);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isQuietModeEnabled = isQuietModeEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isQuietModeEnabled ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        String _arg18 = data.readString();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = PersistableBundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        setSeedAccountData(_arg09, _arg18, _arg2, _arg3, data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        String _result23 = getSeedAccountName();
                        reply.writeNoException();
                        reply.writeString(_result23);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        String _result24 = getSeedAccountType();
                        reply.writeNoException();
                        reply.writeString(_result24);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        PersistableBundle _result25 = getSeedAccountOptions();
                        reply.writeNoException();
                        if (_result25 != null) {
                            reply.writeInt(1);
                            _result25.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        clearSeedAccountData();
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        boolean someUserHasSeedAccount = someUserHasSeedAccount(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(someUserHasSeedAccount ? 1 : 0);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isManagedProfile = isManagedProfile(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isManagedProfile ? 1 : 0);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDemoUser = isDemoUser(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDemoUser ? 1 : 0);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        UserInfo _result26 = createProfileForUserEvenWhenDisallowed(data.readString(), data.readInt(), data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        if (_result26 != null) {
                            reply.writeInt(1);
                            _result26.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserUnlockingOrUnlocked = isUserUnlockingOrUnlocked(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUserUnlockingOrUnlocked ? 1 : 0);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = getManagedProfileBadge(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserUnlocked = isUserUnlocked(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUserUnlocked ? 1 : 0);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserRunning = isUserRunning(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUserRunning ? 1 : 0);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserNameSet = isUserNameSet(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUserNameSet ? 1 : 0);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasRestrictedProfiles = hasRestrictedProfiles();
                        reply.writeNoException();
                        reply.writeInt(hasRestrictedProfiles ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = IntentSender.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        boolean requestQuietModeEnabled = requestQuietModeEnabled(_arg010, _arg13, _arg22, _arg32);
                        reply.writeNoException();
                        reply.writeInt(requestQuietModeEnabled ? 1 : 0);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        String _result28 = getUserName();
                        reply.writeNoException();
                        reply.writeString(_result28);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        long _result29 = getUserStartRealtime();
                        reply.writeNoException();
                        reply.writeLong(_result29);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        long _result30 = getUserUnlockRealtime();
                        reply.writeNoException();
                        reply.writeLong(_result30);
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
        public static class Proxy implements IUserManager {
            public static IUserManager sDefaultImpl;
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

            @Override // android.os.IUserManager
            public int getCredentialOwnerProfile(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCredentialOwnerProfile(userHandle);
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

            @Override // android.os.IUserManager
            public int getProfileParentId(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProfileParentId(userHandle);
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

            @Override // android.os.IUserManager
            public UserInfo createUser(String name, int flags) throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createUser(name, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public UserInfo createProfileForUser(String name, int flags, int userHandle, String[] disallowedPackages) throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    _data.writeInt(userHandle);
                    _data.writeStringArray(disallowedPackages);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createProfileForUser(name, flags, userHandle, disallowedPackages);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public UserInfo createRestrictedProfile(String name, int parentUserHandle) throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(parentUserHandle);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createRestrictedProfile(name, parentUserHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public void setUserEnabled(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserEnabled(userHandle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public void setUserAdmin(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserAdmin(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public void evictCredentialEncryptionKey(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().evictCredentialEncryptionKey(userHandle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public boolean removeUser(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeUser(userHandle);
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

            @Override // android.os.IUserManager
            public boolean removeUserEvenWhenDisallowed(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeUserEvenWhenDisallowed(userHandle);
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

            @Override // android.os.IUserManager
            public void setUserName(int userHandle, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(name);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserName(userHandle, name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public void setUserIcon(int userHandle, Bitmap icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (icon != null) {
                        _data.writeInt(1);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserIcon(userHandle, icon);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public ParcelFileDescriptor getUserIcon(int userHandle) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserIcon(userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public UserInfo getPrimaryUser() throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrimaryUser();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public List<UserInfo> getUsers(boolean excludeDying) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(excludeDying ? 1 : 0);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsers(excludeDying);
                    }
                    _reply.readException();
                    List<UserInfo> _result = _reply.createTypedArrayList(UserInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public List<UserInfo> getProfiles(int userHandle, boolean enabledOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeInt(enabledOnly ? 1 : 0);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProfiles(userHandle, enabledOnly);
                    }
                    _reply.readException();
                    List<UserInfo> _result = _reply.createTypedArrayList(UserInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public int[] getProfileIds(int userId, boolean enabledOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(enabledOnly ? 1 : 0);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProfileIds(userId, enabledOnly);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public boolean canAddMoreManagedProfiles(int userHandle, boolean allowedToRemoveOne) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = true;
                    _data.writeInt(allowedToRemoveOne ? 1 : 0);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canAddMoreManagedProfiles(userHandle, allowedToRemoveOne);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public UserInfo getProfileParent(int userHandle) throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProfileParent(userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public boolean isSameProfileGroup(int userHandle, int otherUserHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeInt(otherUserHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSameProfileGroup(userHandle, otherUserHandle);
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

            @Override // android.os.IUserManager
            public UserInfo getUserInfo(int userHandle) throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserInfo(userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public String getUserAccount(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserAccount(userHandle);
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

            @Override // android.os.IUserManager
            public void setUserAccount(int userHandle, String accountName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(accountName);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserAccount(userHandle, accountName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public long getUserCreationTime(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserCreationTime(userHandle);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public boolean isRestricted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRestricted();
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

            @Override // android.os.IUserManager
            public boolean canHaveRestrictedProfile(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canHaveRestrictedProfile(userHandle);
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

            @Override // android.os.IUserManager
            public int getUserSerialNumber(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserSerialNumber(userHandle);
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

            @Override // android.os.IUserManager
            public int getUserHandle(int userSerialNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userSerialNumber);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserHandle(userSerialNumber);
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

            @Override // android.os.IUserManager
            public int getUserRestrictionSource(String restrictionKey, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserRestrictionSource(restrictionKey, userHandle);
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

            @Override // android.os.IUserManager
            public List<UserManager.EnforcingUser> getUserRestrictionSources(String restrictionKey, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserRestrictionSources(restrictionKey, userHandle);
                    }
                    _reply.readException();
                    List<UserManager.EnforcingUser> _result = _reply.createTypedArrayList(UserManager.EnforcingUser.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public Bundle getUserRestrictions(int userHandle) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserRestrictions(userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public boolean hasBaseUserRestriction(String restrictionKey, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasBaseUserRestriction(restrictionKey, userHandle);
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

            @Override // android.os.IUserManager
            public boolean hasUserRestriction(String restrictionKey, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasUserRestriction(restrictionKey, userHandle);
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

            @Override // android.os.IUserManager
            public boolean hasUserRestrictionOnAnyUser(String restrictionKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(restrictionKey);
                    boolean _result = false;
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasUserRestrictionOnAnyUser(restrictionKey);
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

            @Override // android.os.IUserManager
            public void setUserRestriction(String key, boolean value, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(value ? 1 : 0);
                    _data.writeInt(userHandle);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserRestriction(key, value, userHandle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public void setApplicationRestrictions(String packageName, Bundle restrictions, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (restrictions != null) {
                        _data.writeInt(1);
                        restrictions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userHandle);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setApplicationRestrictions(packageName, restrictions, userHandle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public Bundle getApplicationRestrictions(String packageName) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationRestrictions(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public Bundle getApplicationRestrictionsForUser(String packageName, int userHandle) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationRestrictionsForUser(packageName, userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public void setDefaultGuestRestrictions(Bundle restrictions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (restrictions != null) {
                        _data.writeInt(1);
                        restrictions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultGuestRestrictions(restrictions);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public Bundle getDefaultGuestRestrictions() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultGuestRestrictions();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public boolean markGuestForDeletion(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().markGuestForDeletion(userHandle);
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

            @Override // android.os.IUserManager
            public boolean isQuietModeEnabled(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isQuietModeEnabled(userHandle);
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

            @Override // android.os.IUserManager
            public void setSeedAccountData(int userHandle, String accountName, String accountType, PersistableBundle accountOptions, boolean persist) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(accountName);
                    _data.writeString(accountType);
                    int i = 1;
                    if (accountOptions != null) {
                        _data.writeInt(1);
                        accountOptions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!persist) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSeedAccountData(userHandle, accountName, accountType, accountOptions, persist);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public String getSeedAccountName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSeedAccountName();
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

            @Override // android.os.IUserManager
            public String getSeedAccountType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSeedAccountType();
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

            @Override // android.os.IUserManager
            public PersistableBundle getSeedAccountOptions() throws RemoteException {
                PersistableBundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSeedAccountOptions();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PersistableBundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public void clearSeedAccountData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearSeedAccountData();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public boolean someUserHasSeedAccount(String accountName, String accountType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(accountName);
                    _data.writeString(accountType);
                    boolean _result = false;
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().someUserHasSeedAccount(accountName, accountType);
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

            @Override // android.os.IUserManager
            public boolean isManagedProfile(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isManagedProfile(userId);
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

            @Override // android.os.IUserManager
            public boolean isDemoUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDemoUser(userId);
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

            @Override // android.os.IUserManager
            public UserInfo createProfileForUserEvenWhenDisallowed(String name, int flags, int userHandle, String[] disallowedPackages) throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    _data.writeInt(userHandle);
                    _data.writeStringArray(disallowedPackages);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createProfileForUserEvenWhenDisallowed(name, flags, userHandle, disallowedPackages);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IUserManager
            public boolean isUserUnlockingOrUnlocked(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserUnlockingOrUnlocked(userId);
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

            @Override // android.os.IUserManager
            public int getManagedProfileBadge(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getManagedProfileBadge(userId);
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

            @Override // android.os.IUserManager
            public boolean isUserUnlocked(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserUnlocked(userId);
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

            @Override // android.os.IUserManager
            public boolean isUserRunning(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserRunning(userId);
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

            @Override // android.os.IUserManager
            public boolean isUserNameSet(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserNameSet(userHandle);
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

            @Override // android.os.IUserManager
            public boolean hasRestrictedProfiles() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasRestrictedProfiles();
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

            @Override // android.os.IUserManager
            public boolean requestQuietModeEnabled(String callingPackage, boolean enableQuietMode, int userHandle, IntentSender target) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = true;
                    _data.writeInt(enableQuietMode ? 1 : 0);
                    _data.writeInt(userHandle);
                    if (target != null) {
                        _data.writeInt(1);
                        target.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestQuietModeEnabled(callingPackage, enableQuietMode, userHandle, target);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public String getUserName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserName();
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

            @Override // android.os.IUserManager
            public long getUserStartRealtime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserStartRealtime();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IUserManager
            public long getUserUnlockRealtime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserUnlockRealtime();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUserManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUserManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
