package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;

/* compiled from: Unknown */
public class hj extends hf {
    private OfflineVideo be(String str) {
        long ba = hi.ba(str);
        if (ba == 0) {
            return null;
        }
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        offlineVideo.mSize = ba;
        offlineVideo.mTitle = gq.aN(str);
        offlineVideo.mThumnbailPath = bf(str);
        return offlineVideo;
    }

    private String bf(String str) {
        try {
            String[] list = new File(str).list();
            if (list != null) {
                for (String str2 : list) {
                    String str22;
                    File file = new File(str + "/" + str22);
                    if (file.isDirectory()) {
                        str22 = bf(file.getAbsolutePath());
                        if (str22 != null) {
                            return str22;
                        }
                    } else if (str22.endsWith(".db") || str22.endsWith("tmv")) {
                        return file.getAbsolutePath();
                    }
                }
            }
        } catch (Error e) {
        }
        return null;
    }

    public List<OfflineVideo> a(hg hgVar) {
        List<String> aZ = hi.aZ(hgVar.pa);
        if (aZ == null || aZ.size() == 0) {
            return null;
        }
        List<OfflineVideo> arrayList = new ArrayList();
        for (String be : aZ) {
            OfflineVideo be2 = be(be);
            if (be2 != null) {
                arrayList.add(be2);
            }
        }
        return arrayList.size() != 0 ? arrayList : null;
    }
}
