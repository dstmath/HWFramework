package android.security.keystore;

public interface UserAuthArgs {
    long getBoundToSpecificSecureUserId();

    int getUserAuthenticationValidityDurationSeconds();

    boolean isInvalidatedByBiometricEnrollment();

    boolean isUnlockedDeviceRequired();

    boolean isUserAuthenticationRequired();

    boolean isUserAuthenticationValidWhileOnBody();

    boolean isUserConfirmationRequired();

    boolean isUserPresenceRequired();
}
