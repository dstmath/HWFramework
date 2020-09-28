package android.app.role;

import android.Manifest;
import android.annotation.SystemApi;
import android.app.Service;
import android.app.role.IRoleController;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallback;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;

@SystemApi
public abstract class RoleControllerService extends Service {
    public static final String SERVICE_INTERFACE = "android.app.role.RoleControllerService";
    private Handler mWorkerHandler;
    private HandlerThread mWorkerThread;

    public abstract boolean onAddRoleHolder(String str, String str2, @RoleManager.ManageHoldersFlags int i);

    public abstract boolean onClearRoleHolders(String str, @RoleManager.ManageHoldersFlags int i);

    public abstract boolean onGrantDefaultRoles();

    public abstract boolean onIsApplicationQualifiedForRole(String str, String str2);

    public abstract boolean onIsRoleVisible(String str);

    public abstract boolean onRemoveRoleHolder(String str, String str2, @RoleManager.ManageHoldersFlags int i);

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mWorkerThread = new HandlerThread(RoleControllerService.class.getSimpleName());
        this.mWorkerThread.start();
        this.mWorkerHandler = new Handler(this.mWorkerThread.getLooper());
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.mWorkerThread.quitSafely();
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new IRoleController.Stub() {
            /* class android.app.role.RoleControllerService.AnonymousClass1 */

            @Override // android.app.role.IRoleController
            public void grantDefaultRoles(RemoteCallback callback) {
                enforceCallerSystemUid("grantDefaultRoles");
                Preconditions.checkNotNull(callback, "callback cannot be null");
                RoleControllerService.this.mWorkerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RoleControllerService$1$fmj7uDKaG3BoLl6bhtrA675gRI.INSTANCE, RoleControllerService.this, callback));
            }

            @Override // android.app.role.IRoleController
            public void onAddRoleHolder(String roleName, String packageName, int flags, RemoteCallback callback) {
                enforceCallerSystemUid("onAddRoleHolder");
                Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
                Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
                Preconditions.checkNotNull(callback, "callback cannot be null");
                RoleControllerService.this.mWorkerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RoleControllerService$1$UVI1sAWAcBnt3Enqn2ITLirwtk.INSTANCE, RoleControllerService.this, roleName, packageName, Integer.valueOf(flags), callback));
            }

            @Override // android.app.role.IRoleController
            public void onRemoveRoleHolder(String roleName, String packageName, int flags, RemoteCallback callback) {
                enforceCallerSystemUid("onRemoveRoleHolder");
                Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
                Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
                Preconditions.checkNotNull(callback, "callback cannot be null");
                RoleControllerService.this.mWorkerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RoleControllerService$1$PB6H1df6VvLzUJ3hhB_75mN3u7s.INSTANCE, RoleControllerService.this, roleName, packageName, Integer.valueOf(flags), callback));
            }

            @Override // android.app.role.IRoleController
            public void onClearRoleHolders(String roleName, int flags, RemoteCallback callback) {
                enforceCallerSystemUid("onClearRoleHolders");
                Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
                Preconditions.checkNotNull(callback, "callback cannot be null");
                RoleControllerService.this.mWorkerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RoleControllerService$1$dBm1t_MGyEA9yMAxoOUMOhYVmPo.INSTANCE, RoleControllerService.this, roleName, Integer.valueOf(flags), callback));
            }

            private void enforceCallerSystemUid(String methodName) {
                if (Binder.getCallingUid() != 1000) {
                    throw new SecurityException("Only the system process can call " + methodName + "()");
                }
            }

            @Override // android.app.role.IRoleController
            public void isApplicationQualifiedForRole(String roleName, String packageName, RemoteCallback callback) {
                Bundle bundle = null;
                RoleControllerService.this.enforceCallingPermission(Manifest.permission.MANAGE_ROLE_HOLDERS, null);
                Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
                Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
                Preconditions.checkNotNull(callback, "callback cannot be null");
                if (RoleControllerService.this.onIsApplicationQualifiedForRole(roleName, packageName)) {
                    bundle = Bundle.EMPTY;
                }
                callback.sendResult(bundle);
            }

            @Override // android.app.role.IRoleController
            public void isRoleVisible(String roleName, RemoteCallback callback) {
                Bundle bundle = null;
                RoleControllerService.this.enforceCallingPermission(Manifest.permission.MANAGE_ROLE_HOLDERS, null);
                Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
                Preconditions.checkNotNull(callback, "callback cannot be null");
                if (RoleControllerService.this.onIsRoleVisible(roleName)) {
                    bundle = Bundle.EMPTY;
                }
                callback.sendResult(bundle);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void grantDefaultRoles(RemoteCallback callback) {
        callback.sendResult(onGrantDefaultRoles() ? Bundle.EMPTY : null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void onAddRoleHolder(String roleName, String packageName, @RoleManager.ManageHoldersFlags int flags, RemoteCallback callback) {
        callback.sendResult(onAddRoleHolder(roleName, packageName, flags) ? Bundle.EMPTY : null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void onRemoveRoleHolder(String roleName, String packageName, @RoleManager.ManageHoldersFlags int flags, RemoteCallback callback) {
        callback.sendResult(onRemoveRoleHolder(roleName, packageName, flags) ? Bundle.EMPTY : null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void onClearRoleHolders(String roleName, @RoleManager.ManageHoldersFlags int flags, RemoteCallback callback) {
        callback.sendResult(onClearRoleHolders(roleName, flags) ? Bundle.EMPTY : null);
    }
}
