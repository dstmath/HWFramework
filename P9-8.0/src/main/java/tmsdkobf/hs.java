package tmsdkobf;

import android.content.Intent;
import java.util.Arrays;
import tmsdk.common.module.aresengine.SmsEntity;

public final class hs {
    private hn qx;

    private boolean a(String str, String... strArr) {
        return strArr.length <= 1 ? strArr[0].equals(str) : Arrays.asList(strArr).contains(str);
    }

    /* JADX WARNING: Missing block: B:11:0x004d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if (action != null) {
            if (a(action, "android.provider.Telephony.SMS_RECEIVED", "android.provider.Telephony.SMS_RECEIVED2", "android.provider.Telephony.GSM_SMS_RECEIVED", "android.provider.Telephony.SMS_DELIVER")) {
                this.qx = new hx(intent);
            } else if (type != null) {
                if (a(action, "android.provider.Telephony.WAP_PUSH_RECEIVED", "android.provider.Telephony.WAP_PUSH_GSM_RECEIVED", "android.provider.Telephony.WAP_PUSH_DELIVER")) {
                    if (a(type, "application/vnd.wap.sic", "application/vnd.wap.slc", "application/vnd.wap.coc")) {
                        this.qx = new ia(intent);
                    } else {
                        if (a(type, "application/vnd.wap.mms-message")) {
                            this.qx = new hr(intent);
                        }
                    }
                } else {
                    this.qx = null;
                }
            } else {
                this.qx = null;
            }
        }
    }

    public synchronized SmsEntity bt() {
        SmsEntity smsEntity;
        smsEntity = null;
        if (this.qx != null) {
            smsEntity = this.qx.bt();
            this.qx = null;
        }
        return smsEntity;
    }

    public synchronized boolean bv() {
        return this.qx != null;
    }
}
