package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.module.update.UpdateConfig;

/* compiled from: Unknown */
public class hk extends hf {
    private String a(OfflineVideo offlineVideo) {
        String[] list = new File(offlineVideo.mPath).list();
        if (list != null) {
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

    private OfflineVideo bg(String str) {
        Object obj = null;
        String aN = gq.aN(str);
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        offlineVideo.mSize = hi.ba(str);
        if (offlineVideo.mSize >= UpdateConfig.UPDATE_FLAG_PERMIS_MONITOR_LIST) {
            obj = 1;
        }
        if (obj == null) {
            return null;
        }
        List<String> bb = hi.bb(str + "/" + aN + ".qiyicfg");
        if (bb == null || bb.size() == 0) {
            offlineVideo.mThumnbailPath = a(offlineVideo);
            offlineVideo.mTitle = gq.aO(offlineVideo.mThumnbailPath);
            return offlineVideo;
        }
        for (String str2 : bb) {
            if (str2.startsWith("progress=")) {
                try {
                    offlineVideo.mDownProgress = (int) Float.parseFloat(str2.substring("progress=".length()));
                } catch (Exception e) {
                    offlineVideo.mDownProgress = -1;
                }
            } else if (str2.startsWith("text=")) {
                offlineVideo.mTitle = hi.bc(str2.substring("text=".length()));
            } else if (str2.startsWith("imgUrl=")) {
                offlineVideo.mThumnbailPath = offlineVideo.mPath.replaceFirst("files/.*", "cache/images/default/" + hi.bd(str2.substring("imgUrl=".length()).replaceAll("\\\\", "")) + ".r");
                if (!new File(offlineVideo.mThumnbailPath).exists()) {
                    offlineVideo.mThumnbailPath = a(offlineVideo);
                }
            }
        }
        if (offlineVideo.mThumnbailPath == null) {
            offlineVideo.mThumnbailPath = a(offlineVideo);
        }
        return offlineVideo;
    }

    public List<OfflineVideo> a(hg hgVar) {
        List<String> aZ = hi.aZ(hgVar.pa);
        if (aZ == null || aZ.size() == 0) {
            return null;
        }
        List<OfflineVideo> arrayList = new ArrayList();
        for (String bg : aZ) {
            OfflineVideo bg2 = bg(bg);
            if (bg2 != null) {
                arrayList.add(bg2);
            }
        }
        return arrayList;
    }
}
