package android.permissionpresenterservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.permission.IRuntimePermissionPresenter.Stub;
import android.content.pm.permission.RuntimePermissionPresentationInfo;
import android.content.pm.permission.RuntimePermissionPresenter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallback;
import com.android.internal.os.SomeArgs;
import java.util.List;

public abstract class RuntimePermissionPresenterService extends Service {
    public static final String SERVICE_INTERFACE = "android.permissionpresenterservice.RuntimePermissionPresenterService";
    private Handler mHandler;

    private final class MyHandler extends Handler {
        public static final int MSG_GET_APPS_USING_PERMISSIONS = 2;
        public static final int MSG_GET_APP_PERMISSIONS = 1;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            RemoteCallback callback;
            Bundle result;
            switch (msg.what) {
                case MSG_GET_APP_PERMISSIONS /*1*/:
                    SomeArgs args = msg.obj;
                    String packageName = args.arg1;
                    callback = args.arg2;
                    args.recycle();
                    List<RuntimePermissionPresentationInfo> permissions = RuntimePermissionPresenterService.this.onGetAppPermissions(packageName);
                    if (permissions == null || permissions.isEmpty()) {
                        callback.sendResult(null);
                        return;
                    }
                    result = new Bundle();
                    result.putParcelableList(RuntimePermissionPresenter.KEY_RESULT, permissions);
                    callback.sendResult(result);
                case MSG_GET_APPS_USING_PERMISSIONS /*2*/:
                    callback = (RemoteCallback) msg.obj;
                    List<ApplicationInfo> apps = RuntimePermissionPresenterService.this.onGetAppsUsingPermissions(msg.arg1 == MSG_GET_APP_PERMISSIONS);
                    if (apps == null || apps.isEmpty()) {
                        callback.sendResult(null);
                        return;
                    }
                    result = new Bundle();
                    result.putParcelableList(RuntimePermissionPresenter.KEY_RESULT, apps);
                    callback.sendResult(result);
                default:
            }
        }
    }

    public abstract List<RuntimePermissionPresentationInfo> onGetAppPermissions(String str);

    public abstract List<ApplicationInfo> onGetAppsUsingPermissions(boolean z);

    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new MyHandler(base.getMainLooper());
    }

    public final IBinder onBind(Intent intent) {
        return new Stub() {
            public void getAppPermissions(String packageName, RemoteCallback callback) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = packageName;
                args.arg2 = callback;
                RuntimePermissionPresenterService.this.mHandler.obtainMessage(1, args).sendToTarget();
            }

            public void getAppsUsingPermissions(boolean system, RemoteCallback callback) {
                int i;
                Handler -get0 = RuntimePermissionPresenterService.this.mHandler;
                if (system) {
                    i = 1;
                } else {
                    i = 0;
                }
                -get0.obtainMessage(2, i, 0, callback).sendToTarget();
            }
        };
    }
}
