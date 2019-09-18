package com.android.server.rms.iaware.dev;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import com.huawei.pgmng.plug.PGSdk;
import java.util.List;

public class DevSchedUtil {
    public static final int INVALID_UID = -1;

    public static boolean isInvalidAppInfo(String processName, int uid, int pid) {
        return processName == null || pid < 0 || uid <= 1000;
    }

    public static SceneInfo getSceneInfo(int sceneId, List<SceneInfo> sceneList) {
        if (sceneList == null || sceneList.size() == 0 || -1 == sceneId) {
            return null;
        }
        for (SceneInfo scene : sceneList) {
            if (scene != null && scene.getSceneId() == sceneId) {
                return scene;
            }
        }
        return null;
    }

    public static String getTopFrontApp(Context context) {
        if (context == null) {
            return null;
        }
        PGSdk pgsdk = PGSdk.getInstance();
        if (pgsdk == null) {
            return null;
        }
        try {
            return pgsdk.getTopFrontApp(context);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static int getUidByPkgName(String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return -1;
        }
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkgName, 0, ActivityManager.getCurrentUser());
            if (ai != null) {
                return ai.uid;
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }
}
