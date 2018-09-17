package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;

public class rr extends rn {
    private OfflineVideo du(String str) {
        long dq = rq.dq(str);
        if (dq == 0) {
            return null;
        }
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        offlineVideo.mSize = dq;
        offlineVideo.mTitle = rh.di(str);
        offlineVideo.mThumnbailPath = dv(str);
        return offlineVideo;
    }

    private String dv(String str) {
        try {
            String[] list = new File(str).list();
            if (list != null) {
                String[] strArr = list;
                for (String str2 : list) {
                    File file = new File(str + "/" + str2);
                    if (file.isDirectory()) {
                        String dv = dv(file.getAbsolutePath());
                        if (dv != null) {
                            return dv;
                        }
                    } else if (str2.endsWith(".db") || str2.endsWith("tmv")) {
                        return file.getAbsolutePath();
                    }
                }
            }
        } catch (Error e) {
        }
        return null;
    }

    public List<OfflineVideo> a(ro roVar) {
        List<String> dp = rq.dp(roVar.Ok);
        if (dp == null || dp.size() == 0) {
            return null;
        }
        List<OfflineVideo> arrayList = new ArrayList();
        for (String du : dp) {
            OfflineVideo du2 = du(du);
            if (du2 != null) {
                arrayList.add(du2);
            }
        }
        if (arrayList.size() == 0) {
            arrayList = null;
        }
        return arrayList;
    }
}
