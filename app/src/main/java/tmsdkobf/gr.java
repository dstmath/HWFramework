package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.i;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class gr {
    public static String pn;
    private static List<String> po;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.gr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.gr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.gr.<clinit>():void");
    }

    public static gu a(Context context, boolean z) {
        return b(context, z);
    }

    private static gw a(gu guVar, cw cwVar, gw gwVar, boolean z) {
        if (cwVar.gp == null || cwVar.gq == null || cwVar.gr == null) {
            return null;
        }
        String aP = aP(cwVar.gp);
        if (gwVar == null || gwVar.om == null || !aP.equals(gwVar.om)) {
            gw gwVar2 = (gw) guVar.pu.get(aP);
            if (gwVar2 == null) {
                gwVar2 = new gw();
            }
            gwVar2.om = aP;
            if (gwVar2.px == null) {
                gwVar2.px = new ArrayList();
            }
            gwVar2.px.add(cwVar.gq);
            if (gwVar2.py == null) {
                gwVar2.py = new ArrayList();
            }
            if (z && !TextUtils.isEmpty(cwVar.gs)) {
                gwVar2.py.add(cwVar.gs);
            } else {
                gwVar2.py.add(cwVar.gr);
            }
            guVar.pu.put(gwVar2.om, gwVar2);
            return gwVar2;
        }
        if (gwVar.px == null) {
            gwVar.px = new ArrayList();
        }
        gwVar.px.add(cwVar.gq);
        if (gwVar.py == null) {
            gwVar.py = new ArrayList();
        }
        if (z && !TextUtils.isEmpty(cwVar.gs)) {
            gwVar.py.add(cwVar.gs);
        } else {
            gwVar.py.add(cwVar.gr);
        }
        return gwVar;
    }

    private static void a(gu guVar, cw cwVar) {
        if (cwVar.gp != null) {
            for (String str : po) {
                guVar.pt.add((str + cwVar.gp).toLowerCase());
            }
        }
    }

    private static void a(gu guVar, gw gwVar, gv gvVar) {
        gw gwVar2 = (gwVar != null && gvVar.om.equals(gwVar.om)) ? gwVar : (gw) guVar.pu.get(gvVar.om);
        if (gwVar2 == null) {
            gwVar2 = new gw();
            gwVar2.om = gvVar.om;
            guVar.pu.put(gwVar2.om, gwVar2);
        }
        if (gwVar2.op == null) {
            gwVar2.op = new ArrayList();
        }
        gwVar2.op.add(gvVar);
    }

    public static String aP(String str) {
        if (str == null) {
            return null;
        }
        String toLowerCase = str.toLowerCase();
        if (toLowerCase.length() > 1 && toLowerCase.endsWith("/")) {
            toLowerCase = toLowerCase.substring(0, toLowerCase.length() - 1);
        }
        return toLowerCase;
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
            if (!(cwVar.go == null || cwVar.gp == null || cwVar.gq == null || !cwVar.go.equals("1"))) {
                hashMap.put(cwVar.gp, cwVar.gq);
            }
        }
        return hashMap;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static gu b(Context context, boolean z) {
        gw gwVar = null;
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.intToString(40225) + ".dat", UpdateConfig.intToString(40225), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null) {
            na.r("deepclean", " list is empty");
            return null;
        }
        gu guVar = new gu();
        guVar.pu = new HashMap();
        guVar.pt = new HashSet();
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            if (cwVar.go != null) {
                try {
                    switch (Integer.valueOf(cwVar.go).intValue()) {
                        case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                            a(guVar, cwVar);
                            break;
                        case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                            gwVar = a(guVar, cwVar, gwVar, z);
                            break;
                        case FileInfo.TYPE_BIGFILE /*3*/:
                            b(guVar, cwVar, gwVar, z);
                            break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                gwVar = gwVar;
            }
        }
        return guVar;
    }

    private static void b(gu guVar, cw cwVar, gw gwVar, boolean z) {
        if (cwVar.gp != null && cwVar.gq != null && cwVar.gr != null && cwVar.gs != null) {
            gv gvVar = new gv();
            String str = (z && !TextUtils.isEmpty(cwVar.gu)) ? cwVar.gu : cwVar.gr;
            gvVar.mDescription = str;
            gvVar.om = aP(cwVar.gq);
            gvVar.op = new ArrayList();
            String[] split = cwVar.gp.split("#");
            if (split != null) {
                int aI;
                for (String aP : split) {
                    gvVar.op.add(aP(aP).substring(gvVar.om.length()));
                }
                str = (z && !TextUtils.isEmpty(cwVar.gu)) ? cwVar.gu : cwVar.gr;
                gvVar.mDescription = str;
                try {
                    gvVar.pw = Integer.valueOf(cwVar.gs).intValue();
                    a(guVar, gwVar, gvVar);
                    aI = gn.aI(cwVar.gt);
                    if (aI > 0) {
                        gvVar.mDescription = !z ? gvVar.mDescription + "(" + String.format(i.dh("cn_in_recent_days"), new Object[]{Integer.valueOf(aI)}) + ")" : gvVar.mDescription + "(" + String.format(i.dh("eng_in_recent_days"), new Object[]{Integer.valueOf(aI)}) + ")";
                        gvVar.pe = "0," + aI;
                        gv gvVar2 = new gv();
                        gvVar2.om = gvVar.om;
                        gvVar2.op = new ArrayList();
                        gvVar2.op.addAll(gvVar.op);
                        String str2 = (z && !TextUtils.isEmpty(cwVar.gu)) ? cwVar.gu + "(" + String.format(i.dh("eng_days_ago"), new Object[]{Integer.valueOf(aI)}) + ")" : cwVar.gr + "(" + String.format(i.dh("cn_days_ago"), new Object[]{Integer.valueOf(aI)}) + ")";
                        gvVar2.mDescription = str2;
                        gvVar2.pw = 1;
                        gvVar2.pe = "" + aI + ",-";
                        a(guVar, gwVar, gvVar2);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
