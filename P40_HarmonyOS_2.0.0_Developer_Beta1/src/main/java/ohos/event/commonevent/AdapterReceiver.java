package ohos.event.commonevent;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.utils.Sequenceable;
import ohos.utils.adapter.PacMapUtils;

public class AdapterReceiver extends BroadcastReceiver {
    private static final String KEY_PUBLISH_PERMISSION = "key_publish_permission";
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String NOTIFICATION_USER_INPUT = "harmony_notification_user_input";
    private static final String TAG = "AdapterReceiver";
    private final ICommonEventSubscriber zidaneSubscriber;

    public AdapterReceiver(ICommonEventSubscriber iCommonEventSubscriber) throws IllegalArgumentException {
        this.zidaneSubscriber = iCommonEventSubscriber;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        CommonEventData commonEventData;
        Bundle resultsFromIntent;
        setAsyncCommonEventResult();
        if (intent == null) {
            commonEventData = new CommonEventData();
        } else {
            intent.removeExtra(KEY_PUBLISH_PERMISSION);
            ohos.aafwk.content.Intent orElse = DataConverter.createZidaneIntent(false, intent).orElse(null);
            if (!(orElse == null || (resultsFromIntent = RemoteInput.getResultsFromIntent(intent)) == null)) {
                orElse.setParam(NOTIFICATION_USER_INPUT, (Sequenceable) PacMapUtils.convertFromBundle(resultsFromIntent));
            }
            commonEventData = new CommonEventData(orElse);
        }
        if (isOrderedBroadcast()) {
            commonEventData.setCode(getResultCode());
            commonEventData.setData(getResultData());
        }
        try {
            this.zidaneSubscriber.onReceive(commonEventData);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "AdapterReceiver::onReceive remote exception", new Object[0]);
        }
    }

    private void setAsyncCommonEventResult() {
        BroadcastReceiver.PendingResult pendingResult = getPendingResult();
        if (pendingResult != null) {
            ICommonEventSubscriber iCommonEventSubscriber = this.zidaneSubscriber;
            if (iCommonEventSubscriber instanceof CommonEventSubscriberHost) {
                ((CommonEventSubscriberHost) iCommonEventSubscriber).setAsyncCommonEventResult(new AsyncCommonEventResult(new AsyncCommonEventResultProxy(this, pendingResult), isOrderedBroadcast(), isInitialStickyBroadcast()));
            }
        }
    }
}
