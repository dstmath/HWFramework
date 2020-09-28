package android.permissionpresenterservice;

import android.annotation.SystemApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.permission.IRuntimePermissionPresenter;
import android.content.pm.permission.RuntimePermissionPresentationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallback;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import java.util.List;

@SystemApi
@Deprecated
public abstract class RuntimePermissionPresenterService extends Service {
    private static final String KEY_RESULT = "android.content.pm.permission.RuntimePermissionPresenter.key.result";
    public static final String SERVICE_INTERFACE = "android.permissionpresenterservice.RuntimePermissionPresenterService";
    private Handler mHandler;

    public abstract List<RuntimePermissionPresentationInfo> onGetAppPermissions(String str);

    @Override // android.content.ContextWrapper
    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new Handler(base.getMainLooper());
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new IRuntimePermissionPresenter.Stub() {
            /* class android.permissionpresenterservice.RuntimePermissionPresenterService.AnonymousClass1 */

            @Override // android.content.pm.permission.IRuntimePermissionPresenter
            public void getAppPermissions(String packageName, RemoteCallback callback) {
                Preconditions.checkNotNull(packageName, "packageName");
                Preconditions.checkNotNull(callback, "callback");
                RuntimePermissionPresenterService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RuntimePermissionPresenterService$1$hIxcH5_fyEVhEY0ZwjDuhvJriA.INSTANCE, RuntimePermissionPresenterService.this, packageName, callback));
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getAppPermissions(String packageName, RemoteCallback callback) {
        List<RuntimePermissionPresentationInfo> permissions = onGetAppPermissions(packageName);
        if (permissions == null || permissions.isEmpty()) {
            callback.sendResult(null);
            return;
        }
        Bundle result = new Bundle();
        result.putParcelableList(KEY_RESULT, permissions);
        callback.sendResult(result);
    }
}
