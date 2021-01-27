package ohos.media.common.sessioncore;

import java.util.List;
import ohos.utils.PacMap;

public abstract class AVSubscriptionCallback {
    public void onAVElementListLoaded(String str, List<AVElement> list) {
    }

    public void onAVElementListLoaded(String str, List<AVElement> list, PacMap pacMap) {
    }

    public void onError(String str) {
    }

    public void onError(String str, PacMap pacMap) {
    }
}
