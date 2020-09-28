package android.appwidget;

import android.annotation.UnsupportedAppUsage;
import android.app.IServiceConnection;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;
import com.android.internal.appwidget.IAppWidgetService;
import java.util.Collections;
import java.util.List;

public class AppWidgetManager {
    public static final String ACTION_APPWIDGET_BIND = "android.appwidget.action.APPWIDGET_BIND";
    public static final String ACTION_APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";
    public static final String ACTION_APPWIDGET_DELETED = "android.appwidget.action.APPWIDGET_DELETED";
    public static final String ACTION_APPWIDGET_DISABLED = "android.appwidget.action.APPWIDGET_DISABLED";
    public static final String ACTION_APPWIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";
    public static final String ACTION_APPWIDGET_HOST_RESTORED = "android.appwidget.action.APPWIDGET_HOST_RESTORED";
    public static final String ACTION_APPWIDGET_OPTIONS_CHANGED = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
    public static final String ACTION_APPWIDGET_PICK = "android.appwidget.action.APPWIDGET_PICK";
    public static final String ACTION_APPWIDGET_RESTORED = "android.appwidget.action.APPWIDGET_RESTORED";
    public static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String ACTION_KEYGUARD_APPWIDGET_PICK = "android.appwidget.action.KEYGUARD_APPWIDGET_PICK";
    public static final String EXTRA_APPWIDGET_ID = "appWidgetId";
    public static final String EXTRA_APPWIDGET_IDS = "appWidgetIds";
    public static final String EXTRA_APPWIDGET_OLD_IDS = "appWidgetOldIds";
    public static final String EXTRA_APPWIDGET_OPTIONS = "appWidgetOptions";
    public static final String EXTRA_APPWIDGET_PREVIEW = "appWidgetPreview";
    public static final String EXTRA_APPWIDGET_PROVIDER = "appWidgetProvider";
    public static final String EXTRA_APPWIDGET_PROVIDER_PROFILE = "appWidgetProviderProfile";
    public static final String EXTRA_CATEGORY_FILTER = "categoryFilter";
    public static final String EXTRA_CUSTOM_EXTRAS = "customExtras";
    public static final String EXTRA_CUSTOM_INFO = "customInfo";
    public static final String EXTRA_CUSTOM_SORT = "customSort";
    public static final String EXTRA_HOST_ID = "hostId";
    public static final int INVALID_APPWIDGET_ID = 0;
    public static final String META_DATA_APPWIDGET_PROVIDER = "android.appwidget.provider";
    public static final String OPTION_APPWIDGET_HOST_CATEGORY = "appWidgetCategory";
    public static final String OPTION_APPWIDGET_MAX_HEIGHT = "appWidgetMaxHeight";
    public static final String OPTION_APPWIDGET_MAX_WIDTH = "appWidgetMaxWidth";
    public static final String OPTION_APPWIDGET_MIN_HEIGHT = "appWidgetMinHeight";
    public static final String OPTION_APPWIDGET_MIN_WIDTH = "appWidgetMinWidth";
    private final Context mContext;
    private final DisplayMetrics mDisplayMetrics;
    private final String mPackageName;
    @UnsupportedAppUsage
    private final IAppWidgetService mService;

    public static AppWidgetManager getInstance(Context context) {
        return (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
    }

    public AppWidgetManager(Context context, IAppWidgetService service) {
        this.mContext = context;
        this.mPackageName = context.getOpPackageName();
        this.mService = service;
        this.mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    public void updateAppWidget(int[] appWidgetIds, RemoteViews views) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.updateAppWidgetIds(this.mPackageName, appWidgetIds, views);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void updateAppWidgetOptions(int appWidgetId, Bundle options) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.updateAppWidgetOptions(this.mPackageName, appWidgetId, options);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public Bundle getAppWidgetOptions(int appWidgetId) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return Bundle.EMPTY;
        }
        try {
            return iAppWidgetService.getAppWidgetOptions(this.mPackageName, appWidgetId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateAppWidget(int appWidgetId, RemoteViews views) {
        if (this.mService != null) {
            updateAppWidget(new int[]{appWidgetId}, views);
        }
    }

    public void partiallyUpdateAppWidget(int[] appWidgetIds, RemoteViews views) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.partiallyUpdateAppWidgetIds(this.mPackageName, appWidgetIds, views);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void partiallyUpdateAppWidget(int appWidgetId, RemoteViews views) {
        if (this.mService != null) {
            partiallyUpdateAppWidget(new int[]{appWidgetId}, views);
        }
    }

    public void updateAppWidget(ComponentName provider, RemoteViews views) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.updateAppWidgetProvider(provider, views);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void updateAppWidgetProviderInfo(ComponentName provider, String metaDataKey) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.updateAppWidgetProviderInfo(provider, metaDataKey);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void notifyAppWidgetViewDataChanged(int[] appWidgetIds, int viewId) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.notifyAppWidgetViewDataChanged(this.mPackageName, appWidgetIds, viewId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void notifyAppWidgetViewDataChanged(int appWidgetId, int viewId) {
        if (this.mService != null) {
            notifyAppWidgetViewDataChanged(new int[]{appWidgetId}, viewId);
        }
    }

    public List<AppWidgetProviderInfo> getInstalledProvidersForProfile(UserHandle profile) {
        if (this.mService == null) {
            return Collections.emptyList();
        }
        return getInstalledProvidersForProfile(1, profile, null);
    }

    public List<AppWidgetProviderInfo> getInstalledProvidersForPackage(String packageName, UserHandle profile) {
        if (packageName == null) {
            throw new NullPointerException("A non-null package must be passed to this method. If you want all widgets regardless of package, see getInstalledProvidersForProfile(UserHandle)");
        } else if (this.mService == null) {
            return Collections.emptyList();
        } else {
            return getInstalledProvidersForProfile(1, profile, packageName);
        }
    }

    public List<AppWidgetProviderInfo> getInstalledProviders() {
        if (this.mService == null) {
            return Collections.emptyList();
        }
        return getInstalledProvidersForProfile(1, null, null);
    }

    @UnsupportedAppUsage
    public List<AppWidgetProviderInfo> getInstalledProviders(int categoryFilter) {
        if (this.mService == null) {
            return Collections.emptyList();
        }
        return getInstalledProvidersForProfile(categoryFilter, null, null);
    }

    @UnsupportedAppUsage
    public List<AppWidgetProviderInfo> getInstalledProvidersForProfile(int categoryFilter, UserHandle profile, String packageName) {
        if (this.mService == null) {
            return Collections.emptyList();
        }
        if (profile == null) {
            profile = this.mContext.getUser();
        }
        try {
            ParceledListSlice<AppWidgetProviderInfo> providers = this.mService.getInstalledProvidersForProfile(categoryFilter, profile.getIdentifier(), packageName);
            if (providers == null) {
                return Collections.emptyList();
            }
            for (AppWidgetProviderInfo info : providers.getList()) {
                info.updateDimensions(this.mDisplayMetrics);
            }
            return providers.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return null;
        }
        try {
            AppWidgetProviderInfo info = iAppWidgetService.getAppWidgetInfo(this.mPackageName, appWidgetId);
            if (info != null) {
                info.updateDimensions(this.mDisplayMetrics);
            }
            return info;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void bindAppWidgetId(int appWidgetId, ComponentName provider) {
        if (this.mService != null) {
            bindAppWidgetId(appWidgetId, provider, null);
        }
    }

    @UnsupportedAppUsage
    public void bindAppWidgetId(int appWidgetId, ComponentName provider, Bundle options) {
        if (this.mService != null) {
            bindAppWidgetIdIfAllowed(appWidgetId, this.mContext.getUser(), provider, options);
        }
    }

    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, ComponentName provider) {
        if (this.mService == null) {
            return false;
        }
        return bindAppWidgetIdIfAllowed(appWidgetId, this.mContext.getUserId(), provider, (Bundle) null);
    }

    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, ComponentName provider, Bundle options) {
        if (this.mService == null) {
            return false;
        }
        return bindAppWidgetIdIfAllowed(appWidgetId, this.mContext.getUserId(), provider, options);
    }

    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, UserHandle user, ComponentName provider, Bundle options) {
        if (this.mService == null) {
            return false;
        }
        return bindAppWidgetIdIfAllowed(appWidgetId, user.getIdentifier(), provider, options);
    }

    public boolean hasBindAppWidgetPermission(String packageName, int userId) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return false;
        }
        try {
            return iAppWidgetService.hasBindAppWidgetPermission(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasBindAppWidgetPermission(String packageName) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return false;
        }
        try {
            return iAppWidgetService.hasBindAppWidgetPermission(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBindAppWidgetPermission(String packageName, boolean permission) {
        if (this.mService != null) {
            setBindAppWidgetPermission(packageName, this.mContext.getUserId(), permission);
        }
    }

    public void setBindAppWidgetPermission(String packageName, int userId, boolean permission) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService != null) {
            try {
                iAppWidgetService.setBindAppWidgetPermission(packageName, userId, permission);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @UnsupportedAppUsage
    public boolean bindRemoteViewsService(Context context, int appWidgetId, Intent intent, IServiceConnection connection, int flags) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return false;
        }
        try {
            return iAppWidgetService.bindRemoteViewsService(context.getOpPackageName(), appWidgetId, intent, context.getIApplicationThread(), context.getActivityToken(), connection, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int[] getAppWidgetIds(ComponentName provider) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return new int[0];
        }
        try {
            return iAppWidgetService.getAppWidgetIds(provider);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isBoundWidgetPackage(String packageName, int userId) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return false;
        }
        try {
            return iAppWidgetService.isBoundWidgetPackage(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    private boolean bindAppWidgetIdIfAllowed(int appWidgetId, int profileId, ComponentName provider, Bundle options) {
        IAppWidgetService iAppWidgetService = this.mService;
        if (iAppWidgetService == null) {
            return false;
        }
        try {
            return iAppWidgetService.bindAppWidgetId(this.mPackageName, appWidgetId, profileId, provider, options);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isRequestPinAppWidgetSupported() {
        try {
            return this.mService.isRequestPinAppWidgetSupported();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean requestPinAppWidget(ComponentName provider, PendingIntent successCallback) {
        return requestPinAppWidget(provider, null, successCallback);
    }

    public boolean requestPinAppWidget(ComponentName provider, Bundle extras, PendingIntent successCallback) {
        try {
            return this.mService.requestPinAppWidget(this.mPackageName, provider, extras, successCallback == null ? null : successCallback.getIntentSender());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
