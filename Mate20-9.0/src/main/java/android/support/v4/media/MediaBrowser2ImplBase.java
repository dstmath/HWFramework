package android.support.v4.media;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowser2;
import android.util.Log;
import java.util.concurrent.Executor;

class MediaBrowser2ImplBase extends MediaController2ImplBase implements MediaBrowser2.SupportLibraryImpl {
    MediaBrowser2ImplBase(Context context, MediaController2 instance, SessionToken2 token, Executor executor, MediaBrowser2.BrowserCallback callback) {
        super(context, instance, token, executor, callback);
    }

    public MediaBrowser2.BrowserCallback getCallback() {
        return (MediaBrowser2.BrowserCallback) super.getCallback();
    }

    public void getLibraryRoot(Bundle rootHints) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(31);
        if (iSession2 != null) {
            try {
                iSession2.getLibraryRoot(this.mControllerStub, rootHints);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void subscribe(String parentId, Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(34);
        if (iSession2 != null) {
            try {
                iSession2.subscribe(this.mControllerStub, parentId, extras);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void unsubscribe(String parentId) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(35);
        if (iSession2 != null) {
            try {
                iSession2.unsubscribe(this.mControllerStub, parentId);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void getChildren(String parentId, int page, int pageSize, Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(29);
        if (iSession2 != null) {
            try {
                iSession2.getChildren(this.mControllerStub, parentId, page, pageSize, extras);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void getItem(String mediaId) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(30);
        if (iSession2 != null) {
            try {
                iSession2.getItem(this.mControllerStub, mediaId);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void search(String query, Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(33);
        if (iSession2 != null) {
            try {
                iSession2.search(this.mControllerStub, query, extras);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }

    public void getSearchResult(String query, int page, int pageSize, Bundle extras) {
        IMediaSession2 iSession2 = getSessionInterfaceIfAble(32);
        if (iSession2 != null) {
            try {
                iSession2.getSearchResult(this.mControllerStub, query, page, pageSize, extras);
            } catch (RemoteException e) {
                Log.w("MC2ImplBase", "Cannot connect to the service or the session is gone", e);
            }
        }
    }
}
