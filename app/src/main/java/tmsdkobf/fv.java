package tmsdkobf;

import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdk.common.utils.h;

/* compiled from: Unknown */
public class fv {
    nc nf;

    public fv() {
        this.nf = new nc("imsiInfo");
    }

    public boolean v() {
        String imsi;
        boolean z;
        String str = null;
        d.e("ImsiChecker", "isImsiChanged [Beg]");
        IDualPhoneInfoFetcher cx = jq.cx();
        if (cx != null) {
            imsi = cx.getIMSI(0);
            str = cx.getIMSI(1);
            z = true;
        } else {
            imsi = h.D(TMSDKContext.getApplicaionContext());
            z = false;
        }
        if (imsi == null) {
            imsi = "";
        }
        if (str == null) {
            str = "";
        }
        String string = this.nf.getString("IMSI1", "");
        String string2 = this.nf.getString("IMSI2", "");
        this.nf.a("IMSI1", imsi, false);
        if (z) {
            this.nf.a("IMSI2", str, false);
        }
        if (this.nf.getBoolean("IS_FIRST", true)) {
            this.nf.a("IS_FIRST", false, false);
            d.e("ImsiChecker", "isImsiChanged [End][First--ture]");
            return false;
        }
        d.e("ImsiChecker", "isImsiChanged-lastImsi:[" + string + "][" + string2 + "]");
        d.e("ImsiChecker", "isImsiChanged-currImsi:[" + imsi + "][" + str + "]");
        if (string.compareTo(imsi) == 0) {
            if (z) {
                if (string2.compareTo(str) == 0) {
                }
            }
            d.e("ImsiChecker", "isImsiChanged [End][false]");
            return false;
        }
        d.e("ImsiChecker", "isImsiChanged [End][true]");
        return true;
    }
}
