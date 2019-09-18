package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwTelephonyFactory;

public class AbstractUiccController extends Handler {
    UiccControllerReference mReference = HwTelephonyFactory.getHwUiccManager().createHwUiccControllerReference(this);

    public interface UiccControllerReference {
        int getCardType(int i);

        void getUiccCardStatus(Message message, int i);

        void notifyFdnStatusChange();

        void processRadioPowerDownIfNoCard(UiccCard[] uiccCardArr);

        void registerForFdnStatusChange(Handler handler, int i, Object obj);

        void unregisterForFdnStatusChange(Handler handler);
    }

    public int getCardType(int slotId) {
        return this.mReference.getCardType(slotId);
    }

    public void processRadioPowerDownIfNoCard(UiccCard[] uiccCards) {
        this.mReference.processRadioPowerDownIfNoCard(uiccCards);
    }

    public void registerForFdnStatusChange(Handler h, int what, Object obj) {
        this.mReference.registerForFdnStatusChange(h, what, obj);
    }

    public void unregisterForFdnStatusChange(Handler h) {
        this.mReference.unregisterForFdnStatusChange(h);
    }

    public void notifyFdnStatusChange() {
        this.mReference.notifyFdnStatusChange();
    }

    public void getUiccCardStatus(Message result, int slotId) {
        this.mReference.getUiccCardStatus(result, slotId);
    }
}
