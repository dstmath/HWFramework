package tmsdk.bg.module.network;

import android.content.Context;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.ScriptHelper;
import tmsdkobf.dl;
import tmsdkobf.dm;
import tmsdkobf.nc;
import tmsdkobf.nj;

/* compiled from: Unknown */
final class f {
    private static String ym;
    private Context mContext;
    private String xY;
    private final String yj;
    private final String yk;
    private final String yl;
    private final List<String> yn;
    private final List<String> yo;
    private final ArrayList<String> yp;
    private nc yq;
    private int yr;
    private String ys;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.module.network.f.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.module.network.f.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.module.network.f.<clinit>():void");
    }

    public f(String str) {
        this.yj = "MOBILE";
        this.yk = "WIFI";
        this.yl = "EXCLUDE";
        this.yn = new ArrayList();
        this.yo = new ArrayList();
        this.yp = new ArrayList();
        this.mContext = TMSDKContext.getApplicaionContext();
        this.yr = 0;
        this.xY = str;
        this.yq = new nc("NetInterfaceManager");
    }

    private void a(dm dmVar) {
        if (dmVar != null && dmVar.iA != null) {
            Iterator it = dmVar.iA.iterator();
            while (it.hasNext()) {
                dl dlVar = (dl) it.next();
                if ("MOBILE".equalsIgnoreCase(dlVar.ix)) {
                    this.yn.clear();
                    this.yn.addAll(dlVar.iy);
                } else if ("WIFI".equalsIgnoreCase(dlVar.ix)) {
                    this.yo.clear();
                    this.yo.addAll(dlVar.iy);
                } else if ("EXCLUDE".equalsIgnoreCase(dlVar.ix)) {
                    this.yp.clear();
                    this.yp.addAll(dlVar.iy);
                }
            }
        }
    }

    private boolean a(List<String> list, String str) {
        for (String startsWith : list) {
            if (str.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    private boolean cb(String str) {
        if (!str.startsWith("ppp")) {
            return false;
        }
        if (this.ys != null && this.ys.equals(str)) {
            return true;
        }
        this.ys = dS();
        return this.ys != null && this.ys.equals(str);
    }

    private dm dQ() {
        return (dm) nj.b(this.mContext, UpdateConfig.TRAFFIC_MONITOR_CONFIG_NAME, UpdateConfig.intToString(20001), new dm());
    }

    private String dS() {
        List<String> dT = dT();
        if (dT.size() <= 1) {
            return null;
        }
        String str;
        List arrayList = new ArrayList(1);
        for (String str2 : dT) {
            if (str2.startsWith("ppp")) {
                arrayList.add(str2);
            }
        }
        if (arrayList == null || arrayList.size() <= 0) {
            return (String) dT.get(0);
        }
        str2 = (String) arrayList.get(0);
        if (arrayList.size() <= 1) {
            return str2;
        }
        m(arrayList);
        return str2;
    }

    private List<String> dT() {
        List arrayList = new ArrayList(1);
        CharSequence runScript = ScriptHelper.runScript((int) CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY, "ip route");
        if (runScript != null) {
            Matcher matcher = Pattern.compile("dev\\s+([\\w]+)").matcher(runScript);
            while (matcher.find()) {
                String group = matcher.group(1);
                if (!arrayList.contains(group)) {
                    arrayList.add(group);
                }
            }
        }
        return arrayList;
    }

    private void m(List<String> list) {
        String replaceAll = n(list).replaceAll("\n", ",");
        StringBuilder stringBuilder = new StringBuilder("IpAddr: ");
        stringBuilder.append(replaceAll).append(";");
        if (this.yq != null) {
            this.yq.a(ym, stringBuilder.toString(), true);
        }
    }

    private String n(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        CharSequence runScript = ScriptHelper.runScript((int) CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY, "ip addr");
        if (runScript != null) {
            StringBuilder stringBuilder2 = new StringBuilder("(");
            for (String str : list) {
                stringBuilder2.append("(?:" + str + ")|");
            }
            stringBuilder2.deleteCharAt(stringBuilder2.length() - 1);
            stringBuilder2.append(")");
            Matcher matcher = Pattern.compile("^\\d+:\\s+" + stringBuilder2.toString() + ".*$\n*" + "(^[^\\d].*$\n*)*", 8).matcher(runScript);
            while (matcher.find()) {
                String group = matcher.group(0);
                if (group != null) {
                    stringBuilder.append(group);
                }
            }
        }
        return stringBuilder.toString();
    }

    public boolean bZ(String str) {
        return !cb(str) && a(this.yn, str);
    }

    public boolean ca(String str) {
        return !cb(str) && a(this.yo, str);
    }

    public void dR() {
        a(dQ());
    }
}
