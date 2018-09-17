package tmsdk.fg.module.urlcheck;

import android.content.Context;
import tmsdk.common.module.urlcheck.UrlCheckResult;
import tmsdk.common.utils.f;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.kt;

public final class UrlCheckManager extends BaseManagerF {
    public static final String TAG = "TMSDK_UrlCheckManager";
    private a Re;

    public int checkUrl(String str, ICheckUrlCallback iCheckUrlCallback) {
        f.f(TAG, "checkUrl");
        kt.aE(29956);
        return this.Re.a(str, iCheckUrlCallback);
    }

    public UrlCheckResult checkUrlSync(String str) {
        kt.aE(29956);
        f.f(TAG, "checkUrlSync");
        return this.Re.dJ(str);
    }

    public void onCreate(Context context) {
        this.Re = new a();
        this.Re.onCreate(context);
        a(this.Re);
    }
}
