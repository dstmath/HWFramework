package tmsdkobf;

import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;
import tmsdk.common.utils.l;

public class ge {
    public boolean R() {
        String imsi;
        f.d("ImsiChecker", "isImsiChanged [Beg]");
        md mdVar = new md("imsiInfo");
        String str = null;
        Object obj = null;
        IDualPhoneInfoFetcher bO = im.bO();
        if (bO != null) {
            obj = 1;
            imsi = bO.getIMSI(0);
            str = bO.getIMSI(1);
        } else {
            imsi = l.M(TMSDKContext.getApplicaionContext());
        }
        if (imsi == null) {
            imsi = "";
        }
        if (str == null) {
            str = "";
        }
        String string = mdVar.getString("IMSI1", "");
        String string2 = mdVar.getString("IMSI2", "");
        mdVar.a("IMSI1", imsi, false);
        if (obj != null) {
            mdVar.a("IMSI2", str, false);
        }
        if (mdVar.getBoolean("IS_FIRST", true)) {
            mdVar.a("IS_FIRST", false, false);
            f.d("ImsiChecker", "isImsiChanged [End][First--ture]");
            return false;
        }
        f.d("ImsiChecker", "isImsiChanged-lastImsi:[" + string + "][" + string2 + "]");
        f.d("ImsiChecker", "isImsiChanged-currImsi:[" + imsi + "][" + str + "]");
        if (string.compareTo(imsi) == 0 && (obj == null || string2.compareTo(str) == 0)) {
            f.d("ImsiChecker", "isImsiChanged [End][false]");
            return false;
        }
        f.d("ImsiChecker", "isImsiChanged [End][true]");
        return true;
    }
}
