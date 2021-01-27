package com.android.server.biometrics.face;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.biometrics.face.V1_0.IBiometricsFace;
import android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback;
import android.hardware.biometrics.face.V1_0.OptionalBool;
import android.hardware.face.Face;
import android.hardware.face.FaceManager;
import android.hardware.face.IFaceService;
import android.hardware.face.IFaceServiceReceiver;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.NativeHandle;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.DumpUtils;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.am.AssistDataRequester;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.BiometricUtils;
import com.android.server.biometrics.Constants;
import com.android.server.biometrics.EnumerateClient;
import com.android.server.biometrics.RemovalClient;
import com.android.server.biometrics.face.FaceService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FaceService extends BiometricServiceBase {
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.biometrics.face.ACTION_LOCKOUT_RESET";
    private static final int CHALLENGE_TIMEOUT_SEC = 600;
    private static final boolean DEBUG = true;
    private static final String FACE_DATA_DIR = "facedata";
    protected static final String TAG = "FaceService";
    private int[] mBiometricPromptIgnoreList = getContext().getResources().getIntArray(17236022);
    private int[] mBiometricPromptIgnoreListVendor = getContext().getResources().getIntArray(17236025);
    private int mCurrentUserLockoutMode;
    @GuardedBy({"this"})
    private IBiometricsFace mDaemon;
    private IBiometricsFaceClientCallback mDaemonCallback = new IBiometricsFaceClientCallback.Stub() {
        /* class com.android.server.biometrics.face.FaceService.AnonymousClass1 */

        @Override // android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onEnrollResult(long deviceId, int faceId, int userId, int remaining) {
            FaceService.this.mHandler.post(new Runnable(userId, faceId, deviceId, remaining) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$1$Dg7kqAVO92T8FbodjRCfn9vSkto */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ long f$3;
                private final /* synthetic */ int f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r6;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.AnonymousClass1.this.lambda$onEnrollResult$0$FaceService$1(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$onEnrollResult$0$FaceService$1(int userId, int faceId, long deviceId, int remaining) {
            FaceService.super.handleEnrollResult(new Face(FaceService.this.getBiometricUtils().getUniqueName(FaceService.this.getContext(), userId), faceId, deviceId), remaining);
        }

        @Override // android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onAcquired(long deviceId, int userId, int acquiredInfo, int vendorCode) {
            FaceService.this.mHandler.post(new Runnable(deviceId, acquiredInfo, vendorCode) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$1$7DzDQwoPfgYi40WuB8Xi0hA3qVQ */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.AnonymousClass1.this.lambda$onAcquired$1$FaceService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onAcquired$1$FaceService$1(long deviceId, int acquiredInfo, int vendorCode) {
            FaceService.super.handleAcquired(deviceId, acquiredInfo, vendorCode);
        }

        @Override // android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onAuthenticated(long deviceId, int faceId, int userId, ArrayList<Byte> token) {
            FaceService.this.mHandler.post(new Runnable(faceId, deviceId, token) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$1$GcU4ZG1fdDLhKvSxuMwfPargEnI */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ long f$2;
                private final /* synthetic */ ArrayList f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.AnonymousClass1.this.lambda$onAuthenticated$2$FaceService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onAuthenticated$2$FaceService$1(int faceId, long deviceId, ArrayList token) {
            FaceService.super.handleAuthenticated(new Face("", faceId, deviceId), token);
        }

        @Override // android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onError(long deviceId, int userId, int error, int vendorCode) {
            FaceService.this.mHandler.post(new Runnable(deviceId, error, vendorCode) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$1$s3kBxUsmTmDZC9YLbT5yPR3KOWo */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.AnonymousClass1.this.lambda$onError$3$FaceService$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onError$3$FaceService$1(long deviceId, int error, int vendorCode) {
            FaceService.super.handleError(deviceId, error, vendorCode);
            if (error == 1) {
                Slog.w(FaceService.TAG, "Got ERROR_HW_UNAVAILABLE; try reconnecting next client.");
                synchronized (this) {
                    FaceService.this.mDaemon = null;
                    FaceService.this.mHalDeviceId = 0;
                    FaceService.this.mCurrentUserId = -10000;
                }
            }
        }

        @Override // android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onRemoved(long deviceId, ArrayList<Integer> faceIds, int userId) {
            FaceService.this.mHandler.post(new Runnable(faceIds, deviceId) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$1$jaJb2y4UkoXOtV5wJimfIPNA_PM */
                private final /* synthetic */ ArrayList f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.AnonymousClass1.this.lambda$onRemoved$4$FaceService$1(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onRemoved$4$FaceService$1(ArrayList faceIds, long deviceId) {
            if (!faceIds.isEmpty()) {
                for (int i = 0; i < faceIds.size(); i++) {
                    FaceService.super.handleRemoved(new Face("", ((Integer) faceIds.get(i)).intValue(), deviceId), (faceIds.size() - i) - 1);
                }
                return;
            }
            FaceService.super.handleRemoved(new Face("", 0, deviceId), 0);
        }

        @Override // android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onEnumerate(long deviceId, ArrayList<Integer> faceIds, int userId) throws RemoteException {
            FaceService.this.mHandler.post(new Runnable(faceIds, deviceId) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$1$81olYJI06zsG8LvXV_gD76jaNyg */
                private final /* synthetic */ ArrayList f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.AnonymousClass1.this.lambda$onEnumerate$5$FaceService$1(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onEnumerate$5$FaceService$1(ArrayList faceIds, long deviceId) {
            if (!faceIds.isEmpty()) {
                for (int i = 0; i < faceIds.size(); i++) {
                    FaceService.super.handleEnumerate(new Face("", ((Integer) faceIds.get(i)).intValue(), deviceId), (faceIds.size() - i) - 1);
                }
                return;
            }
            FaceService.super.handleEnumerate(null, 0);
        }

        @Override // android.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback
        public void onLockoutChanged(long duration) {
            Slog.d(FaceService.TAG, "onLockoutChanged: " + duration);
            if (duration == 0) {
                FaceService.this.mCurrentUserLockoutMode = 0;
            } else if (duration == JobStatus.NO_LATEST_RUNTIME) {
                FaceService.this.mCurrentUserLockoutMode = 2;
            } else {
                FaceService.this.mCurrentUserLockoutMode = 1;
            }
            FaceService.this.mHandler.post(new Runnable(duration) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$1$OiHHyHFXrIcrZYUfSsfE2as1qE */
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.AnonymousClass1.this.lambda$onLockoutChanged$6$FaceService$1(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onLockoutChanged$6$FaceService$1(long duration) {
            if (duration == 0) {
                FaceService.this.notifyLockoutResetMonitors();
            }
        }
    };
    private final BiometricServiceBase.DaemonWrapper mDaemonWrapper = new BiometricServiceBase.DaemonWrapper() {
        /* class com.android.server.biometrics.face.FaceService.AnonymousClass2 */

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int authenticate(long operationId, int groupId) throws RemoteException {
            IBiometricsFace daemon = FaceService.this.getFaceDaemon();
            if (daemon != null) {
                return daemon.authenticate(operationId);
            }
            Slog.w(FaceService.TAG, "authenticate(): no face HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int cancel() throws RemoteException {
            IBiometricsFace daemon = FaceService.this.getFaceDaemon();
            if (daemon != null) {
                return daemon.cancel();
            }
            Slog.w(FaceService.TAG, "cancel(): no face HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int remove(int groupId, int biometricId) throws RemoteException {
            IBiometricsFace daemon = FaceService.this.getFaceDaemon();
            if (daemon != null) {
                return daemon.remove(biometricId);
            }
            Slog.w(FaceService.TAG, "remove(): no face HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int enumerate() throws RemoteException {
            IBiometricsFace daemon = FaceService.this.getFaceDaemon();
            if (daemon != null) {
                return daemon.enumerate();
            }
            Slog.w(FaceService.TAG, "enumerate(): no face HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int enroll(byte[] cryptoToken, int groupId, int timeout, ArrayList<Integer> disabledFeatures) throws RemoteException {
            IBiometricsFace daemon = FaceService.this.getFaceDaemon();
            if (daemon == null) {
                Slog.w(FaceService.TAG, "enroll(): no face HAL!");
                return 3;
            }
            ArrayList<Byte> token = new ArrayList<>();
            for (byte b : cryptoToken) {
                token.add(Byte.valueOf(b));
            }
            return daemon.enroll(token, timeout, disabledFeatures);
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public void resetLockout(byte[] cryptoToken) throws RemoteException {
            IBiometricsFace daemon = FaceService.this.getFaceDaemon();
            if (daemon == null) {
                Slog.w(FaceService.TAG, "resetLockout(): no face HAL!");
                return;
            }
            ArrayList<Byte> token = new ArrayList<>();
            for (byte b : cryptoToken) {
                token.add(Byte.valueOf(b));
            }
            daemon.resetLockout(token);
        }
    };
    private int[] mEnrollIgnoreList = getContext().getResources().getIntArray(17236023);
    private int[] mEnrollIgnoreListVendor = getContext().getResources().getIntArray(17236026);
    private final FaceConstants mFaceConstants = new FaceConstants();
    private int[] mKeyguardIgnoreList = getContext().getResources().getIntArray(17236024);
    private int[] mKeyguardIgnoreListVendor = getContext().getResources().getIntArray(17236027);

    private final class FaceAuthClient extends BiometricServiceBase.AuthenticationClientImpl {
        private int mLastAcquire;

        public FaceAuthClient(Context context, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation) {
            super(context, daemon, halDeviceId, token, listener, targetUserId, groupId, opId, restricted, owner, cookie, requireConfirmation);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.LoggableMonitor
        public int statsModality() {
            return FaceService.this.statsModality();
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean shouldFrameworkHandleLockout() {
            return false;
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean wasUserDetected() {
            return this.mLastAcquire != 11;
        }

        @Override // com.android.server.biometrics.AuthenticationClient, com.android.server.biometrics.ClientMonitor
        public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated, ArrayList<Byte> token) {
            return super.onAuthenticated(identifier, authenticated, token) || !authenticated;
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public int[] getAcquireIgnorelist() {
            if (isBiometricPrompt()) {
                return FaceService.this.mBiometricPromptIgnoreList;
            }
            return FaceService.this.mKeyguardIgnoreList;
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public int[] getAcquireVendorIgnorelist() {
            if (isBiometricPrompt()) {
                return FaceService.this.mBiometricPromptIgnoreListVendor;
            }
            return FaceService.this.mKeyguardIgnoreListVendor;
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public boolean onAcquired(int acquireInfo, int vendorCode) {
            this.mLastAcquire = acquireInfo;
            if (acquireInfo == 13) {
                String name = getContext().getString(17040131);
                String title = getContext().getString(17040132);
                String content = getContext().getString(17040130);
                Intent intent = new Intent("android.settings.FACE_SETTINGS");
                intent.setPackage("com.android.settings");
                PendingIntent pendingIntent = PendingIntent.getActivityAsUser(getContext(), 0, intent, 0, null, UserHandle.CURRENT);
                NotificationManager nm = (NotificationManager) getContext().getSystemService(NotificationManager.class);
                NotificationChannel channel = new NotificationChannel(FaceService.TAG, name, 4);
                Notification notification = new Notification.Builder(getContext(), FaceService.TAG).setSmallIcon(17302459).setContentTitle(title).setContentText(content).setSubText(name).setOnlyAlertOnce(true).setLocalOnly(true).setAutoCancel(true).setCategory("sys").setContentIntent(pendingIntent).build();
                nm.createNotificationChannel(channel);
                nm.notifyAsUser(null, 0, notification, UserHandle.CURRENT);
            }
            return super.onAcquired(acquireInfo, vendorCode);
        }
    }

    /* access modifiers changed from: private */
    public final class FaceServiceWrapper extends IFaceService.Stub {
        private FaceServiceWrapper() {
        }

        public long generateChallenge(IBinder token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            return FaceService.this.startGenerateChallenge(token);
        }

        public int revokeChallenge(IBinder token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            return FaceService.this.startRevokeChallenge(token);
        }

        public void enroll(IBinder token, byte[] cryptoToken, IFaceServiceReceiver receiver, String opPackageName, int[] disabledFeatures) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            BiometricServiceBase.EnrollClientImpl client = new BiometricServiceBase.EnrollClientImpl(FaceService.this.getContext(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), FaceService.this.mCurrentUserId, 0, cryptoToken, FaceService.this.isRestricted(), opPackageName, disabledFeatures) {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass1 */

                @Override // com.android.server.biometrics.ClientMonitor
                public int[] getAcquireIgnorelist() {
                    return FaceService.this.mEnrollIgnoreList;
                }

                @Override // com.android.server.biometrics.ClientMonitor
                public int[] getAcquireVendorIgnorelist() {
                    return FaceService.this.mEnrollIgnoreListVendor;
                }

                @Override // com.android.server.biometrics.EnrollClient
                public boolean shouldVibrate() {
                    return false;
                }

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FaceService.this.statsModality();
                }
            };
            FaceService faceService = FaceService.this;
            faceService.enrollInternal(client, faceService.mCurrentUserId);
        }

        public void cancelEnrollment(IBinder token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.cancelEnrollmentInternal(token);
        }

        public void authenticate(IBinder token, long opId, int userId, IFaceServiceReceiver receiver, int flags, String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.this.updateActiveGroup(userId, opPackageName);
            boolean restricted = FaceService.this.isRestricted();
            FaceService faceService = FaceService.this;
            FaceService.this.authenticateInternal(new FaceAuthClient(faceService.getContext(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), FaceService.this.mCurrentUserId, 0, opId, restricted, opPackageName, 0, false), opId, opPackageName);
        }

        public void prepareForAuthentication(boolean requireConfirmation, IBinder token, long opId, int groupId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.this.updateActiveGroup(groupId, opPackageName);
            FaceService faceService = FaceService.this;
            FaceService.this.authenticateInternal(new FaceAuthClient(faceService.getContext(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new BiometricPromptServiceListenerImpl(wrapperReceiver), FaceService.this.mCurrentUserId, 0, opId, true, opPackageName, cookie, requireConfirmation), opId, opPackageName, callingUid, callingPid, callingUserId);
        }

        public void startPreparedClient(int cookie) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.startCurrentClient(cookie);
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.this.cancelAuthenticationInternal(token, opPackageName);
        }

        public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.this.cancelAuthenticationInternal(token, opPackageName, callingUid, callingPid, callingUserId, fromClient);
        }

        public void setActiveUser(int userId) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.setActiveUserInternal(userId);
        }

        public void remove(IBinder token, int faceId, int userId, IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (token == null) {
                Slog.w(FaceService.TAG, "remove(): token is null");
                return;
            }
            FaceService.this.removeInternal(new RemovalClient(FaceService.this.getContext(), FaceService.this.getConstants(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), faceId, 0, userId, FaceService.this.isRestricted(), token.toString(), FaceService.this.getBiometricUtils()) {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass2 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FaceService.this.statsModality();
                }
            });
        }

        public void enumerate(IBinder token, int userId, IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.enumerateInternal(new EnumerateClient(FaceService.this.getContext(), FaceService.this.getConstants(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), userId, userId, FaceService.this.isRestricted(), FaceService.this.getContext().getOpPackageName()) {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass3 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FaceService.this.statsModality();
                }
            });
        }

        public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.super.addLockoutResetCallback(callback);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(FaceService.this.getContext(), FaceService.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (args.length > 1 && "--hal".equals(args[0])) {
                        FaceService.this.dumpHal(fd, (String[]) Arrays.copyOfRange(args, 1, args.length, args.getClass()));
                    } else if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                        FaceService.this.dumpInternal(pw);
                    } else {
                        FaceService.this.dumpProto(fd);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            boolean z = false;
            if (!FaceService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                if (!(FaceService.this.getFaceDaemon() == null || FaceService.this.mHalDeviceId == 0)) {
                    z = true;
                }
                return z;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void rename(final int faceId, final String name) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (FaceService.this.isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
                FaceService.this.mHandler.post(new Runnable() {
                    /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        FaceService.this.getBiometricUtils().renameBiometricForUser(FaceService.this.getContext(), FaceService.this.mCurrentUserId, faceId, name);
                    }
                });
            }
        }

        public List<Face> getEnrolledFaces(int userId, String opPackageName) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (!FaceService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return null;
            }
            return FaceService.this.getEnrolledTemplates(userId);
        }

        public boolean hasEnrolledFaces(int userId, String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            if (!FaceService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            return FaceService.this.hasEnrolledBiometrics(userId);
        }

        public long getAuthenticatorId(String opPackageName) {
            return FaceService.this.getAuthenticatorId(opPackageName);
        }

        public void resetLockout(byte[] token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService faceService = FaceService.this;
            if (!faceService.hasEnrolledBiometrics(faceService.mCurrentUserId)) {
                Slog.w(FaceService.TAG, "Ignoring lockout reset, no templates enrolled");
                return;
            }
            try {
                FaceService.this.mDaemonWrapper.resetLockout(token);
            } catch (RemoteException e) {
                Slog.e(FaceService.this.getTag(), "Unable to reset lockout", e);
            }
        }

        public void setFeature(int feature, boolean enabled, byte[] token, IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.mHandler.post(new Runnable(feature, token, enabled, receiver) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$90SfiQt6t6j1lelzx7WObEaKw */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ byte[] f$2;
                private final /* synthetic */ boolean f$3;
                private final /* synthetic */ IFaceServiceReceiver f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.FaceServiceWrapper.this.lambda$setFeature$0$FaceService$FaceServiceWrapper(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$setFeature$0$FaceService$FaceServiceWrapper(int feature, byte[] token, boolean enabled, IFaceServiceReceiver receiver) {
            FaceService faceService = FaceService.this;
            if (!faceService.hasEnrolledBiometrics(faceService.mCurrentUserId)) {
                Slog.e(FaceService.TAG, "No enrolled biometrics while setting feature: " + feature);
                return;
            }
            ArrayList<Byte> byteToken = new ArrayList<>();
            for (byte b : token) {
                byteToken.add(Byte.valueOf(b));
            }
            int faceId = getFirstTemplateForUser(FaceService.this.mCurrentUserId);
            if (FaceService.this.mDaemon != null) {
                try {
                    receiver.onFeatureSet(FaceService.this.mDaemon.setFeature(feature, enabled, byteToken, faceId) == 0, feature);
                } catch (RemoteException e) {
                    Slog.e(FaceService.this.getTag(), "Unable to set feature: " + feature + " to enabled:" + enabled, e);
                }
            }
        }

        public void getFeature(int feature, IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.mHandler.post(new Runnable(feature, receiver) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$bvJGfJiSrf8aMe_VJi9dsWabykQ */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ IFaceServiceReceiver f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FaceService.FaceServiceWrapper.this.lambda$getFeature$1$FaceService$FaceServiceWrapper(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$getFeature$1$FaceService$FaceServiceWrapper(int feature, IFaceServiceReceiver receiver) {
            FaceService faceService = FaceService.this;
            if (!faceService.hasEnrolledBiometrics(faceService.mCurrentUserId)) {
                Slog.e(FaceService.TAG, "No enrolled biometrics while getting feature: " + feature);
                return;
            }
            int faceId = getFirstTemplateForUser(FaceService.this.mCurrentUserId);
            if (FaceService.this.mDaemon != null) {
                try {
                    OptionalBool result = FaceService.this.mDaemon.getFeature(feature, faceId);
                    receiver.onFeatureGet(result.status == 0, feature, result.value);
                } catch (RemoteException e) {
                    Slog.e(FaceService.this.getTag(), "Unable to getRequireAttention", e);
                }
            }
        }

        public void userActivity() {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (FaceService.this.mDaemon != null) {
                try {
                    FaceService.this.mDaemon.userActivity();
                } catch (RemoteException e) {
                    Slog.e(FaceService.this.getTag(), "Unable to send userActivity", e);
                }
            }
        }

        private int getFirstTemplateForUser(int user) {
            List<Face> faces = FaceService.this.getEnrolledTemplates(user);
            if (!faces.isEmpty()) {
                return faces.get(0).getBiometricId();
            }
            return 0;
        }
    }

    private class BiometricPromptServiceListenerImpl extends BiometricServiceBase.BiometricServiceListener {
        BiometricPromptServiceListenerImpl(IBiometricServiceReceiverInternal wrapperReceiver) {
            super(wrapperReceiver);
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAcquired(FaceManager.getMappedAcquiredInfo(acquiredInfo, vendorCode), FaceManager.getAcquiredString(FaceService.this.getContext(), acquiredInfo, vendorCode));
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onError(cookie, error, FaceManager.getErrorString(FaceService.this.getContext(), error, vendorCode));
            }
        }
    }

    private class ServiceListenerImpl implements BiometricServiceBase.ServiceListener {
        private IFaceServiceReceiver mFaceServiceReceiver;

        public ServiceListenerImpl(IFaceServiceReceiver receiver) {
            this.mFaceServiceReceiver = receiver;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFaceServiceReceiver iFaceServiceReceiver = this.mFaceServiceReceiver;
            if (iFaceServiceReceiver != null) {
                iFaceServiceReceiver.onEnrollResult(identifier.getDeviceId(), identifier.getBiometricId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            IFaceServiceReceiver iFaceServiceReceiver = this.mFaceServiceReceiver;
            if (iFaceServiceReceiver != null) {
                iFaceServiceReceiver.onAcquired(deviceId, acquiredInfo, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationSucceeded(long deviceId, BiometricAuthenticator.Identifier biometric, int userId) throws RemoteException {
            if (this.mFaceServiceReceiver == null) {
                return;
            }
            if (biometric == null || (biometric instanceof Face)) {
                this.mFaceServiceReceiver.onAuthenticationSucceeded(deviceId, (Face) biometric, userId);
            } else {
                Slog.e(FaceService.TAG, "onAuthenticationSucceeded received non-face biometric");
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationFailed(long deviceId) throws RemoteException {
            IFaceServiceReceiver iFaceServiceReceiver = this.mFaceServiceReceiver;
            if (iFaceServiceReceiver != null) {
                iFaceServiceReceiver.onAuthenticationFailed(deviceId);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            IFaceServiceReceiver iFaceServiceReceiver = this.mFaceServiceReceiver;
            if (iFaceServiceReceiver != null) {
                iFaceServiceReceiver.onError(deviceId, error, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFaceServiceReceiver iFaceServiceReceiver = this.mFaceServiceReceiver;
            if (iFaceServiceReceiver != null) {
                iFaceServiceReceiver.onRemoved(identifier.getDeviceId(), identifier.getBiometricId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnumerated(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFaceServiceReceiver iFaceServiceReceiver = this.mFaceServiceReceiver;
            if (iFaceServiceReceiver != null) {
                iFaceServiceReceiver.onEnumerated(identifier.getDeviceId(), identifier.getBiometricId(), remaining);
            }
        }
    }

    public FaceService(Context context) {
        super(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.biometrics.face.FaceService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.biometrics.face.FaceService$FaceServiceWrapper, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.biometrics.BiometricServiceBase, com.android.server.SystemService
    public void onStart() {
        super.onStart();
        publishBinderService("face", new FaceServiceWrapper());
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.biometrics.face.$$Lambda$FaceService$rveb67MoYJ0egfY6LLl05KvUz8 */

            @Override // java.lang.Runnable
            public final void run() {
                IBiometricsFace unused = FaceService.this.getFaceDaemon();
            }
        }, "FaceService.onStart");
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
        return this.mDaemonWrapper;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricUtils getBiometricUtils() {
        return FaceUtils.getInstance();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public Constants getConstants() {
        return this.mFaceConstants;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasReachedEnrollmentLimit(int userId) {
        if (getEnrolledTemplates(userId).size() < getContext().getResources().getInteger(17694812)) {
            return false;
        }
        Slog.w(TAG, "Too many faces registered, user: " + userId);
        return true;
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public void serviceDied(long cookie) {
        super.serviceDied(cookie);
        this.mDaemon = null;
        this.mCurrentUserId = -10000;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void updateActiveGroup(int userId, String clientPackage) {
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon != null) {
            try {
                int userId2 = getUserOrWorkProfileId(clientPackage, userId);
                if (userId2 != this.mCurrentUserId) {
                    File faceDir = new File(Environment.getDataVendorDeDirectory(userId2), FACE_DATA_DIR);
                    if (!faceDir.exists()) {
                        if (!faceDir.mkdir()) {
                            Slog.v(TAG, "Cannot make directory: " + faceDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(faceDir)) {
                            Slog.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    daemon.setActiveUser(userId2, faceDir.getAbsolutePath());
                    this.mCurrentUserId = userId2;
                }
                this.mAuthenticatorIds.put(Integer.valueOf(userId2), Long.valueOf(hasEnrolledBiometrics(userId2) ? daemon.getAuthenticatorId().value : 0));
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to setActiveUser():", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutResetIntent() {
        return ACTION_LOCKOUT_RESET;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutBroadcastPermission() {
        return "android.permission.RESET_FACE_LOCKOUT";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void handleUserSwitching(int userId) {
        super.handleUserSwitching(userId);
        this.mCurrentUserLockoutMode = 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasEnrolledBiometrics(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        return getBiometricUtils().getBiometricsForUser(getContext(), userId).size() > 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getManageBiometricPermission() {
        return "android.permission.MANAGE_BIOMETRIC";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void checkUseBiometricPermission() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean checkAppOps(int uid, String opPackageName) {
        return this.mAppOps.noteOp(78, uid, opPackageName) == 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public List<Face> getEnrolledTemplates(int userId) {
        return getBiometricUtils().getBiometricsForUser(getContext(), userId);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void notifyClientActiveCallbacks(boolean isActive) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int statsModality() {
        return 4;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int getLockoutMode() {
        return this.mCurrentUserLockoutMode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public synchronized IBiometricsFace getFaceDaemon() {
        if (this.mDaemon == null) {
            Slog.v(TAG, "mDaemon was null, reconnect to face");
            try {
                this.mDaemon = IBiometricsFace.getService();
            } catch (NoSuchElementException e) {
            } catch (RemoteException e2) {
                Slog.e(TAG, "Failed to get biometric interface", e2);
            }
            if (this.mDaemon == null) {
                Slog.w(TAG, "face HIDL not available");
                return null;
            }
            this.mDaemon.asBinder().linkToDeath(this, 0);
            try {
                this.mHalDeviceId = this.mDaemon.setCallback(this.mDaemonCallback).value;
            } catch (RemoteException e3) {
                Slog.e(TAG, "Failed to open face HAL", e3);
                this.mDaemon = null;
            }
            Slog.v(TAG, "Face HAL id: " + this.mHalDeviceId);
            if (this.mHalDeviceId != 0) {
                loadAuthenticatorIds();
                updateActiveGroup(ActivityManager.getCurrentUser(), null);
                doTemplateCleanupForUser(ActivityManager.getCurrentUser());
            } else {
                Slog.w(TAG, "Failed to open Face HAL!");
                MetricsLogger.count(getContext(), "faced_openhal_error", 1);
                this.mDaemon = null;
            }
        }
        return this.mDaemon;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long startGenerateChallenge(IBinder token) {
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startGenerateChallenge: no face HAL!");
            return 0;
        }
        try {
            return daemon.generateChallenge(600).value;
        } catch (RemoteException e) {
            Slog.e(TAG, "startGenerateChallenge failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startRevokeChallenge(IBinder token) {
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startRevokeChallenge: no face HAL!");
            return 0;
        }
        try {
            return daemon.revokeChallenge();
        } catch (RemoteException e) {
            Slog.e(TAG, "startRevokeChallenge failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Face Manager");
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int userId = user.getUserHandle().getIdentifier();
                int N = getBiometricUtils().getBiometricsForUser(getContext(), userId).size();
                BiometricServiceBase.PerformanceStats stats = (BiometricServiceBase.PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
                BiometricServiceBase.PerformanceStats cryptoStats = (BiometricServiceBase.PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
                JSONObject set = new JSONObject();
                set.put("id", userId);
                set.put(AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, N);
                set.put("accept", stats != null ? stats.accept : 0);
                set.put("reject", stats != null ? stats.reject : 0);
                set.put("acquire", stats != null ? stats.acquire : 0);
                set.put("lockout", stats != null ? stats.lockout : 0);
                set.put("permanentLockout", stats != null ? stats.permanentLockout : 0);
                set.put("acceptCrypto", cryptoStats != null ? cryptoStats.accept : 0);
                set.put("rejectCrypto", cryptoStats != null ? cryptoStats.reject : 0);
                set.put("acquireCrypto", cryptoStats != null ? cryptoStats.acquire : 0);
                set.put("lockoutCrypto", cryptoStats != null ? cryptoStats.lockout : 0);
                set.put("permanentLockoutCrypto", cryptoStats != null ? cryptoStats.permanentLockout : 0);
                sets.put(set);
            }
            dump.put("prints", sets);
        } catch (JSONException e) {
            Slog.e(TAG, "dump formatting failure", e);
        }
        pw.println(dump);
        pw.println("HAL Deaths: " + this.mHALDeathCount);
        this.mHALDeathCount = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            long userToken = proto.start(2246267895809L);
            proto.write(1120986464257L, userId);
            proto.write(1120986464258L, getBiometricUtils().getBiometricsForUser(getContext(), userId).size());
            BiometricServiceBase.PerformanceStats normal = (BiometricServiceBase.PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
            if (normal != null) {
                long countsToken = proto.start(1146756268035L);
                proto.write(1120986464257L, normal.accept);
                proto.write(1120986464258L, normal.reject);
                proto.write(1120986464259L, normal.acquire);
                proto.write(1120986464260L, normal.lockout);
                proto.write(1120986464261L, normal.lockout);
                proto.end(countsToken);
            }
            BiometricServiceBase.PerformanceStats crypto = (BiometricServiceBase.PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
            if (crypto != null) {
                long countsToken2 = proto.start(1146756268036L);
                proto.write(1120986464257L, crypto.accept);
                proto.write(1120986464258L, crypto.reject);
                proto.write(1120986464259L, crypto.acquire);
                proto.write(1120986464260L, crypto.lockout);
                proto.write(1120986464261L, crypto.lockout);
                proto.end(countsToken2);
            }
            proto.end(userToken);
        }
        proto.flush();
        this.mPerformanceMap.clear();
        this.mCryptoPerformanceMap.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpHal(FileDescriptor fd, String[] args) {
        IBiometricsFace daemon;
        if ((Build.IS_ENG || Build.IS_USERDEBUG) && !SystemProperties.getBoolean("ro.face.disable_debug_data", false) && !SystemProperties.getBoolean("persist.face.disable_debug_data", false) && (daemon = getFaceDaemon()) != null) {
            FileOutputStream devnull = null;
            try {
                devnull = new FileOutputStream("/dev/null");
                daemon.debug(new NativeHandle(new FileDescriptor[]{devnull.getFD(), fd}, new int[0], false), new ArrayList<>(Arrays.asList(args)));
                try {
                    devnull.close();
                } catch (IOException e) {
                }
            } catch (RemoteException | IOException ex) {
                Slog.d(TAG, "error while reading face debugging data", ex);
                if (devnull != null) {
                    devnull.close();
                }
            } catch (Throwable th) {
                if (devnull != null) {
                    try {
                        devnull.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        }
    }
}
