package ohos.media.common.sessioncore;

import java.util.List;
import ohos.media.common.sessioncore.delegate.IAVBrowserResultDelegate;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AVBrowserResult {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVBrowserResult.class);
    private final IAVBrowserResultDelegate delegate;
    private final Object label;

    public AVBrowserResult(Object obj, IAVBrowserResultDelegate iAVBrowserResultDelegate) {
        this.label = obj;
        this.delegate = iAVBrowserResultDelegate;
    }

    public final void sendAVElement(AVElement aVElement) {
        LOGGER.debug("sendAVElement for label: %{public}s", this.label);
        this.delegate.sendAVElement(aVElement);
    }

    public final void sendAVElementList(List<AVElement> list) {
        LOGGER.debug("sendAVElementList for label: %{public}s", this.label);
        this.delegate.sendAVElementList(list);
    }

    public final void detachForRetrieveAsync() {
        LOGGER.debug("detach for label: %{public}s", this.label);
        this.delegate.detachForRetrieveAsync();
    }
}
