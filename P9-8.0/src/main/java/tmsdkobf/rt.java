package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;

public class rt extends rn {
    private OfflineVideo dw(String str) {
        Object obj = null;
        String di = rh.di(str);
        if (di == null) {
            return null;
        }
        long dq = rq.dq(str);
        if (dq > 0) {
            obj = 1;
        }
        if (obj == null) {
            return null;
        }
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        offlineVideo.mSize = dq;
        offlineVideo.mTitle = di;
        String[] list = new File(str).list();
        if (list != null) {
            String[] strArr = list;
            for (String str2 : list) {
                if (str2.startsWith(di + "_")) {
                    offlineVideo.mThumnbailPath = str + "/" + str2;
                    break;
                }
            }
        }
        return offlineVideo;
    }

    public List<OfflineVideo> a(ro roVar) {
        List<String> dp = rq.dp(roVar.Ok);
        List<OfflineVideo> arrayList = new ArrayList();
        for (String dw : dp) {
            OfflineVideo dw2 = dw(dw);
            if (dw2 != null) {
                arrayList.add(dw2);
            }
        }
        return arrayList;
    }
}
