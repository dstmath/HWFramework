package android.media.projection;

import android.content.Context;
import android.content.Intent;
import android.media.projection.IMediaProjectionManager.Stub;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import java.util.Map;

public final class MediaProjectionManager {
    public static final String EXTRA_APP_TOKEN = "android.media.projection.extra.EXTRA_APP_TOKEN";
    public static final String EXTRA_MEDIA_PROJECTION = "android.media.projection.extra.EXTRA_MEDIA_PROJECTION";
    private static final String TAG = "MediaProjectionManager";
    public static final int TYPE_MIRRORING = 1;
    public static final int TYPE_PRESENTATION = 2;
    public static final int TYPE_SCREEN_CAPTURE = 0;
    private Map<Callback, CallbackDelegate> mCallbacks = new ArrayMap();
    private Context mContext;
    private IMediaProjectionManager mService = Stub.asInterface(ServiceManager.getService(Context.MEDIA_PROJECTION_SERVICE));

    public static abstract class Callback {
        public abstract void onStart(MediaProjectionInfo mediaProjectionInfo);

        public abstract void onStop(MediaProjectionInfo mediaProjectionInfo);
    }

    private static final class CallbackDelegate extends IMediaProjectionWatcherCallback.Stub {
        private Callback mCallback;
        private Handler mHandler;

        public CallbackDelegate(Callback callback, Handler handler) {
            this.mCallback = callback;
            if (handler == null) {
                handler = new Handler();
            }
            this.mHandler = handler;
        }

        public void onStart(final MediaProjectionInfo info) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CallbackDelegate.this.mCallback.onStart(info);
                }
            });
        }

        public void onStop(final MediaProjectionInfo info) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CallbackDelegate.this.mCallback.onStop(info);
                }
            });
        }
    }

    public MediaProjectionManager(Context context) {
        this.mContext = context;
    }

    public Intent createScreenCaptureIntent() {
        Intent i = new Intent();
        i.setClassName("com.android.systemui", "com.android.systemui.media.MediaProjectionPermissionActivity");
        return i;
    }

    public MediaProjection getMediaProjection(int resultCode, Intent resultData) {
        if (resultCode != -1 || resultData == null) {
            return null;
        }
        IBinder projection = resultData.getIBinderExtra(EXTRA_MEDIA_PROJECTION);
        if (projection == null) {
            return null;
        }
        return new MediaProjection(this.mContext, IMediaProjection.Stub.asInterface(projection));
    }

    public MediaProjectionInfo getActiveProjectionInfo() {
        try {
            return this.mService.getActiveProjectionInfo();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get the active projection info", e);
            return null;
        }
    }

    public void stopActiveProjection() {
        try {
            this.mService.stopActiveProjection();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to stop the currently active media projection", e);
        }
    }

    public void addCallback(Callback callback, Handler handler) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        CallbackDelegate delegate = new CallbackDelegate(callback, handler);
        this.mCallbacks.put(callback, delegate);
        try {
            this.mService.addCallback(delegate);
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to add callbacks to MediaProjection service", e);
        }
    }

    public void removeCallback(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        CallbackDelegate delegate = (CallbackDelegate) this.mCallbacks.remove(callback);
        if (delegate != null) {
            try {
                this.mService.removeCallback(delegate);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to add callbacks to MediaProjection service", e);
            }
        }
    }
}
