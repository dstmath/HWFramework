package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import tmsdk.common.OfflineVideo;

/* compiled from: Unknown */
public class ho extends hf {
    Pattern qC;
    Pattern qD;
    Pattern qE;
    Pattern qF;

    public ho() {
        this.qD = Pattern.compile("\"progress\":([0-9]{1,3})");
        this.qC = Pattern.compile("\"title\":\"([^\"]*)\"");
        this.qE = Pattern.compile("\"seconds\":([0-9]{1,3})");
        this.qF = Pattern.compile("\"playTime\":([0-9]{1,3})");
    }

    private OfflineVideo bg(String str) {
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        if (new File(str + "/1.png").exists()) {
            offlineVideo.mThumnbailPath = str + "/1.png";
        }
        List bb = hi.bb(str + "/info");
        if (bb == null || bb.size() == 0) {
            return null;
        }
        String str2 = (String) bb.get(0);
        offlineVideo.mTitle = hi.b(str2, this.qC);
        offlineVideo.mDownProgress = hi.a(str2, this.qD);
        int a = hi.a(str2, this.qF);
        int a2 = hi.a(str2, this.qE);
        offlineVideo.mPlayProgress = a2 <= 0 ? -1 : (a * 100) / a2;
        offlineVideo.mSize = hi.ba(str);
        return offlineVideo;
    }

    public List<OfflineVideo> a(hg hgVar) {
        if (hgVar.pa == null) {
            return null;
        }
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
