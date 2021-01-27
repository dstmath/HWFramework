package ohos.event.notification.adapter;

import android.content.Context;
import ohos.event.commonevent.CesAdapterManager;
import ohos.event.notification.AnsAdapterManager;

public class NotificationMgrAdapter {
    public static void init(Context context) {
        CesAdapterManager.getInstance().init(context);
        AnsAdapterManager.initAnsNative();
    }

    private NotificationMgrAdapter() {
    }
}
