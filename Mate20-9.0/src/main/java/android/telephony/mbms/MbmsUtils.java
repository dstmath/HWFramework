package android.telephony.mbms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.telecom.Logging.Session;
import android.telephony.MbmsDownloadSession;
import android.telephony.MbmsStreamingSession;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MbmsUtils {
    private static final String LOG_TAG = "MbmsUtils";

    public static boolean isContainedIn(File parent, File child) {
        try {
            return child.getCanonicalPath().startsWith(parent.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to resolve canonical paths: " + e);
        }
    }

    public static ComponentName toComponentName(ComponentInfo ci) {
        return new ComponentName(ci.packageName, ci.name);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0034 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0035 A[SYNTHETIC, Splitter:B:17:0x0035] */
    public static ComponentName getOverrideServiceName(Context context, String serviceAction) {
        char c;
        String metaDataKey = null;
        int hashCode = serviceAction.hashCode();
        if (hashCode != -1374878107) {
            if (hashCode == -407466459 && serviceAction.equals(MbmsDownloadSession.MBMS_DOWNLOAD_SERVICE_ACTION)) {
                c = 0;
                switch (c) {
                    case 0:
                        metaDataKey = MbmsDownloadSession.MBMS_DOWNLOAD_SERVICE_OVERRIDE_METADATA;
                        break;
                    case 1:
                        metaDataKey = MbmsStreamingSession.MBMS_STREAMING_SERVICE_OVERRIDE_METADATA;
                        break;
                }
                if (metaDataKey == null) {
                    return null;
                }
                try {
                    ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
                    if (appInfo.metaData == null) {
                        return null;
                    }
                    String serviceComponent = appInfo.metaData.getString(metaDataKey);
                    if (serviceComponent == null) {
                        return null;
                    }
                    return ComponentName.unflattenFromString(serviceComponent);
                } catch (PackageManager.NameNotFoundException e) {
                    return null;
                }
            }
        } else if (serviceAction.equals(MbmsStreamingSession.MBMS_STREAMING_SERVICE_ACTION)) {
            c = 1;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            if (metaDataKey == null) {
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
        if (metaDataKey == null) {
        }
    }

    public static ServiceInfo getMiddlewareServiceInfo(Context context, String serviceAction) {
        List<ResolveInfo> services;
        PackageManager packageManager = context.getPackageManager();
        Intent queryIntent = new Intent();
        queryIntent.setAction(serviceAction);
        ComponentName overrideService = getOverrideServiceName(context, serviceAction);
        if (overrideService == null) {
            services = packageManager.queryIntentServices(queryIntent, 1048576);
        } else {
            queryIntent.setComponent(overrideService);
            services = packageManager.queryIntentServices(queryIntent, 131072);
        }
        if (services == null || services.size() == 0) {
            Log.w(LOG_TAG, "No MBMS services found, cannot get service info");
            return null;
        } else if (services.size() <= 1) {
            return services.get(0).serviceInfo;
        } else {
            Log.w(LOG_TAG, "More than one MBMS service found, cannot get unique service");
            return null;
        }
    }

    public static int startBinding(Context context, String serviceAction, ServiceConnection serviceConnection) {
        Intent bindIntent = new Intent();
        ServiceInfo mbmsServiceInfo = getMiddlewareServiceInfo(context, serviceAction);
        if (mbmsServiceInfo == null) {
            return 1;
        }
        bindIntent.setComponent(toComponentName(mbmsServiceInfo));
        context.bindService(bindIntent, serviceConnection, 1);
        return 0;
    }

    public static File getEmbmsTempFileDirForService(Context context, String serviceId) {
        return new File(MbmsTempFileProvider.getEmbmsTempFileDir(context), serviceId.replaceAll("[^a-zA-Z0-9_]", Session.SESSION_SEPARATION_CHAR_CHILD));
    }
}
