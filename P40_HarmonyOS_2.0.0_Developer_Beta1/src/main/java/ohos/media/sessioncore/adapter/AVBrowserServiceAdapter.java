package ohos.media.sessioncore.adapter;

import android.content.pm.ParceledListSlice;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.service.media.IMediaBrowserService;
import android.service.media.IMediaBrowserServiceCallbacks;
import android.service.media.MediaBrowserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.app.GeneralReceiver;
import ohos.media.common.adapter.AVBrowserResultAdapter;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.common.sessioncore.AVSubscriptionCallback;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.common.utils.AVUtils;
import ohos.media.sessioncore.adapter.IAVBrowserService;
import ohos.media.sessioncore.delegate.IAVBrowserServiceDelegate;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;

public class AVBrowserServiceAdapter implements IAVBrowserService {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVBrowserServiceAdapter.class);
    private static AVBrowserServiceAdapter instance;
    private IMediaBrowserService browserService;
    private final Handler receiverHandler;
    private final Map<IAVBrowserService.Callback, IMediaBrowserServiceCallbacks> serviceCallbackBinderMap = new HashMap();
    private Wrapper serviceWrapper;
    private final Map<AVSubscriptionCallback, IBinder> subscriptionBinderMap = new HashMap();

    private AVBrowserServiceAdapter() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        this.receiverHandler = new Handler();
    }

    public static synchronized AVBrowserServiceAdapter getInstance() {
        AVBrowserServiceAdapter aVBrowserServiceAdapter;
        synchronized (AVBrowserServiceAdapter.class) {
            if (instance == null) {
                instance = new AVBrowserServiceAdapter();
            }
            aVBrowserServiceAdapter = instance;
        }
        return aVBrowserServiceAdapter;
    }

    private static IMediaBrowserServiceCallbacks convertCallback(final IAVBrowserService.Callback callback) {
        return new IMediaBrowserServiceCallbacks.Stub() {
            /* class ohos.media.sessioncore.adapter.AVBrowserServiceAdapter.AnonymousClass1 */

            public void onConnect(String str, MediaSession.Token token, Bundle bundle) throws RemoteException {
                AVBrowserServiceAdapter.LOGGER.debug("Success connect to browser service", new Object[0]);
                IAVBrowserService.Callback.this.onConnected(str, AVConverter.convertToken(token), AVUtils.convert2PacMap(bundle));
            }

            public void onConnectFailed() throws RemoteException {
                AVBrowserServiceAdapter.LOGGER.debug("Failed connect to browser service", new Object[0]);
                IAVBrowserService.Callback.this.onConnectFailed();
            }

            public void onLoadChildren(String str, ParceledListSlice parceledListSlice) throws RemoteException {
                AVBrowserServiceAdapter.LOGGER.debug("onLoadChildren browser service", new Object[0]);
                IAVBrowserService.Callback.this.onLoadChildren(str, AVConverter.convert2MediaItemList(parceledListSlice));
            }

            public void onLoadChildrenWithOptions(String str, ParceledListSlice parceledListSlice, Bundle bundle) throws RemoteException {
                AVBrowserServiceAdapter.LOGGER.debug("onLoadChildrenWithOptions browser service", new Object[0]);
                IAVBrowserService.Callback.this.onLoadChildrenWithOptions(str, AVConverter.convert2MediaItemList(parceledListSlice), AVUtils.convert2PacMap(bundle));
            }
        };
    }

    /* access modifiers changed from: package-private */
    public void setBrowserService(IMediaBrowserService iMediaBrowserService) {
        this.browserService = iMediaBrowserService;
    }

    public void setBrowserDelegate(IAVBrowserServiceDelegate iAVBrowserServiceDelegate) {
        this.serviceWrapper = new Wrapper(iAVBrowserServiceDelegate);
    }

    /* access modifiers changed from: package-private */
    public Wrapper getServiceWrapper() {
        return this.serviceWrapper;
    }

    @Override // ohos.media.sessioncore.adapter.IAVBrowserService
    public void connect(String str, PacMap pacMap, IAVBrowserService.Callback callback) {
        try {
            LOGGER.info("connect to browser service, packageName: %{public}s", str);
            IMediaBrowserServiceCallbacks convertCallback = convertCallback(callback);
            this.serviceCallbackBinderMap.put(callback, convertCallback);
            this.browserService.connect(str, AVUtils.convert2Bundle(pacMap), convertCallback);
        } catch (RemoteException unused) {
            LOGGER.error("Failed to connect browser service, packageName: %{public}s", str);
            callback.onConnectFailed();
        }
    }

    @Override // ohos.media.sessioncore.adapter.IAVBrowserService
    public void disconnect(IAVBrowserService.Callback callback) {
        try {
            IMediaBrowserServiceCallbacks remove = this.serviceCallbackBinderMap.remove(callback);
            if (remove == null) {
                LOGGER.warn("Browser service has already disconnected", new Object[0]);
            } else {
                this.browserService.disconnect(remove);
            }
        } catch (RemoteException unused) {
            LOGGER.error("Failed to disconnect browser service", new Object[0]);
        }
    }

    @Override // ohos.media.sessioncore.adapter.IAVBrowserService
    public void addSubscription(String str, AVSubscriptionCallback aVSubscriptionCallback, PacMap pacMap, IAVBrowserService.Callback callback) {
        Binder binder = new Binder();
        this.subscriptionBinderMap.put(aVSubscriptionCallback, binder);
        try {
            IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks = this.serviceCallbackBinderMap.get(callback);
            if (iMediaBrowserServiceCallbacks == null) {
                LOGGER.warn("Browser service has already disconnected", new Object[0]);
            } else {
                this.browserService.addSubscription(str, binder, AVUtils.convert2Bundle(pacMap), iMediaBrowserServiceCallbacks);
            }
        } catch (RemoteException unused) {
            LOGGER.error("Failed to addSubscription to browser service, id: %{public}s", str);
        }
    }

    @Override // ohos.media.sessioncore.adapter.IAVBrowserService
    public void removeSubscription(String str, AVSubscriptionCallback aVSubscriptionCallback, IAVBrowserService.Callback callback) {
        IBinder remove = this.subscriptionBinderMap.remove(aVSubscriptionCallback);
        if (remove == null) {
            LOGGER.warn("No subscription to remove", new Object[0]);
            return;
        }
        try {
            IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks = this.serviceCallbackBinderMap.get(callback);
            if (iMediaBrowserServiceCallbacks == null) {
                LOGGER.warn("Browser service has already disconnected", new Object[0]);
            } else {
                this.browserService.removeSubscription(str, remove, iMediaBrowserServiceCallbacks);
            }
        } catch (RemoteException unused) {
            LOGGER.error("Failed to removeSubscription from browser service, id: %{public}s", str);
        }
    }

    @Override // ohos.media.sessioncore.adapter.IAVBrowserService
    public void getMediaItem(String str, GeneralReceiver generalReceiver, IAVBrowserService.Callback callback) {
        try {
            IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks = this.serviceCallbackBinderMap.get(callback);
            if (iMediaBrowserServiceCallbacks == null) {
                LOGGER.warn("Browser service has already disconnected", new Object[0]);
            } else {
                this.browserService.getMediaItem(str, AVConverter.convert2ResultReceiver(generalReceiver, this.receiverHandler), iMediaBrowserServiceCallbacks);
            }
        } catch (RemoteException unused) {
            LOGGER.error("Failed to getMediaItem from browser service", new Object[0]);
        }
    }

    public AVToken getAVToken() {
        return AVConverter.convertToken(this.serviceWrapper.getSessionToken());
    }

    public void setAVToken(AVToken aVToken) {
        try {
            this.serviceWrapper.setSessionToken(AVConverter.convertToken(aVToken));
        } catch (IllegalStateException unused) {
            throw new IllegalStateException("The AVToken has already been set");
        }
    }

    public PacMap getBrowserOptions() {
        try {
            return AVUtils.convert2PacMap(this.serviceWrapper.getBrowserRootHints());
        } catch (IllegalStateException unused) {
            throw new IllegalStateException("This should be called inside of onGetRoot or onLoadAVElementList or onLoadAVElement methods");
        }
    }

    public AVCallerUserInfo getCallerUserInfo() {
        try {
            return AVConverter.convertRemoteUserInfo(this.serviceWrapper.getCurrentBrowserInfo());
        } catch (IllegalStateException unused) {
            throw new IllegalStateException("This should be called inside of onGetRoot or onLoadAVElementList or onLoadAVElement methods");
        }
    }

    public void notifyAVElementListUpdated(String str) {
        this.serviceWrapper.notifyChildrenChanged(str);
    }

    public void notifyAVElementListUpdated(String str, PacMap pacMap) {
        this.serviceWrapper.notifyChildrenChanged(str, AVUtils.convert2Bundle(pacMap));
    }

    static class Wrapper extends MediaBrowserService {
        private final IAVBrowserServiceDelegate browserDelegate;

        private Wrapper(IAVBrowserServiceDelegate iAVBrowserServiceDelegate) {
            this.browserDelegate = iAVBrowserServiceDelegate;
        }

        @Override // android.service.media.MediaBrowserService, android.app.Service
        public void onCreate() {
            AVBrowserServiceAdapter.LOGGER.debug("Wrapper onCreate start", new Object[0]);
            super.onCreate();
            AVBrowserServiceAdapter.LOGGER.debug("Wrapper onCreate end", new Object[0]);
        }

        @Override // android.service.media.MediaBrowserService
        public MediaBrowserService.BrowserRoot onGetRoot(String str, int i, Bundle bundle) {
            return AVConverter.convertBrowserRoot(this.browserDelegate.onGetRoot(str, i, AVUtils.convert2PacMap(bundle)));
        }

        @Override // android.service.media.MediaBrowserService
        public void onLoadChildren(String str, MediaBrowserService.Result<List<MediaBrowser.MediaItem>> result) {
            this.browserDelegate.onLoadAVElementList(str, new AVBrowserResultAdapter(result).getResult());
        }

        @Override // android.service.media.MediaBrowserService
        public void onLoadChildren(String str, MediaBrowserService.Result<List<MediaBrowser.MediaItem>> result, Bundle bundle) {
            this.browserDelegate.onLoadAVElementList(str, new AVBrowserResultAdapter(result).getResult(), AVUtils.convert2PacMap(bundle));
        }

        @Override // android.service.media.MediaBrowserService
        public void onLoadItem(String str, MediaBrowserService.Result<MediaBrowser.MediaItem> result) {
            this.browserDelegate.onLoadAVElement(str, new AVBrowserResultAdapter(result).getResult());
        }
    }
}
