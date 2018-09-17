package android.support.v4.media;

import android.content.Context;
import android.content.Intent;
import android.media.browse.MediaBrowser.MediaItem;
import android.media.session.MediaSession.Token;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.service.media.MediaBrowserService;
import android.service.media.MediaBrowserService.Result;
import android.support.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(21)
class MediaBrowserServiceCompatApi21 {

    public interface ServiceCompatProxy {
        BrowserRoot onGetRoot(String str, int i, Bundle bundle);

        void onLoadChildren(String str, ResultWrapper<List<Parcel>> resultWrapper);
    }

    static class BrowserRoot {
        final Bundle mExtras;
        final String mRootId;

        BrowserRoot(String rootId, Bundle extras) {
            this.mRootId = rootId;
            this.mExtras = extras;
        }
    }

    static class MediaBrowserServiceAdaptor extends MediaBrowserService {
        final ServiceCompatProxy mServiceProxy;

        MediaBrowserServiceAdaptor(Context context, ServiceCompatProxy serviceWrapper) {
            attachBaseContext(context);
            this.mServiceProxy = serviceWrapper;
        }

        public android.service.media.MediaBrowserService.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
            BrowserRoot browserRoot = this.mServiceProxy.onGetRoot(clientPackageName, clientUid, rootHints == null ? null : new Bundle(rootHints));
            if (browserRoot == null) {
                return null;
            }
            return new android.service.media.MediaBrowserService.BrowserRoot(browserRoot.mRootId, browserRoot.mExtras);
        }

        public void onLoadChildren(String parentId, Result<List<MediaItem>> result) {
            this.mServiceProxy.onLoadChildren(parentId, new ResultWrapper(result));
        }
    }

    static class ResultWrapper<T> {
        Result mResultObj;

        ResultWrapper(Result result) {
            this.mResultObj = result;
        }

        public void sendResult(T result) {
            if (result instanceof List) {
                this.mResultObj.sendResult(parcelListToItemList((List) result));
            } else if (result instanceof Parcel) {
                Parcel parcel = (Parcel) result;
                parcel.setDataPosition(0);
                this.mResultObj.sendResult(MediaItem.CREATOR.createFromParcel(parcel));
                parcel.recycle();
            } else {
                this.mResultObj.sendResult(null);
            }
        }

        public void detach() {
            this.mResultObj.detach();
        }

        List<MediaItem> parcelListToItemList(List<Parcel> parcelList) {
            if (parcelList == null) {
                return null;
            }
            List<MediaItem> items = new ArrayList();
            for (Parcel parcel : parcelList) {
                parcel.setDataPosition(0);
                items.add((MediaItem) MediaItem.CREATOR.createFromParcel(parcel));
                parcel.recycle();
            }
            return items;
        }
    }

    MediaBrowserServiceCompatApi21() {
    }

    public static Object createService(Context context, ServiceCompatProxy serviceProxy) {
        return new MediaBrowserServiceAdaptor(context, serviceProxy);
    }

    public static void onCreate(Object serviceObj) {
        ((MediaBrowserService) serviceObj).onCreate();
    }

    public static IBinder onBind(Object serviceObj, Intent intent) {
        return ((MediaBrowserService) serviceObj).onBind(intent);
    }

    public static void setSessionToken(Object serviceObj, Object token) {
        ((MediaBrowserService) serviceObj).setSessionToken((Token) token);
    }

    public static void notifyChildrenChanged(Object serviceObj, String parentId) {
        ((MediaBrowserService) serviceObj).notifyChildrenChanged(parentId);
    }
}
