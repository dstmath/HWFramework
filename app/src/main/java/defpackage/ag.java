package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

/* renamed from: ag */
public class ag {
    private static ag aQ;
    private static final HashMap aS = null;
    private HashMap aR;
    private Context context;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: ag.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: ag.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: ag.<clinit>():void");
    }

    private ag(Context context) {
        this.aR = new HashMap();
        this.context = null;
        this.context = context;
        init();
    }

    public static int a(Context context, String str, int i) {
        try {
            Object c = ag.c(context, str);
            if (c != null) {
                i = ((Integer) c).intValue();
            }
        } catch (Exception e) {
        }
        return i;
    }

    public static long a(Context context, String str, long j) {
        try {
            Object c = ag.c(context, str);
            if (c != null) {
                j = ((Long) c).longValue();
            }
        } catch (Exception e) {
        }
        return j;
    }

    public static String a(Context context, String str, String str2) {
        Object c = ag.c(context, str);
        if (c == null) {
            return str2;
        }
        String str3;
        try {
            str3 = (String) c;
        } catch (Exception e) {
            aw.d("PushLog2828", "getString from config failed!");
            str3 = str2;
        }
        return str3;
    }

    public static void a(Context context, g gVar) {
        if (gVar == null || gVar.p == null) {
            aw.e("PushLog2828", "set value err, cfg is null or itemName is null, cfg:" + gVar);
        } else if (ag.n(context) == null) {
            aw.e("PushLog2828", "System init failed in set Value");
        } else {
            aQ.aR.put(gVar.p, gVar);
            aQ.b(context, gVar);
        }
    }

    public static boolean a(Context context, String str, boolean z) {
        try {
            Object c = ag.c(context, str);
            if (c != null) {
                z = ((Boolean) c).booleanValue();
            }
        } catch (Exception e) {
        }
        return z;
    }

    private boolean b(Context context, g gVar) {
        if (context == null) {
            context = this.context;
        }
        bt btVar = new bt(context, "pushConfig");
        if (Boolean.class == gVar.r) {
            btVar.a(gVar.p, ((Boolean) gVar.q).booleanValue());
        } else if (String.class == gVar.r) {
            btVar.f(gVar.p, (String) gVar.q);
        } else if (Long.class == gVar.r) {
            btVar.a(gVar.p, (Long) gVar.q);
        } else if (Integer.class == gVar.r) {
            btVar.a(gVar.p, (Integer) gVar.q);
        } else if (Float.class == gVar.r) {
            btVar.a(gVar.p, (Float) gVar.q);
        }
        return true;
    }

    private static void bu() {
        aS.clear();
        aS.put("cloudpush_isLogLocal", new g("cloudpush_isLogLocal", Boolean.class, Boolean.valueOf(false)));
        aS.put("cloudpush_pushLogLevel", new g("cloudpush_pushLogLevel", Integer.class, Integer.valueOf(4)));
        aS.put("cloudpush_isReportLog", new g("cloudpush_isReportLog", Boolean.class, Boolean.valueOf(false)));
        aS.put("cloudpush_isNoDelayConnect", new g("cloudpush_isNoDelayConnect", Boolean.class, Boolean.valueOf(false)));
        aS.put("cloudpush_isSupportUpdate", new g("cloudpush_isSupportUpdate", Boolean.class, Boolean.valueOf(false)));
        aS.put("cloudpush_isSupportCollectSocketInfo", new g("cloudpush_isSupportCollectSocketInfo", Boolean.class, Boolean.valueOf(false)));
        aS.put("cloudpush_trsIp", new g("cloudpush_trsIp", String.class, "push.hicloud.com"));
        aS.put("cloudpush_fixHeatBeat", new g("cloudpush_fixHeatBeat", String.class, " unit sec"));
        aS.put("USE_SSL", new g("USE_SSL", Integer.class, Integer.valueOf(ChannelType.aH.ordinal())));
    }

    private void bw() {
        this.aR.clear();
        this.aR.putAll(aS);
        for (Entry entry : new bt(this.context, "pushConfig").getAll().entrySet()) {
            this.aR.put(entry.getKey(), new g((String) entry.getKey(), entry.getValue().getClass(), entry.getValue()));
        }
    }

    private static Object c(Context context, String str) {
        if (ag.n(context) == null) {
            return null;
        }
        g gVar = (g) aQ.aR.get(str);
        return gVar == null ? null : gVar.q;
    }

    private static g d(Context context, String str) {
        if (ag.n(context) == null || str == null) {
            return null;
        }
        g gVar = (g) aQ.aR.get(str);
        return gVar == null ? null : gVar;
    }

    public static synchronized ag n(Context context) {
        ag agVar;
        synchronized (ag.class) {
            if (aQ != null) {
                agVar = aQ;
            } else if (context == null) {
                agVar = null;
            } else {
                aQ = new ag(context);
                agVar = aQ;
            }
        }
        return agVar;
    }

    public static ChannelType o(Context context) {
        g d = ag.d(context, "USE_SSL");
        ChannelType channelType = ChannelType.aH;
        if (d == null) {
            return channelType;
        }
        aw.d("PushLog2828", " " + d);
        Integer num = (Integer) d.q;
        if (num.intValue() >= 0 && num.intValue() < ChannelType.values().length) {
            return ChannelType.values()[num.intValue()];
        }
        aw.e("PushLog2828", "useSSL:" + d.q + " is invalid cfg");
        return channelType;
    }

    public void bv() {
        bt btVar = new bt(this.context, "pushConfig");
        Set<String> keySet = this.aR.keySet();
        LinkedList linkedList = new LinkedList();
        for (String str : keySet) {
            if (!(aS.containsKey(str) || "NeedMyServiceRun".equals(str) || "version_config".equals(str))) {
                aw.d("PushLog2828", "item " + str + " remove from " + "pushConfig" + " in deleteNoSysCfg");
                linkedList.add(str);
                btVar.z(str);
            }
        }
        Iterator it = linkedList.iterator();
        while (it.hasNext()) {
            this.aR.remove((String) it.next());
        }
    }

    public void init() {
        ag.bu();
        bw();
    }
}
