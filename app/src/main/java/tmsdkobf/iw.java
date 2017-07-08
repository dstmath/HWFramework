package tmsdkobf;

import android.content.Intent;
import java.util.Arrays;
import tmsdk.common.module.aresengine.SmsEntity;

/* compiled from: Unknown */
public final class iw {
    private ir sY;

    private boolean a(String str, String... strArr) {
        return strArr.length <= 1 ? strArr[0].equals(str) : Arrays.asList(strArr).contains(str);
    }

    public synchronized void b(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if (action != null) {
            if (a(action, "android.provider.Telephony.SMS_RECEIVED", "android.provider.Telephony.SMS_RECEIVED2", "android.provider.Telephony.GSM_SMS_RECEIVED", "android.provider.Telephony.SMS_DELIVER")) {
                this.sY = new jb(intent);
            } else if (type != null) {
                if (a(action, "android.provider.Telephony.WAP_PUSH_RECEIVED", "android.provider.Telephony.WAP_PUSH_GSM_RECEIVED", "android.provider.Telephony.WAP_PUSH_DELIVER")) {
                    if (a(type, "application/vnd.wap.sic", "application/vnd.wap.slc", "application/vnd.wap.coc")) {
                        this.sY = new je(intent);
                    } else {
                        if (a(type, "application/vnd.wap.mms-message")) {
                            this.sY = new iv(intent);
                        }
                    }
                } else {
                    this.sY = null;
                }
            } else {
                this.sY = null;
            }
        }
    }

    public synchronized SmsEntity ca() {
        SmsEntity smsEntity = null;
        synchronized (this) {
            if (this.sY != null) {
                smsEntity = this.sY.ca();
                this.sY = null;
            }
        }
        return smsEntity;
    }

    public synchronized boolean cc() {
        return this.sY != null;
    }
}
