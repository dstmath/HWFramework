package android.appwidget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
    private final DisplayMetrics mDisplayMetrics;
    private final String mPackageName;
    private final IAppWidgetService mService;

    public static AppWidgetManager getInstance(Context context) {
        return (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
    }

    public AppWidgetManager(Context context, IAppWidgetService service) {
        this.mPackageName = context.getOpPackageName();
        this.mService = service;
        this.mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    public void updateAppWidget(int[] appWidgetIds, RemoteViews views) {
        if (this.mService != null) {
            try {
                this.mService.updateAppWidgetIds(this.mPackageName, appWidgetIds, views);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void updateAppWidgetOptions(int appWidgetId, Bundle options) {
        if (this.mService != null) {
            try {
                this.mService.updateAppWidgetOptions(this.mPackageName, appWidgetId, options);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public Bundle getAppWidgetOptions(int appWidgetId) {
        if (this.mService == null) {
            return Bundle.EMPTY;
        }
        try {
            return this.mService.getAppWidgetOptions(this.mPackageName, appWidgetId);
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
        if (this.mService != null) {
            try {
                this.mService.partiallyUpdateAppWidgetIds(this.mPackageName, appWidgetIds, views);
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
        if (this.mService != null) {
            try {
                this.mService.updateAppWidgetProvider(provider, views);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void notifyAppWidgetViewDataChanged(int[] appWidgetIds, int viewId) {
        if (this.mService != null) {
            try {
                this.mService.notifyAppWidgetViewDataChanged(this.mPackageName, appWidgetIds, viewId);
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
        return getInstalledProvidersForProfile(1, profile);
    }

    public List<AppWidgetProviderInfo> getInstalledProviders() {
        if (this.mService == null) {
            return Collections.emptyList();
        }
        return getInstalledProvidersForProfile(1, null);
    }

    public List<AppWidgetProviderInfo> getInstalledProviders(int categoryFilter) {
        if (this.mService == null) {
            return Collections.emptyList();
        }
        return getInstalledProvidersForProfile(categoryFilter, null);
    }

    public List<AppWidgetProviderInfo> getInstalledProvidersForProfile(int categoryFilter, UserHandle profile) {
        if (this.mService == null) {
            return Collections.emptyList();
        }
        if (profile == null) {
            profile = Process.myUserHandle();
        }
        try {
            ParceledListSlice<AppWidgetProviderInfo> providers = this.mService.getInstalledProvidersForProfile(categoryFilter, profile.getIdentifier());
            if (providers == null) {
                return Collections.emptyList();
            }
            for (AppWidgetProviderInfo info : providers.getList()) {
                convertSizesToPixels(info);
            }
            return providers.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        if (this.mService == null) {
            return null;
        }
        try {
            AppWidgetProviderInfo info = this.mService.getAppWidgetInfo(this.mPackageName, appWidgetId);
            if (info != null) {
                convertSizesToPixels(info);
            }
            return info;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void bindAppWidgetId(int appWidgetId, ComponentName provider) {
        if (this.mService != null) {
            bindAppWidgetId(appWidgetId, provider, null);
        }
    }

    public void bindAppWidgetId(int appWidgetId, ComponentName provider, Bundle options) {
        if (this.mService != null) {
            bindAppWidgetIdIfAllowed(appWidgetId, Process.myUserHandle(), provider, options);
        }
    }

    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, ComponentName provider) {
        if (this.mService == null) {
            return false;
        }
        return bindAppWidgetIdIfAllowed(appWidgetId, UserHandle.myUserId(), provider, null);
    }

    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, ComponentName provider, Bundle options) {
        if (this.mService == null) {
            return false;
        }
        return bindAppWidgetIdIfAllowed(appWidgetId, UserHandle.myUserId(), provider, options);
    }

    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, UserHandle user, ComponentName provider, Bundle options) {
        if (this.mService == null) {
            return false;
        }
        return bindAppWidgetIdIfAllowed(appWidgetId, user.getIdentifier(), provider, options);
    }

    public boolean hasBindAppWidgetPermission(String packageName, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasBindAppWidgetPermission(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasBindAppWidgetPermission(String packageName) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasBindAppWidgetPermission(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBindAppWidgetPermission(String packageName, boolean permission) {
        if (this.mService != null) {
            setBindAppWidgetPermission(packageName, UserHandle.myUserId(), permission);
        }
    }

    public void setBindAppWidgetPermission(String packageName, int userId, boolean permission) {
        if (this.mService != null) {
            try {
                this.mService.setBindAppWidgetPermission(packageName, userId, permission);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void bindRemoteViewsService(String packageName, int appWidgetId, Intent intent, IBinder connection) {
        if (this.mService != null) {
            try {
                this.mService.bindRemoteViewsService(packageName, appWidgetId, intent, connection);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void unbindRemoteViewsService(String packageName, int appWidgetId, Intent intent) {
        if (this.mService != null) {
            try {
                this.mService.unbindRemoteViewsService(packageName, appWidgetId, intent);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int[] getAppWidgetIds(ComponentName provider) {
        if (this.mService == null) {
            return new int[INVALID_APPWIDGET_ID];
        }
        try {
            return this.mService.getAppWidgetIds(provider);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isBoundWidgetPackage(String packageName, int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isBoundWidgetPackage(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean bindAppWidgetIdIfAllowed(int appWidgetId, int profileId, ComponentName provider, Bundle options) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.bindAppWidgetId(this.mPackageName, appWidgetId, profileId, provider, options);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void convertSizesToPixels(AppWidgetProviderInfo info) {
        info.minWidth = TypedValue.complexToDimensionPixelSize(info.minWidth, this.mDisplayMetrics);
        info.minHeight = TypedValue.complexToDimensionPixelSize(info.minHeight, this.mDisplayMetrics);
        info.minResizeWidth = TypedValue.complexToDimensionPixelSize(info.minResizeWidth, this.mDisplayMetrics);
        info.minResizeHeight = TypedValue.complexToDimensionPixelSize(info.minResizeHeight, this.mDisplayMetrics);
    }
}
