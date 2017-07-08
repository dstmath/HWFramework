package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class rg {
    public static final String NC = null;
    public static HashMap<String, String> ND;
    public static final List<String> po = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rg.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rg.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rg.<clinit>():void");
    }

    public static List<rj> R(boolean z) {
        return c(TMSDKContext.getApplicaionContext(), z);
    }

    public static HashMap<String, String> aV() {
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), "40291.dat", "40291", new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null) {
            return null;
        }
        HashMap<String, String> hashMap = new HashMap();
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            if (!(cwVar.go == null || cwVar.gp == null || cwVar.gq == null || !cwVar.go.equals("2"))) {
                hashMap.put(cwVar.gp, cwVar.gq);
            }
        }
        return hashMap;
    }

    private static List<rj> c(Context context, boolean z) {
        if (ND == null) {
            ND = aV();
        }
        List<rj> arrayList = new ArrayList();
        cx cxVar = (cx) nj.b(context, UpdateConfig.WeixinTrashCleanNew, UpdateConfig.intToString(40233), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null || cxVar.gw.size() == 0) {
            return arrayList;
        }
        Iterator it = cxVar.gw.iterator();
        int i = 1;
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            if (TextUtils.isEmpty(cwVar.gr) || TextUtils.isEmpty(cwVar.gp)) {
                d.c("RuleParser", "info2,4null");
            } else {
                rj rjVar = new rj();
                if (z) {
                    rjVar.mName = (String) ND.get(cwVar.go);
                    if (TextUtils.isEmpty(rjVar.mName)) {
                        rjVar.mName = cwVar.go;
                    }
                } else {
                    try {
                        rjVar.mName = cwVar.go;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rjVar.pN = dw(cwVar.gp);
                rjVar.mScanType = cG(Integer.parseInt(cwVar.gq));
                rjVar.mCleanType = Integer.parseInt(cwVar.gr);
                if (!TextUtils.isEmpty(cwVar.gs)) {
                    String[] split = cwVar.gs.split("\\|");
                    if (split.length > 0) {
                        try {
                            rjVar.NT = Integer.parseInt(split[0]);
                        } catch (Exception e2) {
                        }
                    }
                    if (split.length > 1) {
                        try {
                            rjVar.NU = Integer.parseInt(split[1]);
                        } catch (Exception e3) {
                        }
                    }
                    if (split.length > 2) {
                        try {
                            rjVar.NV = Integer.parseInt(split[2]);
                        } catch (Exception e4) {
                        }
                    }
                    if (split.length > 3) {
                        try {
                            rjVar.NW = Integer.parseInt(split[3]);
                        } catch (Exception e5) {
                        }
                    }
                }
                rjVar.mClearTip = cwVar.gt;
                if (z) {
                    rjVar.mClearTip = (String) ND.get(cwVar.gt);
                    if (TextUtils.isEmpty(rjVar.mClearTip)) {
                        rjVar.mClearTip = cwVar.gt;
                    }
                } else {
                    rjVar.mClearTip = cwVar.gt;
                }
                if (!TextUtils.isEmpty(cwVar.gu)) {
                    rjVar.NX = rl.dB(cwVar.gu);
                }
                if (!TextUtils.isEmpty(cwVar.gv)) {
                    rjVar.NY = rk.dA(cwVar.gv);
                }
                rjVar.NS = i;
                int i2 = i + 1;
                arrayList.add(rjVar);
                d.c("RuleParser", "rule " + rjVar.mName + " scantype= " + rjVar.mScanType + " cleartype=" + rjVar.mCleanType);
                i = i2;
            }
        }
        return arrayList;
    }

    private static int cG(int i) {
        switch (i) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                return 0;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                return 1;
            case FileInfo.TYPE_BIGFILE /*3*/:
                return 2;
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                return 100;
            default:
                return 100;
        }
    }

    private static List<String> dw(String str) {
        List arrayList = new ArrayList();
        String[] split = str.split("\\|");
        if (split != null && split.length > 0) {
            for (String str2 : split) {
                if (str2.indexOf("*") == -1) {
                    for (String str22 : dy(str22)) {
                        arrayList.add(str22);
                    }
                } else {
                    Collection dx = dx(str22);
                    if (dx != null && dx.size() > 0) {
                        arrayList.addAll(dx);
                    }
                }
            }
        }
        return arrayList;
    }

    private static List<String> dx(String str) {
        List<String> arrayList = new ArrayList();
        if (str.indexOf("*") == -1) {
            arrayList.add(str);
        } else {
            String[] split = str.split("\\*");
            if (split != null && split.length > 0) {
                List<String> dy = dy(split[0]);
                if (split.length != 1) {
                    if (split.length >= 2) {
                        if (!"/".equals(split[1])) {
                        }
                    }
                    for (String str2 : dy) {
                        File file = new File(str2);
                        if (file.exists() && file.isDirectory()) {
                            String[] list = file.list();
                            if (list != null && list.length > 0) {
                                for (String str3 : list) {
                                    String str4 = str2 + "/" + str3;
                                    file = new File(str4);
                                    if (file.exists() && file.isDirectory()) {
                                        String[] list2 = file.list();
                                        if (list2 != null && list2.length > 0) {
                                            for (String str5 : list2) {
                                                if (!TextUtils.isEmpty(str5) && str5.equalsIgnoreCase(dz(split[1]))) {
                                                    arrayList.add(str4 + "/" + dz(str5));
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (String str22 : dy) {
                    arrayList.add(str22);
                }
            }
        }
        return arrayList;
    }

    public static List<String> dy(String str) {
        if (str == null) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        for (String str2 : po) {
            Object obj = str2 + "/" + dz(str);
            if (obj.length() > 1 && obj.endsWith("/")) {
                obj = obj.substring(0, obj.length() - 1);
            }
            arrayList.add(obj);
        }
        return arrayList;
    }

    public static String dz(String str) {
        while (str.startsWith("/")) {
            str = str.substring(1);
        }
        return str;
    }
}
