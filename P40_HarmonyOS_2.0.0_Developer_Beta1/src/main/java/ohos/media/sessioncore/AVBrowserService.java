package ohos.media.sessioncore;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.media.common.sessioncore.AVBrowserResult;
import ohos.media.common.sessioncore.AVBrowserRoot;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.sessioncore.adapter.AVBrowserServiceAdapter;
import ohos.media.sessioncore.adapter.AVBrowserServiceHelper;
import ohos.media.sessioncore.delegate.IAVBrowserServiceDelegate;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;

public abstract class AVBrowserService extends Ability implements IAVBrowserServiceDelegate {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVBrowserService.class);
    public static final String SERVICE_ACTION = "action.media.browse.AVBrowserService";
    private AVBrowserServiceAdapter adapter;

    @Override // ohos.media.sessioncore.delegate.IAVBrowserServiceDelegate
    public abstract AVBrowserRoot onGetRoot(String str, int i, PacMap pacMap);

    @Override // ohos.media.sessioncore.delegate.IAVBrowserServiceDelegate
    public abstract void onLoadAVElement(String str, AVBrowserResult aVBrowserResult);

    @Override // ohos.media.sessioncore.delegate.IAVBrowserServiceDelegate
    public abstract void onLoadAVElementList(String str, AVBrowserResult aVBrowserResult);

    @Override // ohos.media.sessioncore.delegate.IAVBrowserServiceDelegate
    public abstract void onLoadAVElementList(String str, AVBrowserResult aVBrowserResult, PacMap pacMap);

    /* access modifiers changed from: protected */
    public void onStart(Intent intent) {
        LOGGER.debug("AVBrowserService onStart", new Object[0]);
        AVBrowserService.super.onStart(intent);
        this.adapter = AVBrowserServiceAdapter.getInstance();
        this.adapter.setBrowserDelegate(this);
        AVBrowserServiceHelper.initService(this);
    }

    public AVToken getAVToken() {
        return this.adapter.getAVToken();
    }

    public void setAVToken(AVToken aVToken) {
        if (aVToken != null) {
            this.adapter.setAVToken(aVToken);
            return;
        }
        throw new IllegalArgumentException("token cannot be null");
    }

    public final PacMap getBrowserOptions() {
        return this.adapter.getBrowserOptions();
    }

    public final AVCallerUserInfo getCallerUserInfo() {
        return this.adapter.getCallerUserInfo();
    }

    public void notifyAVElementListUpdated(String str) {
        if (str != null) {
            this.adapter.notifyAVElementListUpdated(str);
            return;
        }
        throw new IllegalArgumentException("parentMediaId cannot be null");
    }

    public void notifyAVElementListUpdated(String str, PacMap pacMap) {
        if (str == null) {
            throw new IllegalArgumentException("parentMediaId cannot be null");
        } else if (pacMap != null) {
            this.adapter.notifyAVElementListUpdated(str, pacMap);
        } else {
            throw new IllegalArgumentException("options cannot be null");
        }
    }
}
