package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import tmsdk.common.OfflineVideo;

public class rw extends rn {
    Pattern PZ = Pattern.compile("\"title\":\"([^\"]*)\"");
    Pattern Qa = Pattern.compile("\"progress\":([0-9]{1,3})");
    Pattern Qb = Pattern.compile("\"seconds\":([0-9]{1,3})");
    Pattern Qc = Pattern.compile("\"playTime\":([0-9]{1,3})");

    private OfflineVideo dw(String str) {
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = str;
        if (new File(str + "/1.png").exists()) {
            offlineVideo.mThumnbailPath = str + "/1.png";
        }
        List dr = rq.dr(str + "/info");
        if (dr == null || dr.size() == 0) {
            return null;
        }
        String str2 = (String) dr.get(0);
        offlineVideo.mTitle = rq.b(str2, this.PZ);
        offlineVideo.mDownProgress = rq.a(str2, this.Qa);
        int a = rq.a(str2, this.Qc);
        int a2 = rq.a(str2, this.Qb);
        offlineVideo.mPlayProgress = a2 <= 0 ? -1 : (a * 100) / a2;
        offlineVideo.mSize = rq.dq(str);
        return offlineVideo;
    }

    public List<OfflineVideo> a(ro roVar) {
        if (roVar.Ok == null) {
            return null;
        }
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
