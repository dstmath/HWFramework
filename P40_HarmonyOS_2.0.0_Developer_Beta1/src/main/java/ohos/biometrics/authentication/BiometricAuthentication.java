package ohos.biometrics.authentication;

import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import ohos.aafwk.ability.Ability;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class BiometricAuthentication {
    private static final String ACCESS_PERMISSION = "ohos.permission.ACCESS_BIOMETRIC";
    public static final int BA_CHECK_AUTH_TYPE_NOT_SUPPORT = 1;
    public static final int BA_CHECK_DISTRIBUTED_AUTH_NOT_SUPPORT = 3;
    public static final int BA_CHECK_NOT_ENROLLED = 4;
    public static final int BA_CHECK_SECURE_LEVEL_NOT_SUPPORT = 2;
    public static final int BA_CHECK_SUPPORTED = 0;
    public static final int BA_CHECK_UNAVAILABLE = 5;
    public static final int BA_FAILED = -1;
    public static final int BA_SUCCESS = 0;
    private static final int BIOMETRIC_DOMAIN = 218113024;
    static final int DFT_EVENT_ID_BIOMETRIC_AUTH = 940101021;
    public static final int INVALID_BIOMETRIC_ID = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) BIOMETRIC_DOMAIN, "BiometricAuthentication");
    private static final int PERMISSION_GRANTED = 0;
    private static volatile BiometricAuthentication sInstance;
    private AuthType mAuthType;
    private final Context mContext;
    private final BiometricAuthenticationProxy mProxy;

    public enum AuthType {
        AUTH_TYPE_BIOMETRIC_ALL,
        AUTH_TYPE_BIOMETRIC_FINGERPRINT_ONLY,
        AUTH_TYPE_BIOMETRIC_FACE_ONLY
    }

    public static class AuthenticationTips {
        public int errorCode = -1;
        public int tipEvent = 0;
        public String tipInfo = "";
        public int tipValue = 0;
    }

    public enum SecureLevel {
        SECURE_LEVEL_S1,
        SECURE_LEVEL_S2,
        SECURE_LEVEL_S3,
        SECURE_LEVEL_S4
    }

    public static class SystemAuthDialogInfo implements Sequenceable {
        public String authDescription;
        public String authTitle;
        public String customButtonText;

        public boolean marshalling(Parcel parcel) {
            return parcel.writeString(this.authTitle) && parcel.writeString(this.authDescription) && parcel.writeString(this.customButtonText);
        }

        public boolean unmarshalling(Parcel parcel) {
            this.authTitle = parcel.readString();
            this.authDescription = parcel.readString();
            this.customButtonText = parcel.readString();
            return true;
        }
    }

    private BiometricAuthentication(Context context) {
        this.mContext = context;
        this.mProxy = new BiometricAuthenticationProxy(context);
    }

    public static BiometricAuthentication getInstance(Ability ability) throws IllegalAccessException {
        if (ability != null) {
            Context applicationContext = ability.getApplicationContext();
            if (applicationContext == null) {
                throw new IllegalArgumentException("GetApplicationContext is null");
            } else if (applicationContext.verifySelfPermission(ACCESS_PERMISSION) == 0) {
                if (sInstance == null) {
                    synchronized (BiometricAuthentication.class) {
                        if (sInstance == null) {
                            sInstance = new BiometricAuthentication(applicationContext);
                        }
                    }
                }
                return sInstance;
            } else {
                throw new IllegalAccessException("No permission to access biometric authentication!");
            }
        } else {
            throw new IllegalArgumentException("Input parameter is null!");
        }
    }

    public int checkAuthenticationAvailability(AuthType authType, SecureLevel secureLevel, boolean z) {
        Context context = this.mContext;
        if (context == null || context.verifySelfPermission(ACCESS_PERMISSION) != 0) {
            HiLog.error(LABEL, "No permission to access checkAuthenticationAvailability", new Object[0]);
            return -1;
        }
        this.mAuthType = authType;
        return this.mProxy.checkAuthenticationAvailability(authType, secureLevel, z);
    }

    public int execAuthenticationAction(AuthType authType, SecureLevel secureLevel, boolean z, boolean z2, SystemAuthDialogInfo systemAuthDialogInfo) {
        Context context = this.mContext;
        if (context == null || context.verifySelfPermission(ACCESS_PERMISSION) != 0) {
            HiLog.error(LABEL, "No permission to access execAuthenticationAction", new Object[0]);
            return -1;
        }
        this.mAuthType = authType;
        return this.mProxy.execAuthenticationAction(authType, secureLevel, z, z2, systemAuthDialogInfo);
    }

    public AuthenticationTips getAuthenticationTips() {
        AuthenticationTips authenticationTips = new AuthenticationTips();
        Context context = this.mContext;
        if (context == null || context.verifySelfPermission(ACCESS_PERMISSION) != 0) {
            HiLog.error(LABEL, "No permission to access getAuthenticationTips", new Object[0]);
            return authenticationTips;
        } else if (this.mAuthType == null) {
            HiLog.error(LABEL, "getAuthenticationTips: authentication type is null", new Object[0]);
            return authenticationTips;
        } else if (AnonymousClass1.$SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[this.mAuthType.ordinal()] == 1) {
            return this.mProxy.getAuthenticationTips();
        } else {
            HiLog.error(LABEL, "Authentication type error! type = %{public}d", new Object[]{this.mAuthType});
            return authenticationTips;
        }
    }

    /* renamed from: ohos.biometrics.authentication.BiometricAuthentication$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType = new int[AuthType.values().length];

        static {
            try {
                $SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[AuthType.AUTH_TYPE_BIOMETRIC_FACE_ONLY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    public int cancelAuthenticationAction() {
        Context context = this.mContext;
        if (context != null && context.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            return this.mProxy.cancelAuthenticationAction(this.mAuthType);
        }
        HiLog.error(LABEL, "No permission to access cancelAuthenticationAction", new Object[0]);
        return -1;
    }

    public void setSecureObjectSignature(Signature signature) {
        Context context = this.mContext;
        if (context == null || context.verifySelfPermission(ACCESS_PERMISSION) != 0) {
            HiLog.error(LABEL, "No permission to access setSecureObjectSignature", new Object[0]);
        } else {
            this.mProxy.setSecureObjectSignature(signature);
        }
    }

    public Signature getSecureObjectSignature() {
        Context context = this.mContext;
        if (context != null && context.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            return this.mProxy.getSecureObjectSignature();
        }
        HiLog.error(LABEL, "No permission to access getSecureObjectSignature", new Object[0]);
        return null;
    }

    public void setSecureObjectCipher(Cipher cipher) {
        Context context = this.mContext;
        if (context == null || context.verifySelfPermission(ACCESS_PERMISSION) != 0) {
            HiLog.error(LABEL, "No permission to access setSecureObjectCipher", new Object[0]);
        } else {
            this.mProxy.setSecureObjectCipher(cipher);
        }
    }

    public Cipher getSecureObjectCipher() {
        Context context = this.mContext;
        if (context != null && context.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            return this.mProxy.getSecureObjectCipher();
        }
        HiLog.error(LABEL, "No permission to access getSecureObjectCipher", new Object[0]);
        return null;
    }

    public void setSecureObjectMac(Mac mac) {
        Context context = this.mContext;
        if (context == null || context.verifySelfPermission(ACCESS_PERMISSION) != 0) {
            HiLog.error(LABEL, "No permission to access setSecureObjectMac", new Object[0]);
        } else {
            this.mProxy.setSecureObjectMac(mac);
        }
    }

    public Mac getSecureObjectMac() {
        Context context = this.mContext;
        if (context != null && context.verifySelfPermission(ACCESS_PERMISSION) == 0) {
            return this.mProxy.getSecureObjectMac();
        }
        HiLog.error(LABEL, "No permission to access getSecureObjectMac", new Object[0]);
        return null;
    }

    public int getAuthenticatedBiometricId() {
        Context context = this.mContext;
        if (context == null || context.verifySelfPermission(ACCESS_PERMISSION) != 0) {
            HiLog.error(LABEL, "No permission to access getAuthenticationId", new Object[0]);
            return -1;
        } else if (this.mAuthType == null) {
            HiLog.error(LABEL, "getAuthenticationTips: authentication type is null", new Object[0]);
            return -1;
        } else if (AnonymousClass1.$SwitchMap$ohos$biometrics$authentication$BiometricAuthentication$AuthType[this.mAuthType.ordinal()] == 1) {
            return this.mProxy.getAuthedFaceId();
        } else {
            HiLog.error(LABEL, "Authentication type error! type = %{public}d", new Object[]{this.mAuthType});
            return -1;
        }
    }
}
