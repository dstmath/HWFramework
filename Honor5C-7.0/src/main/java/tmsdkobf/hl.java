package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;

/* compiled from: Unknown */
public class hl extends hf {
    private OfflineVideo bg(String str) {
        int i = 0;
        String aN = gq.aN(str);
        if (aN == null) {
            return null;
        }
        long ba = hi.ba(str);
        if ((ba > 0 ? 1 : 0) == 0) {
            return null;
        }
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        offlineVideo.mSize = ba;
        offlineVideo.mTitle = aN;
        String[] list = new File(str).list();
        if (list != null) {
            int length = list.length;
            while (i < length) {
                String str2 = list[i];
                if (str2.startsWith(aN + "_")) {
                    offlineVideo.mThumnbailPath = str + "/" + str2;
                    break;
                }
                i++;
            }
        }
        return offlineVideo;
    }

    public List<OfflineVideo> a(hg hgVar) {
        List<String> aZ = hi.aZ(hgVar.pa);
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
