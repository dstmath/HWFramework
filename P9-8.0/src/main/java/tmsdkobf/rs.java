package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.module.update.UpdateConfig;

public class rs extends rn {
    private OfflineVideo dw(String str) {
        Object obj = null;
        String di = rh.di(str);
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        offlineVideo.mSize = rq.dq(str);
        if (offlineVideo.mSize >= UpdateConfig.UPDATE_FLAG_PERMIS_MONITOR_LIST) {
            obj = 1;
        }
        if (obj == null) {
            return null;
        }
        List<String> dr = rq.dr(str + "/" + di + ".qiyicfg");
        if (dr == null || dr.size() == 0) {
            offlineVideo.mThumnbailPath = e(offlineVideo);
            offlineVideo.mTitle = rh.dj(offlineVideo.mThumnbailPath);
            return offlineVideo;
        }
        for (String str2 : dr) {
            if (str2.startsWith("progress=")) {
                try {
                    offlineVideo.mDownProgress = (int) Float.parseFloat(str2.substring("progress=".length()));
                } catch (Exception e) {
                    offlineVideo.mDownProgress = -1;
                }
            } else if (str2.startsWith("text=")) {
                offlineVideo.mTitle = rq.ds(str2.substring("text=".length()));
            } else if (str2.startsWith("imgUrl=")) {
                String str3 = "files/.*";
                offlineVideo.mThumnbailPath = offlineVideo.mPath.replaceFirst(str3, "cache/images/default/" + rq.dt(str2.substring("imgUrl=".length()).replaceAll("\\\\", "")) + ".r");
                if (!new File(offlineVideo.mThumnbailPath).exists()) {
                    offlineVideo.mThumnbailPath = e(offlineVideo);
                }
            }
        }
        if (offlineVideo.mThumnbailPath == null) {
            offlineVideo.mThumnbailPath = e(offlineVideo);
        }
        return offlineVideo;
    }

    private String e(OfflineVideo offlineVideo) {
        String[] list = new File(offlineVideo.mPath).list();
        if (list != null) {
            String[] strArr = list;
            for (String str : list) {
                if (str.endsWith(".f4v") || str.endsWith(".mp4")) {
                    String str2 = offlineVideo.mPath + "/" + str;
                    if ((new File(str2).length() <= UpdateConfig.UPDATE_FLAG_PERMIS_MONITOR_LIST ? 1 : null) == null) {
                        return str2;
                    }
                }
            }
        }
        return null;
    }

    public List<OfflineVideo> a(ro roVar) {
        List<String> dp = rq.dp(roVar.Ok);
        if (dp == null || dp.size() == 0) {
            return null;
        }
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
