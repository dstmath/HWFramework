package android.media.projection;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioRecord;
import android.media.projection.IMediaProjectionCallback;
import android.os.Binder;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Surface;
import java.util.ArrayList;
import java.util.Map;

public final class MediaProjection {
    private static final String TAG = "MediaProjection";
    private static final String TAG_CTAIFS = "ctaifs";
    private static boolean sHwInfo = true;
    /* access modifiers changed from: private */
    public final Map<Callback, CallbackRecord> mCallbacks = new ArrayMap();
    private final Context mContext;
    private final IMediaProjection mImpl;

    public static abstract class Callback {
        public void onStop() {
        }
    }

    private static final class CallbackRecord {
        /* access modifiers changed from: private */
        public final Callback mCallback;
        private final Handler mHandler;

        public CallbackRecord(Callback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public void onStop() {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CallbackRecord.this.mCallback.onStop();
                }
            });
        }
    }

    private final class MediaProjectionCallback extends IMediaProjectionCallback.Stub {
        private MediaProjectionCallback() {
        }

        public void onStop() {
            for (CallbackRecord cbr : MediaProjection.this.mCallbacks.values()) {
                cbr.onStop();
            }
        }
    }

    public MediaProjection(Context context, IMediaProjection impl) {
        this.mContext = context;
        this.mImpl = impl;
        try {
            this.mImpl.start(new MediaProjectionCallback());
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to start media projection", e);
        }
    }

    public void registerCallback(Callback callback, Handler handler) {
        if (callback != null) {
            if (handler == null) {
                handler = new Handler();
            }
            this.mCallbacks.put(callback, new CallbackRecord(callback, handler));
            return;
        }
        throw new IllegalArgumentException("callback should not be null");
    }

    public void unregisterCallback(Callback callback) {
        if (callback != null) {
            this.mCallbacks.remove(callback);
            return;
        }
        throw new IllegalArgumentException("callback should not be null");
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int dpi, boolean isSecure, Surface surface, VirtualDisplay.Callback callback, Handler handler) {
        return ((DisplayManager) this.mContext.getSystemService(Context.DISPLAY_SERVICE)).createVirtualDisplay(this, name, width, height, dpi, surface, (isSecure ? 4 : 0) | 16 | 2, callback, handler, null);
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int dpi, int flags, Surface surface, VirtualDisplay.Callback callback, Handler handler) {
        if (sHwInfo) {
            log("createVirtualDisplay");
        }
        return ((DisplayManager) this.mContext.getSystemService(Context.DISPLAY_SERVICE)).createVirtualDisplay(this, name, width, height, dpi, surface, flags, callback, handler, null);
    }

    public AudioRecord createAudioRecord(int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes) {
        return null;
    }

    public void stop() {
        try {
            this.mImpl.stop();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to stop projection", e);
        }
    }

    public IMediaProjection getProjection() {
        return this.mImpl;
    }

    private void log(String methodName) {
        String pkgName = getCallingPackageName();
        String applicationName = getApplicationName(pkgName);
        String backgroundScreenShot = this.mContext.getString(33685929);
        Log.i(TAG_CTAIFS, " <" + applicationName + ">[" + applicationName + "][" + pkgName + "][" + methodName + "] " + backgroundScreenShot);
    }

    public String getApplicationName(String pkgName) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return (String) pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getCallingPackageName() {
        PackageManager pm = this.mContext.getPackageManager();
        int uid = Binder.getCallingUid();
        if (pm == null) {
            return String.valueOf(uid);
        }
        String[] uidPackages = pm.getPackagesForUid(uid);
        if (uidPackages == null) {
            return String.valueOf(uid);
        }
        if (uidPackages.length == 1) {
            return uidPackages[0];
        }
        ArrayList<String> resultPackage = new ArrayList<>();
        String[] pidPackages = getPackageNamesByPID(Process.myPid());
        if (pidPackages == null) {
            return String.valueOf(uid);
        }
        for (String packUid : uidPackages) {
            if (packUid != null) {
                for (String packPid : pidPackages) {
                    if (packPid != null && packUid.equals(packPid)) {
                        resultPackage.add(packPid);
                    }
                }
            }
        }
        if (resultPackage.size() == 1) {
            return resultPackage.get(0);
        }
        if (resultPackage.size() > 1) {
            String result = resultPackage.get(0);
            for (int i = 1; i < resultPackage.size(); i++) {
                result = result + "|" + resultPackage.get(i);
            }
            return result;
        }
        String name = pm.getNameForUid(uid);
        if (name != null) {
            return name;
        }
        return String.valueOf(uid);
    }

    private String[] getPackageNamesByPID(int pID) {
        PackageManager packageManager = this.mContext.getPackageManager();
        for (ActivityManager.RunningAppProcessInfo info : ((ActivityManager) this.mContext.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
            if (info != null && info.pid == pID) {
                return info.pkgList;
            }
        }
        return null;
    }
}
