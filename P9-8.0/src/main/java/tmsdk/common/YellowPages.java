package tmsdk.common;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateManager;
import tmsdk.common.utils.f;
import tmsdk.common.utils.r;
import tmsdkobf.ko;
import tmsdkobf.lu;

public class YellowPages {
    private static YellowPages xw;
    private String xx;

    static {
        TMSDKContext.registerNatives(10, YellowPages.class);
    }

    private YellowPages() {
        init(TMSDKContext.getApplicaionContext());
    }

    private String bq(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String str2 = null;
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicReference atomicReference = new AtomicReference();
        int nQueryDataByNumberJNI = nQueryDataByNumberJNI(this.xx, str, atomicInteger, atomicReference);
        if (nQueryDataByNumberJNI == 0) {
            try {
                str2 = new String((byte[]) atomicReference.get(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        f.e("yellowPage", "error code::" + nQueryDataByNumberJNI);
        return str2;
    }

    public static YellowPages getInstance() {
        if (xw == null) {
            Class cls = YellowPages.class;
            synchronized (YellowPages.class) {
                if (xw == null) {
                    xw = new YellowPages();
                }
            }
        }
        return xw;
    }

    private void init(Context context) {
        String str = UpdateConfig.YELLOW_PAGEV2_LARGE;
        if (TextUtils.isEmpty(r.k(context, str))) {
            str = UpdateConfig.YELLOW_PAGE;
            lu.b(context, str, null);
        }
        this.xx = ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).getFileSavePath() + File.separator + str;
    }

    private native int nQueryDataByNumberJNI(String str, String str2, AtomicInteger atomicInteger, AtomicReference<byte[]> atomicReference);

    private native int nUpdate(String str, String str2);

    public String query(String str) {
        Object bq = bq(str);
        if (!TextUtils.isEmpty(bq)) {
            return bq;
        }
        Object aY = ko.aY(str);
        return TextUtils.isEmpty(aY) ? null : bq(aY);
    }

    public int update(String str) {
        return nUpdate(this.xx, str);
    }
}
