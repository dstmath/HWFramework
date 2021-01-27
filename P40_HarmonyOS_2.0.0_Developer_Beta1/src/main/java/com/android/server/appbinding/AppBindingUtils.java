package com.android.server.appbinding;

import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.util.Log;
import java.util.List;

public class AppBindingUtils {
    private static final String TAG = "AppBindingUtils";

    private AppBindingUtils() {
    }

    public static ServiceInfo findService(String packageName, int userId, String serviceAction, String servicePermission, Class<?> serviceClassForLogging, IPackageManager ipm, StringBuilder errorMessage) {
        String simpleClassName = serviceClassForLogging.getSimpleName();
        Intent intent = new Intent(serviceAction);
        intent.setPackage(packageName);
        errorMessage.setLength(0);
        try {
            ParceledListSlice<ResolveInfo> pls = ipm.queryIntentServices(intent, (String) null, 0, userId);
            if (pls != null) {
                if (pls.getList().size() != 0) {
                    List<ResolveInfo> list = pls.getList();
                    if (list.size() > 1) {
                        errorMessage.append("More than one " + simpleClassName + "'s found in package " + packageName + ".  They'll all be ignored.");
                        Log.e(TAG, errorMessage.toString());
                        return null;
                    }
                    ServiceInfo si = list.get(0).serviceInfo;
                    if (servicePermission.equals(si.permission)) {
                        return si;
                    }
                    errorMessage.append(simpleClassName + " " + si.getComponentName().flattenToShortString() + " must be protected with " + servicePermission + ".");
                    Log.e(TAG, errorMessage.toString());
                    return null;
                }
            }
            errorMessage.append("Service with " + serviceAction + " not found.");
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }
}
