package ohos.media.sessioncore.adapter;

import java.util.List;
import ohos.app.GeneralReceiver;
import ohos.media.common.sessioncore.AVElement;
import ohos.media.common.sessioncore.AVSubscriptionCallback;
import ohos.media.common.sessioncore.AVToken;
import ohos.utils.PacMap;

public interface IAVBrowserService {

    public interface Callback {
        void onConnectFailed();

        void onConnected(String str, AVToken aVToken, PacMap pacMap);

        void onLoadChildren(String str, List<AVElement> list);

        void onLoadChildrenWithOptions(String str, List<AVElement> list, PacMap pacMap);
    }

    public interface Connection {
        void onConnectFailed();

        void onConnected(IAVBrowserService iAVBrowserService);

        void onDisconnected();
    }

    void addSubscription(String str, AVSubscriptionCallback aVSubscriptionCallback, PacMap pacMap, Callback callback);

    void connect(String str, PacMap pacMap, Callback callback);

    void disconnect(Callback callback);

    void getMediaItem(String str, GeneralReceiver generalReceiver, Callback callback);

    void removeSubscription(String str, AVSubscriptionCallback aVSubscriptionCallback, Callback callback);
}
