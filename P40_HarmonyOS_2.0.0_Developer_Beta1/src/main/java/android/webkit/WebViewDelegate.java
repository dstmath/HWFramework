package android.webkit;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.Application;
import android.app.ResourcesManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.RecordingCanvas;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewRootImpl;
import com.android.internal.util.ArrayUtils;

@SystemApi
public final class WebViewDelegate {

    public interface OnTraceEnabledChangeListener {
        void onTraceEnabledChange(boolean z);
    }

    @UnsupportedAppUsage
    WebViewDelegate() {
    }

    public void setOnTraceEnabledChangeListener(final OnTraceEnabledChangeListener listener) {
        SystemProperties.addChangeCallback(new Runnable() {
            /* class android.webkit.WebViewDelegate.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                listener.onTraceEnabledChange(WebViewDelegate.this.isTraceTagEnabled());
            }
        });
    }

    public boolean isTraceTagEnabled() {
        return Trace.isTagEnabled(16);
    }

    @Deprecated
    public boolean canInvokeDrawGlFunctor(View containerView) {
        return true;
    }

    @Deprecated
    public void invokeDrawGlFunctor(View containerView, long nativeDrawGLFunctor, boolean waitForCompletion) {
        ViewRootImpl.invokeFunctor(nativeDrawGLFunctor, waitForCompletion);
    }

    @Deprecated
    public void callDrawGlFunction(Canvas canvas, long nativeDrawGLFunctor) {
        if (canvas instanceof RecordingCanvas) {
            ((RecordingCanvas) canvas).drawGLFunctor2(nativeDrawGLFunctor, null);
            return;
        }
        throw new IllegalArgumentException(canvas.getClass().getName() + " is not a DisplayList canvas");
    }

    @Deprecated
    public void callDrawGlFunction(Canvas canvas, long nativeDrawGLFunctor, Runnable releasedRunnable) {
        if (canvas instanceof RecordingCanvas) {
            ((RecordingCanvas) canvas).drawGLFunctor2(nativeDrawGLFunctor, releasedRunnable);
            return;
        }
        throw new IllegalArgumentException(canvas.getClass().getName() + " is not a DisplayList canvas");
    }

    public void drawWebViewFunctor(Canvas canvas, int functor) {
        if (canvas instanceof RecordingCanvas) {
            ((RecordingCanvas) canvas).drawWebViewFunctor(functor);
            return;
        }
        throw new IllegalArgumentException(canvas.getClass().getName() + " is not a RecordingCanvas canvas");
    }

    @Deprecated
    public void detachDrawGlFunctor(View containerView, long nativeDrawGLFunctor) {
        ViewRootImpl viewRootImpl = containerView.getViewRootImpl();
        if (nativeDrawGLFunctor != 0 && viewRootImpl != null) {
            viewRootImpl.detachFunctor(nativeDrawGLFunctor);
        }
    }

    public int getPackageId(Resources resources, String packageName) {
        SparseArray<String> packageIdentifiers = resources.getAssets().getAssignedPackageIdentifiers();
        for (int i = 0; i < packageIdentifiers.size(); i++) {
            if (packageName.equals(packageIdentifiers.valueAt(i))) {
                return packageIdentifiers.keyAt(i);
            }
        }
        throw new RuntimeException("Package not found: " + packageName);
    }

    public Application getApplication() {
        return ActivityThread.currentApplication();
    }

    public String getErrorString(Context context, int errorCode) {
        return LegacyErrorStrings.getString(errorCode, context);
    }

    public void addWebViewAssetPath(Context context) {
        String[] newAssetPaths = WebViewFactory.getLoadedPackageInfo().applicationInfo.getAllApkPaths();
        ApplicationInfo appInfo = context.getApplicationInfo();
        String[] newLibAssets = appInfo.sharedLibraryFiles;
        for (String newAssetPath : newAssetPaths) {
            newLibAssets = (String[]) ArrayUtils.appendElement(String.class, newLibAssets, newAssetPath);
        }
        if (newLibAssets != appInfo.sharedLibraryFiles) {
            appInfo.sharedLibraryFiles = newLibAssets;
            ResourcesManager.getInstance().appendLibAssetsForMainAssetPath(appInfo.getBaseResourcePath(), newAssetPaths);
        }
    }

    public boolean isMultiProcessEnabled() {
        try {
            return WebViewFactory.getUpdateService().isMultiProcessEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getDataDirectorySuffix() {
        return WebViewFactory.getDataDirectorySuffix();
    }
}
