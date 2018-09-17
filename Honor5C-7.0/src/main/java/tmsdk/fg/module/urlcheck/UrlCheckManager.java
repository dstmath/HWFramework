package tmsdk.fg.module.urlcheck;

import android.content.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmsdk.common.module.urlcheck.UrlCheckResult;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.jg;
import tmsdkobf.ma;

/* compiled from: Unknown */
public final class UrlCheckManager extends BaseManagerF {
    private a OT;

    public UrlCheckResult checkUrl(String str) {
        if (jg.cl()) {
            return null;
        }
        ma.by(29956);
        return this.OT.checkUrl(str);
    }

    public Map<String, UrlCheckResult> checkUrlEx(List<String> list) {
        if (jg.cl()) {
            return new HashMap(0);
        }
        ma.by(29956);
        return this.OT.checkUrlEx(list);
    }

    public void onCreate(Context context) {
        this.OT = new a();
        this.OT.onCreate(context);
        a(this.OT);
    }
}
