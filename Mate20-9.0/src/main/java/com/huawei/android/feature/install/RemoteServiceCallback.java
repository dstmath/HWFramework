package com.huawei.android.feature.install;

import android.os.Bundle;
import android.util.Log;
import com.huawei.android.feature.IDynamicInstallCallback;
import com.huawei.android.feature.tasks.TaskHolder;
import java.util.List;

public class RemoteServiceCallback<TResult> extends IDynamicInstallCallback.Stub {
    private static final String TAG = RemoteServiceCallback.class.getSimpleName();
    protected RemoteServiceConnector mInstaller;
    /* access modifiers changed from: protected */
    public TaskHolder<TResult> mTaskHolder;

    public RemoteServiceCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<TResult> taskHolder) {
        this.mInstaller = remoteServiceConnector;
        this.mTaskHolder = taskHolder;
    }

    public void onCancelInstall(int i, Bundle bundle) {
        this.mInstaller.quit();
        Log.d(TAG, "onCancelInstall");
    }

    public void onDeferredInstall(Bundle bundle) {
        this.mInstaller.quit();
        Log.d(TAG, "onDeferredInstall");
    }

    public void onDeferredUninstall(Bundle bundle) {
        this.mInstaller.quit();
        Log.d(TAG, "onDeferredUninstall");
    }

    public void onError(Bundle bundle) {
        this.mInstaller.quit();
        Log.d(TAG, "onError");
        this.mTaskHolder.notifyException(new InstallException(bundle.getInt("error_code")));
    }

    public void onGetSession(int i, Bundle bundle) {
        this.mInstaller.quit();
        Log.d(TAG, "onGetSession");
    }

    public void onGetSessionStates(List<Bundle> list) {
        this.mInstaller.quit();
        Log.d(TAG, "onGetSessionStates");
    }

    public void onStartInstall(int i, Bundle bundle) {
        this.mInstaller.quit();
        Log.d(TAG, "onStartInstall sessionId " + i);
    }
}
