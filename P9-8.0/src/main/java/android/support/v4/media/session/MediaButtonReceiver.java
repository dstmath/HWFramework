package android.support.v4.media.session;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.os.BuildCompat;
import android.util.Log;
import android.view.KeyEvent;
import java.util.List;

public class MediaButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaButtonReceiver";

    private static class MediaButtonConnectionCallback extends ConnectionCallback {
        private final Context mContext;
        private final Intent mIntent;
        private MediaBrowserCompat mMediaBrowser;
        private final PendingResult mPendingResult;

        MediaButtonConnectionCallback(Context context, Intent intent, PendingResult pendingResult) {
            this.mContext = context;
            this.mIntent = intent;
            this.mPendingResult = pendingResult;
        }

        void setMediaBrowser(MediaBrowserCompat mediaBrowser) {
            this.mMediaBrowser = mediaBrowser;
        }

        public void onConnected() {
            try {
                new MediaControllerCompat(this.mContext, this.mMediaBrowser.getSessionToken()).dispatchMediaButtonEvent((KeyEvent) this.mIntent.getParcelableExtra("android.intent.extra.KEY_EVENT"));
            } catch (RemoteException e) {
                Log.e(MediaButtonReceiver.TAG, "Failed to create a media controller", e);
            }
            finish();
        }

        public void onConnectionSuspended() {
            finish();
        }

        public void onConnectionFailed() {
            finish();
        }

        private void finish() {
            this.mMediaBrowser.disconnect();
            this.mPendingResult.finish();
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null && ("android.intent.action.MEDIA_BUTTON".equals(intent.getAction()) ^ 1) == 0 && (intent.hasExtra("android.intent.extra.KEY_EVENT") ^ 1) == 0) {
            ComponentName mediaButtonServiceComponentName = getServiceComponentByAction(context, "android.intent.action.MEDIA_BUTTON");
            if (mediaButtonServiceComponentName != null) {
                intent.setComponent(mediaButtonServiceComponentName);
                startForegroundService(context, intent);
                return;
            }
            ComponentName mediaBrowserServiceComponentName = getServiceComponentByAction(context, MediaBrowserServiceCompat.SERVICE_INTERFACE);
            if (mediaBrowserServiceComponentName != null) {
                PendingResult pendingResult = goAsync();
                Context applicationContext = context.getApplicationContext();
                MediaButtonConnectionCallback connectionCallback = new MediaButtonConnectionCallback(applicationContext, intent, pendingResult);
                MediaBrowserCompat mediaBrowser = new MediaBrowserCompat(applicationContext, mediaBrowserServiceComponentName, connectionCallback, null);
                connectionCallback.setMediaBrowser(mediaBrowser);
                mediaBrowser.connect();
                return;
            }
            throw new IllegalStateException("Could not find any Service that handles android.intent.action.MEDIA_BUTTON or implements a media browser service.");
        }
        Log.d(TAG, "Ignore unsupported intent: " + intent);
    }

    public static KeyEvent handleIntent(MediaSessionCompat mediaSessionCompat, Intent intent) {
        if (mediaSessionCompat == null || intent == null || ("android.intent.action.MEDIA_BUTTON".equals(intent.getAction()) ^ 1) != 0 || (intent.hasExtra("android.intent.extra.KEY_EVENT") ^ 1) != 0) {
            return null;
        }
        KeyEvent ke = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
        mediaSessionCompat.getController().dispatchMediaButtonEvent(ke);
        return ke;
    }

    public static PendingIntent buildMediaButtonPendingIntent(Context context, long action) {
        ComponentName mbrComponent = getMediaButtonReceiverComponent(context);
        if (mbrComponent != null) {
            return buildMediaButtonPendingIntent(context, mbrComponent, action);
        }
        Log.w(TAG, "A unique media button receiver could not be found in the given context, so couldn't build a pending intent.");
        return null;
    }

    public static PendingIntent buildMediaButtonPendingIntent(Context context, ComponentName mbrComponent, long action) {
        if (mbrComponent == null) {
            Log.w(TAG, "The component name of media button receiver should be provided.");
            return null;
        }
        int keyCode = PlaybackStateCompat.toKeyCode(action);
        if (keyCode == 0) {
            Log.w(TAG, "Cannot build a media button pending intent with the given action: " + action);
            return null;
        }
        Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
        intent.setComponent(mbrComponent);
        intent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, keyCode));
        return PendingIntent.getBroadcast(context, keyCode, intent, 0);
    }

    static ComponentName getMediaButtonReceiverComponent(Context context) {
        Intent queryIntent = new Intent("android.intent.action.MEDIA_BUTTON");
        queryIntent.setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryBroadcastReceivers(queryIntent, 0);
        if (resolveInfos.size() == 1) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(0);
            return new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
        }
        if (resolveInfos.size() > 1) {
            Log.w(TAG, "More than one BroadcastReceiver that handles android.intent.action.MEDIA_BUTTON was found, returning null.");
        }
        return null;
    }

    private static void startForegroundService(Context context, Intent intent) {
        if (BuildCompat.isAtLeastO()) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private static ComponentName getServiceComponentByAction(Context context, String action) {
        PackageManager pm = context.getPackageManager();
        Intent queryIntent = new Intent(action);
        queryIntent.setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(queryIntent, 0);
        if (resolveInfos.size() == 1) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(0);
            return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
        } else if (resolveInfos.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Expected 1 service that handles " + action + ", found " + resolveInfos.size());
        }
    }
}
