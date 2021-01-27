package android.content;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IntentSenderEx {
    private IntentSender mIntentSender;

    public IntentSenderEx(IIntentSenderEx iIntentSenderEx) {
        if (iIntentSenderEx != null) {
            this.mIntentSender = new IntentSender(iIntentSenderEx.getIntentSender());
        }
    }

    public IntentSender getIntentSender() {
        return this.mIntentSender;
    }
}
