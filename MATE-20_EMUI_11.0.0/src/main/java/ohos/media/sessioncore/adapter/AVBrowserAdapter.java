package ohos.media.sessioncore.adapter;

import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.app.GeneralReceiver;
import ohos.bundle.ElementName;
import ohos.media.common.sessioncore.AVConnectionCallback;
import ohos.media.common.sessioncore.AVElement;
import ohos.media.common.sessioncore.AVElementCallback;
import ohos.media.common.sessioncore.AVSubscriptionCallback;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.sessioncore.adapter.IAVBrowserService;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;
import ohos.utils.Sequenceable;

public class AVBrowserAdapter {
    private static final int BROWSER_CONNECT_STATE_CONNECTED = 3;
    private static final int BROWSER_CONNECT_STATE_CONNECTING = 2;
    private static final int BROWSER_CONNECT_STATE_DISCONNECTED = 1;
    private static final int BROWSER_CONNECT_STATE_DISCONNECTING = 0;
    private static final int BROWSER_CONNECT_STATE_SUSPENDED = 4;
    private static final int EVENT_POST_TASK = 0;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVBrowserAdapter.class);
    private final AVConnectionCallback callback;
    private AVBrowserServiceConnection connection;
    private Runnable connectionRunnable = new Runnable() {
        /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (AVBrowserAdapter.this.state == 0) {
                AVBrowserAdapter.LOGGER.error("connect failed, state is %{public}d", Integer.valueOf(AVBrowserAdapter.this.state));
                AVBrowserAdapter.this.callback.onConnectFailed();
            } else if (AVBrowserAdapter.this.service == null && AVBrowserAdapter.this.serviceCallback == null) {
                AVBrowserAdapter.this.state = 2;
                Intent intent = new Intent();
                intent.setElement(AVBrowserAdapter.this.elementName);
                intent.setAction(AVBrowserAdapter.this.serviceAction);
                AVBrowserAdapter aVBrowserAdapter = AVBrowserAdapter.this;
                aVBrowserAdapter.connection = new AVBrowserServiceConnection();
                AVBrowserServiceHelper.connectService(AVBrowserAdapter.this.context, intent, AVBrowserAdapter.this.connection);
            } else {
                AVBrowserAdapter.LOGGER.error("connect failed, service and callback should be null", new Object[0]);
                AVBrowserAdapter.this.callback.onConnectFailed();
            }
        }
    };
    private final Context context;
    private Runnable disconnectRunnable = new Runnable() {
        /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            if (!(AVBrowserAdapter.this.service == null || AVBrowserAdapter.this.serviceCallback == null)) {
                AVBrowserAdapter.this.service.disconnect(AVBrowserAdapter.this.serviceCallback);
            }
            int i = AVBrowserAdapter.this.state;
            AVBrowserAdapter.this.forceCloseConnection();
            if (i != 0) {
                AVBrowserAdapter.this.state = i;
            }
        }
    };
    private final ElementName elementName;
    private volatile PacMap extras;
    private volatile Handler handler;
    private final PacMap rootHints;
    private volatile String rootId;
    private IAVBrowserService service;
    private volatile String serviceAction;
    private ServiceCallback serviceCallback;
    private volatile int state = 1;
    private final ArrayMap<String, Subscription> subscriptions = new ArrayMap<>();
    private volatile AVToken token;

    public AVBrowserAdapter(Context context2, ElementName elementName2, AVConnectionCallback aVConnectionCallback, PacMap pacMap, String str) {
        if (context2 == null) {
            LOGGER.error("Create AVBrowserAdapter failed, context is null", new Object[0]);
            throw new IllegalArgumentException("context cannot be null");
        } else if (elementName2 == null) {
            LOGGER.error("Create AVBrowserAdapter failed, name is null", new Object[0]);
            throw new IllegalArgumentException("element name cannot be null");
        } else if (aVConnectionCallback != null) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            this.handler = new Handler();
            this.context = context2;
            this.elementName = elementName2;
            this.callback = aVConnectionCallback;
            if (pacMap == null) {
                this.rootHints = null;
            } else {
                this.rootHints = new PacMap();
                this.rootHints.putAll(pacMap);
            }
            this.serviceAction = str;
        } else {
            LOGGER.error("Create AVBrowserAdapter failed, callback is null", new Object[0]);
            throw new IllegalArgumentException("connection callback cannot be null");
        }
    }

    public void connect() {
        if (this.state == 0 || this.state == 1) {
            this.state = 2;
            this.handler.post(this.connectionRunnable);
            return;
        }
        LOGGER.error("connect failed, state is invalid value of %{public}d", Integer.valueOf(this.state));
        throw new IllegalStateException("connect() called while neither disconnecting nor disconnected");
    }

    public void disconnect() {
        this.state = 0;
        this.handler.post(this.disconnectRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void forceCloseConnection() {
        Context context2;
        AVBrowserServiceConnection aVBrowserServiceConnection = this.connection;
        if (!(aVBrowserServiceConnection == null || (context2 = this.context) == null)) {
            AVBrowserServiceHelper.disconnectService(context2, aVBrowserServiceConnection);
        }
        this.state = 1;
        this.connection = null;
        this.service = null;
        this.serviceCallback = null;
        this.rootId = null;
        this.token = null;
    }

    public boolean isConnected() {
        return this.state == 3;
    }

    public ElementName getElementName() {
        return this.elementName;
    }

    public String getRootMediaId() {
        return this.rootId;
    }

    public PacMap getOptions() {
        return this.extras;
    }

    public AVToken getAVToken() {
        return this.token;
    }

    public void subscribeByParentMediaId(String str, AVSubscriptionCallback aVSubscriptionCallback) {
        subscribeInternal(str, null, aVSubscriptionCallback);
    }

    public void subscribeByParentMediaId(String str, PacMap pacMap, AVSubscriptionCallback aVSubscriptionCallback) {
        if (pacMap != null) {
            PacMap pacMap2 = new PacMap();
            pacMap2.putAll(pacMap);
            subscribeInternal(str, pacMap2, aVSubscriptionCallback);
            return;
        }
        LOGGER.warn("subscribe failed, options is null", new Object[0]);
        throw new IllegalArgumentException("options cannot be null");
    }

    private void subscribeInternal(final String str, final PacMap pacMap, final AVSubscriptionCallback aVSubscriptionCallback) {
        if (str == null || str.length() == 0) {
            LOGGER.error("subscribeInternal failed, parentMediaId invalid", new Object[0]);
            throw new IllegalArgumentException("parentMediaId cannot be empty");
        } else if (aVSubscriptionCallback != null) {
            Subscription subscription = this.subscriptions.get(str);
            if (subscription == null) {
                subscription = new Subscription();
                this.subscriptions.put(str, subscription);
            }
            subscription.putCallback(pacMap, aVSubscriptionCallback);
            if (this.service == null || !isConnected()) {
                LOGGER.error("subscribeInternal error, state is %{public}d", Integer.valueOf(this.state));
            } else {
                this.handler.post(new Runnable() {
                    /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        AVBrowserAdapter.this.service.addSubscription(str, aVSubscriptionCallback, pacMap, AVBrowserAdapter.this.serviceCallback);
                    }
                });
            }
        } else {
            LOGGER.error("subscribeInternal failed, callback is null", new Object[0]);
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    public void unsubscribeByParentMediaId(String str) {
        unsubscribeInternal(str, null);
    }

    public void unsubscribeByParentMediaId(String str, AVSubscriptionCallback aVSubscriptionCallback) {
        if (aVSubscriptionCallback != null) {
            unsubscribeInternal(str, aVSubscriptionCallback);
        } else {
            LOGGER.error("unsubscribe failed, callback is null", new Object[0]);
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    private void unsubscribeInternal(final String str, final AVSubscriptionCallback aVSubscriptionCallback) {
        if (str == null || str.length() == 0) {
            LOGGER.error("unsubscribeInternal failed, parentMediaId invalid", new Object[0]);
            throw new IllegalArgumentException("parentMediaId cannot be empty");
        }
        Subscription subscription = this.subscriptions.get(str);
        if (subscription == null) {
            LOGGER.warn("unsubscribeInternal error, can't find id", new Object[0]);
            return;
        }
        if (aVSubscriptionCallback != null) {
            List<AVSubscriptionCallback> callbacks = subscription.getCallbacks();
            List<PacMap> optionsList = subscription.getOptionsList();
            for (int size = callbacks.size() - 1; size >= 0; size--) {
                if (callbacks.get(size) == aVSubscriptionCallback) {
                    if (this.service != null && isConnected()) {
                        this.handler.post(new Runnable() {
                            /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass5 */

                            @Override // java.lang.Runnable
                            public void run() {
                                AVBrowserAdapter.this.service.removeSubscription(str, aVSubscriptionCallback, AVBrowserAdapter.this.serviceCallback);
                            }
                        });
                    }
                    callbacks.remove(size);
                    optionsList.remove(size);
                }
            }
        } else if (this.service != null && isConnected()) {
            this.handler.post(new Runnable() {
                /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    AVBrowserAdapter.this.service.removeSubscription(str, null, AVBrowserAdapter.this.serviceCallback);
                }
            });
        }
        if (subscription.isEmpty() || aVSubscriptionCallback == null) {
            this.subscriptions.remove(str);
        }
    }

    /* access modifiers changed from: private */
    public static class Subscription {
        public static final String EXTRA_PAGE = "android.media.browse.extra.PAGE";
        public static final String EXTRA_PAGE_SIZE = "android.media.browse.extra.PAGE_SIZE";
        private final List<AVSubscriptionCallback> callbacks = new ArrayList();
        private final List<PacMap> optionsList = new ArrayList();

        Subscription() {
        }

        public boolean isEmpty() {
            return this.callbacks.isEmpty();
        }

        public List<PacMap> getOptionsList() {
            return this.optionsList;
        }

        public List<AVSubscriptionCallback> getCallbacks() {
            return this.callbacks;
        }

        public AVSubscriptionCallback getCallback(PacMap pacMap) {
            for (int i = 0; i < this.optionsList.size(); i++) {
                if (isSameOption(this.optionsList.get(i), pacMap)) {
                    return this.callbacks.get(i);
                }
            }
            return null;
        }

        public void putCallback(PacMap pacMap, AVSubscriptionCallback aVSubscriptionCallback) {
            for (int i = 0; i < this.optionsList.size(); i++) {
                if (isSameOption(this.optionsList.get(i), pacMap)) {
                    this.callbacks.set(i, aVSubscriptionCallback);
                    return;
                }
            }
            this.callbacks.add(aVSubscriptionCallback);
            this.optionsList.add(pacMap);
        }

        /* access modifiers changed from: package-private */
        public boolean isSameOption(PacMap pacMap, PacMap pacMap2) {
            if (pacMap == pacMap2) {
                return true;
            }
            return pacMap == null ? pacMap2.getIntValue(EXTRA_PAGE, -1) == -1 && pacMap2.getIntValue(EXTRA_PAGE_SIZE, -1) == -1 : pacMap2 == null ? pacMap.getIntValue(EXTRA_PAGE, -1) == -1 && pacMap.getIntValue(EXTRA_PAGE_SIZE, -1) == -1 : pacMap.getIntValue(EXTRA_PAGE, -1) == pacMap2.getIntValue(EXTRA_PAGE, -1) && pacMap.getIntValue(EXTRA_PAGE_SIZE, -1) == pacMap2.getIntValue(EXTRA_PAGE_SIZE, -1);
        }
    }

    public void getAVElement(final String str, final AVElementCallback aVElementCallback) {
        if (str == null || str.length() == 0) {
            LOGGER.error("getElement failed, id invalid", new Object[0]);
            throw new IllegalArgumentException("mediaId cannot be empty");
        } else if (aVElementCallback == null) {
            LOGGER.error("getElement failed, elementCallback is null", new Object[0]);
            throw new IllegalArgumentException("elementCallback cannot be null");
        } else if (this.service == null) {
            LOGGER.error("getElement failed, service is null", new Object[0]);
        } else {
            this.handler.post(new Runnable() {
                /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    AVBrowserAdapter.this.service.getMediaItem(str, new GeneralReceiver() {
                        /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass6.AnonymousClass1 */

                        public void sendResult(int i, PacMap pacMap) {
                            if (AVBrowserAdapter.this.isConnected()) {
                                if (i != 0 || pacMap == null || !pacMap.hasKey("media_item")) {
                                    aVElementCallback.onError(str);
                                    return;
                                }
                                Sequenceable sequenceable = pacMap.getSequenceable("media_item").get();
                                if (sequenceable == null || (sequenceable instanceof AVElement)) {
                                    aVElementCallback.onAVElementLoaded((AVElement) sequenceable);
                                } else {
                                    aVElementCallback.onError(str);
                                }
                            }
                        }
                    }, AVBrowserAdapter.this.serviceCallback);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ServiceCallback createServiceCallback() {
        return new ServiceCallback(this);
    }

    /* access modifiers changed from: private */
    public class AVBrowserServiceConnection implements IAVBrowserService.Connection {
        private AVBrowserServiceConnection() {
        }

        @Override // ohos.media.sessioncore.adapter.IAVBrowserService.Connection
        public void onConnected(final IAVBrowserService iAVBrowserService) {
            postOrRun(new Runnable() {
                /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AVBrowserServiceConnection.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!AVBrowserServiceConnection.this.isCurrentConnection("ServiceConnection:onConnected")) {
                        AVBrowserAdapter.LOGGER.error("onConnected error, not current connection", new Object[0]);
                        return;
                    }
                    AVBrowserAdapter.this.service = iAVBrowserService;
                    AVBrowserAdapter.this.serviceCallback = AVBrowserAdapter.this.createServiceCallback();
                    AVBrowserAdapter.this.state = 2;
                    AVBrowserAdapter.this.service.connect(AVBrowserAdapter.this.context.getBundleName(), AVBrowserAdapter.this.rootHints, AVBrowserAdapter.this.serviceCallback);
                }
            });
        }

        @Override // ohos.media.sessioncore.adapter.IAVBrowserService.Connection
        public void onConnectFailed() {
            postOrRun(new Runnable() {
                /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AVBrowserServiceConnection.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!AVBrowserServiceConnection.this.isCurrentConnection("ServiceConnection:onConnectFailed")) {
                        AVBrowserAdapter.LOGGER.error("onConnectFailed error, not current connection", new Object[0]);
                        return;
                    }
                    AVBrowserAdapter.this.service = null;
                    AVBrowserAdapter.this.serviceCallback = null;
                    AVBrowserAdapter.this.state = 4;
                    AVBrowserAdapter.this.callback.onConnectFailed();
                }
            });
        }

        @Override // ohos.media.sessioncore.adapter.IAVBrowserService.Connection
        public void onDisconnected() {
            postOrRun(new Runnable() {
                /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AVBrowserServiceConnection.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!AVBrowserServiceConnection.this.isCurrentConnection("ServiceConnection:onDisconnected")) {
                        AVBrowserAdapter.LOGGER.error("onDisconnected error, not current connection", new Object[0]);
                        return;
                    }
                    AVBrowserAdapter.this.service = null;
                    AVBrowserAdapter.this.serviceCallback = null;
                    AVBrowserAdapter.this.state = 4;
                    AVBrowserAdapter.this.callback.onDisconnected();
                }
            });
        }

        public boolean isCurrentConnection(String str) {
            if (AVBrowserAdapter.this.connection == this && AVBrowserAdapter.this.state != 0 && AVBrowserAdapter.this.state != 1) {
                return true;
            }
            AVBrowserAdapter.LOGGER.error("isCurrentConnection error, func=%{public}s, state=%{public}d", str, Integer.valueOf(AVBrowserAdapter.this.state));
            return false;
        }

        private void postOrRun(Runnable runnable) {
            if (Thread.currentThread() == AVBrowserAdapter.this.handler.getLooper().getThread()) {
                runnable.run();
            } else {
                AVBrowserAdapter.this.handler.post(runnable);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ServiceCallback implements IAVBrowserService.Callback {
        private WeakReference<AVBrowserAdapter> avBrowser;

        ServiceCallback(AVBrowserAdapter aVBrowserAdapter) {
            this.avBrowser = new WeakReference<>(aVBrowserAdapter);
        }

        @Override // ohos.media.sessioncore.adapter.IAVBrowserService.Callback
        public void onConnected(String str, AVToken aVToken, PacMap pacMap) {
            AVBrowserAdapter aVBrowserAdapter = this.avBrowser.get();
            if (aVBrowserAdapter == null) {
                AVBrowserAdapter.LOGGER.error("onConnected error, browser is null", new Object[0]);
            } else {
                aVBrowserAdapter.onServiceConnected(this, str, aVToken, pacMap);
            }
        }

        @Override // ohos.media.sessioncore.adapter.IAVBrowserService.Callback
        public void onConnectFailed() {
            AVBrowserAdapter aVBrowserAdapter = this.avBrowser.get();
            if (aVBrowserAdapter == null) {
                AVBrowserAdapter.LOGGER.error("onConnectFailed error, browser is null", new Object[0]);
            } else {
                aVBrowserAdapter.onConnectionFailed(this);
            }
        }

        @Override // ohos.media.sessioncore.adapter.IAVBrowserService.Callback
        public void onLoadChildren(String str, List<AVElement> list) {
            onLoadChildrenWithOptions(str, list, null);
        }

        @Override // ohos.media.sessioncore.adapter.IAVBrowserService.Callback
        public void onLoadChildrenWithOptions(String str, List<AVElement> list, PacMap pacMap) {
            AVBrowserAdapter aVBrowserAdapter = this.avBrowser.get();
            if (aVBrowserAdapter == null) {
                AVBrowserAdapter.LOGGER.error("onLoadChildrenWithOptions error, browser is null", new Object[0]);
            } else {
                aVBrowserAdapter.onLoadChildren(this, str, list, pacMap);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onServiceConnected(final ServiceCallback serviceCallback2, final String str, final AVToken aVToken, final PacMap pacMap) {
        postTask(new Runnable() {
            /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                if (!AVBrowserAdapter.this.isCurrentCallback(serviceCallback2, "onServiceConnected")) {
                    AVBrowserAdapter.LOGGER.error("onServiceConnected error, not current callback", new Object[0]);
                } else if (AVBrowserAdapter.this.state != 2) {
                    AVBrowserAdapter.LOGGER.error("onServiceConnected error, state %{public}d invalid", Integer.valueOf(AVBrowserAdapter.this.state));
                } else if (AVBrowserAdapter.this.service == null) {
                    AVBrowserAdapter.LOGGER.error("onServiceConnected error, service is null", new Object[0]);
                } else {
                    AVBrowserAdapter.this.rootId = str;
                    AVBrowserAdapter.this.token = aVToken;
                    AVBrowserAdapter.this.extras = pacMap;
                    AVBrowserAdapter.this.state = 3;
                    AVBrowserAdapter.this.callback.onConnected();
                    for (Map.Entry entry : AVBrowserAdapter.this.subscriptions.entrySet()) {
                        String str = (String) entry.getKey();
                        Subscription subscription = (Subscription) entry.getValue();
                        List<AVSubscriptionCallback> callbacks = subscription.getCallbacks();
                        List<PacMap> optionsList = subscription.getOptionsList();
                        for (int i = 0; i < callbacks.size(); i++) {
                            AVBrowserAdapter.this.service.addSubscription(str, callbacks.get(i), optionsList.get(i), AVBrowserAdapter.this.serviceCallback);
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCurrentCallback(ServiceCallback serviceCallback2, String str) {
        if (this.serviceCallback == serviceCallback2 && this.state != 0 && this.state != 1) {
            return true;
        }
        LOGGER.error("isCurrentCallback error, state=%{public}d, func=%{public}s", Integer.valueOf(this.state), str);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConnectionFailed(final ServiceCallback serviceCallback2) {
        postTask(new Runnable() {
            /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                if (!AVBrowserAdapter.this.isCurrentCallback(serviceCallback2, "onConnectionFailed")) {
                    AVBrowserAdapter.LOGGER.error("onConnectionFailed error, not current callback", new Object[0]);
                } else if (AVBrowserAdapter.this.state != 2) {
                    AVBrowserAdapter.LOGGER.error("onConnectionFailed error, state %{public}d invalid", Integer.valueOf(AVBrowserAdapter.this.state));
                } else {
                    AVBrowserAdapter.this.forceCloseConnection();
                    AVBrowserAdapter.this.callback.onConnectFailed();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onLoadChildren(final ServiceCallback serviceCallback2, final String str, final List<AVElement> list, final PacMap pacMap) {
        postTask(new Runnable() {
            /* class ohos.media.sessioncore.adapter.AVBrowserAdapter.AnonymousClass9 */

            @Override // java.lang.Runnable
            public void run() {
                AVSubscriptionCallback callback;
                if (!AVBrowserAdapter.this.isCurrentCallback(serviceCallback2, "onLoadChildren")) {
                    AVBrowserAdapter.LOGGER.error("onLoadChildren error, not current callback", new Object[0]);
                    return;
                }
                Subscription subscription = (Subscription) AVBrowserAdapter.this.subscriptions.get(str);
                if (subscription != null && (callback = subscription.getCallback(pacMap)) != null) {
                    List<AVElement> list = list;
                    PacMap pacMap = pacMap;
                    if (pacMap == null) {
                        if (list == null) {
                            callback.onError(str);
                        } else {
                            callback.onAVElementListLoaded(str, list);
                        }
                    } else if (list == null) {
                        callback.onError(str, pacMap);
                    } else {
                        callback.onAVElementListLoaded(str, list, pacMap);
                    }
                }
            }
        });
    }

    private synchronized boolean postTask(Runnable runnable) {
        if (this.handler.post(runnable)) {
            return true;
        }
        LOGGER.error("postTask failed, post return false", new Object[0]);
        return false;
    }
}
