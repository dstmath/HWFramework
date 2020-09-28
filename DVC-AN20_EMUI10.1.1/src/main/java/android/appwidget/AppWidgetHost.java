package android.appwidget;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.SparseArray;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.internal.appwidget.IAppWidgetHost;
import com.android.internal.appwidget.IAppWidgetService;
import java.lang.ref.WeakReference;
import java.util.List;

public class AppWidgetHost {
    static final int HANDLE_PROVIDERS_CHANGED = 3;
    static final int HANDLE_PROVIDER_CHANGED = 2;
    static final int HANDLE_UPDATE = 1;
    @UnsupportedAppUsage
    static final int HANDLE_VIEW_DATA_CHANGED = 4;
    private static final String TAG = "AppWidgetHost";
    @UnsupportedAppUsage
    static IAppWidgetService sService;
    static boolean sServiceInitialized = false;
    static final Object sServiceLock = new Object();
    private final Callbacks mCallbacks;
    private String mContextOpPackageName;
    private DisplayMetrics mDisplayMetrics;
    @UnsupportedAppUsage
    private final Handler mHandler;
    private final int mHostId;
    private RemoteViews.OnClickHandler mOnClickHandler;
    private final SparseArray<AppWidgetHostView> mViews;

    static class Callbacks extends IAppWidgetHost.Stub {
        private final WeakReference<Handler> mWeakHandler;

        public Callbacks(Handler handler) {
            this.mWeakHandler = new WeakReference<>(handler);
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void updateAppWidget(int appWidgetId, RemoteViews views) {
            if (isLocalBinder() && views != null) {
                views = views.clone();
            }
            Handler handler = this.mWeakHandler.get();
            if (handler == null) {
                Slog.i(AppWidgetHost.TAG, "handler is null when updateAppWidget");
            } else {
                handler.obtainMessage(1, appWidgetId, 0, views).sendToTarget();
            }
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void providerChanged(int appWidgetId, AppWidgetProviderInfo info) {
            if (isLocalBinder() && info != null) {
                info = info.clone();
            }
            Handler handler = this.mWeakHandler.get();
            if (handler == null) {
                Slog.i(AppWidgetHost.TAG, "handler is null when providerChanged");
            } else {
                handler.obtainMessage(2, appWidgetId, 0, info).sendToTarget();
            }
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void providersChanged() {
            Handler handler = this.mWeakHandler.get();
            if (handler == null) {
                Slog.i(AppWidgetHost.TAG, "handler is null when providersChanged");
            } else {
                handler.obtainMessage(3).sendToTarget();
            }
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void viewDataChanged(int appWidgetId, int viewId) {
            Handler handler = this.mWeakHandler.get();
            if (handler == null) {
                Slog.i(AppWidgetHost.TAG, "handler is null when viewDataChanged");
            } else {
                handler.obtainMessage(4, appWidgetId, viewId).sendToTarget();
            }
        }

        private static boolean isLocalBinder() {
            return Process.myPid() == Binder.getCallingPid();
        }
    }

    class UpdateHandler extends Handler {
        public UpdateHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                AppWidgetHost.this.updateAppWidgetView(msg.arg1, (RemoteViews) msg.obj);
            } else if (i == 2) {
                AppWidgetHost.this.onProviderChanged(msg.arg1, (AppWidgetProviderInfo) msg.obj);
            } else if (i == 3) {
                AppWidgetHost.this.onProvidersChanged();
            } else if (i == 4) {
                AppWidgetHost.this.viewDataChanged(msg.arg1, msg.arg2);
            }
        }
    }

    public AppWidgetHost(Context context, int hostId) {
        this(context, hostId, null, context.getMainLooper());
    }

    @UnsupportedAppUsage
    public AppWidgetHost(Context context, int hostId, RemoteViews.OnClickHandler handler, Looper looper) {
        this.mViews = new SparseArray<>();
        this.mContextOpPackageName = context.getOpPackageName();
        this.mHostId = hostId;
        this.mOnClickHandler = handler;
        this.mHandler = new UpdateHandler(looper);
        this.mCallbacks = new Callbacks(this.mHandler);
        this.mDisplayMetrics = context.getResources().getDisplayMetrics();
        bindService(context);
    }

    private static void bindService(Context context) {
        synchronized (sServiceLock) {
            if (sServiceInitialized) {
                Slog.i(TAG, "sServiceInitialized is false when bindService, so return");
                return;
            }
            sServiceInitialized = true;
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS) || context.getResources().getBoolean(R.bool.config_enableAppWidgetService)) {
                sService = IAppWidgetService.Stub.asInterface(ServiceManager.getService(Context.APPWIDGET_SERVICE));
            } else {
                Slog.i(TAG, "the aplication has no widget feature, or config_enableAppWidgetService in resources is true.");
            }
        }
    }

    public void startListening() {
        int[] idsToUpdate;
        if (sService == null) {
            Slog.i(TAG, "sService is null when startListening, so return.");
            return;
        }
        synchronized (this.mViews) {
            int N = this.mViews.size();
            idsToUpdate = new int[N];
            for (int i = 0; i < N; i++) {
                idsToUpdate[i] = this.mViews.keyAt(i);
            }
        }
        try {
            List<PendingHostUpdate> updates = sService.startListening(this.mCallbacks, this.mContextOpPackageName, this.mHostId, idsToUpdate).getList();
            int N2 = updates.size();
            for (int i2 = 0; i2 < N2; i2++) {
                PendingHostUpdate update = updates.get(i2);
                int i3 = update.type;
                if (i3 == 0) {
                    updateAppWidgetView(update.appWidgetId, update.views);
                } else if (i3 == 1) {
                    onProviderChanged(update.appWidgetId, update.widgetInfo);
                } else if (i3 == 2) {
                    viewDataChanged(update.appWidgetId, update.viewId);
                }
            }
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public void stopListening() {
        IAppWidgetService iAppWidgetService = sService;
        if (iAppWidgetService == null) {
            Slog.i(TAG, "sService is null when stopListening");
            return;
        }
        try {
            iAppWidgetService.stopListening(this.mContextOpPackageName, this.mHostId);
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public int allocateAppWidgetId() {
        IAppWidgetService iAppWidgetService = sService;
        if (iAppWidgetService == null) {
            Slog.i(TAG, "sService is null when allocateAppWidgetId, so return -1");
            return -1;
        }
        try {
            return iAppWidgetService.allocateAppWidgetId(this.mContextOpPackageName, this.mHostId);
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public final void startAppWidgetConfigureActivityForResult(Activity activity, int appWidgetId, int intentFlags, int requestCode, Bundle options) {
        IAppWidgetService iAppWidgetService = sService;
        if (iAppWidgetService != null) {
            try {
                IntentSender intentSender = iAppWidgetService.createAppWidgetConfigIntentSender(this.mContextOpPackageName, appWidgetId, intentFlags);
                if (intentSender != null) {
                    activity.startIntentSenderForResult(intentSender, requestCode, null, 0, 0, 0, options);
                    return;
                }
                throw new ActivityNotFoundException();
            } catch (IntentSender.SendIntentException e) {
                throw new ActivityNotFoundException();
            } catch (RemoteException e2) {
                throw new RuntimeException("system server dead?", e2);
            }
        }
    }

    public int[] getAppWidgetIds() {
        IAppWidgetService iAppWidgetService = sService;
        if (iAppWidgetService == null) {
            return new int[0];
        }
        try {
            return iAppWidgetService.getAppWidgetIdsForHost(this.mContextOpPackageName, this.mHostId);
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public void deleteAppWidgetId(int appWidgetId) {
        if (sService != null) {
            synchronized (this.mViews) {
                this.mViews.remove(appWidgetId);
                try {
                    sService.deleteAppWidgetId(this.mContextOpPackageName, appWidgetId);
                } catch (RemoteException e) {
                    throw new RuntimeException("system server dead?", e);
                }
            }
        }
    }

    public void deleteHost() {
        IAppWidgetService iAppWidgetService = sService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.deleteHost(this.mContextOpPackageName, this.mHostId);
            } catch (RemoteException e) {
                throw new RuntimeException("system server dead?", e);
            }
        }
    }

    public static void deleteAllHosts() {
        IAppWidgetService iAppWidgetService = sService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.deleteAllHosts();
            } catch (RemoteException e) {
                throw new RuntimeException("system server dead?", e);
            }
        }
    }

    public final AppWidgetHostView createView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        if (sService == null) {
            Slog.i(TAG, "sService is null when createView, so return.");
            return null;
        }
        AppWidgetHostView view = onCreateView(context, appWidgetId, appWidget);
        view.setOnClickHandler(this.mOnClickHandler);
        view.setAppWidget(appWidgetId, appWidget);
        synchronized (this.mViews) {
            this.mViews.put(appWidgetId, view);
        }
        try {
            view.updateAppWidget(sService.getAppWidgetViews(this.mContextOpPackageName, appWidgetId));
            return view;
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    /* access modifiers changed from: protected */
    public AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        return new AppWidgetHostView(context, this.mOnClickHandler);
    }

    /* access modifiers changed from: protected */
    public void onProviderChanged(int appWidgetId, AppWidgetProviderInfo appWidget) {
        AppWidgetHostView v;
        appWidget.updateDimensions(this.mDisplayMetrics);
        synchronized (this.mViews) {
            v = this.mViews.get(appWidgetId);
        }
        if (v != null) {
            v.resetAppWidget(appWidget);
        } else {
            Slog.i(TAG, "AppWidgetHostView is null");
        }
    }

    /* access modifiers changed from: protected */
    public void onProvidersChanged() {
    }

    /* access modifiers changed from: package-private */
    public void updateAppWidgetView(int appWidgetId, RemoteViews views) {
        AppWidgetHostView v;
        synchronized (this.mViews) {
            v = this.mViews.get(appWidgetId);
        }
        if (v != null) {
            v.updateAppWidget(views);
        } else {
            Slog.i(TAG, "appWidget hostView is null");
        }
    }

    /* access modifiers changed from: package-private */
    public void viewDataChanged(int appWidgetId, int viewId) {
        AppWidgetHostView v;
        synchronized (this.mViews) {
            v = this.mViews.get(appWidgetId);
        }
        if (v != null) {
            v.viewDataChanged(viewId);
        } else {
            Slog.i(TAG, "appWidget hostView is null when do viewDataChanged");
        }
    }

    /* access modifiers changed from: protected */
    public void clearViews() {
        synchronized (this.mViews) {
            this.mViews.clear();
        }
    }
}
