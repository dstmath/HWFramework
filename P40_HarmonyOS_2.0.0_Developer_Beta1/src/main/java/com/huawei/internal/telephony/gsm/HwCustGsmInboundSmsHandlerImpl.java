package com.huawei.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import android.provider.Settings;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;

public class HwCustGsmInboundSmsHandlerImpl extends HwCustGsmInboundSmsHandler {
    private static final int EVENT_SIM_RECORDS_LOADED = 100;
    private static final int INVALID_VOICE_MESSAGE_COUNT = -1;
    private static final int MAX_VOICE_MESSAGE_COUNT = 255;
    private static final int NONE_VOICE_MESSAGE_COUNT = 0;
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    private static final String TAG = "HwCustGsmInboundSmsHandlerImpl";
    private int mMwiNumber = 0;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.huawei.internal.telephony.gsm.HwCustGsmInboundSmsHandlerImpl */
    /* JADX WARN: Multi-variable type inference failed */
    public HwCustGsmInboundSmsHandlerImpl(Context context, Phone phone) {
        super(context, phone);
        phone.registerForSimRecordsLoaded(this, (int) EVENT_SIM_RECORDS_LOADED, (Object) null);
    }

    public String getSubIdKey(int slotId) {
        return "VM_sub" + slotId;
    }

    public void clearVmWhenImsiChange(Context context, int slotId) {
        if (context != null) {
            if (slotId == 0 || slotId == 1) {
                SubscriptionController subscriptionController = SubscriptionController.getInstance();
                if (subscriptionController != null) {
                    int subId = subscriptionController.getSubIdUsingPhoneId(slotId);
                    if (subId == INVALID_VOICE_MESSAGE_COUNT) {
                        Rlog.d(TAG, "invalid subId");
                        return;
                    }
                    int oldSubId = Settings.Global.getInt(context.getContentResolver(), getSubIdKey(slotId), INVALID_VOICE_MESSAGE_COUNT);
                    if (!(oldSubId == INVALID_VOICE_MESSAGE_COUNT || subId == oldSubId)) {
                        Rlog.d(TAG, "clear mMwiNumber to 0");
                        this.mMwiNumber = 0;
                    }
                    if (subId != oldSubId) {
                        Settings.Global.putInt(context.getContentResolver(), getSubIdKey(slotId), subId);
                        return;
                    }
                    return;
                }
                return;
            }
            Rlog.d(TAG, "invalid slotId");
        }
    }

    public void setMwiNumber(int mwiNumber) {
        this.mMwiNumber = mwiNumber;
    }

    public void onSimRecordsLoaded() {
        clearVmWhenImsiChange(this.mContext, this.mPhone.getPhoneId());
        int i = this.mMwiNumber;
        if (i != 0) {
            if (i < 0) {
                this.mMwiNumber = INVALID_VOICE_MESSAGE_COUNT;
            } else if (i > MAX_VOICE_MESSAGE_COUNT) {
                this.mMwiNumber = MAX_VOICE_MESSAGE_COUNT;
            } else {
                Rlog.d(TAG, "mMwiNumber is between 0 and 0xff");
            }
            Rlog.d(TAG, "mMwiNumber = " + this.mMwiNumber);
            this.mPhone.setVoiceMessageCount(this.mMwiNumber);
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == EVENT_SIM_RECORDS_LOADED) {
            onSimRecordsLoaded();
        }
    }
}
