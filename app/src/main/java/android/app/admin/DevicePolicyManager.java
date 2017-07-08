package android.app.admin;

import android.Manifest.permission;
import android.app.admin.IDevicePolicyManager.Stub;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.Credentials;
import android.util.Log;
import com.android.org.conscrypt.TrustedCertificateStore;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DevicePolicyManager {
    public static final String ACTION_ADD_DEVICE_ADMIN = "android.app.action.ADD_DEVICE_ADMIN";
    public static final String ACTION_BUGREPORT_SHARING_ACCEPTED = "com.android.server.action.BUGREPORT_SHARING_ACCEPTED";
    public static final String ACTION_BUGREPORT_SHARING_DECLINED = "com.android.server.action.BUGREPORT_SHARING_DECLINED";
    public static final String ACTION_DEVICE_OWNER_CHANGED = "android.app.action.DEVICE_OWNER_CHANGED";
    public static final String ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED = "android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED";
    public static final String ACTION_MANAGED_PROFILE_PROVISIONED = "android.app.action.MANAGED_PROFILE_PROVISIONED";
    public static final String ACTION_PROVISION_FINALIZATION = "android.app.action.PROVISION_FINALIZATION";
    public static final String ACTION_PROVISION_MANAGED_DEVICE = "android.app.action.PROVISION_MANAGED_DEVICE";
    public static final String ACTION_PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE = "android.app.action.PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE";
    public static final String ACTION_PROVISION_MANAGED_PROFILE = "android.app.action.PROVISION_MANAGED_PROFILE";
    public static final String ACTION_PROVISION_MANAGED_SHAREABLE_DEVICE = "android.app.action.PROVISION_MANAGED_SHAREABLE_DEVICE";
    public static final String ACTION_PROVISION_MANAGED_USER = "android.app.action.PROVISION_MANAGED_USER";
    public static final String ACTION_REMOTE_BUGREPORT_DISPATCH = "android.intent.action.REMOTE_BUGREPORT_DISPATCH";
    public static final String ACTION_SET_NEW_PARENT_PROFILE_PASSWORD = "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD";
    public static final String ACTION_SET_NEW_PASSWORD = "android.app.action.SET_NEW_PASSWORD";
    public static final String ACTION_SET_PROFILE_OWNER = "android.app.action.SET_PROFILE_OWNER";
    public static final String ACTION_START_ENCRYPTION = "android.app.action.START_ENCRYPTION";
    public static final String ACTION_SYSTEM_UPDATE_POLICY_CHANGED = "android.app.action.SYSTEM_UPDATE_POLICY_CHANGED";
    public static final int ENCRYPTION_STATUS_ACTIVATING = 2;
    public static final int ENCRYPTION_STATUS_ACTIVE = 3;
    public static final int ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY = 4;
    public static final int ENCRYPTION_STATUS_ACTIVE_PER_USER = 5;
    public static final int ENCRYPTION_STATUS_INACTIVE = 1;
    public static final int ENCRYPTION_STATUS_UNSUPPORTED = 0;
    public static final String EXTRA_ADD_EXPLANATION = "android.app.extra.ADD_EXPLANATION";
    public static final String EXTRA_BUGREPORT_NOTIFICATION_TYPE = "android.app.extra.bugreport_notification_type";
    public static final String EXTRA_DEVICE_ADMIN = "android.app.extra.DEVICE_ADMIN";
    public static final String EXTRA_PROFILE_OWNER_NAME = "android.app.extra.PROFILE_OWNER_NAME";
    public static final String EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE = "android.app.extra.PROVISIONING_ACCOUNT_TO_MIGRATE";
    public static final String EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE = "android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME = "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_MINIMUM_VERSION_CODE = "android.app.extra.PROVISIONING_DEVICE_ADMIN_MINIMUM_VERSION_CODE";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_COOKIE_HEADER = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_COOKIE_HEADER";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION";
    @Deprecated
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME = "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME";
    public static final String EXTRA_PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM = "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM";
    public static final String EXTRA_PROVISIONING_EMAIL_ADDRESS = "android.app.extra.PROVISIONING_EMAIL_ADDRESS";
    public static final String EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED = "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED";
    public static final String EXTRA_PROVISIONING_LOCALE = "android.app.extra.PROVISIONING_LOCALE";
    public static final String EXTRA_PROVISIONING_LOCAL_TIME = "android.app.extra.PROVISIONING_LOCAL_TIME";
    public static final String EXTRA_PROVISIONING_LOGO_URI = "android.app.extra.PROVISIONING_LOGO_URI";
    public static final String EXTRA_PROVISIONING_MAIN_COLOR = "android.app.extra.PROVISIONING_MAIN_COLOR";
    public static final String EXTRA_PROVISIONING_SKIP_ENCRYPTION = "android.app.extra.PROVISIONING_SKIP_ENCRYPTION";
    public static final String EXTRA_PROVISIONING_SKIP_USER_SETUP = "android.app.extra.PROVISIONING_SKIP_USER_SETUP";
    public static final String EXTRA_PROVISIONING_TIME_ZONE = "android.app.extra.PROVISIONING_TIME_ZONE";
    public static final String EXTRA_PROVISIONING_WIFI_HIDDEN = "android.app.extra.PROVISIONING_WIFI_HIDDEN";
    public static final String EXTRA_PROVISIONING_WIFI_PAC_URL = "android.app.extra.PROVISIONING_WIFI_PAC_URL";
    public static final String EXTRA_PROVISIONING_WIFI_PASSWORD = "android.app.extra.PROVISIONING_WIFI_PASSWORD";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_BYPASS = "android.app.extra.PROVISIONING_WIFI_PROXY_BYPASS";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_HOST = "android.app.extra.PROVISIONING_WIFI_PROXY_HOST";
    public static final String EXTRA_PROVISIONING_WIFI_PROXY_PORT = "android.app.extra.PROVISIONING_WIFI_PROXY_PORT";
    public static final String EXTRA_PROVISIONING_WIFI_SECURITY_TYPE = "android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE";
    public static final String EXTRA_PROVISIONING_WIFI_SSID = "android.app.extra.PROVISIONING_WIFI_SSID";
    public static final String EXTRA_REMOTE_BUGREPORT_HASH = "android.intent.extra.REMOTE_BUGREPORT_HASH";
    public static final int FLAG_MANAGED_CAN_ACCESS_PARENT = 2;
    public static final int FLAG_PARENT_CAN_ACCESS_MANAGED = 1;
    public static final int KEYGUARD_DISABLE_FEATURES_ALL = Integer.MAX_VALUE;
    public static final int KEYGUARD_DISABLE_FEATURES_NONE = 0;
    public static final int KEYGUARD_DISABLE_FINGERPRINT = 32;
    public static final int KEYGUARD_DISABLE_REMOTE_INPUT = 64;
    public static final int KEYGUARD_DISABLE_SECURE_CAMERA = 2;
    public static final int KEYGUARD_DISABLE_SECURE_NOTIFICATIONS = 4;
    public static final int KEYGUARD_DISABLE_TRUST_AGENTS = 16;
    public static final int KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS = 8;
    public static final int KEYGUARD_DISABLE_WIDGETS_ALL = 1;
    public static final int MAKE_USER_EPHEMERAL = 2;
    public static final String MIME_TYPE_PROVISIONING_NFC = "application/com.android.managedprovisioning";
    public static final int NOTIFICATION_BUGREPORT_ACCEPTED_NOT_FINISHED = 2;
    public static final int NOTIFICATION_BUGREPORT_FINISHED_NOT_ACCEPTED = 3;
    public static final int NOTIFICATION_BUGREPORT_STARTED = 1;
    public static final int PASSWORD_QUALITY_ALPHABETIC = 262144;
    public static final int PASSWORD_QUALITY_ALPHANUMERIC = 327680;
    public static final int PASSWORD_QUALITY_BIOMETRIC_WEAK = 32768;
    public static final int PASSWORD_QUALITY_COMPLEX = 393216;
    public static final int PASSWORD_QUALITY_MANAGED = 524288;
    public static final int PASSWORD_QUALITY_NUMERIC = 131072;
    public static final int PASSWORD_QUALITY_NUMERIC_COMPLEX = 196608;
    public static final int PASSWORD_QUALITY_SOMETHING = 65536;
    public static final int PASSWORD_QUALITY_UNSPECIFIED = 0;
    public static final int PERMISSION_GRANT_STATE_DEFAULT = 0;
    public static final int PERMISSION_GRANT_STATE_DENIED = 2;
    public static final int PERMISSION_GRANT_STATE_GRANTED = 1;
    public static final int PERMISSION_POLICY_AUTO_DENY = 2;
    public static final int PERMISSION_POLICY_AUTO_GRANT = 1;
    public static final int PERMISSION_POLICY_PROMPT = 0;
    public static final int RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT = 2;
    public static final int RESET_PASSWORD_REQUIRE_ENTRY = 1;
    public static final int SKIP_SETUP_WIZARD = 1;
    public static final int STATE_USER_PROFILE_COMPLETE = 4;
    public static final int STATE_USER_SETUP_COMPLETE = 2;
    public static final int STATE_USER_SETUP_FINALIZED = 3;
    public static final int STATE_USER_SETUP_INCOMPLETE = 1;
    public static final int STATE_USER_UNMANAGED = 0;
    private static String TAG = null;
    public static final int WIPE_EXTERNAL_STORAGE = 1;
    public static final int WIPE_RESET_PROTECTION_DATA = 2;
    private final Context mContext;
    private final boolean mParentInstance;
    private final IDevicePolicyManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.admin.DevicePolicyManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.admin.DevicePolicyManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.admin.DevicePolicyManager.<clinit>():void");
    }

    private DevicePolicyManager(Context context, boolean parentInstance) {
        this(context, Stub.asInterface(ServiceManager.getService(Context.DEVICE_POLICY_SERVICE)), parentInstance);
    }

    protected DevicePolicyManager(Context context, IDevicePolicyManager service, boolean parentInstance) {
        this.mContext = context;
        this.mService = service;
        this.mParentInstance = parentInstance;
    }

    public static DevicePolicyManager create(Context context) {
        DevicePolicyManager me = new DevicePolicyManager(context, false);
        return me.mService != null ? me : null;
    }

    protected int myUserId() {
        return UserHandle.myUserId();
    }

    public boolean isAdminActive(ComponentName admin) {
        return isAdminActiveAsUser(admin, myUserId());
    }

    public boolean isAdminActiveAsUser(ComponentName admin, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isAdminActive(admin, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isRemovingAdmin(ComponentName admin, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isRemovingAdmin(admin, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ComponentName> getActiveAdmins() {
        throwIfParentInstance("getActiveAdmins");
        return getActiveAdminsAsUser(myUserId());
    }

    public List<ComponentName> getActiveAdminsAsUser(int userId) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getActiveAdmins(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean packageHasActiveAdmins(String packageName) {
        return packageHasActiveAdmins(packageName, myUserId());
    }

    public boolean packageHasActiveAdmins(String packageName, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.packageHasActiveAdmins(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeActiveAdmin(ComponentName admin) {
        throwIfParentInstance("removeActiveAdmin");
        if (this.mService != null) {
            try {
                this.mService.removeActiveAdmin(admin, myUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasGrantedPolicy(ComponentName admin, int usesPolicy) {
        throwIfParentInstance("hasGrantedPolicy");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasGrantedPolicy(admin, usesPolicy, myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isSeparateProfileChallengeAllowed(userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordQuality(ComponentName admin, int quality) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordQuality(admin, quality, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordQuality(ComponentName admin) {
        return getPasswordQuality(admin, myUserId());
    }

    public int getPasswordQuality(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordQuality(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLength(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumLength(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLength(ComponentName admin) {
        return getPasswordMinimumLength(admin, myUserId());
    }

    public int getPasswordMinimumLength(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordMinimumLength(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumUpperCase(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumUpperCase(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumUpperCase(ComponentName admin) {
        return getPasswordMinimumUpperCase(admin, myUserId());
    }

    public int getPasswordMinimumUpperCase(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordMinimumUpperCase(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLowerCase(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumLowerCase(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLowerCase(ComponentName admin) {
        return getPasswordMinimumLowerCase(admin, myUserId());
    }

    public int getPasswordMinimumLowerCase(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordMinimumLowerCase(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumLetters(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumLetters(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumLetters(ComponentName admin) {
        return getPasswordMinimumLetters(admin, myUserId());
    }

    public int getPasswordMinimumLetters(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordMinimumLetters(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumNumeric(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumNumeric(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumNumeric(ComponentName admin) {
        return getPasswordMinimumNumeric(admin, myUserId());
    }

    public int getPasswordMinimumNumeric(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordMinimumNumeric(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumSymbols(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumSymbols(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumSymbols(ComponentName admin) {
        return getPasswordMinimumSymbols(admin, myUserId());
    }

    public int getPasswordMinimumSymbols(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordMinimumSymbols(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordMinimumNonLetter(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordMinimumNonLetter(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getPasswordMinimumNonLetter(ComponentName admin) {
        return getPasswordMinimumNonLetter(admin, myUserId());
    }

    public int getPasswordMinimumNonLetter(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordMinimumNonLetter(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPasswordHistoryLength(ComponentName admin, int length) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordHistoryLength(admin, length, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setPasswordExpirationTimeout(ComponentName admin, long timeout) {
        if (this.mService != null) {
            try {
                this.mService.setPasswordExpirationTimeout(admin, timeout, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getPasswordExpirationTimeout(ComponentName admin) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordExpirationTimeout(admin, myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getPasswordExpiration(ComponentName admin) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPasswordExpiration(admin, myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPasswordHistoryLength(ComponentName admin) {
        return getPasswordHistoryLength(admin, myUserId());
    }

    public int getPasswordHistoryLength(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getPasswordHistoryLength(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPasswordMaximumLength(int quality) {
        return KEYGUARD_DISABLE_TRUST_AGENTS;
    }

    public boolean isActivePasswordSufficient() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isActivePasswordSufficient(myUserId(), this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isProfileActivePasswordSufficientForParent(int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isProfileActivePasswordSufficientForParent(userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getCurrentFailedPasswordAttempts() {
        return getCurrentFailedPasswordAttempts(myUserId());
    }

    public int getCurrentFailedPasswordAttempts(int userHandle) {
        if (this.mService == null) {
            return -1;
        }
        try {
            return this.mService.getCurrentFailedPasswordAttempts(userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getDoNotAskCredentialsOnBoot() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getDoNotAskCredentialsOnBoot();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMaximumFailedPasswordsForWipe(ComponentName admin, int num) {
        if (this.mService != null) {
            try {
                this.mService.setMaximumFailedPasswordsForWipe(admin, num, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName admin) {
        return getMaximumFailedPasswordsForWipe(admin, myUserId());
    }

    public int getMaximumFailedPasswordsForWipe(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getMaximumFailedPasswordsForWipe(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getProfileWithMinimumFailedPasswordsForWipe(int userHandle) {
        if (this.mService == null) {
            return UserHandle.USER_NULL;
        }
        try {
            return this.mService.getProfileWithMinimumFailedPasswordsForWipe(userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean resetPassword(String password, int flags) {
        throwIfParentInstance("resetPassword");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.resetPassword(password, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMaximumTimeToLock(ComponentName admin, long timeMs) {
        if (this.mService != null) {
            try {
                this.mService.setMaximumTimeToLock(admin, timeMs, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public long getMaximumTimeToLock(ComponentName admin) {
        return getMaximumTimeToLock(admin, myUserId());
    }

    public long getMaximumTimeToLock(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getMaximumTimeToLock(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getMaximumTimeToLockForUserAndProfiles(int userHandle) {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getMaximumTimeToLockForUserAndProfiles(userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockNow() {
        if (this.mService != null) {
            try {
                this.mService.lockNow(this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void wipeData(int flags) {
        throwIfParentInstance("wipeData");
        if (this.mService != null) {
            try {
                this.mService.wipeData(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public ComponentName setGlobalProxy(ComponentName admin, Proxy proxySpec, List<String> exclusionList) {
        throwIfParentInstance("setGlobalProxy");
        if (proxySpec == null) {
            throw new NullPointerException();
        } else if (this.mService == null) {
            return null;
        } else {
            try {
                String hostSpec;
                String str;
                if (proxySpec.equals(Proxy.NO_PROXY)) {
                    hostSpec = null;
                    str = null;
                } else if (proxySpec.type().equals(Type.HTTP)) {
                    InetSocketAddress sa = (InetSocketAddress) proxySpec.address();
                    String hostName = sa.getHostName();
                    int port = sa.getPort();
                    hostSpec = hostName + ":" + Integer.toString(port);
                    if (exclusionList == null) {
                        str = ProxyInfo.LOCAL_EXCL_LIST;
                    } else {
                        StringBuilder listBuilder = new StringBuilder();
                        boolean firstDomain = true;
                        for (String exclDomain : exclusionList) {
                            if (firstDomain) {
                                firstDomain = false;
                            } else {
                                listBuilder = listBuilder.append(",");
                            }
                            listBuilder = listBuilder.append(exclDomain.trim());
                        }
                        str = listBuilder.toString();
                    }
                    if (android.net.Proxy.validate(hostName, Integer.toString(port), str) != 0) {
                        throw new IllegalArgumentException();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
                return this.mService.setGlobalProxy(admin, hostSpec, str);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRecommendedGlobalProxy(ComponentName admin, ProxyInfo proxyInfo) {
        throwIfParentInstance("setRecommendedGlobalProxy");
        if (this.mService != null) {
            try {
                this.mService.setRecommendedGlobalProxy(admin, proxyInfo);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public ComponentName getGlobalProxyAdmin() {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getGlobalProxyAdmin(myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int setStorageEncryption(ComponentName admin, boolean encrypt) {
        throwIfParentInstance("setStorageEncryption");
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.setStorageEncryption(admin, encrypt);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getStorageEncryption(ComponentName admin) {
        throwIfParentInstance("getStorageEncryption");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getStorageEncryption(admin, myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStorageEncryptionStatus() {
        throwIfParentInstance("getStorageEncryptionStatus");
        return getStorageEncryptionStatus(myUserId());
    }

    public int getStorageEncryptionStatus(int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getStorageEncryptionStatus(this.mContext.getPackageName(), userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean approveCaCert(String alias, int userHandle, boolean approval) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.approveCaCert(alias, userHandle, approval);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isCaCertApproved(String alias, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isCaCertApproved(alias, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean installCaCert(ComponentName admin, byte[] certBuffer) {
        throwIfParentInstance("installCaCert");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.installCaCert(admin, certBuffer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void uninstallCaCert(ComponentName admin, byte[] certBuffer) {
        throwIfParentInstance("uninstallCaCert");
        if (this.mService != null) {
            try {
                String alias = getCaCertAlias(certBuffer);
                IDevicePolicyManager iDevicePolicyManager = this.mService;
                String[] strArr = new String[WIPE_EXTERNAL_STORAGE];
                strArr[STATE_USER_UNMANAGED] = alias;
                iDevicePolicyManager.uninstallCaCerts(admin, strArr);
            } catch (CertificateException e) {
                Log.w(TAG, "Unable to parse certificate", e);
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }
    }

    public List<byte[]> getInstalledCaCerts(ComponentName admin) {
        List<byte[]> certs = new ArrayList();
        throwIfParentInstance("getInstalledCaCerts");
        if (this.mService != null) {
            try {
                this.mService.enforceCanManageCaCerts(admin);
                TrustedCertificateStore certStore = new TrustedCertificateStore();
                for (String alias : certStore.userAliases()) {
                    try {
                        certs.add(certStore.getCertificate(alias).getEncoded());
                    } catch (CertificateException ce) {
                        Log.w(TAG, "Could not encode certificate: " + alias, ce);
                    }
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        return certs;
    }

    public void uninstallAllUserCaCerts(ComponentName admin) {
        throwIfParentInstance("uninstallAllUserCaCerts");
        if (this.mService != null) {
            try {
                this.mService.uninstallCaCerts(admin, (String[]) new TrustedCertificateStore().userAliases().toArray(new String[STATE_USER_UNMANAGED]));
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasCaCertInstalled(ComponentName admin, byte[] certBuffer) {
        boolean z = false;
        throwIfParentInstance("hasCaCertInstalled");
        if (this.mService != null) {
            try {
                this.mService.enforceCanManageCaCerts(admin);
                if (getCaCertAlias(certBuffer) != null) {
                    z = true;
                }
                return z;
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            } catch (CertificateException ce) {
                Log.w(TAG, "Could not parse certificate", ce);
            }
        }
        return false;
    }

    public boolean installKeyPair(ComponentName admin, PrivateKey privKey, Certificate cert, String alias) {
        Certificate[] certificateArr = new Certificate[WIPE_EXTERNAL_STORAGE];
        certificateArr[STATE_USER_UNMANAGED] = cert;
        return installKeyPair(admin, privKey, certificateArr, alias, false);
    }

    public boolean installKeyPair(ComponentName admin, PrivateKey privKey, Certificate[] certs, String alias, boolean requestAccess) {
        throwIfParentInstance("installKeyPair");
        try {
            Certificate[] certificateArr = new Certificate[WIPE_EXTERNAL_STORAGE];
            certificateArr[STATE_USER_UNMANAGED] = certs[STATE_USER_UNMANAGED];
            byte[] pemCert = Credentials.convertToPem(certificateArr);
            byte[] pemChain = null;
            if (certs.length > WIPE_EXTERNAL_STORAGE) {
                pemChain = Credentials.convertToPem((Certificate[]) Arrays.copyOfRange(certs, WIPE_EXTERNAL_STORAGE, certs.length));
            }
            return this.mService.installKeyPair(admin, ((PKCS8EncodedKeySpec) KeyFactory.getInstance(privKey.getAlgorithm()).getKeySpec(privKey, PKCS8EncodedKeySpec.class)).getEncoded(), pemCert, pemChain, alias, requestAccess);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (GeneralSecurityException e2) {
            Log.w(TAG, "Failed to obtain private key material", e2);
            return false;
        } catch (Exception e3) {
            Log.w(TAG, "Could not pem-encode certificate", e3);
            return false;
        }
    }

    public boolean removeKeyPair(ComponentName admin, String alias) {
        throwIfParentInstance("removeKeyPair");
        try {
            return this.mService.removeKeyPair(admin, alias);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static String getCaCertAlias(byte[] certBuffer) throws CertificateException {
        return new TrustedCertificateStore().getCertificateAlias((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBuffer)));
    }

    public void setCertInstallerPackage(ComponentName admin, String installerPackage) throws SecurityException {
        throwIfParentInstance("setCertInstallerPackage");
        if (this.mService != null) {
            try {
                this.mService.setCertInstallerPackage(admin, installerPackage);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String getCertInstallerPackage(ComponentName admin) throws SecurityException {
        throwIfParentInstance("getCertInstallerPackage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getCertInstallerPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage) throws NameNotFoundException, UnsupportedOperationException {
        setAlwaysOnVpnPackage(admin, vpnPackage, true);
    }

    public void setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdownEnabled) throws NameNotFoundException, UnsupportedOperationException {
        throwIfParentInstance("setAlwaysOnVpnPackage");
        if (this.mService != null) {
            try {
                if (!this.mService.setAlwaysOnVpnPackage(admin, vpnPackage, lockdownEnabled)) {
                    throw new NameNotFoundException(vpnPackage);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String getAlwaysOnVpnPackage(ComponentName admin) {
        throwIfParentInstance("getAlwaysOnVpnPackage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getAlwaysOnVpnPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCameraDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCameraDisabled");
        if (this.mService != null) {
            try {
                this.mService.setCameraDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCameraDisabled(ComponentName admin) {
        throwIfParentInstance("getCameraDisabled");
        return getCameraDisabled(admin, myUserId());
    }

    public boolean getCameraDisabled(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCameraDisabled(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean requestBugreport(ComponentName admin) {
        throwIfParentInstance("requestBugreport");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.requestBugreport(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getGuestUserDisabled(ComponentName admin) {
        return false;
    }

    public void setScreenCaptureDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setScreenCaptureDisabled");
        if (this.mService != null) {
            try {
                this.mService.setScreenCaptureDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getScreenCaptureDisabled(ComponentName admin) {
        throwIfParentInstance("getScreenCaptureDisabled");
        return getScreenCaptureDisabled(admin, myUserId());
    }

    public boolean getScreenCaptureDisabled(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getScreenCaptureDisabled(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAutoTimeRequired(ComponentName admin, boolean required) {
        throwIfParentInstance("setAutoTimeRequired");
        if (this.mService != null) {
            try {
                this.mService.setAutoTimeRequired(admin, required);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getAutoTimeRequired() {
        throwIfParentInstance("getAutoTimeRequired");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getAutoTimeRequired();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setForceEphemeralUsers(ComponentName admin, boolean forceEphemeralUsers) {
        throwIfParentInstance("setForceEphemeralUsers");
        if (this.mService != null) {
            try {
                this.mService.setForceEphemeralUsers(admin, forceEphemeralUsers);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getForceEphemeralUsers(ComponentName admin) {
        throwIfParentInstance("getForceEphemeralUsers");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getForceEphemeralUsers(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setKeyguardDisabledFeatures(ComponentName admin, int which) {
        if (this.mService != null) {
            try {
                this.mService.setKeyguardDisabledFeatures(admin, which, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getKeyguardDisabledFeatures(ComponentName admin) {
        return getKeyguardDisabledFeatures(admin, myUserId());
    }

    public int getKeyguardDisabledFeatures(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getKeyguardDisabledFeatures(admin, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setActiveAdmin(ComponentName policyReceiver, boolean refreshing, int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.setActiveAdmin(policyReceiver, refreshing, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setActiveAdmin(ComponentName policyReceiver, boolean refreshing) {
        setActiveAdmin(policyReceiver, refreshing, myUserId());
    }

    public void getRemoveWarning(ComponentName admin, RemoteCallback result) {
        if (this.mService != null) {
            try {
                this.mService.getRemoveWarning(admin, result, myUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setActivePasswordState(int quality, int length, int letters, int uppercase, int lowercase, int numbers, int symbols, int nonletter, int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.setActivePasswordState(quality, length, letters, uppercase, lowercase, numbers, symbols, nonletter, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportPasswordChanged(int userId) {
        if (this.mService != null) {
            try {
                this.mService.reportPasswordChanged(userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportFailedPasswordAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportFailedPasswordAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportSuccessfulPasswordAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportSuccessfulPasswordAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportFailedFingerprintAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportFailedFingerprintAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportSuccessfulFingerprintAttempt(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportSuccessfulFingerprintAttempt(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportKeyguardDismissed(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportKeyguardDismissed(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportKeyguardSecured(int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.reportKeyguardSecured(userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean setDeviceOwner(ComponentName who) {
        return setDeviceOwner(who, null);
    }

    public boolean setDeviceOwner(ComponentName who, int userId) {
        return setDeviceOwner(who, null, userId);
    }

    public boolean setDeviceOwner(ComponentName who, String ownerName) {
        return setDeviceOwner(who, ownerName, STATE_USER_UNMANAGED);
    }

    public boolean setDeviceOwner(ComponentName who, String ownerName, int userId) throws IllegalArgumentException, IllegalStateException {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setDeviceOwner(who, ownerName, userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isDeviceOwnerApp(String packageName) {
        throwIfParentInstance("isDeviceOwnerApp");
        return isDeviceOwnerAppOnCallingUser(packageName);
    }

    public boolean isDeviceOwnerAppOnCallingUser(String packageName) {
        return isDeviceOwnerAppOnAnyUserInner(packageName, true);
    }

    public boolean isDeviceOwnerAppOnAnyUser(String packageName) {
        return isDeviceOwnerAppOnAnyUserInner(packageName, false);
    }

    public ComponentName getDeviceOwnerComponentOnCallingUser() {
        return getDeviceOwnerComponentInner(true);
    }

    public ComponentName getDeviceOwnerComponentOnAnyUser() {
        return getDeviceOwnerComponentInner(false);
    }

    private boolean isDeviceOwnerAppOnAnyUserInner(String packageName, boolean callingUserOnly) {
        if (packageName == null) {
            return false;
        }
        ComponentName deviceOwner = getDeviceOwnerComponentInner(callingUserOnly);
        if (deviceOwner == null) {
            return false;
        }
        return packageName.equals(deviceOwner.getPackageName());
    }

    private ComponentName getDeviceOwnerComponentInner(boolean callingUserOnly) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDeviceOwnerComponent(callingUserOnly);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getDeviceOwnerUserId() {
        if (this.mService == null) {
            return UserHandle.USER_NULL;
        }
        try {
            return this.mService.getDeviceOwnerUserId();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void clearDeviceOwnerApp(String packageName) {
        throwIfParentInstance("clearDeviceOwnerApp");
        if (this.mService != null) {
            try {
                this.mService.clearDeviceOwner(packageName);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public String getDeviceOwner() {
        throwIfParentInstance("getDeviceOwner");
        ComponentName name = getDeviceOwnerComponentOnCallingUser();
        if (name != null) {
            return name.getPackageName();
        }
        return null;
    }

    public boolean isDeviceManaged() {
        return getDeviceOwnerComponentOnAnyUser() != null;
    }

    public String getDeviceOwnerNameOnAnyUser() {
        throwIfParentInstance("getDeviceOwnerNameOnAnyUser");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDeviceOwnerName();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public String getDeviceInitializerApp() {
        return null;
    }

    @Deprecated
    public ComponentName getDeviceInitializerComponent() {
        return null;
    }

    public boolean setActiveProfileOwner(ComponentName admin, @Deprecated String ownerName) throws IllegalArgumentException {
        throwIfParentInstance("setActiveProfileOwner");
        if (this.mService == null) {
            return false;
        }
        try {
            int myUserId = myUserId();
            this.mService.setActiveAdmin(admin, false, myUserId);
            return this.mService.setProfileOwner(admin, ownerName, myUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void clearProfileOwner(ComponentName admin) {
        throwIfParentInstance("clearProfileOwner");
        if (this.mService != null) {
            try {
                this.mService.clearProfileOwner(admin);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean hasUserSetupCompleted() {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.hasUserSetupCompleted();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setProfileOwner(ComponentName admin, @Deprecated String ownerName, int userHandle) throws IllegalArgumentException {
        if (this.mService == null) {
            return false;
        }
        if (ownerName == null) {
            try {
                ownerName = ProxyInfo.LOCAL_EXCL_LIST;
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        return this.mService.setProfileOwner(admin, ownerName, userHandle);
    }

    public void setDeviceOwnerLockScreenInfo(ComponentName admin, CharSequence info) {
        throwIfParentInstance("setDeviceOwnerLockScreenInfo");
        if (this.mService != null) {
            try {
                this.mService.setDeviceOwnerLockScreenInfo(admin, info);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getDeviceOwnerLockScreenInfo() {
        throwIfParentInstance("getDeviceOwnerLockScreenInfo");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getDeviceOwnerLockScreenInfo();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String[] setPackagesSuspended(ComponentName admin, String[] packageNames, boolean suspended) {
        throwIfParentInstance("setPackagesSuspended");
        if (this.mService == null) {
            return packageNames;
        }
        try {
            return this.mService.setPackagesSuspended(admin, packageNames, suspended);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isPackageSuspended(ComponentName admin, String packageName) throws NameNotFoundException {
        throwIfParentInstance("isPackageSuspended");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isPackageSuspended(admin, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (IllegalArgumentException e2) {
            throw new NameNotFoundException(packageName);
        }
    }

    public void setProfileEnabled(ComponentName admin) {
        throwIfParentInstance("setProfileEnabled");
        if (this.mService != null) {
            try {
                this.mService.setProfileEnabled(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setProfileName(ComponentName admin, String profileName) {
        throwIfParentInstance("setProfileName");
        if (this.mService != null) {
            try {
                this.mService.setProfileName(admin, profileName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isProfileOwnerApp(String packageName) {
        boolean z = false;
        throwIfParentInstance("isProfileOwnerApp");
        if (this.mService == null) {
            return false;
        }
        try {
            ComponentName profileOwner = this.mService.getProfileOwner(myUserId());
            if (profileOwner != null) {
                z = profileOwner.getPackageName().equals(packageName);
            }
            return z;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public ComponentName getProfileOwner() throws IllegalArgumentException {
        throwIfParentInstance("getProfileOwner");
        return getProfileOwnerAsUser(Process.myUserHandle().getIdentifier());
    }

    public ComponentName getProfileOwnerAsUser(int userId) throws IllegalArgumentException {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getProfileOwner(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getProfileOwnerName() throws IllegalArgumentException {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getProfileOwnerName(Process.myUserHandle().getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getProfileOwnerNameAsUser(int userId) throws IllegalArgumentException {
        throwIfParentInstance("getProfileOwnerNameAsUser");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getProfileOwnerName(userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void addPersistentPreferredActivity(ComponentName admin, IntentFilter filter, ComponentName activity) {
        throwIfParentInstance("addPersistentPreferredActivity");
        if (this.mService != null) {
            try {
                this.mService.addPersistentPreferredActivity(admin, filter, activity);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearPackagePersistentPreferredActivities(ComponentName admin, String packageName) {
        throwIfParentInstance("clearPackagePersistentPreferredActivities");
        if (this.mService != null) {
            try {
                this.mService.clearPackagePersistentPreferredActivities(admin, packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName) throws NameNotFoundException {
        throwIfParentInstance("setApplicationRestrictionsManagingPackage");
        if (this.mService != null) {
            try {
                if (!this.mService.setApplicationRestrictionsManagingPackage(admin, packageName)) {
                    throw new NameNotFoundException(packageName);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String getApplicationRestrictionsManagingPackage(ComponentName admin) {
        throwIfParentInstance("getApplicationRestrictionsManagingPackage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getApplicationRestrictionsManagingPackage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isCallerApplicationRestrictionsManagingPackage() {
        throwIfParentInstance("isCallerApplicationRestrictionsManagingPackage");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isCallerApplicationRestrictionsManagingPackage();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setApplicationRestrictions(ComponentName admin, String packageName, Bundle settings) {
        throwIfParentInstance("setApplicationRestrictions");
        if (this.mService != null) {
            try {
                this.mService.setApplicationRestrictions(admin, packageName, settings);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setTrustAgentConfiguration(ComponentName admin, ComponentName target, PersistableBundle configuration) {
        if (this.mService != null) {
            try {
                this.mService.setTrustAgentConfiguration(admin, target, configuration, this.mParentInstance);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent) {
        return getTrustAgentConfiguration(admin, agent, myUserId());
    }

    public List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin, ComponentName agent, int userHandle) {
        if (this.mService == null) {
            return new ArrayList();
        }
        try {
            return this.mService.getTrustAgentConfiguration(admin, agent, userHandle, this.mParentInstance);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCrossProfileCallerIdDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCrossProfileCallerIdDisabled");
        if (this.mService != null) {
            try {
                this.mService.setCrossProfileCallerIdDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCrossProfileCallerIdDisabled(ComponentName admin) {
        throwIfParentInstance("getCrossProfileCallerIdDisabled");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileCallerIdDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getCrossProfileCallerIdDisabled(UserHandle userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileCallerIdDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCrossProfileContactsSearchDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setCrossProfileContactsSearchDisabled");
        if (this.mService != null) {
            try {
                this.mService.setCrossProfileContactsSearchDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(ComponentName admin) {
        throwIfParentInstance("getCrossProfileContactsSearchDisabled");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileContactsSearchDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getCrossProfileContactsSearchDisabled(UserHandle userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.getCrossProfileContactsSearchDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startManagedQuickContact(String actualLookupKey, long actualContactId, boolean isContactIdIgnored, long directoryId, Intent originalIntent) {
        if (this.mService != null) {
            try {
                this.mService.startManagedQuickContact(actualLookupKey, actualContactId, isContactIdIgnored, directoryId, originalIntent);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void startManagedQuickContact(String actualLookupKey, long actualContactId, Intent originalIntent) {
        startManagedQuickContact(actualLookupKey, actualContactId, false, 0, originalIntent);
    }

    public void setBluetoothContactSharingDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setBluetoothContactSharingDisabled");
        if (this.mService != null) {
            try {
                this.mService.setBluetoothContactSharingDisabled(admin, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean getBluetoothContactSharingDisabled(ComponentName admin) {
        throwIfParentInstance("getBluetoothContactSharingDisabled");
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.getBluetoothContactSharingDisabled(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getBluetoothContactSharingDisabled(UserHandle userHandle) {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.getBluetoothContactSharingDisabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addCrossProfileIntentFilter(ComponentName admin, IntentFilter filter, int flags) {
        throwIfParentInstance("addCrossProfileIntentFilter");
        if (this.mService != null) {
            try {
                this.mService.addCrossProfileIntentFilter(admin, filter, flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearCrossProfileIntentFilters(ComponentName admin) {
        throwIfParentInstance("clearCrossProfileIntentFilters");
        if (this.mService != null) {
            try {
                this.mService.clearCrossProfileIntentFilters(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean setPermittedAccessibilityServices(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setPermittedAccessibilityServices");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setPermittedAccessibilityServices(admin, packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedAccessibilityServices(ComponentName admin) {
        throwIfParentInstance("getPermittedAccessibilityServices");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedAccessibilityServices(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isAccessibilityServicePermittedByAdmin(ComponentName admin, String packageName, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isAccessibilityServicePermittedByAdmin(admin, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedAccessibilityServices(int userId) {
        throwIfParentInstance("getPermittedAccessibilityServices");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedAccessibilityServicesForUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setPermittedInputMethods(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setPermittedInputMethods");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setPermittedInputMethods(admin, packageNames);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedInputMethods(ComponentName admin) {
        throwIfParentInstance("getPermittedInputMethods");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedInputMethods(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInputMethodPermittedByAdmin(ComponentName admin, String packageName, int userHandle) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isInputMethodPermittedByAdmin(admin, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getPermittedInputMethodsForCurrentUser() {
        throwIfParentInstance("getPermittedInputMethodsForCurrentUser");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getPermittedInputMethodsForCurrentUser();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getKeepUninstalledPackages(ComponentName admin) {
        throwIfParentInstance("getKeepUninstalledPackages");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getKeepUninstalledPackages(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setKeepUninstalledPackages(ComponentName admin, List<String> packageNames) {
        throwIfParentInstance("setKeepUninstalledPackages");
        if (this.mService != null) {
            try {
                this.mService.setKeepUninstalledPackages(admin, packageNames);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public UserHandle createUser(ComponentName admin, String name) {
        return null;
    }

    @Deprecated
    public UserHandle createAndInitializeUser(ComponentName admin, String name, String ownerName, ComponentName profileOwnerComponent, Bundle adminExtras) {
        return null;
    }

    public UserHandle createAndManageUser(ComponentName admin, String name, ComponentName profileOwner, PersistableBundle adminExtras, int flags) {
        throwIfParentInstance("createAndManageUser");
        try {
            return this.mService.createAndManageUser(admin, name, profileOwner, adminExtras, flags);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean removeUser(ComponentName admin, UserHandle userHandle) {
        throwIfParentInstance("removeUser");
        try {
            return this.mService.removeUser(admin, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean switchUser(ComponentName admin, UserHandle userHandle) {
        throwIfParentInstance("switchUser");
        try {
            return this.mService.switchUser(admin, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bundle getApplicationRestrictions(ComponentName admin, String packageName) {
        throwIfParentInstance("getApplicationRestrictions");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getApplicationRestrictions(admin, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addUserRestriction(ComponentName admin, String key) {
        throwIfParentInstance("addUserRestriction");
        if (this.mService != null) {
            try {
                this.mService.setUserRestriction(admin, key, true);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void clearUserRestriction(ComponentName admin, String key) {
        throwIfParentInstance("clearUserRestriction");
        if (this.mService != null) {
            try {
                this.mService.setUserRestriction(admin, key, false);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public Bundle getUserRestrictions(ComponentName admin) {
        throwIfParentInstance("getUserRestrictions");
        Bundle ret = null;
        if (this.mService != null) {
            try {
                ret = this.mService.getUserRestrictions(admin);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return ret == null ? new Bundle() : ret;
    }

    public boolean setApplicationHidden(ComponentName admin, String packageName, boolean hidden) {
        throwIfParentInstance("setApplicationHidden");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.setApplicationHidden(admin, packageName, hidden);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isApplicationHidden(ComponentName admin, String packageName) {
        throwIfParentInstance("isApplicationHidden");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isApplicationHidden(admin, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void enableSystemApp(ComponentName admin, String packageName) {
        throwIfParentInstance("enableSystemApp");
        if (this.mService != null) {
            try {
                this.mService.enableSystemApp(admin, packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int enableSystemApp(ComponentName admin, Intent intent) {
        throwIfParentInstance("enableSystemApp");
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.enableSystemAppWithIntent(admin, intent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAccountManagementDisabled(ComponentName admin, String accountType, boolean disabled) {
        throwIfParentInstance("setAccountManagementDisabled");
        if (this.mService != null) {
            try {
                this.mService.setAccountManagementDisabled(admin, accountType, disabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String[] getAccountTypesWithManagementDisabled() {
        throwIfParentInstance("getAccountTypesWithManagementDisabled");
        return getAccountTypesWithManagementDisabledAsUser(myUserId());
    }

    public String[] getAccountTypesWithManagementDisabledAsUser(int userId) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getAccountTypesWithManagementDisabledAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLockTaskPackages(ComponentName admin, String[] packages) throws SecurityException {
        throwIfParentInstance("setLockTaskPackages");
        if (this.mService != null) {
            try {
                this.mService.setLockTaskPackages(admin, packages);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String[] getLockTaskPackages(ComponentName admin) {
        throwIfParentInstance("getLockTaskPackages");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getLockTaskPackages(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isLockTaskPermitted(String pkg) {
        throwIfParentInstance("isLockTaskPermitted");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isLockTaskPermitted(pkg);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setGlobalSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setGlobalSetting");
        if (this.mService != null) {
            try {
                this.mService.setGlobalSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setSecureSetting(ComponentName admin, String setting, String value) {
        throwIfParentInstance("setSecureSetting");
        if (this.mService != null) {
            try {
                this.mService.setSecureSetting(admin, setting, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRestrictionsProvider(ComponentName admin, ComponentName provider) {
        throwIfParentInstance("setRestrictionsProvider");
        if (this.mService != null) {
            try {
                this.mService.setRestrictionsProvider(admin, provider);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public void setMasterVolumeMuted(ComponentName admin, boolean on) {
        throwIfParentInstance("setMasterVolumeMuted");
        if (this.mService != null) {
            try {
                this.mService.setMasterVolumeMuted(admin, on);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean isMasterVolumeMuted(ComponentName admin) {
        throwIfParentInstance("isMasterVolumeMuted");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isMasterVolumeMuted(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setUninstallBlocked(ComponentName admin, String packageName, boolean uninstallBlocked) {
        throwIfParentInstance("setUninstallBlocked");
        if (this.mService != null) {
            try {
                this.mService.setUninstallBlocked(admin, packageName, uninstallBlocked);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public boolean isUninstallBlocked(ComponentName admin, String packageName) {
        throwIfParentInstance("isUninstallBlocked");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isUninstallBlocked(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean addCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        throwIfParentInstance("addCrossProfileWidgetProvider");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.addCrossProfileWidgetProvider(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName) {
        throwIfParentInstance("removeCrossProfileWidgetProvider");
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.removeCrossProfileWidgetProvider(admin, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<String> getCrossProfileWidgetProviders(ComponentName admin) {
        throwIfParentInstance("getCrossProfileWidgetProviders");
        if (this.mService != null) {
            try {
                List<String> providers = this.mService.getCrossProfileWidgetProviders(admin);
                if (providers != null) {
                    return providers;
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        return Collections.emptyList();
    }

    public void setUserIcon(ComponentName admin, Bitmap icon) {
        throwIfParentInstance("setUserIcon");
        try {
            this.mService.setUserIcon(admin, icon);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setSystemUpdatePolicy(ComponentName admin, SystemUpdatePolicy policy) {
        throwIfParentInstance("setSystemUpdatePolicy");
        if (this.mService != null) {
            try {
                this.mService.setSystemUpdatePolicy(admin, policy);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public SystemUpdatePolicy getSystemUpdatePolicy() {
        throwIfParentInstance("getSystemUpdatePolicy");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getSystemUpdatePolicy();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setKeyguardDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setKeyguardDisabled");
        try {
            return this.mService.setKeyguardDisabled(admin, disabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setStatusBarDisabled(ComponentName admin, boolean disabled) {
        throwIfParentInstance("setStatusBarDisabled");
        try {
            return this.mService.setStatusBarDisabled(admin, disabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void notifyPendingSystemUpdate(long updateReceivedTime) {
        throwIfParentInstance("notifyPendingSystemUpdate");
        if (this.mService != null) {
            try {
                this.mService.notifyPendingSystemUpdate(updateReceivedTime);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public void setPermissionPolicy(ComponentName admin, int policy) {
        throwIfParentInstance("setPermissionPolicy");
        try {
            this.mService.setPermissionPolicy(admin, policy);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getPermissionPolicy(ComponentName admin) {
        throwIfParentInstance("getPermissionPolicy");
        try {
            return this.mService.getPermissionPolicy(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setPermissionGrantState(ComponentName admin, String packageName, String permission, int grantState) {
        throwIfParentInstance("setPermissionGrantState");
        try {
            return this.mService.setPermissionGrantState(admin, packageName, permission, grantState);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getPermissionGrantState(ComponentName admin, String packageName, String permission) {
        throwIfParentInstance("getPermissionGrantState");
        try {
            return this.mService.getPermissionGrantState(admin, packageName, permission);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isProvisioningAllowed(String action) {
        throwIfParentInstance("isProvisioningAllowed");
        try {
            return this.mService.isProvisioningAllowed(action);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isManagedProfile(ComponentName admin) {
        throwIfParentInstance("isManagedProfile");
        try {
            return this.mService.isManagedProfile(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isSystemOnlyUser(ComponentName admin) {
        try {
            return this.mService.isSystemOnlyUser(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public String getWifiMacAddress(ComponentName admin) {
        throwIfParentInstance("getWifiMacAddress");
        try {
            return this.mService.getWifiMacAddress(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void reboot(ComponentName admin) {
        throwIfParentInstance("reboot");
        try {
            this.mService.reboot(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setShortSupportMessage(ComponentName admin, CharSequence message) {
        throwIfParentInstance("setShortSupportMessage");
        if (this.mService != null) {
            try {
                this.mService.setShortSupportMessage(admin, message);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getShortSupportMessage(ComponentName admin) {
        throwIfParentInstance("getShortSupportMessage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getShortSupportMessage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setLongSupportMessage(ComponentName admin, CharSequence message) {
        throwIfParentInstance("setLongSupportMessage");
        if (this.mService != null) {
            try {
                this.mService.setLongSupportMessage(admin, message);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public CharSequence getLongSupportMessage(ComponentName admin) {
        throwIfParentInstance("getLongSupportMessage");
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getLongSupportMessage(admin);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public CharSequence getShortSupportMessageForUser(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getShortSupportMessageForUser(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public CharSequence getLongSupportMessageForUser(ComponentName admin, int userHandle) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getLongSupportMessageForUser(admin, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DevicePolicyManager getParentProfileInstance(ComponentName admin) {
        throwIfParentInstance("getParentProfileInstance");
        try {
            if (this.mService.isManagedProfile(admin)) {
                return new DevicePolicyManager(this.mContext, true);
            }
            throw new SecurityException("The current user does not have a parent profile.");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSecurityLoggingEnabled(ComponentName admin, boolean enabled) {
        throwIfParentInstance("setSecurityLoggingEnabled");
        try {
            this.mService.setSecurityLoggingEnabled(admin, enabled);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isSecurityLoggingEnabled(ComponentName admin) {
        throwIfParentInstance("isSecurityLoggingEnabled");
        try {
            return this.mService.isSecurityLoggingEnabled(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<SecurityEvent> retrieveSecurityLogs(ComponentName admin) {
        throwIfParentInstance("retrieveSecurityLogs");
        try {
            ParceledListSlice<SecurityEvent> list = this.mService.retrieveSecurityLogs(admin);
            if (list != null) {
                return list.getList();
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public DevicePolicyManager getParentProfileInstance(UserInfo uInfo) {
        this.mContext.checkSelfPermission(permission.MANAGE_PROFILE_AND_DEVICE_OWNERS);
        if (uInfo.isManagedProfile()) {
            return new DevicePolicyManager(this.mContext, true);
        }
        throw new SecurityException("The user " + uInfo.id + " does not have a parent profile.");
    }

    public List<SecurityEvent> retrievePreRebootSecurityLogs(ComponentName admin) {
        throwIfParentInstance("retrievePreRebootSecurityLogs");
        try {
            ParceledListSlice<SecurityEvent> list = this.mService.retrievePreRebootSecurityLogs(admin);
            if (list != null) {
                return list.getList();
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setOrganizationColor(ComponentName admin, int color) {
        throwIfParentInstance("setOrganizationColor");
        try {
            this.mService.setOrganizationColor(admin, color | Color.BLACK);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setOrganizationColorForUser(int color, int userId) {
        try {
            this.mService.setOrganizationColorForUser(color | Color.BLACK, userId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getOrganizationColor(ComponentName admin) {
        throwIfParentInstance("getOrganizationColor");
        try {
            return this.mService.getOrganizationColor(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getOrganizationColorForUser(int userHandle) {
        try {
            return this.mService.getOrganizationColorForUser(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void setOrganizationName(ComponentName admin, CharSequence title) {
        throwIfParentInstance("setOrganizationName");
        try {
            this.mService.setOrganizationName(admin, title);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public CharSequence getOrganizationName(ComponentName admin) {
        throwIfParentInstance("getOrganizationName");
        try {
            return this.mService.getOrganizationName(admin);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public CharSequence getOrganizationNameForUser(int userHandle) {
        try {
            return this.mService.getOrganizationNameForUser(userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getUserProvisioningState() {
        throwIfParentInstance("getUserProvisioningState");
        if (this.mService == null) {
            return STATE_USER_UNMANAGED;
        }
        try {
            return this.mService.getUserProvisioningState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUserProvisioningState(int state, int userHandle) {
        if (this.mService != null) {
            try {
                this.mService.setUserProvisioningState(state, userHandle);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setAffiliationIds(ComponentName admin, Set<String> ids) {
        throwIfParentInstance("setAffiliationIds");
        try {
            this.mService.setAffiliationIds(admin, new ArrayList(ids));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isAffiliatedUser() {
        throwIfParentInstance("isAffiliatedUser");
        try {
            return this.mService != null ? this.mService.isAffiliatedUser() : false;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUninstallInQueue(String packageName) {
        try {
            return this.mService.isUninstallInQueue(packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void uninstallPackageWithActiveAdmins(String packageName) {
        try {
            this.mService.uninstallPackageWithActiveAdmins(packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void forceRemoveActiveAdmin(ComponentName adminReceiver, int userHandle) {
        try {
            this.mService.forceRemoveActiveAdmin(adminReceiver, userHandle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    private void throwIfParentInstance(String functionName) {
        if (this.mParentInstance) {
            throw new SecurityException(functionName + " cannot be called on the parent instance");
        }
    }
}
