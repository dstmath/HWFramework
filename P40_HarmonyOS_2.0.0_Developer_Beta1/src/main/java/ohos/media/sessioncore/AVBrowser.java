package ohos.media.sessioncore;

import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.media.common.sessioncore.AVConnectionCallback;
import ohos.media.common.sessioncore.AVElementCallback;
import ohos.media.common.sessioncore.AVSubscriptionCallback;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.sessioncore.adapter.AVBrowserAdapter;
import ohos.utils.PacMap;

public final class AVBrowser {
    private AVBrowserAdapter browserAdapter;

    public AVBrowser(Context context, ElementName elementName, AVConnectionCallback aVConnectionCallback, PacMap pacMap) {
        this.browserAdapter = new AVBrowserAdapter(context, elementName, aVConnectionCallback, pacMap, AVBrowserService.SERVICE_ACTION);
    }

    public void connect() {
        this.browserAdapter.connect();
    }

    public void disconnect() {
        this.browserAdapter.disconnect();
    }

    public boolean isConnected() {
        return this.browserAdapter.isConnected();
    }

    public ElementName getElementName() {
        return this.browserAdapter.getElementName();
    }

    public String getRootMediaId() {
        return this.browserAdapter.getRootMediaId();
    }

    public PacMap getOptions() {
        return this.browserAdapter.getOptions();
    }

    public AVToken getAVToken() {
        return this.browserAdapter.getAVToken();
    }

    public void getAVElement(String str, AVElementCallback aVElementCallback) {
        this.browserAdapter.getAVElement(str, aVElementCallback);
    }

    public void subscribeByParentMediaId(String str, AVSubscriptionCallback aVSubscriptionCallback) {
        this.browserAdapter.subscribeByParentMediaId(str, aVSubscriptionCallback);
    }

    public void subscribeByParentMediaId(String str, PacMap pacMap, AVSubscriptionCallback aVSubscriptionCallback) {
        this.browserAdapter.subscribeByParentMediaId(str, pacMap, aVSubscriptionCallback);
    }

    public void unsubscribeByParentMediaId(String str) {
        this.browserAdapter.unsubscribeByParentMediaId(str);
    }

    public void unsubscribeByParentMediaId(String str, AVSubscriptionCallback aVSubscriptionCallback) {
        this.browserAdapter.unsubscribeByParentMediaId(str, aVSubscriptionCallback);
    }
}
