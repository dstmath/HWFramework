package android.permission;

import android.Manifest;
import android.annotation.SystemApi;
import android.app.ActivityThread;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.permission.IPermissionController;
import android.permission.PermissionControllerManager;
import android.telecom.ParcelableCallAnalytics;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.infra.AbstractMultiplePendingRequestsRemoteService;
import com.android.internal.infra.AbstractRemoteService;
import com.android.internal.util.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import libcore.io.IoUtils;

@SystemApi
public final class PermissionControllerManager {
    public static final int COUNT_ONLY_WHEN_GRANTED = 1;
    public static final int COUNT_WHEN_SYSTEM = 2;
    public static final String KEY_RESULT = "android.permission.PermissionControllerManager.key.result";
    public static final int REASON_INSTALLER_POLICY_VIOLATION = 2;
    public static final int REASON_MALWARE = 1;
    private static final String TAG = PermissionControllerManager.class.getSimpleName();
    private static final Object sLock = new Object();
    @GuardedBy({"sLock"})
    private static ArrayMap<Pair<Integer, Thread>, RemoteService> sRemoteServices = new ArrayMap<>(1);
    private final Context mContext;
    private final RemoteService mRemoteService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CountPermissionAppsFlag {
    }

    public interface OnCountPermissionAppsResultCallback {
        void onCountPermissionApps(int i);
    }

    public interface OnGetAppPermissionResultCallback {
        void onGetAppPermissions(List<RuntimePermissionPresentationInfo> list);
    }

    public interface OnGetRuntimePermissionBackupCallback {
        void onGetRuntimePermissionsBackup(byte[] bArr);
    }

    public interface OnPermissionUsageResultCallback {
        void onPermissionUsageResult(List<RuntimePermissionUsageInfo> list);
    }

    public static abstract class OnRevokeRuntimePermissionsCallback {
        public abstract void onRevokeRuntimePermissions(Map<String, List<String>> map);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Reason {
    }

    public PermissionControllerManager(Context context, Handler handler) {
        synchronized (sLock) {
            Pair<Integer, Thread> key = new Pair<>(Integer.valueOf(context.getUserId()), handler.getLooper().getThread());
            RemoteService remoteService = sRemoteServices.get(key);
            if (remoteService == null) {
                Intent intent = new Intent(PermissionControllerService.SERVICE_INTERFACE);
                intent.setPackage(context.getPackageManager().getPermissionControllerPackageName());
                remoteService = new RemoteService(ActivityThread.currentApplication(), context.getPackageManager().resolveService(intent, 0).getComponentInfo().getComponentName(), handler, context.getUser());
                sRemoteServices.put(key, remoteService);
            }
            this.mRemoteService = remoteService;
        }
        this.mContext = context;
    }

    public void revokeRuntimePermissions(Map<String, List<String>> request, boolean doDryRun, int reason, Executor executor, OnRevokeRuntimePermissionsCallback callback) {
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(callback);
        Preconditions.checkNotNull(request);
        for (Map.Entry<String, List<String>> appRequest : request.entrySet()) {
            Preconditions.checkNotNull(appRequest.getKey());
            Preconditions.checkCollectionElementsNotNull(appRequest.getValue(), "permissions");
        }
        if (this.mContext.checkSelfPermission(Manifest.permission.REVOKE_RUNTIME_PERMISSIONS) == 0) {
            RemoteService remoteService = this.mRemoteService;
            remoteService.scheduleRequest(new PendingRevokeRuntimePermissionRequest(remoteService, request, doDryRun, reason, this.mContext.getPackageName(), executor, callback));
            return;
        }
        throw new SecurityException("android.permission.REVOKE_RUNTIME_PERMISSIONS required");
    }

    public void setRuntimePermissionGrantStateByDeviceAdmin(String callerPackageName, String packageName, String permission, int grantState, Executor executor, Consumer<Boolean> callback) {
        Preconditions.checkStringNotEmpty(callerPackageName);
        Preconditions.checkStringNotEmpty(packageName);
        Preconditions.checkStringNotEmpty(permission);
        boolean z = true;
        if (!(grantState == 1 || grantState == 2 || grantState == 0)) {
            z = false;
        }
        Preconditions.checkArgument(z);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(callback);
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleRequest(new PendingSetRuntimePermissionGrantStateByDeviceAdmin(remoteService, callerPackageName, packageName, permission, grantState, executor, callback));
    }

    public void getRuntimePermissionBackup(UserHandle user, Executor executor, OnGetRuntimePermissionBackupCallback callback) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(callback);
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleRequest(new PendingGetRuntimePermissionBackup(remoteService, user, executor, callback));
    }

    public void restoreRuntimePermissionBackup(byte[] backup, UserHandle user) {
        Preconditions.checkNotNull(backup);
        Preconditions.checkNotNull(user);
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleAsyncRequest(new PendingRestoreRuntimePermissionBackup(remoteService, backup, user));
    }

    public void restoreDelayedRuntimePermissionBackup(String packageName, UserHandle user, Executor executor, Consumer<Boolean> callback) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(callback);
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleRequest(new PendingRestoreDelayedRuntimePermissionBackup(remoteService, packageName, user, executor, callback));
    }

    public void getAppPermissions(String packageName, OnGetAppPermissionResultCallback callback, Handler handler) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(callback);
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleRequest(new PendingGetAppPermissionRequest(remoteService, packageName, callback, handler == null ? remoteService.getHandler() : handler));
    }

    public void revokeRuntimePermission(String packageName, String permissionName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(permissionName);
        this.mRemoteService.scheduleAsyncRequest(new PendingRevokeAppPermissionRequest(packageName, permissionName));
    }

    public void countPermissionApps(List<String> permissionNames, int flags, OnCountPermissionAppsResultCallback callback, Handler handler) {
        Preconditions.checkCollectionElementsNotNull(permissionNames, "permissionNames");
        Preconditions.checkFlagsArgument(flags, 3);
        Preconditions.checkNotNull(callback);
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleRequest(new PendingCountPermissionAppsRequest(remoteService, permissionNames, flags, callback, handler == null ? remoteService.getHandler() : handler));
    }

    public void getPermissionUsages(boolean countSystem, long numMillis, Executor executor, OnPermissionUsageResultCallback callback) {
        Preconditions.checkArgumentNonnegative(numMillis);
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(callback);
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleRequest(new PendingGetPermissionUsagesRequest(remoteService, countSystem, numMillis, executor, callback));
    }

    public void grantOrUpgradeDefaultRuntimePermissions(Executor executor, Consumer<Boolean> callback) {
        RemoteService remoteService = this.mRemoteService;
        remoteService.scheduleRequest(new PendingGrantOrUpgradeDefaultRuntimePermissionsRequest(remoteService, executor, callback));
    }

    static final class RemoteService extends AbstractMultiplePendingRequestsRemoteService<RemoteService, IPermissionController> {
        private static final boolean IS_FPGA = SystemProperties.get("ro.board.boardname", WifiEnterpriseConfig.ENGINE_DISABLE).contains("fpga");
        private static final long MESSAGE_TIMEOUT_MILLIS = (IS_FPGA ? ParcelableCallAnalytics.MILLIS_IN_5_MINUTES : JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);
        private static final long UNBIND_TIMEOUT_MILLIS = (IS_FPGA ? 100000 : JobInfo.MIN_BACKOFF_MILLIS);

        RemoteService(Context context, ComponentName componentName, Handler handler, UserHandle user) {
            super(context, PermissionControllerService.SERVICE_INTERFACE, componentName, user.getIdentifier(), $$Lambda$PermissionControllerManager$RemoteService$L8NTbqIPWKu7tyiOxbu_00YKss.INSTANCE, handler, 0, false, 1);
        }

        static /* synthetic */ void lambda$new$0(RemoteService service) {
            String str = PermissionControllerManager.TAG;
            Log.e(str, "RemoteService " + service + " died");
        }

        /* access modifiers changed from: package-private */
        public Handler getHandler() {
            return this.mHandler;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.infra.AbstractRemoteService
        public IPermissionController getServiceInterface(IBinder binder) {
            return IPermissionController.Stub.asInterface(binder);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.infra.AbstractRemoteService
        public long getTimeoutIdleBindMillis() {
            return UNBIND_TIMEOUT_MILLIS;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.infra.AbstractRemoteService
        public long getRemoteRequestMillis() {
            return MESSAGE_TIMEOUT_MILLIS;
        }

        @Override // com.android.internal.infra.AbstractRemoteService
        public void scheduleRequest(AbstractRemoteService.BasePendingRequest<RemoteService, IPermissionController> pendingRequest) {
            super.scheduleRequest(pendingRequest);
        }

        @Override // com.android.internal.infra.AbstractRemoteService
        public void scheduleAsyncRequest(AbstractRemoteService.AsyncRequest<IPermissionController> request) {
            super.scheduleAsyncRequest(request);
        }
    }

    /* access modifiers changed from: private */
    public static class FileReaderTask<Callback extends Consumer<byte[]>> extends AsyncTask<Void, Void, byte[]> {
        private final Callback mCallback;
        private ParcelFileDescriptor mLocalPipe;
        private ParcelFileDescriptor mRemotePipe;

        FileReaderTask(Callback callback) {
            this.mCallback = callback;
        }

        /* access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPreExecute() {
            try {
                ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
                this.mLocalPipe = pipe[0];
                this.mRemotePipe = pipe[1];
            } catch (IOException e) {
                Log.e(PermissionControllerManager.TAG, "Could not create pipe needed to get runtime permission backup", e);
            }
        }

        /* access modifiers changed from: package-private */
        public ParcelFileDescriptor getRemotePipe() {
            return this.mRemotePipe;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x002e, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x002f, code lost:
            r2.addSuppressed(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0032, code lost:
            throw r3;
         */
        public byte[] doInBackground(Void... ignored) {
            ByteArrayOutputStream combinedBuffer = new ByteArrayOutputStream();
            InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(this.mLocalPipe);
            byte[] buffer = new byte[16384];
            while (true) {
                if (!isCancelled()) {
                    int numRead = in.read(buffer);
                    if (numRead == -1) {
                        break;
                    }
                    combinedBuffer.write(buffer, 0, numRead);
                }
            }
            try {
                in.close();
            } catch (IOException | NullPointerException e) {
                Log.e(PermissionControllerManager.TAG, "Error reading runtime permission backup", e);
                combinedBuffer.reset();
            }
            return combinedBuffer.toByteArray();
        }

        /* access modifiers changed from: package-private */
        public void interruptRead() {
            IoUtils.closeQuietly(this.mLocalPipe);
        }

        /* access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled() {
            onPostExecute(new byte[0]);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(byte[] backup) {
            IoUtils.closeQuietly(this.mLocalPipe);
            this.mCallback.accept(backup);
        }
    }

    /* access modifiers changed from: private */
    public static class FileWriterTask extends AsyncTask<byte[], Void, Void> {
        private static final int CHUNK_SIZE = 4096;
        private ParcelFileDescriptor mLocalPipe;
        private ParcelFileDescriptor mRemotePipe;

        private FileWriterTask() {
        }

        /* access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPreExecute() {
            try {
                ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
                this.mRemotePipe = pipe[0];
                this.mLocalPipe = pipe[1];
            } catch (IOException e) {
                Log.e(PermissionControllerManager.TAG, "Could not create pipe needed to send runtime permission backup", e);
            }
        }

        /* access modifiers changed from: package-private */
        public ParcelFileDescriptor getRemotePipe() {
            return this.mRemotePipe;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
            r0.addSuppressed(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
            throw r3;
         */
        public Void doInBackground(byte[]... in) {
            byte[] buffer = in[0];
            OutputStream out = new ParcelFileDescriptor.AutoCloseOutputStream(this.mLocalPipe);
            for (int offset = 0; offset < buffer.length; offset += 4096) {
                out.write(buffer, offset, Math.min(4096, buffer.length - offset));
            }
            try {
                out.close();
                return null;
            } catch (IOException | NullPointerException e) {
                Log.e(PermissionControllerManager.TAG, "Error sending runtime permission backup", e);
                return null;
            }
        }

        /* access modifiers changed from: package-private */
        public void interruptWrite() {
            IoUtils.closeQuietly(this.mLocalPipe);
        }

        /* access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled() {
            onPostExecute((Void) null);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void ignored) {
            IoUtils.closeQuietly(this.mLocalPipe);
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingRevokeRuntimePermissionRequest extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> {
        private final OnRevokeRuntimePermissionsCallback mCallback;
        private final String mCallingPackage;
        private final boolean mDoDryRun;
        private final Executor mExecutor;
        private final int mReason;
        private final RemoteCallback mRemoteCallback;
        private final Map<String, List<String>> mRequest;

        private PendingRevokeRuntimePermissionRequest(RemoteService service, Map<String, List<String>> request, boolean doDryRun, int reason, String callingPackage, Executor executor, OnRevokeRuntimePermissionsCallback callback) {
            super(service);
            this.mRequest = request;
            this.mDoDryRun = doDryRun;
            this.mReason = reason;
            this.mCallingPackage = callingPackage;
            this.mExecutor = executor;
            this.mCallback = callback;
            this.mRemoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(executor, callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingRevokeRuntimePermissionRequest$StUWUj0fmNRuCwuUzh3M5C7e_o0 */
                private final /* synthetic */ Executor f$1;
                private final /* synthetic */ PermissionControllerManager.OnRevokeRuntimePermissionsCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    PermissionControllerManager.PendingRevokeRuntimePermissionRequest.this.lambda$new$1$PermissionControllerManager$PendingRevokeRuntimePermissionRequest(this.f$1, this.f$2, bundle);
                }
            }, null);
        }

        public /* synthetic */ void lambda$new$1$PermissionControllerManager$PendingRevokeRuntimePermissionRequest(Executor executor, OnRevokeRuntimePermissionsCallback callback, Bundle result) {
            executor.execute(new Runnable(result, callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingRevokeRuntimePermissionRequest$RY69_9rYfdoaXdLj_Ux62tZUXg */
                private final /* synthetic */ Bundle f$1;
                private final /* synthetic */ PermissionControllerManager.OnRevokeRuntimePermissionsCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PermissionControllerManager.PendingRevokeRuntimePermissionRequest.this.lambda$new$0$PermissionControllerManager$PendingRevokeRuntimePermissionRequest(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PermissionControllerManager$PendingRevokeRuntimePermissionRequest(Bundle result, OnRevokeRuntimePermissionsCallback callback) {
            long token = Binder.clearCallingIdentity();
            try {
                Map<String, List<String>> revoked = new ArrayMap<>();
                try {
                    Bundle bundleizedRevoked = result.getBundle(PermissionControllerManager.KEY_RESULT);
                    for (String packageName : bundleizedRevoked.keySet()) {
                        Preconditions.checkNotNull(packageName);
                        ArrayList<String> permissions = bundleizedRevoked.getStringArrayList(packageName);
                        Preconditions.checkCollectionElementsNotNull(permissions, "permissions");
                        revoked.put(packageName, permissions);
                    }
                } catch (Exception e) {
                    Log.e(PermissionControllerManager.TAG, "Could not read result when revoking runtime permissions", e);
                }
                callback.onRevokeRuntimePermissions(revoked);
            } finally {
                Binder.restoreCallingIdentity(token);
                finish();
            }
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.permission.$$Lambda$PermissionControllerManager$PendingRevokeRuntimePermissionRequest$HQXgA6xx0k7jv6y22RQn3Fx34QQ */

                    @Override // java.lang.Runnable
                    public final void run() {
                        PermissionControllerManager.PendingRevokeRuntimePermissionRequest.this.lambda$onTimeout$2$PermissionControllerManager$PendingRevokeRuntimePermissionRequest();
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public /* synthetic */ void lambda$onTimeout$2$PermissionControllerManager$PendingRevokeRuntimePermissionRequest() {
            this.mCallback.onRevokeRuntimePermissions(Collections.emptyMap());
        }

        @Override // java.lang.Runnable
        public void run() {
            Bundle bundledizedRequest = new Bundle();
            for (Map.Entry<String, List<String>> appRequest : this.mRequest.entrySet()) {
                bundledizedRequest.putStringArrayList(appRequest.getKey(), new ArrayList<>(appRequest.getValue()));
            }
            try {
                ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).revokeRuntimePermissions(bundledizedRequest, this.mDoDryRun, this.mReason, this.mCallingPackage, this.mRemoteCallback);
            } catch (RemoteException e) {
                Log.e(PermissionControllerManager.TAG, "Error revoking runtime permission", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingGetRuntimePermissionBackup extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> implements Consumer<byte[]> {
        private final FileReaderTask<PendingGetRuntimePermissionBackup> mBackupReader;
        private final OnGetRuntimePermissionBackupCallback mCallback;
        private final Executor mExecutor;
        private final UserHandle mUser;

        private PendingGetRuntimePermissionBackup(RemoteService service, UserHandle user, Executor executor, OnGetRuntimePermissionBackupCallback callback) {
            super(service);
            this.mUser = user;
            this.mExecutor = executor;
            this.mCallback = callback;
            this.mBackupReader = new FileReaderTask<>(this);
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            this.mBackupReader.cancel(true);
            this.mBackupReader.interruptRead();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mBackupReader.getStatus() == AsyncTask.Status.PENDING) {
                this.mBackupReader.execute(new Void[0]);
                ParcelFileDescriptor remotePipe = this.mBackupReader.getRemotePipe();
                try {
                    ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).getRuntimePermissionBackup(this.mUser, remotePipe);
                } catch (RemoteException e) {
                    Log.e(PermissionControllerManager.TAG, "Error getting runtime permission backup", e);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(remotePipe);
                    throw th;
                }
                IoUtils.closeQuietly(remotePipe);
            }
        }

        /* JADX INFO: finally extract failed */
        public void accept(byte[] backup) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(backup) {
                    /* class android.permission.$$Lambda$PermissionControllerManager$PendingGetRuntimePermissionBackup$TnLX6gxZCMF3D0czwj_XwNhPIgE */
                    private final /* synthetic */ byte[] f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        PermissionControllerManager.PendingGetRuntimePermissionBackup.this.lambda$accept$0$PermissionControllerManager$PendingGetRuntimePermissionBackup(this.f$1);
                    }
                });
                Binder.restoreCallingIdentity(token);
                finish();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public /* synthetic */ void lambda$accept$0$PermissionControllerManager$PendingGetRuntimePermissionBackup(byte[] backup) {
            this.mCallback.onGetRuntimePermissionsBackup(backup);
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingSetRuntimePermissionGrantStateByDeviceAdmin extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> {
        private final Consumer<Boolean> mCallback;
        private final String mCallerPackageName;
        private final Executor mExecutor;
        private final int mGrantState;
        private final String mPackageName;
        private final String mPermission;
        private final RemoteCallback mRemoteCallback;

        private PendingSetRuntimePermissionGrantStateByDeviceAdmin(RemoteService service, String callerPackageName, String packageName, String permission, int grantState, Executor executor, Consumer<Boolean> callback) {
            super(service);
            this.mCallerPackageName = callerPackageName;
            this.mPackageName = packageName;
            this.mPermission = permission;
            this.mGrantState = grantState;
            this.mExecutor = executor;
            this.mCallback = callback;
            this.mRemoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(executor, callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin$9CrKvc4Mj43M641VzAbk1z_vjck */
                private final /* synthetic */ Executor f$1;
                private final /* synthetic */ Consumer f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    PermissionControllerManager.PendingSetRuntimePermissionGrantStateByDeviceAdmin.this.lambda$new$1$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin(this.f$1, this.f$2, bundle);
                }
            }, null);
        }

        public /* synthetic */ void lambda$new$1$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin(Executor executor, Consumer callback, Bundle result) {
            executor.execute(new Runnable(callback, result) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin$L3EtiNpasfEGfE2sSUKhkdYUg */
                private final /* synthetic */ Consumer f$1;
                private final /* synthetic */ Bundle f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PermissionControllerManager.PendingSetRuntimePermissionGrantStateByDeviceAdmin.this.lambda$new$0$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin(Consumer callback, Bundle result) {
            long token = Binder.clearCallingIdentity();
            try {
                callback.accept(Boolean.valueOf(result.getBoolean(PermissionControllerManager.KEY_RESULT, false)));
            } finally {
                Binder.restoreCallingIdentity(token);
                finish();
            }
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.permission.$$Lambda$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin$cgbsG1socgf6wsJmCUAPmhjKmw */

                    @Override // java.lang.Runnable
                    public final void run() {
                        PermissionControllerManager.PendingSetRuntimePermissionGrantStateByDeviceAdmin.this.lambda$onTimeout$2$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin();
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public /* synthetic */ void lambda$onTimeout$2$PermissionControllerManager$PendingSetRuntimePermissionGrantStateByDeviceAdmin() {
            this.mCallback.accept(false);
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).setRuntimePermissionGrantStateByDeviceAdmin(this.mCallerPackageName, this.mPackageName, this.mPermission, this.mGrantState, this.mRemoteCallback);
            } catch (RemoteException e) {
                String str = PermissionControllerManager.TAG;
                Log.e(str, "Error setting permissions state for device admin " + this.mPackageName, e);
            }
        }
    }

    private static final class PendingRestoreRuntimePermissionBackup implements AbstractRemoteService.AsyncRequest<IPermissionController> {
        private final byte[] mBackup;
        private final FileWriterTask mBackupSender;
        private final UserHandle mUser;

        private PendingRestoreRuntimePermissionBackup(RemoteService service, byte[] backup, UserHandle user) {
            this.mBackup = backup;
            this.mUser = user;
            this.mBackupSender = new FileWriterTask();
        }

        public void run(IPermissionController service) {
            if (this.mBackupSender.getStatus() == AsyncTask.Status.PENDING) {
                this.mBackupSender.execute(this.mBackup);
                ParcelFileDescriptor remotePipe = this.mBackupSender.getRemotePipe();
                try {
                    service.restoreRuntimePermissionBackup(this.mUser, remotePipe);
                } catch (RemoteException e) {
                    Log.e(PermissionControllerManager.TAG, "Error sending runtime permission backup", e);
                    this.mBackupSender.cancel(false);
                    this.mBackupSender.interruptWrite();
                } catch (Throwable th) {
                    IoUtils.closeQuietly(remotePipe);
                    throw th;
                }
                IoUtils.closeQuietly(remotePipe);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingRestoreDelayedRuntimePermissionBackup extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> {
        private final Consumer<Boolean> mCallback;
        private final Executor mExecutor;
        private final String mPackageName;
        private final RemoteCallback mRemoteCallback;
        private final UserHandle mUser;

        private PendingRestoreDelayedRuntimePermissionBackup(RemoteService service, String packageName, UserHandle user, Executor executor, Consumer<Boolean> callback) {
            super(service);
            this.mPackageName = packageName;
            this.mUser = user;
            this.mExecutor = executor;
            this.mCallback = callback;
            this.mRemoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(executor, callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup$S_BIiPaqfMH7CNqPH_RO6xHRCeQ */
                private final /* synthetic */ Executor f$1;
                private final /* synthetic */ Consumer f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    PermissionControllerManager.PendingRestoreDelayedRuntimePermissionBackup.this.lambda$new$1$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup(this.f$1, this.f$2, bundle);
                }
            }, null);
        }

        public /* synthetic */ void lambda$new$1$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup(Executor executor, Consumer callback, Bundle result) {
            executor.execute(new Runnable(callback, result) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup$ZGmiW2RcTI6YZLE1JgWr0ufJGk */
                private final /* synthetic */ Consumer f$1;
                private final /* synthetic */ Bundle f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PermissionControllerManager.PendingRestoreDelayedRuntimePermissionBackup.this.lambda$new$0$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup(Consumer callback, Bundle result) {
            long token = Binder.clearCallingIdentity();
            try {
                callback.accept(Boolean.valueOf(result.getBoolean(PermissionControllerManager.KEY_RESULT, false)));
            } finally {
                Binder.restoreCallingIdentity(token);
                finish();
            }
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.permission.$$Lambda$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup$eZmglu5wkoNFQT0fHebFoNMze8 */

                    @Override // java.lang.Runnable
                    public final void run() {
                        PermissionControllerManager.PendingRestoreDelayedRuntimePermissionBackup.this.lambda$onTimeout$2$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup();
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public /* synthetic */ void lambda$onTimeout$2$PermissionControllerManager$PendingRestoreDelayedRuntimePermissionBackup() {
            this.mCallback.accept(true);
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).restoreDelayedRuntimePermissionBackup(this.mPackageName, this.mUser, this.mRemoteCallback);
            } catch (RemoteException e) {
                String str = PermissionControllerManager.TAG;
                Log.e(str, "Error restoring delayed permissions for " + this.mPackageName, e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingGetAppPermissionRequest extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> {
        private final OnGetAppPermissionResultCallback mCallback;
        private final String mPackageName;
        private final RemoteCallback mRemoteCallback;

        private PendingGetAppPermissionRequest(RemoteService service, String packageName, OnGetAppPermissionResultCallback callback, Handler handler) {
            super(service);
            this.mPackageName = packageName;
            this.mCallback = callback;
            this.mRemoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingGetAppPermissionRequest$7R0rGbvqPEHrjxlrMX66LMgfTj4 */
                private final /* synthetic */ PermissionControllerManager.OnGetAppPermissionResultCallback f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    PermissionControllerManager.PendingGetAppPermissionRequest.this.lambda$new$0$PermissionControllerManager$PendingGetAppPermissionRequest(this.f$1, bundle);
                }
            }, handler);
        }

        public /* synthetic */ void lambda$new$0$PermissionControllerManager$PendingGetAppPermissionRequest(OnGetAppPermissionResultCallback callback, Bundle result) {
            List<RuntimePermissionPresentationInfo> permissions = null;
            if (result != null) {
                permissions = result.getParcelableArrayList(PermissionControllerManager.KEY_RESULT);
            }
            if (permissions == null) {
                permissions = Collections.emptyList();
            }
            callback.onGetAppPermissions(permissions);
            finish();
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            this.mCallback.onGetAppPermissions(Collections.emptyList());
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).getAppPermissions(this.mPackageName, this.mRemoteCallback);
            } catch (RemoteException e) {
                Log.e(PermissionControllerManager.TAG, "Error getting app permission", e);
            }
        }
    }

    private static final class PendingRevokeAppPermissionRequest implements AbstractRemoteService.AsyncRequest<IPermissionController> {
        private final String mPackageName;
        private final String mPermissionName;

        private PendingRevokeAppPermissionRequest(String packageName, String permissionName) {
            this.mPackageName = packageName;
            this.mPermissionName = permissionName;
        }

        public void run(IPermissionController remoteInterface) {
            try {
                remoteInterface.revokeRuntimePermission(this.mPackageName, this.mPermissionName);
            } catch (RemoteException e) {
                Log.e(PermissionControllerManager.TAG, "Error revoking app permission", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingCountPermissionAppsRequest extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> {
        private final OnCountPermissionAppsResultCallback mCallback;
        private final int mFlags;
        private final List<String> mPermissionNames;
        private final RemoteCallback mRemoteCallback;

        private PendingCountPermissionAppsRequest(RemoteService service, List<String> permissionNames, int flags, OnCountPermissionAppsResultCallback callback, Handler handler) {
            super(service);
            this.mPermissionNames = permissionNames;
            this.mFlags = flags;
            this.mCallback = callback;
            this.mRemoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingCountPermissionAppsRequest$5yk4p2I96nUHJ1QRErjoF1iiLLY */
                private final /* synthetic */ PermissionControllerManager.OnCountPermissionAppsResultCallback f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    PermissionControllerManager.PendingCountPermissionAppsRequest.this.lambda$new$0$PermissionControllerManager$PendingCountPermissionAppsRequest(this.f$1, bundle);
                }
            }, handler);
        }

        public /* synthetic */ void lambda$new$0$PermissionControllerManager$PendingCountPermissionAppsRequest(OnCountPermissionAppsResultCallback callback, Bundle result) {
            int numApps;
            if (result != null) {
                numApps = result.getInt(PermissionControllerManager.KEY_RESULT);
            } else {
                numApps = 0;
            }
            callback.onCountPermissionApps(numApps);
            finish();
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            this.mCallback.onCountPermissionApps(0);
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).countPermissionApps(this.mPermissionNames, this.mFlags, this.mRemoteCallback);
            } catch (RemoteException e) {
                Log.e(PermissionControllerManager.TAG, "Error counting permission apps", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingGetPermissionUsagesRequest extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> {
        private final OnPermissionUsageResultCallback mCallback;
        private final boolean mCountSystem;
        private final long mNumMillis;
        private final RemoteCallback mRemoteCallback;

        private PendingGetPermissionUsagesRequest(RemoteService service, boolean countSystem, long numMillis, Executor executor, OnPermissionUsageResultCallback callback) {
            super(service);
            this.mCountSystem = countSystem;
            this.mNumMillis = numMillis;
            this.mCallback = callback;
            this.mRemoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(executor, callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingGetPermissionUsagesRequest$M0RAdfneqBIIFQEhfWzd068mi7g */
                private final /* synthetic */ Executor f$1;
                private final /* synthetic */ PermissionControllerManager.OnPermissionUsageResultCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    PermissionControllerManager.PendingGetPermissionUsagesRequest.this.lambda$new$1$PermissionControllerManager$PendingGetPermissionUsagesRequest(this.f$1, this.f$2, bundle);
                }
            }, null);
        }

        public /* synthetic */ void lambda$new$1$PermissionControllerManager$PendingGetPermissionUsagesRequest(Executor executor, OnPermissionUsageResultCallback callback, Bundle result) {
            executor.execute(new Runnable(result, callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingGetPermissionUsagesRequest$WBIc65bpG47GE1DYeIzY6NX7Oyw */
                private final /* synthetic */ Bundle f$1;
                private final /* synthetic */ PermissionControllerManager.OnPermissionUsageResultCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PermissionControllerManager.PendingGetPermissionUsagesRequest.this.lambda$new$0$PermissionControllerManager$PendingGetPermissionUsagesRequest(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PermissionControllerManager$PendingGetPermissionUsagesRequest(Bundle result, OnPermissionUsageResultCallback callback) {
            List<RuntimePermissionUsageInfo> users;
            long token = Binder.clearCallingIdentity();
            if (result != null) {
                try {
                    users = result.getParcelableArrayList(PermissionControllerManager.KEY_RESULT);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    finish();
                    throw th;
                }
            } else {
                users = Collections.emptyList();
            }
            callback.onPermissionUsageResult(users);
            Binder.restoreCallingIdentity(token);
            finish();
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            this.mCallback.onPermissionUsageResult(Collections.emptyList());
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).getPermissionUsages(this.mCountSystem, this.mNumMillis, this.mRemoteCallback);
            } catch (RemoteException e) {
                Log.e(PermissionControllerManager.TAG, "Error counting permission users", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingGrantOrUpgradeDefaultRuntimePermissionsRequest extends AbstractRemoteService.PendingRequest<RemoteService, IPermissionController> {
        private final Consumer<Boolean> mCallback;
        private final RemoteCallback mRemoteCallback;

        private PendingGrantOrUpgradeDefaultRuntimePermissionsRequest(RemoteService service, Executor executor, Consumer<Boolean> callback) {
            super(service);
            this.mCallback = callback;
            this.mRemoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(executor, callback) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingGrantOrUpgradeDefaultRuntimePermissionsRequest$khE8_2qLkPzjjwzPXI9vCg1JiSo */
                private final /* synthetic */ Executor f$1;
                private final /* synthetic */ Consumer f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.os.RemoteCallback.OnResultListener
                public final void onResult(Bundle bundle) {
                    PermissionControllerManager.PendingGrantOrUpgradeDefaultRuntimePermissionsRequest.this.lambda$new$1$PermissionControllerManager$PendingGrantOrUpgradeDefaultRuntimePermissionsRequest(this.f$1, this.f$2, bundle);
                }
            }, null);
        }

        public /* synthetic */ void lambda$new$1$PermissionControllerManager$PendingGrantOrUpgradeDefaultRuntimePermissionsRequest(Executor executor, Consumer callback, Bundle result) {
            executor.execute(new Runnable(callback, result) {
                /* class android.permission.$$Lambda$PermissionControllerManager$PendingGrantOrUpgradeDefaultRuntimePermissionsRequest$LF2T0wqhyO211uMsePvWLLBRNHc */
                private final /* synthetic */ Consumer f$1;
                private final /* synthetic */ Bundle f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PermissionControllerManager.PendingGrantOrUpgradeDefaultRuntimePermissionsRequest.this.lambda$new$0$PermissionControllerManager$PendingGrantOrUpgradeDefaultRuntimePermissionsRequest(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PermissionControllerManager$PendingGrantOrUpgradeDefaultRuntimePermissionsRequest(Consumer callback, Bundle result) {
            long token = Binder.clearCallingIdentity();
            try {
                callback.accept(Boolean.valueOf(result != null));
            } finally {
                Binder.restoreCallingIdentity(token);
                finish();
            }
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteService remoteService) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mCallback.accept(false);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                ((IPermissionController) ((RemoteService) getService()).getServiceInterface()).grantOrUpgradeDefaultRuntimePermissions(this.mRemoteCallback);
            } catch (RemoteException e) {
                Log.e(PermissionControllerManager.TAG, "Error granting or upgrading runtime permissions", e);
            }
        }
    }
}
