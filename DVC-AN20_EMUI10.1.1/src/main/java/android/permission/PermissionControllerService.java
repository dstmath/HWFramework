package android.permission;

import android.Manifest;
import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallback;
import android.os.UserHandle;
import android.permission.IPermissionController;
import android.permission.PermissionControllerService;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

@SystemApi
public abstract class PermissionControllerService extends Service {
    private static final String LOG_TAG = PermissionControllerService.class.getSimpleName();
    public static final String SERVICE_INTERFACE = "android.permission.PermissionControllerService";

    public abstract void onCountPermissionApps(List<String> list, int i, IntConsumer intConsumer);

    public abstract void onGetAppPermissions(String str, Consumer<List<RuntimePermissionPresentationInfo>> consumer);

    public abstract void onGetPermissionUsages(boolean z, long j, Consumer<List<RuntimePermissionUsageInfo>> consumer);

    public abstract void onGetRuntimePermissionsBackup(UserHandle userHandle, OutputStream outputStream, Runnable runnable);

    public abstract void onGrantOrUpgradeDefaultRuntimePermissions(Runnable runnable);

    public abstract void onRestoreDelayedRuntimePermissionsBackup(String str, UserHandle userHandle, Consumer<Boolean> consumer);

    public abstract void onRestoreRuntimePermissionsBackup(UserHandle userHandle, InputStream inputStream, Runnable runnable);

    public abstract void onRevokeRuntimePermission(String str, String str2, Runnable runnable);

    public abstract void onRevokeRuntimePermissions(Map<String, List<String>> map, boolean z, int i, String str, Consumer<Map<String, List<String>>> consumer);

    public abstract void onSetRuntimePermissionGrantStateByDeviceAdmin(String str, String str2, String str3, int i, Consumer<Boolean> consumer);

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new IPermissionController.Stub() {
            /* class android.permission.PermissionControllerService.AnonymousClass1 */

            @Override // android.permission.IPermissionController
            public void revokeRuntimePermissions(Bundle bundleizedRequest, boolean doDryRun, int reason, String callerPackageName, RemoteCallback callback) {
                Preconditions.checkNotNull(bundleizedRequest, "bundleizedRequest");
                Preconditions.checkNotNull(callerPackageName);
                Preconditions.checkNotNull(callback);
                Map<String, List<String>> request = new ArrayMap<>();
                for (String packageName : bundleizedRequest.keySet()) {
                    Preconditions.checkNotNull(packageName);
                    ArrayList<String> permissions = bundleizedRequest.getStringArrayList(packageName);
                    Preconditions.checkCollectionElementsNotNull(permissions, "permissions");
                    request.put(packageName, permissions);
                }
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.REVOKE_RUNTIME_PERMISSIONS, null);
                try {
                    boolean z = false;
                    if (getCallingUid() == PermissionControllerService.this.getPackageManager().getPackageInfo(callerPackageName, 0).applicationInfo.uid) {
                        z = true;
                    }
                    Preconditions.checkArgument(z);
                    PermissionControllerService.this.onRevokeRuntimePermissions(request, doDryRun, reason, callerPackageName, new Consumer() {
                        /* class android.permission.$$Lambda$PermissionControllerService$1$__ZsT0Jo3iLdGM0gy2UV6ea_oEw */

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            PermissionControllerService.AnonymousClass1.lambda$revokeRuntimePermissions$0(RemoteCallback.this, (Map) obj);
                        }
                    });
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            static /* synthetic */ void lambda$revokeRuntimePermissions$0(RemoteCallback callback, Map revoked) {
                Preconditions.checkNotNull(revoked);
                Bundle bundledizedRevoked = new Bundle();
                for (Map.Entry<String, List<String>> appRevocation : revoked.entrySet()) {
                    Preconditions.checkNotNull(appRevocation.getKey());
                    Preconditions.checkCollectionElementsNotNull(appRevocation.getValue(), "permissions");
                    bundledizedRevoked.putStringArrayList(appRevocation.getKey(), new ArrayList<>(appRevocation.getValue()));
                }
                Bundle result = new Bundle();
                result.putBundle(PermissionControllerManager.KEY_RESULT, bundledizedRevoked);
                callback.sendResult(result);
            }

            /* JADX WARNING: Code restructure failed: missing block: B:10:0x0030, code lost:
                $closeResource(r1, r0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x0033, code lost:
                throw r2;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:9:0x002f, code lost:
                r2 = move-exception;
             */
            @Override // android.permission.IPermissionController
            public void getRuntimePermissionBackup(UserHandle user, ParcelFileDescriptor pipe) {
                Preconditions.checkNotNull(user);
                Preconditions.checkNotNull(pipe);
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.GET_RUNTIME_PERMISSIONS, null);
                try {
                    OutputStream backup = new ParcelFileDescriptor.AutoCloseOutputStream(pipe);
                    CountDownLatch latch = new CountDownLatch(1);
                    PermissionControllerService permissionControllerService = PermissionControllerService.this;
                    Objects.requireNonNull(latch);
                    permissionControllerService.onGetRuntimePermissionsBackup(user, backup, new Runnable(latch) {
                        /* class android.permission.$$Lambda$5k6tNlswoNAjCdgttrkQIe8VHVs */
                        private final /* synthetic */ CountDownLatch f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final void run() {
                            this.f$0.countDown();
                        }
                    });
                    latch.await();
                    $closeResource(null, backup);
                } catch (IOException e) {
                    Log.e(PermissionControllerService.LOG_TAG, "Could not open pipe to write backup to", e);
                } catch (InterruptedException e2) {
                    Log.e(PermissionControllerService.LOG_TAG, "getRuntimePermissionBackup timed out", e2);
                }
            }

            private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
                if (x0 != null) {
                    try {
                        x1.close();
                    } catch (Throwable th) {
                        x0.addSuppressed(th);
                    }
                } else {
                    x1.close();
                }
            }

            /* JADX WARNING: Code restructure failed: missing block: B:10:0x0030, code lost:
                $closeResource(r1, r0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x0033, code lost:
                throw r2;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:9:0x002f, code lost:
                r2 = move-exception;
             */
            @Override // android.permission.IPermissionController
            public void restoreRuntimePermissionBackup(UserHandle user, ParcelFileDescriptor pipe) {
                Preconditions.checkNotNull(user);
                Preconditions.checkNotNull(pipe);
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.GRANT_RUNTIME_PERMISSIONS, null);
                try {
                    InputStream backup = new ParcelFileDescriptor.AutoCloseInputStream(pipe);
                    CountDownLatch latch = new CountDownLatch(1);
                    PermissionControllerService permissionControllerService = PermissionControllerService.this;
                    Objects.requireNonNull(latch);
                    permissionControllerService.onRestoreRuntimePermissionsBackup(user, backup, new Runnable(latch) {
                        /* class android.permission.$$Lambda$5k6tNlswoNAjCdgttrkQIe8VHVs */
                        private final /* synthetic */ CountDownLatch f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final void run() {
                            this.f$0.countDown();
                        }
                    });
                    latch.await();
                    $closeResource(null, backup);
                } catch (IOException e) {
                    Log.e(PermissionControllerService.LOG_TAG, "Could not open pipe to read backup from", e);
                } catch (InterruptedException e2) {
                    Log.e(PermissionControllerService.LOG_TAG, "restoreRuntimePermissionBackup timed out", e2);
                }
            }

            @Override // android.permission.IPermissionController
            public void restoreDelayedRuntimePermissionBackup(String packageName, UserHandle user, RemoteCallback callback) {
                Preconditions.checkNotNull(packageName);
                Preconditions.checkNotNull(user);
                Preconditions.checkNotNull(callback);
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.GRANT_RUNTIME_PERMISSIONS, null);
                PermissionControllerService.this.onRestoreDelayedRuntimePermissionsBackup(packageName, user, new Consumer() {
                    /* class android.permission.$$Lambda$PermissionControllerService$1$byERALVqclrc25diZo2Ly0OtfwI */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        PermissionControllerService.AnonymousClass1.lambda$restoreDelayedRuntimePermissionBackup$1(RemoteCallback.this, (Boolean) obj);
                    }
                });
            }

            static /* synthetic */ void lambda$restoreDelayedRuntimePermissionBackup$1(RemoteCallback callback, Boolean hasMoreBackup) {
                Bundle result = new Bundle();
                result.putBoolean(PermissionControllerManager.KEY_RESULT, hasMoreBackup.booleanValue());
                callback.sendResult(result);
            }

            @Override // android.permission.IPermissionController
            public void getAppPermissions(String packageName, RemoteCallback callback) {
                Preconditions.checkNotNull(packageName, "packageName");
                Preconditions.checkNotNull(callback, "callback");
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.GET_RUNTIME_PERMISSIONS, null);
                PermissionControllerService.this.onGetAppPermissions(packageName, new Consumer() {
                    /* class android.permission.$$Lambda$PermissionControllerService$1$ROtJOrojS2cjqvX59tSprAvs1o */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        PermissionControllerService.AnonymousClass1.lambda$getAppPermissions$2(RemoteCallback.this, (List) obj);
                    }
                });
            }

            static /* synthetic */ void lambda$getAppPermissions$2(RemoteCallback callback, List permissions) {
                if (permissions == null || permissions.isEmpty()) {
                    callback.sendResult(null);
                    return;
                }
                Bundle result = new Bundle();
                result.putParcelableList(PermissionControllerManager.KEY_RESULT, permissions);
                callback.sendResult(result);
            }

            @Override // android.permission.IPermissionController
            public void revokeRuntimePermission(String packageName, String permissionName) {
                Preconditions.checkNotNull(packageName, "packageName");
                Preconditions.checkNotNull(permissionName, "permissionName");
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.REVOKE_RUNTIME_PERMISSIONS, null);
                CountDownLatch latch = new CountDownLatch(1);
                PermissionControllerService permissionControllerService = PermissionControllerService.this;
                Objects.requireNonNull(latch);
                permissionControllerService.onRevokeRuntimePermission(packageName, permissionName, new Runnable(latch) {
                    /* class android.permission.$$Lambda$5k6tNlswoNAjCdgttrkQIe8VHVs */
                    private final /* synthetic */ CountDownLatch f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run() {
                        this.f$0.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Log.e(PermissionControllerService.LOG_TAG, "revokeRuntimePermission timed out", e);
                }
            }

            @Override // android.permission.IPermissionController
            public void countPermissionApps(List<String> permissionNames, int flags, RemoteCallback callback) {
                Preconditions.checkCollectionElementsNotNull(permissionNames, "permissionNames");
                Preconditions.checkFlagsArgument(flags, 3);
                Preconditions.checkNotNull(callback, "callback");
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.GET_RUNTIME_PERMISSIONS, null);
                PermissionControllerService.this.onCountPermissionApps(permissionNames, flags, new IntConsumer() {
                    /* class android.permission.$$Lambda$PermissionControllerService$1$i3vGLgbFSsM1LDWQDjRkXStMIUE */

                    public final void accept(int i) {
                        PermissionControllerService.AnonymousClass1.lambda$countPermissionApps$3(RemoteCallback.this, i);
                    }
                });
            }

            static /* synthetic */ void lambda$countPermissionApps$3(RemoteCallback callback, int numApps) {
                Bundle result = new Bundle();
                result.putInt(PermissionControllerManager.KEY_RESULT, numApps);
                callback.sendResult(result);
            }

            @Override // android.permission.IPermissionController
            public void getPermissionUsages(boolean countSystem, long numMillis, RemoteCallback callback) {
                Preconditions.checkArgumentNonnegative(numMillis);
                Preconditions.checkNotNull(callback, "callback");
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.GET_RUNTIME_PERMISSIONS, null);
                PermissionControllerService.this.onGetPermissionUsages(countSystem, numMillis, new Consumer() {
                    /* class android.permission.$$Lambda$PermissionControllerService$1$oEdK7RdXzZpRIDF40ujz7uvW1Ts */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        PermissionControllerService.AnonymousClass1.lambda$getPermissionUsages$4(RemoteCallback.this, (List) obj);
                    }
                });
            }

            static /* synthetic */ void lambda$getPermissionUsages$4(RemoteCallback callback, List users) {
                if (users == null || users.isEmpty()) {
                    callback.sendResult(null);
                    return;
                }
                Bundle result = new Bundle();
                result.putParcelableList(PermissionControllerManager.KEY_RESULT, users);
                callback.sendResult(result);
            }

            @Override // android.permission.IPermissionController
            public void setRuntimePermissionGrantStateByDeviceAdmin(String callerPackageName, String packageName, String permission, int grantState, RemoteCallback callback) {
                Preconditions.checkStringNotEmpty(callerPackageName);
                Preconditions.checkStringNotEmpty(packageName);
                Preconditions.checkStringNotEmpty(permission);
                boolean z = true;
                if (!(grantState == 1 || grantState == 2 || grantState == 0)) {
                    z = false;
                }
                Preconditions.checkArgument(z);
                Preconditions.checkNotNull(callback);
                if (grantState == 2) {
                    PermissionControllerService.this.enforceCallingPermission(Manifest.permission.GRANT_RUNTIME_PERMISSIONS, null);
                }
                if (grantState == 2) {
                    PermissionControllerService.this.enforceCallingPermission(Manifest.permission.REVOKE_RUNTIME_PERMISSIONS, null);
                }
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.ADJUST_RUNTIME_PERMISSIONS_POLICY, null);
                PermissionControllerService.this.onSetRuntimePermissionGrantStateByDeviceAdmin(callerPackageName, packageName, permission, grantState, new Consumer() {
                    /* class android.permission.$$Lambda$PermissionControllerService$1$Sp35OTwahalQfZumoUDJ70lCKe0 */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        PermissionControllerService.AnonymousClass1.lambda$setRuntimePermissionGrantStateByDeviceAdmin$5(RemoteCallback.this, (Boolean) obj);
                    }
                });
            }

            static /* synthetic */ void lambda$setRuntimePermissionGrantStateByDeviceAdmin$5(RemoteCallback callback, Boolean wasSet) {
                Bundle result = new Bundle();
                result.putBoolean(PermissionControllerManager.KEY_RESULT, wasSet.booleanValue());
                callback.sendResult(result);
            }

            @Override // android.permission.IPermissionController
            public void grantOrUpgradeDefaultRuntimePermissions(RemoteCallback callback) {
                Preconditions.checkNotNull(callback, "callback");
                PermissionControllerService.this.enforceCallingPermission(Manifest.permission.ADJUST_RUNTIME_PERMISSIONS_POLICY, null);
                PermissionControllerService.this.onGrantOrUpgradeDefaultRuntimePermissions(new Runnable() {
                    /* class android.permission.$$Lambda$PermissionControllerService$1$aoBUJn0rgfJAYfvz7rYL8N9wr_Y */

                    public final void run() {
                        PermissionControllerService.AnonymousClass1.lambda$grantOrUpgradeDefaultRuntimePermissions$6(RemoteCallback.this);
                    }
                });
            }
        };
    }
}
