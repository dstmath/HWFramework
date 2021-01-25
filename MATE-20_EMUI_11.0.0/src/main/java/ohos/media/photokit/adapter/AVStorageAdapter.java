package ohos.media.photokit.adapter;

import android.provider.MediaStore;
import java.util.Set;
import ohos.app.Context;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

public class AVStorageAdapter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVStorageAdapter.class);

    public static Set<String> fetchExternalVolumeNames(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context or volumeName should not be null");
        } else if (context.getHostContext() instanceof android.content.Context) {
            return MediaStore.getExternalVolumeNames((android.content.Context) context.getHostContext());
        } else {
            throw new IllegalArgumentException("Invalid context");
        }
    }

    public static Uri fetchMediaResource(Context context, Uri uri) {
        if (context == null || uri == null) {
            throw new IllegalArgumentException("context or documentUri should not be null");
        } else if (context.getHostContext() instanceof android.content.Context) {
            try {
                return UriConverter.convertToZidaneContentUri(MediaStore.getMediaUri((android.content.Context) context.getHostContext(), UriConverter.convertToAndroidContentUri(uri)), "");
            } catch (RuntimeException e) {
                LOGGER.error("get media uri throw exception:%{public}s", e.getMessage());
                return null;
            }
        } else {
            throw new IllegalArgumentException("Invalid context");
        }
    }

    public static Uri fetchDocumentResource(Context context, Uri uri) {
        if (context == null || uri == null) {
            throw new IllegalArgumentException("context or mediaUri should not be null");
        } else if (context.getHostContext() instanceof android.content.Context) {
            try {
                return UriConverter.convertToZidaneContentUri(MediaStore.getMediaUri((android.content.Context) context.getHostContext(), UriConverter.convertToAndroidContentUri(uri)), "");
            } catch (RuntimeException e) {
                LOGGER.error("get document uri throw exception:%{public}s", e.getMessage());
                return null;
            }
        } else {
            throw new IllegalArgumentException("Invalid context");
        }
    }

    public static String fetchVersion(Context context, String str) {
        if (context == null || str == null) {
            throw new IllegalArgumentException("context or volumeName should not be null");
        } else if (context.getHostContext() instanceof android.content.Context) {
            return MediaStore.getVersion((android.content.Context) context.getHostContext(), str);
        } else {
            throw new IllegalArgumentException("invalid context");
        }
    }

    public static final class Audio {
        public static String convertNameToKey(String str) {
            return MediaStore.Audio.keyFor(str);
        }
    }
}
