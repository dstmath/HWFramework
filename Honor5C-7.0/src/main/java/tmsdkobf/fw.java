package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public class fw {
    private static fw ng;
    private final String nh;
    private final String ni;
    private final String nj;
    private final String nk;
    private final String nl;
    private final String nm;
    private final String nn;
    private final String no;
    private ln np;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fw.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fw.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fw.<clinit>():void");
    }

    private fw(Context context) {
        this.nh = "ConfigInfo";
        this.ni = "check_imsi";
        this.nj = "rqd";
        this.nk = "sk";
        this.nl = "first_run_time";
        this.nm = "app_code_version";
        this.nn = "app_code_old_version";
        this.no = "report_usage_info_time";
        this.np = (ln) fe.ad(9);
    }

    public static fw w() {
        if (ng == null) {
            synchronized (fw.class) {
                if (ng == null) {
                    ng = new fw(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return ng;
    }

    public long A() {
        return x().getLong("ad", 0);
    }

    public long B() {
        return x().getLong("sl", 0);
    }

    public Boolean C() {
        return Boolean.valueOf(x().getBoolean("a_s", true));
    }

    public Boolean D() {
        return Boolean.valueOf(x().getBoolean("sr_s", true));
    }

    public Boolean E() {
        return Boolean.valueOf(x().getBoolean("opt_s", true));
    }

    public Boolean F() {
        return Boolean.valueOf(x().getBoolean("ps_s", true));
    }

    public Boolean G() {
        return Boolean.valueOf(x().getBoolean("tmslite_switch", false));
    }

    public Boolean H() {
        return Boolean.valueOf(x().getBoolean("ac_swi", false));
    }

    public Boolean I() {
        return Boolean.valueOf(x().getBoolean("per_s", false));
    }

    public Boolean J() {
        return Boolean.valueOf(x().getBoolean("per_other_s", false));
    }

    public Boolean K() {
        return Boolean.valueOf(x().getBoolean("ht_swi", false));
    }

    public Boolean L() {
        return Boolean.valueOf(x().getBoolean("virus_update", false));
    }

    public long M() {
        return x().getLong("st_lastime", 0);
    }

    public long N() {
        return x().getLong("st_vaildtime", 0);
    }

    public void a(Boolean bool) {
        x().d("a_s", bool.booleanValue());
    }

    public void ah(int i) {
        x().e("ae", i);
    }

    public void ai(int i) {
        x().e("st_count", i);
    }

    public void b(long j) {
        x().d("ad", j);
    }

    public void b(Boolean bool) {
        x().d("sr_s", bool.booleanValue());
    }

    public void c(long j) {
        x().d("sl", j);
    }

    public void c(Boolean bool) {
        x().d("wifi_s", bool.booleanValue());
    }

    public void d(long j) {
        x().d("st_lastime", j);
    }

    public void d(Boolean bool) {
        x().d("opt_s", bool.booleanValue());
    }

    public void e(long j) {
        x().d("st_vaildtime", j);
    }

    public void e(Boolean bool) {
        x().d("ps_s", bool.booleanValue());
    }

    public void f(Boolean bool) {
        x().d("tmslite_switch", bool.booleanValue());
    }

    public void g(Boolean bool) {
        x().d("per_s", bool.booleanValue());
    }

    public int getStartCount() {
        return x().getInt("st_count", 0);
    }

    public void h(Boolean bool) {
        x().d("per_other_s", bool.booleanValue());
    }

    public void i(Boolean bool) {
        x().d("ac_swi", bool.booleanValue());
    }

    public void j(Boolean bool) {
        x().d("ht_swi", bool.booleanValue());
    }

    public void k(Boolean bool) {
        x().d("virus_update", bool.booleanValue());
    }

    public lf x() {
        return this.np.getPreferenceService("ConfigInfo");
    }

    public lf y() {
        return this.np.getPreferenceService("sk");
    }

    public int z() {
        return x().getInt("ae", -1);
    }
}
