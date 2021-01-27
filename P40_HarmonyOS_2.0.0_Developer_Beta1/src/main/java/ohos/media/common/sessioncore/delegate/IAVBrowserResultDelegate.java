package ohos.media.common.sessioncore.delegate;

import java.util.List;
import ohos.media.common.sessioncore.AVElement;

public interface IAVBrowserResultDelegate {
    void detachForRetrieveAsync();

    void sendAVElement(AVElement aVElement);

    void sendAVElementList(List<AVElement> list);
}
