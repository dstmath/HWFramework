package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmsdk.common.OfflineVideo;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.tcc.VideoMetaRunner;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.f;

public class rp {
    HashMap<String, rn> PU = new HashMap();
    String[] PV;
    final boolean PW;

    public rp(String[] strArr, boolean z) {
        this.PW = z;
        this.PU.put("youku", new rw());
        this.PU.put("qiyi", new rs());
        this.PU.put("qqlive", new rr());
        this.PU.put("sohu", new rt());
        this.PU.put("storm", new ru(this.PW));
        this.PV = strArr;
    }

    private List<OfflineVideo> L(List<OfflineVideo> list) {
        if (list.size() == 0) {
            return list;
        }
        List<OfflineVideo> arrayList = new ArrayList();
        Collection arrayList2 = new ArrayList();
        String str = null;
        for (OfflineVideo offlineVideo : list) {
            if (str == null || str.equals(offlineVideo.mAdapter)) {
                str = offlineVideo.mAdapter;
                arrayList2.add(offlineVideo);
            } else {
                M(arrayList2);
                arrayList.addAll(arrayList2);
                arrayList2.clear();
                str = offlineVideo.mAdapter;
                arrayList2.add(offlineVideo);
            }
        }
        M(arrayList2);
        arrayList.addAll(arrayList2);
        return arrayList;
    }

    private void M(List<OfflineVideo> list) {
        Collections.sort(list, new Comparator<OfflineVideo>() {
            /* renamed from: a */
            public int compare(OfflineVideo offlineVideo, OfflineVideo offlineVideo2) {
                int i = 0;
                int i2 = -1;
                int i3 = 1;
                int status = offlineVideo.getStatus();
                int status2 = offlineVideo2.getStatus();
                if (status != status2) {
                    if (status > status2) {
                        i3 = -1;
                    }
                    return i3;
                } else if (offlineVideo.mSize == offlineVideo2.mSize) {
                    return 0;
                } else {
                    if (offlineVideo.mSize <= offlineVideo2.mSize) {
                        i = 1;
                    }
                    if (i != 0) {
                        i2 = 1;
                    }
                    return i2;
                }
            }
        });
    }

    private List<OfflineVideo> N(List<OfflineVideo> list) {
        if (list == null || list.size() == 0) {
            return list;
        }
        int acquireRoot = ScriptHelper.acquireRoot();
        f.h("VideoMetaRetriever", "root state " + acquireRoot);
        if (acquireRoot != 0) {
            return list;
        }
        OfflineVideo.dumpToFile(list);
        String str = TMSDKContext.getApplicaionContext().getApplicationInfo().sourceDir;
        String name = VideoMetaRunner.class.getName();
        String format = String.format("export CLASSPATH=%s && exec app_process /system/bin %s %s", new Object[]{str, name, OfflineVideo.getOfflineDatabase()});
        String str2 = "/data/local/offline_video_script";
        String format2 = String.format("echo '%s' > %s", new Object[]{format, str2});
        String format3 = String.format("chmod 755 %s", new Object[]{str2});
        f.h("PiDeepClean", "cmd " + format2);
        f.h("VideoMetaRetriever", "cmd " + format2);
        String runScript = ScriptHelper.runScript(10000, format2, format3, str2);
        List<OfflineVideo> readOfflineVideos = OfflineVideo.readOfflineVideos();
        if (readOfflineVideos == null) {
            readOfflineVideos = list;
        }
        return readOfflineVideos;
    }

    private String[] dn(String str) {
        if (this.PV == null) {
            return null;
        }
        String toLowerCase = str.toLowerCase();
        for (String str2 : this.PV) {
            if (toLowerCase.startsWith(str2)) {
                return do(str2);
            }
        }
        return null;
    }

    public String[] do(String str) {
        Map j = rm.km().j(str, ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        if (j == null || j.size() == 0) {
            return null;
        }
        String str2;
        String cS;
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        for (String str3 : j.keySet()) {
            try {
                arrayList.add(str3);
                arrayList2.add(j.get(str3));
            } catch (Exception e) {
            }
        }
        rj rjVar = new rj();
        rjVar.init();
        String str32 = rjVar.J(arrayList);
        if (str32 != null) {
            str2 = str32;
            cS = rjVar.cS(str32);
        } else {
            int K = rjVar.K(arrayList);
            if (K == -1) {
                K = 0;
            }
            str2 = (String) arrayList.get(K);
            cS = (String) arrayList2.get(K);
        }
        if (cS == null || str2 == null) {
            return null;
        }
        f.e("xx", str2 + "  " + cS);
        return new String[]{cS.trim(), str2.trim()};
    }

    public List<OfflineVideo> ko() {
        List<ro> kp = rv.kp();
        if (kp == null || kp.size() == 0) {
            return null;
        }
        List jZ = rh.jZ();
        ArrayList arrayList = new ArrayList();
        for (ro roVar : kp) {
            if (!TextUtils.isEmpty(roVar.mAdapter)) {
                rn rnVar = (rn) this.PU.get(roVar.mAdapter);
                if (rnVar != null) {
                    List<OfflineVideo> a = rnVar.a(roVar);
                    if (!(a == null || a.size() == 0)) {
                        String[] dn = dn(rh.a(((OfflineVideo) a.get(0)).mPath, jZ));
                        for (OfflineVideo offlineVideo : a) {
                            offlineVideo.mAdapter = roVar.mAdapter;
                            offlineVideo.mPlayers = roVar.mPlayers;
                            if (dn != null) {
                                offlineVideo.mAppName = dn[0];
                                offlineVideo.mPackage = dn[1];
                            }
                        }
                        arrayList.addAll(a);
                    }
                }
            }
        }
        List<OfflineVideo> L = L(N(arrayList));
        long j = 0;
        long j2 = 0;
        long j3 = 0;
        long j4 = 0;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (OfflineVideo offlineVideo2 : L) {
            switch (offlineVideo2.getStatus()) {
                case 0:
                    j += offlineVideo2.mSize;
                    i++;
                    break;
                case 1:
                    j2 += offlineVideo2.mSize;
                    i2++;
                    break;
                case 2:
                    j3 += offlineVideo2.mSize;
                    i3++;
                    break;
                case 3:
                    j4 += offlineVideo2.mSize;
                    i4++;
                    break;
                default:
                    break;
            }
        }
        int i5 = ((i + i2) + i3) + i4;
        f.h("PiDeepClean", "EMID_Secure_DeepClean_OfflineVideo_ScanResult " + (((((j + j2) + j3) + j4) >> 10) + "," + i5 + "," + (j >> 10) + "," + i + "," + (j2 >> 10) + "," + i2 + "," + (j3 >> 10) + "," + i3 + "," + (j4 >> 10) + "," + i4));
        return L;
    }
}
