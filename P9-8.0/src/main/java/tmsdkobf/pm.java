package tmsdkobf;

import android.util.SparseArray;

public final class pm {
    static SparseArray<pn> Ke = new SparseArray();

    static {
        Ke.append(0, new pn("info", "reportSoftList"));
        Ke.append(1, new pn("info", "reportChannelInfo"));
        Ke.append(2, new pn("virusinfo", "getVirusInfos"));
        Ke.append(6, new pn("cloudcheck", "getAnalyseInfo"));
        Ke.append(7, new pn("info", "getUpdatesV2"));
        Ke.append(9, new pn("info", "getGuid"));
        Ke.append(19, new pn("info", "browerCheck"));
        Ke.append(11, new pn("conf", "getConfigV3CPT"));
        Ke.append(12, new pn("sms", "reportSms"));
        Ke.append(13, new pn("sms", "reportTel"));
        Ke.append(14, new pn("sms", "reportSoftFeature"));
        Ke.append(15, new pn("traffic", "getTrafficTemplate"));
        Ke.append(16, new pn("traffic", "getQueryInfo"));
        Ke.append(17, new pn("check", "checkUrl"));
        Ke.append(18, new pn("check", "checkUrlExt"));
        Ke.append(20, new pn("check", "getlicencedate"));
        Ke.append(999, new pn("tipsmain", "getMainTips"));
        Ke.append(21, new pn("sms", "reportHitTel"));
        Ke.append(21, new pn("sms", "reportHitTel"));
        Ke.append(22, new pn("antitheft", "reportCmdResult"));
        Ke.append(23, new pn("phonenumquery", "getTagPhonenum"));
    }

    public static pp bP(int i) {
        return new pp(i, (pn) Ke.get(i));
    }
}
