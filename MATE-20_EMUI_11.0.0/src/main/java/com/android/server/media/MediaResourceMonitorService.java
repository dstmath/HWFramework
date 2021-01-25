package com.android.server.media;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.IMediaResourceMonitor;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.Slog;
import com.android.server.SystemService;

public class MediaResourceMonitorService extends SystemService {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String SERVICE_NAME = "media_resource_monitor";
    private static final String TAG = "MediaResourceMonitor";
    private final MediaResourceMonitorImpl mMediaResourceMonitorImpl = new MediaResourceMonitorImpl();

    public MediaResourceMonitorService(Context context) {
        super(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.media.MediaResourceMonitorService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.media.MediaResourceMonitorService$MediaResourceMonitorImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService(SERVICE_NAME, this.mMediaResourceMonitorImpl);
    }

    class MediaResourceMonitorImpl extends IMediaResourceMonitor.Stub {
        MediaResourceMonitorImpl() {
        }

        public void notifyResourceGranted(int pid, int type) throws RemoteException {
            if (MediaResourceMonitorService.DEBUG) {
                Slog.d(MediaResourceMonitorService.TAG, "notifyResourceGranted(pid=" + pid + ", type=" + type + ")");
            }
            long identity = Binder.clearCallingIdentity();
            try {
                String[] pkgNames = getPackageNamesFromPid(pid);
                if (pkgNames != null) {
                    int[] userIds = ((UserManager) MediaResourceMonitorService.this.getContext().getSystemService("user")).getEnabledProfileIds(ActivityManager.getCurrentUser());
                    if (userIds == null || userIds.length == 0) {
                        Binder.restoreCallingIdentity(identity);
                        return;
                    }
                    Intent intent = new Intent("android.intent.action.MEDIA_RESOURCE_GRANTED");
                    intent.putExtra("android.intent.extra.PACKAGES", pkgNames);
                    intent.putExtra("android.intent.extra.MEDIA_RESOURCE_TYPE", type);
                    for (int userId : userIds) {
                        MediaResourceMonitorService.this.getContext().sendBroadcastAsUser(intent, UserHandle.of(userId), "android.permission.RECEIVE_MEDIA_RESOURCE_USAGE");
                    }
                    Binder.restoreCallingIdentity(identity);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private String[] getPackageNamesFromPid(int pid) {
            try {
                for (ActivityManager.RunningAppProcessInfo proc : ActivityManager.getService().getRunningAppProcesses()) {
                    if (proc.pid == pid) {
                        return proc.pkgList;
                    }
                }
                return null;
            } catch (RemoteException e) {
                Slog.w(MediaResourceMonitorService.TAG, "ActivityManager.getRunningAppProcesses() failed");
                return null;
            }
        }
    }
}
