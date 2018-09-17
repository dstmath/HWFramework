package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.tcc.VideoMetaRunner;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.gw.a;

/* compiled from: Unknown */
public class hh {
    HashMap<String, hf> qx;
    String[] qy;
    final boolean qz;

    public hh(String[] strArr, boolean z) {
        this.qx = new HashMap();
        this.qz = z;
        this.qx.put("youku", new ho());
        this.qx.put("qiyi", new hk());
        this.qx.put("qqlive", new hj());
        this.qx.put("sohu", new hl());
        this.qx.put("storm", new hm(this.qz));
        this.qy = strArr;
    }

    private String[] aX(String str) {
        if (this.qy == null) {
            return null;
        }
        String toLowerCase = str.toLowerCase();
        for (String str2 : this.qy) {
            if (toLowerCase.startsWith(str2)) {
                return aY(str2);
            }
        }
        return null;
    }

    private List<OfflineVideo> f(List<OfflineVideo> list) {
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
                g(arrayList2);
                arrayList.addAll(arrayList2);
                arrayList2.clear();
                str = offlineVideo.mAdapter;
                arrayList2.add(offlineVideo);
            }
            str = str;
        }
        g(arrayList2);
        arrayList.addAll(arrayList2);
        return arrayList;
    }

    private void g(List<OfflineVideo> list) {
        Collections.sort(list, new Comparator<OfflineVideo>() {
            final /* synthetic */ hh qA;

            {
                this.qA = r1;
            }

            public int a(OfflineVideo offlineVideo, OfflineVideo offlineVideo2) {
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

            public /* synthetic */ int compare(Object obj, Object obj2) {
                return a((OfflineVideo) obj, (OfflineVideo) obj2);
            }
        });
    }

    private List<OfflineVideo> h(List<OfflineVideo> list) {
        if (list == null || list.size() == 0) {
            return list;
        }
        int acquireRoot = ScriptHelper.acquireRoot();
        d.g("VideoMetaRetriever", "root state " + acquireRoot);
        if (acquireRoot != 0) {
            return list;
        }
        OfflineVideo.dumpToFile(list);
        String str = TMSDKContext.getApplicaionContext().getApplicationInfo().sourceDir;
        String name = VideoMetaRunner.class.getName();
        str = String.format("export CLASSPATH=%s && exec app_process /system/bin %s %s", new Object[]{str, name, OfflineVideo.getOfflineDatabase()});
        name = "/data/local/offline_video_script";
        str = String.format("echo '%s' > %s", new Object[]{str, name});
        String format = String.format("chmod 755 %s", new Object[]{name});
        d.g("PiDeepClean", "cmd " + str);
        d.g("VideoMetaRetriever", "cmd " + str);
        ScriptHelper.runScript(10000, str, format, name);
        List<OfflineVideo> readOfflineVideos = OfflineVideo.readOfflineVideos();
        if (readOfflineVideos != null) {
            list = readOfflineVideos;
        }
        return list;
    }

    public String[] aY(String str) {
        List<a> c = gs.aW().c(str, ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        if (c == null || c.size() == 0) {
            return null;
        }
        String aM;
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        for (a aVar : c) {
            try {
                arrayList.add(new String(TccCryptor.decrypt(aVar.pC, null)));
                arrayList2.add(new String(TccCryptor.decrypt(aVar.pD, null)));
            } catch (Exception e) {
            }
        }
        gp gpVar = new gp();
        gpVar.init();
        String c2 = gpVar.c(arrayList);
        if (c2 != null) {
            aM = gpVar.aM(c2);
        } else {
            int d = gpVar.d(arrayList);
            int i = d != -1 ? d : 0;
            c2 = (String) arrayList.get(i);
            aM = (String) arrayList2.get(i);
        }
        if (aM == null || c2 == null) {
            return null;
        }
        d.c("xx", c2 + "  " + aM);
        return new String[]{aM.trim(), c2.trim()};
    }

    public List<OfflineVideo> bw() {
        List<hg> bx = hn.bx();
        if (bx != null && bx.size() != 0) {
            List aT = gq.aT();
            List arrayList = new ArrayList();
            for (hg hgVar : bx) {
                if (!TextUtils.isEmpty(hgVar.mAdapter)) {
                    hf hfVar = (hf) this.qx.get(hgVar.mAdapter);
                    if (hfVar != null) {
                        Collection<OfflineVideo> a = hfVar.a(hgVar);
                        if (!(a == null || a.size() == 0)) {
                            String[] aX = aX(gq.a(((OfflineVideo) a.get(0)).mPath, aT));
                            for (OfflineVideo offlineVideo : a) {
                                offlineVideo.mAdapter = hgVar.mAdapter;
                                offlineVideo.mPlayers = hgVar.mPlayers;
                                if (aX != null) {
                                    offlineVideo.mAppName = aX[0];
                                    offlineVideo.mPackage = aX[1];
                                }
                            }
                            arrayList.addAll(a);
                        }
                    }
                }
            }
            List<OfflineVideo> f = f(h(arrayList));
            long j = 0;
            long j2 = 0;
            long j3 = 0;
            long j4 = 0;
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            Iterator it = f.iterator();
            while (true) {
                long j5 = j;
                j = j2;
                j2 = j3;
                j3 = j4;
                int i5 = i;
                i = i2;
                i2 = i3;
                i3 = i4;
                if (it.hasNext()) {
                    OfflineVideo offlineVideo2 = (OfflineVideo) it.next();
                    long j6;
                    switch (offlineVideo2.getStatus()) {
                        case SpaceManager.ERROR_CODE_OK /*0*/:
                            j6 = offlineVideo2.mSize;
                            i4 = i3;
                            i3 = i2;
                            i2 = i;
                            i = i5 + 1;
                            j4 = j3;
                            j3 = j2;
                            j2 = j;
                            j = j5 + r0;
                            break;
                        case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                            j6 = offlineVideo2.mSize;
                            i4 = i3;
                            i3 = i2;
                            i2 = i + 1;
                            i = i5;
                            j4 = j3;
                            j3 = j2;
                            j2 = j + r0;
                            j = j5;
                            break;
                        case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                            j6 = offlineVideo2.mSize;
                            i4 = i3;
                            i3 = i2 + 1;
                            i2 = i;
                            i = i5;
                            j4 = j3;
                            j3 = j2 + r0;
                            j2 = j;
                            j = j5;
                            break;
                        case FileInfo.TYPE_BIGFILE /*3*/:
                            j6 = offlineVideo2.mSize;
                            i4 = i3 + 1;
                            i3 = i2;
                            i2 = i;
                            i = i5;
                            j4 = j3 + r0;
                            j3 = j2;
                            j2 = j;
                            j = j5;
                            break;
                        default:
                            i4 = i3;
                            i3 = i2;
                            i2 = i;
                            i = i5;
                            j4 = j3;
                            j3 = j2;
                            j2 = j;
                            j = j5;
                            break;
                    }
                }
                String str = ",";
                d.g("PiDeepClean", "EMID_Secure_DeepClean_OfflineVideo_ScanResult " + (((((j5 + j) + j2) + j3) >> 10) + r17 + (((i5 + i) + i2) + i3) + "," + (j5 >> 10) + "," + i5 + "," + (j >> 10) + "," + i + "," + (j2 >> 10) + "," + i2 + "," + (j3 >> 10) + "," + i3));
                return f;
            }
        }
        return null;
    }
}
