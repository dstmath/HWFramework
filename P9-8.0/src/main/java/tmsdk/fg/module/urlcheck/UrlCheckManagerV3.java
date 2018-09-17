package tmsdk.fg.module.urlcheck;

import android.content.Context;
import tmsdk.common.utils.f;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.ic;
import tmsdkobf.kt;

public class UrlCheckManagerV3 extends BaseManagerF {
    public static final String TAG = "TMSDK_UrlCheckManagerV3";
    private b Rm;

    public boolean checkUrl(String str, int i, ICheckUrlCallbackV3 iCheckUrlCallbackV3) {
        if (ic.bE()) {
            return false;
        }
        f.f(TAG, "checkUrl");
        kt.aE(29956);
        return this.Rm.a(str, i, iCheckUrlCallbackV3);
    }

    public void onCreate(Context context) {
        this.Rm = new b();
        this.Rm.onCreate(context);
        a(this.Rm);
    }
}
