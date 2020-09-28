package huawei.android.security.secai.hookcase.hook;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class ContentResolverHook {
    private static final int AUDIO_BEHAVIOR_ID;
    private static final int DOWN_LOAD_BEHAVIOR_ID;
    private static final int IMAGE_BEHAVIOR_ID = BehaviorIdCast.BehaviorId.DOCUMENT_CONTENTRESOLVEQUERY.getValue();
    private static final String TAG = ContentResolverHook.class.getSimpleName();
    private static final int VIDEO_BEHAVIOR_ID;

    ContentResolverHook() {
    }

    static {
        int i = IMAGE_BEHAVIOR_ID;
        AUDIO_BEHAVIOR_ID = i + 1;
        VIDEO_BEHAVIOR_ID = i + 2;
        DOWN_LOAD_BEHAVIOR_ID = i + 3;
    }

    @HookMethod(name = "query", params = {Uri.class, String[].class, Bundle.class, CancellationSignal.class}, targetClass = ContentResolver.class)
    static Cursor queryHook(ContentResolver thisObject, Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Log.i(TAG, "Call System Hook Method: ContentResolver queryHook()");
        if (uri == null) {
            return queryBackup(thisObject, null, projection, queryArgs, cancellationSignal);
        }
        String uriStr = uri.toString();
        if (uriStr.contains("content://media/external/images") || uriStr.contains("content://media/internal/images")) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(IMAGE_BEHAVIOR_ID);
            Log.i(TAG, "Call System Hook Method: ContentResolver queryHook(images)");
        } else if (uriStr.contains("content://media/external/audio") || uriStr.contains("content://media/internal/audio")) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(AUDIO_BEHAVIOR_ID);
            Log.i(TAG, "Call System Hook Method: ContentResolver queryHook(audio)");
        } else if (uriStr.contains("content://media/external/video") || uriStr.contains("content://media/internal/video")) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(VIDEO_BEHAVIOR_ID);
            Log.i(TAG, "Call System Hook Method: ContentResolver queryHook(video)");
        } else if (uriStr.contains("content://downloads/my_downloads")) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(DOWN_LOAD_BEHAVIOR_ID);
            Log.i(TAG, "Call System Hook Method: ContentResolver queryHook(download)");
        } else {
            Log.e(TAG, "Call System Hook Method: no behavior event match");
        }
        return queryBackup(thisObject, uri, projection, queryArgs, cancellationSignal);
    }

    @BackupMethod(name = "query", params = {Uri.class, String[].class, Bundle.class, CancellationSignal.class}, targetClass = ContentResolver.class)
    static Cursor queryBackup(ContentResolver thisObject, Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Hook Method: ContentResolver queryHook()");
        return null;
    }
}
