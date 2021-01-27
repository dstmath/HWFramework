package com.huawei.android.biometric;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.app.trust.TrustManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import com.android.internal.statusbar.IStatusBarService;
import com.android.server.LocalServices;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.ClientMonitor;
import com.android.server.biometrics.Constants;
import com.android.server.biometrics.RemovalClient;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.server.LocalServicesEx;
import com.huawei.fingerprint.IAuthenticator;
import com.huawei.fingerprint.IAuthenticatorListener;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.HwPartIawareUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IExtBiometricsFingerprint;

public class FingerprintServiceEx extends FingerprintService {
    public static final String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
    private static final int DEFAULT_CAPACITY = 10;
    protected static final int ENROLL_UD = 4096;
    public static final String FACE_DETECT_REASON = "fingerprint";
    protected static final int HW_FP_AUTH_BOTH_SPACE = 33554432;
    protected static final int HW_FP_AUTH_UD = 134217728;
    protected static final int HW_FP_AUTH_UG = 67108864;
    public static final int LOCKOUT_NONE = 0;
    public static final int LOCKOUT_PERMANENT = 2;
    public static final int LOCKOUT_TIMED = 1;
    protected static final int MAX_FAILED_ATTEMPTS_LOCKOUT_TIMED = 5;
    public static final int PHASE_BOOT_COMPLETED = 1000;
    protected static final int SPECIAL_USER_ID = -101;
    private static final String TAG = "FingerprintServiceEx";
    private IAuthenticator mAuthenticator = new IAuthenticator.Stub() {
        /* class com.huawei.android.biometric.FingerprintServiceEx.AnonymousClass1 */

        public int verifyUser(IFingerprintServiceReceiver receiver, IAuthenticatorListener listener, int userid, byte[] nonce, String aaid) {
            BiometricServiceReceiverListenerEx receiverEx = new BiometricServiceReceiverListenerEx();
            receiverEx.setServiceListener(new FingerprintService.ServiceListenerImpl(FingerprintServiceEx.this, receiver));
            receiverEx.setFingerprintServiceReceiver(receiver);
            receiverEx.setAuthenticatorListener(listener);
            return FingerprintServiceEx.this.verifyUserEx(receiverEx, userid, nonce, aaid);
        }

        public int cancelVerifyUser(IFingerprintServiceReceiver receiver, int userId) {
            FingerprintServiceEx fingerprintServiceEx = FingerprintServiceEx.this;
            return fingerprintServiceEx.cancelVerifyUserEx(fingerprintServiceEx.createFingerprintServiceReceiverEx(receiver), userId);
        }
    };
    protected ConstantsEx mConstantsEx;
    protected ClientMonitorEx mCurrentClientEx;
    protected DaemonWrapperEx mDaemonWrapperEx;
    protected ClientMonitorEx mPendingClientEx;
    protected TouchscreenEx mProxy = null;
    protected StatusBarManagerServiceEx mStatusBarManagerServiceEx;
    private WindowManagerInternal mWindowManagerInternal;

    public FingerprintServiceEx(Context context) {
        super(context);
        FingerprintSupportEx.getInstance().setContext(context);
        this.mStatusBarManagerServiceEx = new StatusBarManagerServiceEx(this.mStatusBarService);
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
    }

    /* access modifiers changed from: protected */
    public Context getContextEx() {
        return FingerprintServiceEx.super.getContext();
    }

    public void onStart() {
        FingerprintServiceEx.super.onStart();
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                /* class com.huawei.android.biometric.FingerprintServiceEx.AnonymousClass2 */

                public void onUserSwitching(int newUserId) {
                    FingerprintServiceEx.this.onUserSwitching(newUserId);
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    FingerprintServiceEx.this.onUserSwitchComplete(newUserId);
                }
            }, TAG);
        } catch (RemoteException e) {
            Slog.e(TAG, "registerUserSwitchObserver fail");
        } catch (SecurityException e2) {
            Slog.w(TAG, "registerReceiverAsUser fail ");
        }
    }

    /* access modifiers changed from: protected */
    public void publishBinderServiceEx(String name, IBinder service) {
        FingerprintServiceEx.super.publishBinderService(name, service);
    }

    public boolean checkPrivacySpaceEnroll(int userId, int currentUserId) {
        return FingerprintServiceEx.super.checkPrivacySpaceEnroll(userId, currentUserId);
    }

    public boolean checkNeedPowerpush() {
        return FingerprintServiceEx.super.checkNeedPowerpush();
    }

    public void onBootPhase(int phase) {
        FingerprintServiceEx.super.onBootPhase(phase);
    }

    /* access modifiers changed from: protected */
    public int getRealUserIdForApp(int groupId) {
        return FingerprintServiceEx.super.getRealUserIdForApp(groupId);
    }

    /* access modifiers changed from: protected */
    public void onUserSwitching(int newUserId) {
    }

    /* access modifiers changed from: protected */
    public void onUserSwitchComplete(int newUserId) {
    }

    /* access modifiers changed from: protected */
    public long getUdHalDeviceId() {
        return this.mUDHalDeviceId;
    }

    /* access modifiers changed from: protected */
    public long getHalDeviceIdEx() {
        return this.mHalDeviceId;
    }

    /* access modifiers changed from: protected */
    public Uri getSettingsSecureUriFor(String uriInformation) {
        return Settings.Secure.getUriFor(uriInformation);
    }

    /* access modifiers changed from: protected */
    public int getSettingsGlobalInt(ContentResolver resolver, String settingTag, int defaultValue) {
        return Settings.Global.getInt(resolver, settingTag, defaultValue);
    }

    /* access modifiers changed from: protected */
    public int getSettingsSystemIntForUser(ContentResolver resolver, String settingTag, int userId) {
        try {
            return Settings.System.getIntForUser(resolver, settingTag, userId);
        } catch (Settings.SettingNotFoundException e) {
            Log.w(TAG, "getSettingsSystemIntForUser");
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCoverOpen() {
        if (this.mWindowManagerInternal == null) {
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        }
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal == null || windowManagerInternal.isCoverOpen()) {
            return false;
        }
        return true;
    }

    public boolean onHwTransact(int code, Parcel data, Parcel reply, int flags) {
        ParcelEx dataEx = new ParcelEx();
        dataEx.setDataReply(data);
        ParcelEx replyEx = new ParcelEx();
        replyEx.setDataReply(reply);
        return onHwTransact(code, dataEx, replyEx, flags);
    }

    /* access modifiers changed from: protected */
    public boolean onHwTransact(int code, ParcelEx data, ParcelEx reply, int flags) {
        return false;
    }

    /* access modifiers changed from: protected */
    public long getAuTime() {
        return this.auTime;
    }

    /* access modifiers changed from: protected */
    public void setAuTime(long time) {
        this.auTime = time;
    }

    /* access modifiers changed from: protected */
    public class FingerprintAuthClientEx extends FingerprintService.FingerprintAuthClient {
        private BiometricServiceReceiverListenerEx mServiceListener;

        public FingerprintAuthClientEx(ClientMonitorParameterEx clientMonitorParameterEx) {
            super(FingerprintServiceEx.this, clientMonitorParameterEx.getContext(), clientMonitorParameterEx.getDaemon().getDaemonWrapper(), clientMonitorParameterEx.getHalDeviceId(), clientMonitorParameterEx.getToken(), clientMonitorParameterEx.getListener().getServiceListener(), clientMonitorParameterEx.getTargetUserId(), clientMonitorParameterEx.getGroupId(), clientMonitorParameterEx.getOpId(), clientMonitorParameterEx.isRestricted(), clientMonitorParameterEx.getOwner(), clientMonitorParameterEx.getCookie(), clientMonitorParameterEx.isRequireConfirmation());
            this.mPackageName = clientMonitorParameterEx.getOwner();
            this.mFlags = clientMonitorParameterEx.getFlags();
            this.mServiceListener = clientMonitorParameterEx.getListener();
        }

        public BiometricServiceReceiverListenerEx geServicetListener() {
            return this.mServiceListener;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public int getFlags() {
            return this.mFlags;
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            FingerprintServiceEx.super.handleHwFailedAttempt(flags, packagesName);
        }

        public boolean onAuthenticatedEx(BiometricAuthenticatorEx identifier, boolean isAuthenticated, ArrayList<Byte> token) {
            return FingerprintServiceEx.super.onAuthenticated(identifier.getIdentifier(), isAuthenticated, token);
        }

        public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean isAuthenticated, ArrayList<Byte> token) {
            BiometricAuthenticatorEx biometricAuthenticatorEx = new BiometricAuthenticatorEx();
            biometricAuthenticatorEx.setIdentifier(identifier);
            return onAuthenticatedEx(biometricAuthenticatorEx, isAuthenticated, token);
        }

        public int handleFailedAttempt() {
            return FingerprintServiceEx.super.handleFailedAttempt();
        }

        public boolean inLockoutMode() {
            return FingerprintServiceEx.super.inLockoutMode();
        }

        public void resetFailedAttempts() {
            FingerprintServiceEx.super.resetFailedAttempts();
        }

        public void onStart() {
            FingerprintServiceEx.super.onStart();
        }

        public void onStop() {
            FingerprintServiceEx.super.onStop();
        }

        public String getOwnerStringEx() {
            return FingerprintServiceEx.super.getOwnerString();
        }

        public boolean isScreenOn(Context context) {
            return FingerprintServiceEx.super.isScreenOn(context);
        }

        public int getGroupIdEx() {
            return FingerprintServiceEx.super.getGroupId();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private BiometricServiceReceiverListenerEx createFingerprintServiceReceiverEx(IFingerprintServiceReceiver receiver) {
        BiometricServiceReceiverListenerEx receiverEx = new BiometricServiceReceiverListenerEx();
        receiverEx.setFingerprintServiceReceiver(receiver);
        return receiverEx;
    }

    private BiometricAuthenticatorEx createBiometricAuthenticatorEx(BiometricAuthenticator.Identifier identifier) {
        BiometricAuthenticatorEx biometricAuthenticatorEx = new BiometricAuthenticatorEx();
        biometricAuthenticatorEx.setIdentifier(identifier);
        return biometricAuthenticatorEx;
    }

    /* access modifiers changed from: protected */
    public boolean canUseBiometric(String opPackageName, boolean isRequireForeground, int uid, int pid, int userId) {
        return FingerprintServiceEx.super.canUseBiometric(opPackageName, isRequireForeground, uid, pid, userId, false);
    }

    /* access modifiers changed from: protected */
    public boolean canUseBiometric(String opPackageName, boolean isRequireForeground, int uid, int pid, int userId, boolean isDetected) {
        return FingerprintServiceEx.super.canUseBiometric(opPackageName, isRequireForeground, uid, pid, userId, isDetected);
    }

    public void serviceDied(long cookie) {
        FingerprintServiceEx.super.serviceDied(cookie);
    }

    /* access modifiers changed from: protected */
    public boolean isCurrentUserOrProfile(int userId) {
        return FingerprintServiceEx.super.isCurrentUserOrProfile(userId);
    }

    /* access modifiers changed from: protected */
    public int getEffectiveUserId(int userId) {
        return FingerprintServiceEx.super.getEffectiveUserId(userId);
    }

    /* access modifiers changed from: protected */
    public int getHiddenSpaceId() {
        return -100;
    }

    /* access modifiers changed from: protected */
    public int getPrimaryUserId() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getLockoutMode() {
        return FingerprintServiceEx.super.getLockoutMode();
    }

    protected static Uri getUriForSecure(String name) {
        return Settings.Secure.getUriFor(name);
    }

    /* access modifiers changed from: protected */
    public void updateActiveGroup(int userId, String clientPackage) {
        FingerprintServiceEx.super.updateActiveGroup(userId, clientPackage);
    }

    /* access modifiers changed from: protected */
    public void registerContentObserver(Uri uri, boolean isNotifyForDescendents, ContentObserver observer, int userHandle) {
        Context context = FingerprintServiceEx.super.getContext();
        if (context != null) {
            context.getContentResolver().registerContentObserver(uri, isNotifyForDescendents, observer, userHandle);
        }
    }

    /* access modifiers changed from: protected */
    public void startAuthenticationClientEx(Object instance, Class targetClass, AuthenticationClientEx client) {
        invokeParentPrivateFunction(instance, targetClass, "startClient", new Class[]{ClientMonitor.class, Boolean.TYPE}, new Object[]{client, true});
    }

    /* access modifiers changed from: protected */
    public Object invokeParentPrivateFunction(Object instance, Class targetClass, String method, Class[] paramTypes, Object[] params) {
        return new Object();
    }

    /* access modifiers changed from: protected */
    public void notifyAuthCanceled(String topPackage) {
        FingerprintServiceEx.super.notifyAuthCanceled(topPackage);
    }

    /* access modifiers changed from: protected */
    public Handler getHandler() {
        return this.mHandler;
    }

    protected static File getFingerprintFileDirectory(int userId) {
        return Environment.getFingerprintFileDirectory(userId);
    }

    protected static File getUserSystemDirectory(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    public boolean handleEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        if (!FingerprintServiceEx.super.handleEnrollResult(identifier, remaining)) {
            return handleEnrollResultEx(createBiometricAuthenticatorEx(identifier), remaining);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleEnrollResultEx(BiometricAuthenticatorEx identifier, int remaining) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleError(long deviceId, int error, int vendorCode) {
        handleErrorEx(deviceId, error, vendorCode);
        FingerprintServiceEx.super.handleError(deviceId, error, vendorCode);
    }

    /* access modifiers changed from: protected */
    public void handleErrorEx(long deviceId, int error, int vendorCode) {
    }

    /* access modifiers changed from: protected */
    public boolean removeInternalEx(RemovalClientEx client) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void removeInternal(RemovalClient client) {
        RemovalClientEx removalClientEx = new RemovalClientEx();
        removalClientEx.setRemovalClient(client);
        if (removeInternalEx(removalClientEx)) {
            FingerprintServiceEx.super.removeInternal(client);
        }
    }

    /* access modifiers changed from: protected */
    public void handleAuthenticated(BiometricAuthenticator.Identifier identifier, ArrayList<Byte> token) {
        handleAuthenticatedEx(createBiometricAuthenticatorEx(identifier), token);
    }

    /* access modifiers changed from: protected */
    public void handleAuthenticatedEx(BiometricAuthenticatorEx identifier, ArrayList<Byte> token) {
        if (identifier != null) {
            FingerprintServiceEx.super.handleAuthenticated(identifier.getIdentifier(), token);
        }
    }

    /* access modifiers changed from: protected */
    public void handleAcquired(long deviceId, int acquiredInfo, int vendorCode) {
        FingerprintServiceEx.super.handleAcquired(deviceId, acquiredInfo, vendorCode);
    }

    /* access modifiers changed from: protected */
    public void notifyEnrollCanceled() {
        FingerprintServiceEx.super.notifyEnrollCanceled();
    }

    /* access modifiers changed from: protected */
    public FingerprintAuthClientEx creatAuthenticationClientEx(ClientMonitorParameterEx clientMonitorParameterEx) {
        return new FingerprintAuthClientEx(clientMonitorParameterEx);
    }

    /* access modifiers changed from: protected */
    public BiometricServiceBase.AuthenticationClientImpl creatAuthenticationClient(Context context, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean isRestricted, String owner, int cookie, boolean isRequireConfirmation, int flag) {
        DaemonWrapperEx daemonWrapperEx = new DaemonWrapperEx();
        daemonWrapperEx.setDaemonWrapper(daemon);
        BiometricServiceReceiverListenerEx biometricServiceReceiverListenerEx = new BiometricServiceReceiverListenerEx();
        biometricServiceReceiverListenerEx.setServiceListener(listener);
        ClientMonitorParameterEx clientMonitorParameterEx = new ClientMonitorParameterEx();
        clientMonitorParameterEx.setContext(context);
        clientMonitorParameterEx.setDaemon(daemonWrapperEx);
        clientMonitorParameterEx.setHalDeviceId(halDeviceId);
        clientMonitorParameterEx.setToken(token);
        clientMonitorParameterEx.setListener(biometricServiceReceiverListenerEx);
        clientMonitorParameterEx.setTargetUserId(targetUserId);
        clientMonitorParameterEx.setGroupId(groupId);
        clientMonitorParameterEx.setOpId(opId);
        clientMonitorParameterEx.setRestricted(isRestricted);
        clientMonitorParameterEx.setOwner(owner);
        clientMonitorParameterEx.setCookie(cookie);
        clientMonitorParameterEx.setRequireConfirmation(isRequireConfirmation);
        clientMonitorParameterEx.setFlags(flag);
        return creatAuthenticationClientEx(clientMonitorParameterEx);
    }

    /* access modifiers changed from: protected */
    public void resetFailedAttemptsForUser(boolean isClearAttemptCounter, int userId) {
        FingerprintServiceEx.super.resetFailedAttemptsForUser(isClearAttemptCounter, userId);
    }

    /* access modifiers changed from: protected */
    public void addHighlightOnAcquired(int acquiredInfo, int vendorCode) {
    }

    /* access modifiers changed from: protected */
    public int getPowerWakeupReason() {
        return 1;
    }

    /* access modifiers changed from: protected */
    public int getMaxFailedAttemptsLockoutTimed() {
        return 5;
    }

    /* access modifiers changed from: protected */
    public int getFingerprintAcquiredVendor() {
        return 6;
    }

    /* access modifiers changed from: protected */
    public int getFingerprintAcquiredVendorBase() {
        return 1000;
    }

    /* access modifiers changed from: protected */
    public boolean isKeyguardCurrentClient() {
        return FingerprintServiceEx.super.isKeyguardCurrentClient();
    }

    /* access modifiers changed from: protected */
    public String getKeyguardPackage() {
        return this.mKeyguardPackage;
    }

    /* access modifiers changed from: protected */
    public UserManager getUserManager() {
        return ((FingerprintService) this).mUserManager;
    }

    /* access modifiers changed from: protected */
    public void enrollInternalEx(EnrollClientImplEx client, int userId, int flags, String opPackageName) {
    }

    public void enrollInternal(BiometricServiceBase.EnrollClientImpl client, int userId, int flags, String opPackageName) {
        EnrollClientImplEx enrollClientImplEx = new EnrollClientImplEx();
        enrollClientImplEx.setEnrollClientImpl(client);
        enrollInternalEx(enrollClientImplEx, userId, flags, opPackageName);
        FingerprintServiceEx.super.enrollInternal(client);
    }

    /* access modifiers changed from: protected */
    public void udFingerprintAllRemoved(ClientMonitor client, int groupId) {
        if (client != null) {
            if (client instanceof RemovalClient) {
                RemovalClientEx removalClientEx = new RemovalClientEx();
                removalClientEx.setRemovalClient((RemovalClient) client);
                udFingerprintAllRemovedEx(removalClientEx, groupId);
            }
            FingerprintServiceEx.super.udFingerprintAllRemoved(client, groupId);
        }
    }

    /* access modifiers changed from: protected */
    public void udFingerprintAllRemovedEx(RemovalClientEx client, int groupId) {
    }

    /* access modifiers changed from: protected */
    public void dualFingerprintStartAuth(int flags, String opPackageName) {
        FingerprintServiceEx.super.dualFingerprintStartAuth(flags, opPackageName);
    }

    /* access modifiers changed from: protected */
    public boolean hasReachedEnrollmentLimit(int userId) {
        return FingerprintServiceEx.super.hasReachedEnrollmentLimit(userId);
    }

    /* access modifiers changed from: protected */
    public void setEnrolled(int enrolled) {
        ((FingerprintService) this).mEnrolled = enrolled;
    }

    public FingerprintServiceWrapperEx creatFingerprintServiceWrapper() {
        return new FingerprintServiceWrapperEx();
    }

    public class FingerprintServiceWrapperEx extends FingerprintService.FingerprintServiceWrapper {
        public FingerprintServiceWrapperEx() {
            super(FingerprintServiceEx.this);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            try {
                return FingerprintServiceEx.super.onTransact(code, data, reply, flags);
            } catch (RemoteException e) {
                Log.w(FingerprintServiceEx.TAG, "onTransact");
                return false;
            }
        }

        public void authenticateEx(FingerprintParameterEx fingerprintParameterEx) {
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            BiometricServiceReceiverListenerEx biometricReceiver = new BiometricServiceReceiverListenerEx();
            biometricReceiver.setFingerprintServiceReceiver(receiver);
            FingerprintParameterEx fingerprintParameterEx = new FingerprintParameterEx();
            fingerprintParameterEx.setToken(token);
            fingerprintParameterEx.setOpId(opId);
            fingerprintParameterEx.setGroupId(groupId);
            fingerprintParameterEx.setReceiver(biometricReceiver);
            fingerprintParameterEx.setFlags(flags);
            fingerprintParameterEx.setOpPackageName(opPackageName);
            authenticateEx(fingerprintParameterEx);
            FingerprintServiceEx.super.authenticate(token, opId, fingerprintParameterEx.getGroupId(), receiver, flags, opPackageName);
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) {
            BiometricServiceReceiverListenerEx biometricReceiver = new BiometricServiceReceiverListenerEx();
            biometricReceiver.setFingerprintServiceReceiver(receiver);
            removeEx(token, fingerId, groupId, userId, biometricReceiver);
            FingerprintServiceEx.super.remove(token, fingerId, groupId, userId, receiver);
        }

        public void removeEx(IBinder token, int fingerId, int groupId, int userId, BiometricServiceReceiverListenerEx receiver) {
        }

        public void enroll(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            BiometricServiceReceiverListenerEx biometricReceiver = new BiometricServiceReceiverListenerEx();
            biometricReceiver.setFingerprintServiceReceiver(receiver);
            FingerprintParameterEx enrollExParameter = new FingerprintParameterEx();
            enrollExParameter.setToken(token);
            enrollExParameter.setCryptoToken(cryptoToken);
            enrollExParameter.setUserId(userId);
            enrollExParameter.setReceiver(biometricReceiver);
            enrollExParameter.setFlags(flags);
            enrollExParameter.setOpPackageName(opPackageName);
            enrollEx(enrollExParameter);
            FingerprintServiceEx.super.enroll(token, cryptoToken, userId, receiver, flags, opPackageName);
        }

        public void enrollEx(FingerprintParameterEx enrollExParameter) {
        }

        public void addLockoutResetCallbackEx(BiometricServiceReceiverListenerEx callback) throws RemoteException {
        }

        public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
            BiometricServiceReceiverListenerEx callbackEx = new BiometricServiceReceiverListenerEx();
            callbackEx.setBiometricServiceLockoutResetCallback(callback);
            addLockoutResetCallbackEx(callbackEx);
            FingerprintServiceEx.super.addLockoutResetCallback(callback);
        }

        public List<FingerprintEx> getEnrolledFingerprintsWrapper(int userId, String opPackageName) {
            List<Fingerprint> finerprints = FingerprintServiceEx.super.getEnrolledFingerprints(userId, opPackageName);
            List<FingerprintEx> fingerprintsExs = new ArrayList<>(10);
            for (Fingerprint fingerprint : finerprints) {
                FingerprintEx fingerEx = new FingerprintEx(null, 0, 0, 0);
                fingerEx.setFingerprint(fingerprint);
                fingerprintsExs.add(fingerEx);
            }
            return fingerprintsExs;
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            List<FingerprintEx> fingerprintsExs = getEnrolledFingerprintsWrapper(userId, opPackageName);
            if (fingerprintsExs == null || fingerprintsExs.isEmpty()) {
                return FingerprintServiceEx.super.getEnrolledFingerprints(userId, opPackageName);
            }
            List<Fingerprint> finerprints = new ArrayList<>(10);
            for (FingerprintEx fingerprintEx : fingerprintsExs) {
                finerprints.add(fingerprintEx.getFingerprint());
            }
            return finerprints;
        }

        public long preEnroll(IBinder token) {
            return FingerprintServiceEx.super.preEnroll(token);
        }

        public int postEnroll(IBinder token) {
            return FingerprintServiceEx.super.postEnroll(token);
        }

        public void cancelEnrollment(IBinder token) {
            FingerprintServiceEx.super.cancelEnrollment(token);
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            FingerprintServiceEx.super.cancelAuthentication(token, opPackageName);
        }

        public void rename(int fingerId, int groupId, String name) {
            FingerprintServiceEx.super.rename(fingerId, groupId, name);
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            return FingerprintServiceEx.super.hasEnrolledFingerprints(userId, opPackageName);
        }

        public long getAuthenticatorId(String opPackageName) {
            return FingerprintServiceEx.super.getAuthenticatorId(opPackageName);
        }

        public int getRemainingNum() {
            return FingerprintServiceEx.super.getRemainingNum();
        }

        public long getRemainingTime() {
            return FingerprintServiceEx.super.getRemainingTime();
        }
    }

    protected class StatusBarManagerServiceEx {
        private IStatusBarService mStatusBarService;

        public StatusBarManagerServiceEx(IStatusBarService statusBarService) {
            this.mStatusBarService = statusBarService;
        }

        public void onBiometricHelp(String message) {
            try {
                if (this.mStatusBarService != null) {
                    this.mStatusBarService.onBiometricHelp(message);
                }
            } catch (RemoteException e) {
                Log.w(FingerprintServiceEx.TAG, "onBiometricHelp");
            }
        }

        public void onBiometricAuthenticated(boolean isAuthenticated, String failureReason) {
            try {
                if (this.mStatusBarService != null) {
                    this.mStatusBarService.onBiometricAuthenticated(isAuthenticated, failureReason);
                }
            } catch (RemoteException e) {
                Log.w(FingerprintServiceEx.TAG, "onBiometricAuthenticated");
            }
        }
    }

    /* access modifiers changed from: protected */
    public IBinder getAuthenticatorBinder() {
        return this.mAuthenticator.asBinder();
    }

    /* access modifiers changed from: protected */
    public int verifyUserEx(BiometricServiceReceiverListenerEx receiver, int userid, byte[] nonce, String aaid) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int cancelVerifyUserEx(BiometricServiceReceiverListenerEx receiver, int userId) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public String getAuthenticatorName() {
        return "fido_authenticator";
    }

    /* access modifiers changed from: protected */
    public ConstantsEx getConstantsEx() {
        if (this.mConstantsEx == null) {
            this.mConstantsEx = new ConstantsEx();
            this.mConstantsEx.setConstants(FingerprintServiceEx.super.getConstants());
        }
        return this.mConstantsEx;
    }

    /* access modifiers changed from: protected */
    public DaemonWrapperEx getDaemonWrapperEx() {
        if (this.mDaemonWrapperEx == null) {
            this.mDaemonWrapperEx = new DaemonWrapperEx();
            this.mDaemonWrapperEx.setDaemonWrapper(FingerprintServiceEx.super.getDaemonWrapper());
        }
        return this.mDaemonWrapperEx;
    }

    /* access modifiers changed from: protected */
    public ClientMonitorEx getCurrentClientEx() {
        ClientMonitor client = FingerprintServiceEx.super.getCurrentClient();
        if (client == null) {
            Log.i(TAG, "getCurrentClientEx empty");
            return this.mCurrentClientEx;
        }
        if (this.mCurrentClientEx == null) {
            this.mCurrentClientEx = new ClientMonitorEx();
            this.mCurrentClientEx.setClientMonitor(client);
        }
        return this.mCurrentClientEx;
    }

    /* access modifiers changed from: protected */
    public void startCurrentClient(int cookie) {
        FingerprintServiceEx.super.startCurrentClient(cookie);
    }

    /* access modifiers changed from: protected */
    public void removeClientEx(ClientMonitorEx client) {
    }

    /* access modifiers changed from: protected */
    public void removeClient(ClientMonitor client) {
        ClientMonitorEx clientMonitorEx = new ClientMonitorEx();
        clientMonitorEx.setClientMonitor(client);
        removeClientEx(clientMonitorEx);
        FingerprintServiceEx.super.removeClient(client);
    }

    /* access modifiers changed from: protected */
    public ClientMonitorEx getPendingClientEx() {
        if (this.mPendingClientEx == null) {
            this.mPendingClientEx = new ClientMonitorEx();
            this.mPendingClientEx.setClientMonitor(FingerprintServiceEx.super.getPendingClient());
        }
        return this.mPendingClientEx;
    }

    protected class RemovalClientEx {
        private RemovalClient mRemovalClient;

        public RemovalClientEx() {
        }

        public RemovalClient getRemovalClient() {
            return this.mRemovalClient;
        }

        public void setRemovalClient(RemovalClient removalClient) {
            this.mRemovalClient = removalClient;
        }

        public int getBiometricId() {
            RemovalClient removalClient = this.mRemovalClient;
            if (removalClient != null) {
                return removalClient.getBiometricId();
            }
            return 0;
        }

        public int getTargetUserId() {
            RemovalClient removalClient = this.mRemovalClient;
            if (removalClient != null) {
                return removalClient.getTargetUserId();
            }
            return 0;
        }

        public boolean isRemovalClient() {
            RemovalClient removalClient = this.mRemovalClient;
            if (removalClient != null && (removalClient instanceof RemovalClient)) {
                return true;
            }
            return false;
        }

        public BiometricServiceReceiverListenerEx getListener() {
            RemovalClient removalClient = this.mRemovalClient;
            if (removalClient == null || removalClient.getListener() == null) {
                return null;
            }
            BiometricServiceReceiverListenerEx biometricServiceReceiverListenerEx = new BiometricServiceReceiverListenerEx();
            biometricServiceReceiverListenerEx.setServiceListener(this.mRemovalClient.getListener());
            return biometricServiceReceiverListenerEx;
        }
    }

    protected class ServiceListenerImplEx {
        private FingerprintService.ServiceListenerImpl mServiceListenerImpl;

        public ServiceListenerImplEx(BiometricServiceReceiverListenerEx receiver) {
            this.mServiceListenerImpl = new FingerprintService.ServiceListenerImpl(FingerprintServiceEx.this, receiver.getFingerprintServiceReceiver());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isKeyguard(String clientPackage) {
        return FingerprintServiceEx.super.isKeyguard(clientPackage);
    }

    /* access modifiers changed from: protected */
    public int getCurrentUserId() {
        return ((FingerprintService) this).mCurrentUserId;
    }

    /* access modifiers changed from: protected */
    public SparseIntArray getFailedAttempts() {
        return ((FingerprintService) this).mFailedAttempts;
    }

    /* access modifiers changed from: protected */
    public SparseLongArray getLockoutTime() {
        return ((FingerprintService) this).mLockoutTime;
    }

    /* access modifiers changed from: protected */
    public SparseBooleanArray getTimedLockoutCleared() {
        return ((FingerprintService) this).mTimedLockoutCleared;
    }

    /* access modifiers changed from: protected */
    public long getFailLockoutTimeoutMs() {
        return 30000;
    }

    public class DaemonWrapperEx {
        private BiometricServiceBase.DaemonWrapper mDaemonWrapper;

        public DaemonWrapperEx() {
        }

        public BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
            return this.mDaemonWrapper;
        }

        public void setDaemonWrapper(BiometricServiceBase.DaemonWrapper daemonWrapper) {
            this.mDaemonWrapper = daemonWrapper;
        }
    }

    protected class EnrollClientImplEx {
        private BiometricServiceBase.EnrollClientImpl mEnrollClientImpl;

        public EnrollClientImplEx() {
        }

        public void setEnrollClientImpl(BiometricServiceBase.EnrollClientImpl enrollClientImpl) {
            this.mEnrollClientImpl = enrollClientImpl;
        }

        public void setGroupId(int groupId) {
            BiometricServiceBase.EnrollClientImpl enrollClientImpl = this.mEnrollClientImpl;
            if (enrollClientImpl != null) {
                enrollClientImpl.setGroupId(groupId);
            }
        }

        public void setTargetDevice(int deviceIndex) {
            BiometricServiceBase.EnrollClientImpl enrollClientImpl = this.mEnrollClientImpl;
            if (enrollClientImpl != null) {
                enrollClientImpl.setTargetDevice(deviceIndex);
            }
        }
    }

    /* access modifiers changed from: protected */
    public BiometricsFingerprintEx getBiometricsFingerprintDaemon() {
        IBiometricsFingerprint daemon = FingerprintServiceEx.super.getFingerprintDaemon();
        if (daemon == null) {
            return null;
        }
        BiometricsFingerprintEx biometricsFingerprintEx = new BiometricsFingerprintEx();
        biometricsFingerprintEx.setBiometricsFingerprint(daemon);
        return biometricsFingerprintEx;
    }

    /* access modifiers changed from: protected */
    public BiometricsFingerprintEx getFingerprintDaemonEx() {
        try {
            IExtBiometricsFingerprint daemonEx = IExtBiometricsFingerprint.getService();
            if (daemonEx == null) {
                return null;
            }
            BiometricsFingerprintEx biometricsFingerprintEx = new BiometricsFingerprintEx();
            biometricsFingerprintEx.setExtBiometricsFingerprint(daemonEx);
            return biometricsFingerprintEx;
        } catch (NoSuchElementException e) {
            Log.e(TAG, "Service doesn't exist or cannot be opened");
            return null;
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to get biometric interface");
            return null;
        }
    }

    private class AodFaceTrustListenerEx implements TrustManager.TrustListener {
        private AodFaceTrustListenerEx() {
        }

        public void onTrustChanged(boolean isEnabled, int userId, int flags) {
            FingerprintServiceEx.this.onTrustChangedEx(isEnabled, userId, flags);
        }

        public void onTrustManagedChanged(boolean isEnabled, int userId) {
            FingerprintServiceEx.this.onTrustManagedChangedEx(isEnabled, userId);
        }

        public void onTrustError(CharSequence message) {
            FingerprintServiceEx.this.onTrustError(message);
        }
    }

    /* access modifiers changed from: protected */
    public void onTrustChangedEx(boolean isEnabled, int userId, int flags) {
    }

    /* access modifiers changed from: protected */
    public void onTrustManagedChangedEx(boolean isEnabled, int userId) {
    }

    /* access modifiers changed from: protected */
    public void onTrustError(CharSequence message) {
    }

    /* access modifiers changed from: protected */
    public int shouldAuthBothSpaceBiometricEx(FingerprintAuthClientEx client, String opPackageName, int flags) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int shouldAuthBothSpaceBiometric(BiometricServiceBase.AuthenticationClientImpl client, String opPackageName, int flags) {
        if (client instanceof FingerprintAuthClientEx) {
            return shouldAuthBothSpaceBiometricEx((FingerprintAuthClientEx) client, opPackageName, flags);
        }
        return FingerprintServiceEx.super.shouldAuthBothSpaceBiometric(client, opPackageName, flags);
    }

    /* access modifiers changed from: protected */
    public void registerTrustListenerForAod() {
        try {
            AodFaceTrustListenerEx mAodFaceTrustListener = new AodFaceTrustListenerEx();
            Object object = getContext().getSystemService("trust");
            if (object instanceof TrustManager) {
                ((TrustManager) object).registerTrustListener(mAodFaceTrustListener);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "create AODFaceUpdateMonitor SecurityException");
        }
    }

    /* access modifiers changed from: protected */
    public void triggerFaceRecognization() {
        PowerManager powerManager;
        Object object = getContext().getSystemService("power");
        if (!(object instanceof PowerManager) || (powerManager = (PowerManager) object) == null || !powerManager.isInteractive()) {
            Object objectPhone = LocalServicesEx.getService(WindowManagerPolicy.class);
            if (objectPhone != null && (objectPhone instanceof HwPhoneWindowManager)) {
                ((HwPhoneWindowManager) objectPhone).doFaceRecognize(true, FACE_DETECT_REASON);
                return;
            }
            return;
        }
        Log.i(TAG, "not triggerFaceRecognization");
    }

    /* access modifiers changed from: protected */
    public void stopPickupTrunOff() {
        HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.PICK_UP_WAKE_SCREEN_PART_FACTORY_IMPL).getPickUpWakeScreenManager().stopTurnOffController();
    }

    /* access modifiers changed from: protected */
    public void setFingerprintWakeup(boolean isWakeup) {
        HwPartIawareUtil.setFingerprintWakeup(isWakeup);
    }

    /* access modifiers changed from: protected */
    public String getManageBiometricPermission() {
        return FingerprintServiceEx.super.getManageBiometricPermission();
    }

    public class ConstantsEx {
        private Constants mConstants;

        public ConstantsEx() {
        }

        public Constants getConstants() {
            return this.mConstants;
        }

        public void setConstants(Constants constants) {
            this.mConstants = constants;
        }
    }
}
