package ohos.event.commonevent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ohos.tools.Bytrace;

class CesProxyReceiver extends BroadcastReceiver {
    CesProxyReceiver() {
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bytrace.startTrace(2, "JavaSubscriberOnReceive");
            CesAdapterManager.getInstance().onReceiveCommonEvent(intent, getResultCode(), getResultData(), isOrderedBroadcast(), hashCode());
            Bytrace.finishTrace(2, "JavaSubscriberOnReceive");
        }
    }
}
