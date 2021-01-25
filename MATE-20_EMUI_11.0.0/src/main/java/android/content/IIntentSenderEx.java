package android.content;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IIntentSenderEx {
    private IIntentSender mIntentSender;

    public IIntentSender getIntentSender() {
        return this.mIntentSender;
    }

    public void setIntentSender(IIntentSender intentSender) {
        this.mIntentSender = intentSender;
    }
}
