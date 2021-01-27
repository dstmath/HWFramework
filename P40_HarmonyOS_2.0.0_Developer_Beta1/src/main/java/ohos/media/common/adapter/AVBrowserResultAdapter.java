package ohos.media.common.adapter;

import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.service.media.MediaBrowserService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import ohos.media.common.AVDescription;
import ohos.media.common.sessioncore.AVBrowserResult;
import ohos.media.common.sessioncore.AVElement;
import ohos.media.common.sessioncore.delegate.IAVBrowserResultDelegate;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVBrowserResultAdapter<T> implements IAVBrowserResultDelegate {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVBrowserResultAdapter.class);
    private final MediaBrowserService.Result<T> hostResult;
    private AVBrowserResult result;

    public AVBrowserResultAdapter(MediaBrowserService.Result<T> result2) {
        this.hostResult = result2;
    }

    private static AVBrowserResult convertResult(MediaBrowserService.Result<?> result2, AVBrowserResultAdapter<?> aVBrowserResultAdapter) {
        return new AVBrowserResult(reflectResultDebug(MediaBrowserService.Result.class, "mDebug", result2), aVBrowserResultAdapter);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0032  */
    private static Object reflectResultDebug(Class<?> cls, String str, MediaBrowserService.Result<?> result2) {
        Throwable th;
        Field field;
        ReflectiveOperationException e;
        try {
            field = cls.getDeclaredField(str);
            try {
                field.setAccessible(true);
                Object obj = field.get(result2);
                field.setAccessible(false);
                return obj;
            } catch (IllegalAccessException | NoSuchFieldException e2) {
                e = e2;
                try {
                    LOGGER.error("reflectResultDebug failed, e: %{public}s", e.toString());
                    if (field != null) {
                        field.setAccessible(false);
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (field != null) {
                        field.setAccessible(false);
                    }
                    throw th;
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e3) {
            e = e3;
            field = null;
            LOGGER.error("reflectResultDebug failed, e: %{public}s", e.toString());
            if (field != null) {
            }
            return null;
        } catch (Throwable th3) {
            th = th3;
            field = null;
            if (field != null) {
            }
            throw th;
        }
    }

    public static List<MediaBrowser.MediaItem> convertItems(List<AVElement> list) {
        ArrayList arrayList = new ArrayList(list.size());
        for (AVElement aVElement : list) {
            arrayList.add(convertItem(aVElement));
        }
        return arrayList;
    }

    public static MediaBrowser.MediaItem convertItem(AVElement aVElement) {
        return new MediaBrowser.MediaItem(convertDescription(aVElement.getAVDescription()), aVElement.getFlags());
    }

    private static MediaDescription convertDescription(AVDescription aVDescription) {
        return AVDescriptionAdapter.getMediaDescription(aVDescription);
    }

    public AVBrowserResult getResult() {
        if (this.result == null) {
            this.result = convertResult(this.hostResult, this);
        }
        return this.result;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: android.service.media.MediaBrowserService$Result<T> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // ohos.media.common.sessioncore.delegate.IAVBrowserResultDelegate
    public void sendAVElement(AVElement aVElement) {
        try {
            this.hostResult.sendResult(convertItem(aVElement));
        } catch (IllegalStateException unused) {
            throw new IllegalStateException("sendAVElement called twice");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: android.service.media.MediaBrowserService$Result<T> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // ohos.media.common.sessioncore.delegate.IAVBrowserResultDelegate
    public void sendAVElementList(List<AVElement> list) {
        try {
            this.hostResult.sendResult(convertItems(list));
        } catch (IllegalStateException unused) {
            throw new IllegalStateException("sendAVElementList called twice");
        }
    }

    @Override // ohos.media.common.sessioncore.delegate.IAVBrowserResultDelegate
    public void detachForRetrieveAsync() {
        try {
            this.hostResult.detach();
        } catch (IllegalStateException unused) {
            throw new IllegalStateException("detachForRetrieveAsync called twice or called after sendAVElement or sendAVElementList has been called");
        }
    }
}
