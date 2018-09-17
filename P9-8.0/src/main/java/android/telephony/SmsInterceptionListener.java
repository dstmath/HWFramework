package android.telephony;

import android.os.Bundle;
import com.android.internal.telephony.ISmsInterceptionListener;
import com.android.internal.telephony.ISmsInterceptionListener.Stub;

public class SmsInterceptionListener {
    ISmsInterceptionListener callback = new Stub() {
        public int handleSmsDeliverActionInner(Bundle smsInfo) {
            return SmsInterceptionListener.this.handleSmsDeliverAction(smsInfo);
        }

        public int handleWapPushDeliverActionInner(Bundle wapPushInfo) {
            return SmsInterceptionListener.this.handleWapPushDeliverAction(wapPushInfo);
        }

        public boolean sendNumberBlockedRecordInner(Bundle smsInfo) {
            return SmsInterceptionListener.this.sendNumberBlockedRecord(smsInfo);
        }
    };

    public int handleSmsDeliverAction(Bundle smsInfo) {
        return 0;
    }

    public int handleWapPushDeliverAction(Bundle wapPushInfo) {
        return 0;
    }

    public boolean sendNumberBlockedRecord(Bundle smsInfo) {
        return false;
    }
}
