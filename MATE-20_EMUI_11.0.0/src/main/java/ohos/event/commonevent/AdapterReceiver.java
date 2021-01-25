package ohos.event.commonevent;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import java.util.Optional;
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
        if (iCommonEventSubscriber != null) {
            this.zidaneSubscriber = iCommonEventSubscriber;
            return;
        }
        throw new IllegalArgumentException("subscribe can not be null.");
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        setAsyncCommonEventResult();
        if (intent != null) {
            intent.removeExtra(KEY_PUBLISH_PERMISSION);
        }
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = DataConverter.createZidaneIntent(false, intent);
        ohos.aafwk.content.Intent intent2 = null;
        if (createZidaneIntent.isPresent()) {
            intent2 = createZidaneIntent.get();
            Bundle resultsFromIntent = RemoteInput.getResultsFromIntent(intent);
            if (resultsFromIntent != null) {
                intent2.setParam(NOTIFICATION_USER_INPUT, (Sequenceable) PacMapUtils.convertFromBundle(resultsFromIntent));
            }
        }
        CommonEventData commonEventData = new CommonEventData(intent2);
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
