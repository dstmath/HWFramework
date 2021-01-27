package com.huawei.android.media.projection;

import android.app.ActivityThread;
import android.content.Intent;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;

public class MediaProjectionManagerEx {
    private static final String TAG = "MediaProjectionManagerEx";

    private MediaProjectionManagerEx() {
    }

    public static Intent createMediaProjection() {
        Intent intent = new Intent();
        try {
            IBinder binder = ServiceManager.getService("media_projection");
            if (binder == null) {
                return intent;
            }
            IMediaProjectionManager service = IMediaProjectionManager.Stub.asInterface(binder);
            int uid = UserHandle.myUserId();
            String packageName = ActivityThread.currentPackageName();
            if (packageName != null) {
                if (!packageName.isEmpty()) {
                    IMediaProjection projection = service.createProjection(uid, packageName, 0, false);
                    if (projection != null) {
                        intent.putExtra("android.media.projection.extra.EXTRA_MEDIA_PROJECTION", projection.asBinder());
                        return intent;
                    }
                    Log.e(TAG, "createMediaProjection fail!");
                    return intent;
                }
            }
            Log.e(TAG, "package name must not be empty");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "caught exception from media projection service");
            return null;
        }
    }
}
