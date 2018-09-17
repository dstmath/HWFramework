package com.android.server.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.media.IMediaResourceMonitor.Stub;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.SystemService;

public class MediaResourceMonitorService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String SERVICE_NAME = "media_resource_monitor";
    private static final String TAG = "MediaResourceMonitor";
    private final MediaResourceMonitorImpl mMediaResourceMonitorImpl;

    class MediaResourceMonitorImpl extends Stub {
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
                    Intent intent = new Intent("android.intent.action.MEDIA_RESOURCE_GRANTED");
                    intent.putExtra("android.intent.extra.PACKAGES", pkgNames);
                    intent.putExtra("android.intent.extra.MEDIA_RESOURCE_TYPE", type);
                    MediaResourceMonitorService.this.getContext().sendBroadcastAsUser(intent, new UserHandle(ActivityManager.getCurrentUser()), "android.permission.RECEIVE_MEDIA_RESOURCE_USAGE");
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private String[] getPackageNamesFromPid(int pid) {
            try {
                for (RunningAppProcessInfo proc : ActivityManagerNative.getDefault().getRunningAppProcesses()) {
                    if (proc.pid == pid) {
                        return proc.pkgList;
                    }
                }
            } catch (RemoteException e) {
                Slog.w(MediaResourceMonitorService.TAG, "ActivityManager.getRunningAppProcesses() failed");
            }
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.media.MediaResourceMonitorService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.media.MediaResourceMonitorService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.media.MediaResourceMonitorService.<clinit>():void");
    }

    public MediaResourceMonitorService(Context context) {
        super(context);
        this.mMediaResourceMonitorImpl = new MediaResourceMonitorImpl();
    }

    public void onStart() {
        publishBinderService(SERVICE_NAME, this.mMediaResourceMonitorImpl);
    }
}
